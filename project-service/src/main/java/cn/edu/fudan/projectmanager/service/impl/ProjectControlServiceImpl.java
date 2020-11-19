package cn.edu.fudan.projectmanager.service.impl;

import cn.edu.fudan.projectmanager.component.RestInterfaceManager;
import cn.edu.fudan.projectmanager.dao.RepoUserDao;
import cn.edu.fudan.projectmanager.dao.SubRepositoryDao;
import cn.edu.fudan.projectmanager.domain.AccountRoleEnum;
import cn.edu.fudan.projectmanager.domain.topic.NeedDownload;
import cn.edu.fudan.projectmanager.domain.AccountRepository;
import cn.edu.fudan.projectmanager.domain.SubRepository;
import cn.edu.fudan.projectmanager.domain.dto.RepositoryDTO;
import cn.edu.fudan.projectmanager.domain.dto.UserInfoDTO;
import cn.edu.fudan.projectmanager.exception.RunTimeException;
import cn.edu.fudan.projectmanager.service.ProjectControlService;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * description:
 *
 * @author fancying
 * create: 2020-09-27 21:20
 **/
@Service
@Slf4j
@SuppressWarnings("unchecked")
public class ProjectControlServiceImpl implements ProjectControlService {

    @Value("${github.api.path}")
    private String githubApiPath;
    @Value("${repo.url.pattern}")
    private String repoUrlPattern;

    private static Map<String, UserInfoDTO> userInfos = new ConcurrentHashMap<>(32);
    private RestInterfaceManager rest;

    private RepoUserDao repoUserDao;
    private SubRepositoryDao subRepositoryDao;

    private KafkaTemplate kafkaTemplate;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addOneRepo(String token, RepositoryDTO repositoryDTO) throws Exception {
        UserInfoDTO userInfoDTO = getUserInfoByToken(token);
        String url = repositoryDTO.getUrl().trim();
        String repoSource = repositoryDTO.getRepoSource().toLowerCase();
        if (StringUtils.isEmpty(url)) {
            throw new RunTimeException("the repo url is EMPTY!");
        }
        final String urlPostfix = ".git";
        if(url.endsWith(urlPostfix)){
            url = url.substring(0, url.length()-4);
        }
        boolean isPrivate = repositoryDTO.getPrivateRepo();
        Pattern pattern = Pattern.compile(repoUrlPattern);
        Matcher matcher = pattern.matcher(url);
        if (!matcher.matches()) {
            throw new RunTimeException("invalid url!");
        }

        String accountUuid = userInfoDTO.getUuid();
        String branch = repositoryDTO.getBranch() ;
        String username = repositoryDTO.getUsername();
        // TODO password 应该用 base64 加密
        String password = repositoryDTO.getPassword();


        if(isPrivate && StringUtils.isEmpty(username) && StringUtils.isEmpty(password)){
            throw new RunTimeException("this projectName is private,please provide your git username and password!");
        }

        String repoName = repositoryDTO.getRepoName();

        // 一个 Repo目前只扫描一个分支
        if(repoUserDao.hasRepo(accountUuid, url)) {
            throw new RunTimeException("The repo accountName has already been used! ");
        }
        String projectName = repositoryDTO.getProjectName();

        // 普通用户不具备添加项目的权限 上面检查过后不在验证项目是否有被添加过


        String uuid = UUID.randomUUID().toString();
        SubRepository subRepo = SubRepository.builder().
                uuid(uuid).url(url).
                branch(branch).repoSource(repoSource).
                projectName(projectName).importAccountUuid(accountUuid).
                downloadStatus(SubRepository.DOWNLOADING).recycled(SubRepository.RESERVATIONS).build();

        //subRepository表中插入信息
        int effectRow = subRepositoryDao.insertOneRepo(subRepo);

        if (effectRow != 0) {
            //只有subRepository表中不存在才会下载，向RepoManager这个Topic发送消息，请求开始下载
            send(uuid, url, isPrivate, username, password, branch, repoSource);
        }


        AccountRepository accountRepository = AccountRepository.builder().uuid(UUID.randomUUID().toString()).
                name(repoName).accountUuid(accountUuid).
                subRepositoryUuid(uuid).projectName(projectName).build();
        repoUserDao.insertRepoUser(accountRepository);
    }

    @Override
    @SneakyThrows
    public Map<String, Boolean> addRepos(String token, List<RepositoryDTO> repositories){
        Map<String, Boolean> result = new HashMap<>(repositories.size() >> 1);
        repositories.forEach(r -> {
            try {
                addOneRepo(token, r);
                result.put(r.getProjectName(), Boolean.TRUE);
            }catch (Exception e) {
                log.error("add one repo failed, url is {}! message is {}", r.getUrl(), e.getMessage());
                result.put(r.getProjectName(), Boolean.FALSE);
            }
        });
        return result;
    }

    @Override
    public void update(String token, String oldName, String newName, String type) throws Exception {
        UserInfoDTO userInfoDTO = getUserInfoByToken(token);
        String accountUuid = userInfoDTO.getUuid();
        final String project = "project";
        final String repository = "repository";
        if (StringUtils.isEmpty(oldName) || StringUtils.isEmpty(newName) || oldName.equals(newName)) {
            return;
        }


        if (repository.equals(type)) {
            repoUserDao.updateRepoName(accountUuid, oldName, newName);
        }

        // 0 表示超级管理员 只有超级管理员能操作
        if (project.equals(type) && userInfoDTO.getRight() != 0) {
            throw new RunTimeException("this user has no right to change project accountName");
        }

        // TODO 两种操作 1：修改所有的project名称 2：改变repo的所属组

        // 改变project accountName 该repo的所有project accountName 都会改变 只有超级管理员才会有此权限
        log.warn("project accountName changed by {}! old accountName is {}, new accountName is {}", userInfoDTO.getUuid(), oldName, newName);

        /// repoUserDao.updateProjectName();

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String token, String subRepoUuid, Boolean empty) throws Exception {
        UserInfoDTO userInfoDTO = getUserInfoByToken(token);
        // 0 表示超级管理员 只有超级管理员能操作
        if ( userInfoDTO.getRight() != 0) {
            throw new RunTimeException("this user has no right to change project accountName");
        }

        if (! empty) {
            subRepositoryDao.setRecycled(subRepoUuid);
            return;
        }

        repoUserDao.deleteRelation(subRepoUuid);
        subRepositoryDao.deleteRepo(subRepoUuid);
        // TODO 基于 rest 调用所有扫描服务把与该 repo相关的所有数据删除

    }


    @Override
    @SneakyThrows
    public List<SubRepository> query(String token){
        UserInfoDTO userInfoDTO = getUserInfoByToken(token);
        String userUuid = userInfoDTO.getUuid();

        // 用户权限为admin时 查询所有的repo
        if (userInfoDTO.getRight().equals(AccountRoleEnum.ADMIN.getRight())) {
            userUuid = null;
        }

        // todo 用户权限为 DEVELOPER 时不允许查询项目列表

        return subRepositoryDao.getAllSubRepoByAccountUuid(userUuid);
    }


    private void send(String projectId, String url,boolean isPrivate,String username,String password, String branch,String repoSource) {
        NeedDownload needDownload = new NeedDownload(projectId, repoSource, url, isPrivate, username, password , branch);
        kafkaTemplate.send("ProjectManager", JSONObject.toJSONString(needDownload));
        log.info("send message to topic ProjectManage ---> " + JSONObject.toJSONString(needDownload));
    }

    private synchronized UserInfoDTO getUserInfoByToken(String token) throws Exception{
        if (StringUtils.isEmpty(token)) {
            throw new RunTimeException("need user token");
        }

        if (userInfos.containsKey(token)) {
            return userInfos.get(token);
        }
        UserInfoDTO userInfoDTO = rest.getUserInfoByToken(token);
        if (userInfoDTO == null) {
            throw new RunTimeException("get user info failed");
        }
        userInfos.put(token, userInfoDTO);
        return userInfoDTO;
    }

    /**
     * setter
     */

    @Autowired
    public void setRest(RestInterfaceManager rest) {
        this.rest = rest;
    }

    @Autowired
    public void setSubRepositoryDao(SubRepositoryDao subRepositoryDao) {
        this.subRepositoryDao = subRepositoryDao;
    }

    @Autowired
    public void setRepoUserDao(RepoUserDao repoUserDao) {
        this.repoUserDao = repoUserDao;
    }

    @Autowired
    public void setKafkaTemplate(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

}