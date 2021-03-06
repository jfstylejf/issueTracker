package cn.edu.fudan.projectmanager.service.impl;

import cn.edu.fudan.projectmanager.component.RestInterfaceManager;
import cn.edu.fudan.projectmanager.dao.AccountProjectDao;
import cn.edu.fudan.projectmanager.dao.AccountRepositoryDao;
import cn.edu.fudan.projectmanager.dao.ProjectDao;
import cn.edu.fudan.projectmanager.dao.SubRepositoryDao;
import cn.edu.fudan.projectmanager.domain.*;
import cn.edu.fudan.projectmanager.domain.topic.LocalDownLoad;
import cn.edu.fudan.projectmanager.domain.topic.NeedDownload;
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

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    private AccountRepositoryDao accountRepositoryDao;
    private SubRepositoryDao subRepositoryDao;
    private ProjectDao projectDao;
    private AccountProjectDao accountProjectDao;

    private KafkaTemplate kafkaTemplate;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addOneRepo(String token, RepositoryDTO repositoryDTO) throws Exception {
        UserInfoDTO userInfoDTO = getUserInfoByToken(token);
        String url = repositoryDTO.getUrl().trim();
        String repoSource = repositoryDTO.getRepoSource().toLowerCase();
        if (StringUtils.isEmpty(url)) {
            throw new RunTimeException("URL为空!");
        }
        final String urlPostfix = ".git";
        if (url.endsWith(urlPostfix)) {
            url = url.substring(0, url.length() - 4);
        }
        boolean isPrivate = repositoryDTO.getPrivateRepo();
        Pattern pattern = Pattern.compile(repoUrlPattern);
        Matcher matcher = pattern.matcher(url);
        if (!matcher.matches()) {
            throw new RunTimeException("无效URL!");
        }

        String accountUuid = userInfoDTO.getUuid();
        String branch = repositoryDTO.getBranch();
        String username = repositoryDTO.getUsername();
        // TODO password 应该用 base64 加密
        String password = repositoryDTO.getPassword();


        if (isPrivate && StringUtils.isEmpty(username) && StringUtils.isEmpty(password)) {
            throw new RunTimeException("私有项目，请提供您的用户名及密码!");
        }

        String repoName = repositoryDTO.getRepoName();
        if (repoName == null || "".equals(repoName)) {
            repoName = url.substring(url.lastIndexOf("/")).replace("/", "") + "-" + branch;
        }

        // 一个 Repo目前只扫描一个分支
        if (accountRepositoryDao.hasRepo(branch, url)) {
            throw new RunTimeException("该库已存在当前分支! ");
        }

        String projectName = repositoryDTO.getProjectName();

        // 普通用户不具备添加项目的权限 上面检查过后不在验证项目是否有被添加过


        String uuid = UUID.randomUUID().toString();
        SubRepository subRepo = SubRepository.builder().
                uuid(uuid).url(url).
                branch(branch).repoSource(repoSource).repoName(repoName).
                projectName(projectName).importAccountUuid(accountUuid).
                downloadStatus(SubRepository.DOWNLOADING).recycled(SubRepository.RESERVATIONS).build();

        //判断，不允许添加重复repo
        if(subRepositoryDao.getSubRepoByUrl(url) != null ){
            return false;
        }

        //subRepository表中插入信息
        int effectRow = subRepositoryDao.insertOneRepo(subRepo);

        AccountRepository accountRepository = AccountRepository.builder().uuid(UUID.randomUUID().toString()).
                repoName(repoName).accountUuid(accountUuid).
                subRepositoryUuid(uuid).projectName(projectName).build();
        accountRepositoryDao.insertAccountRepository(accountRepository);
        if (effectRow != 0) {
            //只有subRepository表中不存在才会下载，向RepoManager这个Topic发送消息，请求开始下载
            send(uuid, url, isPrivate, username, password, branch, repoSource);
        }
        log.info("success add repo {}", url);
        return true;
    }

    @Override
    public void addOneRepoByLocal(String token, RepositoryDTO repositoryDTO) throws Exception {
        UserInfoDTO userInfoDTOLocal = getUserInfoByToken(token);
        String url = repositoryDTO.getUrl().trim();
        String repoSource = repositoryDTO.getRepoSource().toLowerCase();
        if (StringUtils.isEmpty(url)) {
            throw new RunTimeException("the repo url is EMPTY!");
        }
        final String urlPostfix = ".git";
        if (url.endsWith(urlPostfix)) {
            url = url.substring(0, url.length() - 4);
        }
        boolean isPrivate = repositoryDTO.getPrivateRepo();
        Pattern pattern = Pattern.compile(repoUrlPattern);
        Matcher matcher = pattern.matcher(url);
        if (!matcher.matches()) {
            throw new RunTimeException("invalid url!");
        }

        String accountUuid = userInfoDTOLocal.getUuid();
        String branch = repositoryDTO.getBranch();
        String username = repositoryDTO.getUsername();
        // TODO password 应该用 base64 加密
        String password = repositoryDTO.getPassword();

        if (isPrivate && StringUtils.isEmpty(username) && StringUtils.isEmpty(password)) {
            throw new RunTimeException("this projectName is private,please provide your git username and password!");
        }

        String repoName = repositoryDTO.getRepoName();
        if (repoName == null || "".equals(repoName)) {
            repoName = url.substring(url.lastIndexOf("/")).replace("/", "") + "-" + branch;
        }

        // 一个 Repo目前只扫描一个分支
        if (accountRepositoryDao.hasRepo(branch, url)) {
            throw new RunTimeException("The repo accountName has already been used! ");
        }

        String projectName = repositoryDTO.getProjectName();

        // 普通用户不具备添加项目的权限 上面检查过后不在验证项目是否有被添加过

        String uuid = UUID.randomUUID().toString();
        SubRepository subRepo = SubRepository.builder().
                uuid(uuid).url(url).
                branch(branch).repoSource(repoSource).repoName(repoName).
                projectName(projectName).importAccountUuid(accountUuid).
                downloadStatus(SubRepository.DOWNLOADING).recycled(SubRepository.RESERVATIONS).build();

        //subRepository表中插入信息
        int effectRow = subRepositoryDao.insertOneRepo(subRepo);

        AccountRepository accountRepository = AccountRepository.builder().uuid(UUID.randomUUID().toString()).
                repoName(repoName).accountUuid(accountUuid).
                subRepositoryUuid(uuid).projectName(projectName).build();
        accountRepositoryDao.insertAccountRepository(accountRepository);
        if (effectRow != 0) {
            //只有subRepository表中不存在才会下载，向RepoManager这个Topic发送消息，请求开始下载
            //flag代表添加代码库的方式，1为本地添加，0为前端添加
            int flag = 1;
            sendLocal(flag, uuid, url, username, branch, repoSource, repoName);
        }
        log.info("success add repo {}", url);
    }

    @Override
    @SneakyThrows
    public Map<String, Boolean> addRepos(String token, List<RepositoryDTO> repositories) {
        Map<String, Boolean> result = new HashMap<>(repositories.size() >> 1);
        repositories.forEach(r -> {
            try {
                addOneRepo(token, r);
                result.put(r.getProjectName(), Boolean.TRUE);
            } catch (Exception e) {
                log.error("add one repo failed, url is {}! message is {}", r.getUrl(), e.getMessage());
                result.put(r.getProjectName(), Boolean.FALSE);
            }
        });
        return result;
    }

    @Override
    public void update(String token, String oldProjectName, String newProjectName) throws Exception {
        UserInfoDTO userInfoDTO = getUserInfoByToken(token);
        String accountUuid = userInfoDTO.getUuid();

        if (StringUtils.isEmpty(oldProjectName) || StringUtils.isEmpty(newProjectName) || oldProjectName.equals(newProjectName)) {
            return;
        }
        // 0 表示超级管理员 只有超级管理员能操作
        if (userInfoDTO.getRight() != 0) {
            throw new RunTimeException("this user has no right to change project accountName");
        }
        // 改变projectName 该repo的所有projectName 都会改变 只有超级管理员才会有此权限
        log.warn("projectName changed by {}! old projectName is {}, new projectName is {}", userInfoDTO.getUuid(), oldProjectName, newProjectName);
        accountRepositoryDao.updateProjectNameAR(accountUuid, oldProjectName, newProjectName);
        projectDao.updateProjectNameP(accountUuid, oldProjectName, newProjectName);
        accountProjectDao.updateProjectNameAP(accountUuid, oldProjectName, newProjectName);
        subRepositoryDao.updateProjectNameSR(accountUuid, oldProjectName, newProjectName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String token, String subRepoUuid, Boolean empty) throws Exception {
        UserInfoDTO userInfoDTO = getUserInfoByToken(token);
        // 0 表示超级管理员 只有超级管理员能操作
        if (userInfoDTO.getRight() != 0) {
            throw new RunTimeException("this user has no right to change project accountName");
        }

        if (!empty) {
            subRepositoryDao.setRecycled(subRepoUuid);
            return;
        }
        // TODO 基于 rest 调用所有扫描服务把与该 repo相关的所有数据删除

        accountRepositoryDao.deleteRelation(subRepoUuid);
        subRepositoryDao.deleteRepo(subRepoUuid);
    }


    @Override
    @SneakyThrows
    public List<SubRepository> query(String token) {
        UserInfoDTO userInfoDTO = getUserInfoByToken(token);
        String userUuid = userInfoDTO.getUuid();

        // 用户权限为admin时 查询所有的repo
        if (userInfoDTO.getRight().equals(AccountRoleEnum.ADMIN.getRight())) {
            userUuid = null;
            //return subRepositoryDao.getAllSubRepo();
        }
        if (userInfoDTO.getRight().equals(AccountRoleEnum.LEADER.getRight())) {

            List<SubRepository> leaderRepo = subRepositoryDao.getLeaderRepoByAccountUuid(userUuid);
            List<SubRepository> subRepo = subRepositoryDao.getRepoByAccountUuid(userUuid);
            if (subRepo != null) {
                leaderRepo.addAll(subRepo);
                leaderRepo = leaderRepo.stream().distinct().collect(Collectors.toList());
                leaderRepo.forEach(repo -> {
                    Integer projectId = projectDao.getProjectIdByName(repo.getProjectName());
                    repo.setLeaders(accountProjectDao.getLeaderListByProjectId(projectId));
                });
            }
            return leaderRepo;
        }

        // todo 用户权限为 DEVELOPER 时不允许查询项目列表
        List<SubRepository> allRepoList = subRepositoryDao.getAllSubRepoByAccountUuid(userUuid);
        allRepoList.forEach(repo -> {
            Integer projectId = projectDao.getProjectIdByName(repo.getProjectName());
            repo.setLeaders(accountProjectDao.getLeaderListByProjectId(projectId));
        });
        return allRepoList;
    }

    @Override
    public void addOneProject(String token, String projectName) throws Exception {
        UserInfoDTO userInfoDTO = getUserInfoByToken(token);
        String accountUuid = userInfoDTO.getUuid();
        // 0 表示超级管理员 只有超级管理员能操作
        if (userInfoDTO.getRight() != 0) {
            throw new RunTimeException("this user has no right to add project");
        }


        if (projectDao.getProjectByName(projectName) != null) {
            throw new RunTimeException("project name has been used");
        }
        //project表中插入信息
        projectDao.insertOneProject(accountUuid, projectName);
    }

    @Override
    public boolean deleteProject(String token, String projectName) throws Exception {
        UserInfoDTO userInfoDTO = getUserInfoByToken(token);
        // 0 表示超级管理员 只有超级管理员能操作
        if (userInfoDTO.getRight() != 0) {
            throw new RunTimeException("this user has no right to delete repo!");
        }
        List<String> ProjectRepo = projectDao.getProjectRepo(projectName);
        if(ProjectRepo.size() != 0){
            return false;
        }
        projectDao.deleteProject(projectName);
        return true;
    }

    @Override
    public void updateRepo(String token, String oldRepoName, String newRepoName) throws Exception {
        UserInfoDTO userInfoDTO = getUserInfoByToken(token);
        String accountUuid = userInfoDTO.getUuid();

        if (StringUtils.isEmpty(oldRepoName) || StringUtils.isEmpty(newRepoName) || oldRepoName.equals(newRepoName)) {
            return;
        }
        // 0 表示超级管理员 只有超级管理员能操作
        if (userInfoDTO.getRight() != 0) {
            throw new RunTimeException("this user has no right to change repo Name");
        }
        // 改变repo name 只有超级管理员才会有此权限
        log.warn("repo name changed by {}! old repoName is {}, new repoName is {}", userInfoDTO.getUuid(), oldRepoName, newRepoName);
        subRepositoryDao.updateRepoName(accountUuid, oldRepoName, newRepoName);
    }


    private void send(String projectId, String url, boolean isPrivate, String username, String password, String branch, String repoSource) {
        NeedDownload needDownload = new NeedDownload(projectId, repoSource, url, isPrivate, username, password, branch);
        kafkaTemplate.send("ProjectManager", JSONObject.toJSONString(needDownload));
        log.info("send message to topic ProjectManage ---> " + JSONObject.toJSONString(needDownload));
    }

    //flag, uuid, url,  username, branch, repoSource, repoName
    private void sendLocal(int flag, String projectId, String url, String username, String branch, String repoSource, String repoName) {
        LocalDownLoad localDownLoad = new LocalDownLoad(flag, projectId, url, username, branch, repoSource, repoName);
        kafkaTemplate.send("ProjectManager", JSONObject.toJSONString(localDownLoad));
        log.info("send message to topic ProjectManage ---> " + JSONObject.toJSONString(localDownLoad));
    }

    private synchronized UserInfoDTO getUserInfoByToken(String token) throws Exception {
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


    @Override
    public boolean deleteRepo(@NotNull String token, String repoUuid, String uuid) throws Exception {
        UserInfoDTO userInfoDTO = getUserInfoByToken(token);
        String accountUuid = userInfoDTO.getUuid();
        // 0 表示超级管理员 只有超级管理员能操作
        if (userInfoDTO.getRight() != 0) {
            throw new RunTimeException("this user has no right to delete repo!");
        }
        //该repo的所有projectName 都会改变 只有超级管理员才会有此权限
        log.warn("repo delete by {}! repo uuid is {}", accountUuid, repoUuid);

        if (uuid != null) {
            if (subRepositoryDao.getSubRepoByUuid(uuid) == null) {
                return false;
            } else {
                subRepositoryDao.deleteRepoByUuid(uuid);
                return true;
            }
        }

        if (repoUuid != null) {
            if (subRepositoryDao.getSubRepoByRepoUuid(repoUuid) == null) {
                return false;
            } else {
                // RECYCLED, JIRA, DEPENDENCY, CLONE, MEASURE, CODETRACKER, ISSUE, SCAN, REPOSITORY;

                //JIRA
                boolean deleteJiraRepoSuccess = rest.deleteJiraRepo(repoUuid);
                if (!deleteJiraRepoSuccess) {
                    log.error("jira repo delete failed!");
                }

                //CLONE
                boolean deleteCloneRepoSuccess = rest.deleteCloneRepo(repoUuid);
                if (!deleteCloneRepoSuccess) {
                    log.error("clone repo delete failed!");
                }

                //CODETRACKER
                boolean deleteCodetrackerRepoSuccess = rest.deleteCodetrackerRepo(repoUuid);
                if (!deleteCodetrackerRepoSuccess) {
                    log.error("codetracker repo delete failed!");
                }

                //REPOSITORY
                boolean deleteCommitRepoSucess = rest.deleteCommitRepo(repoUuid);
                if (!deleteCommitRepoSucess) {
                    log.error("commit repo delete failed!");
                }

                //ISSUE
                boolean deleteIssueRepoSuccess = rest.deleteIssueRepo(token, repoUuid);
                if (!deleteIssueRepoSuccess) {
                    log.error("issue repo delete failed!");
                }

                //MEASURE
                boolean deleteMeasureRepoSucess = rest.deleteMeasureRepo(repoUuid);
                if (!deleteMeasureRepoSucess) {
                    log.error("measure repo delete failed!");
                }

                //SCAN
                boolean deleteScanRepoSucess = rest.deleteScanRepo(token, repoUuid);
                if (!deleteScanRepoSucess) {
                    log.error("scan repo delete failed!");
                }

                //DEPENDENCY
                boolean deleteDependencyRepoSucess = rest.deleteDependencyRepo(repoUuid);
                if (!deleteDependencyRepoSucess) {
                    log.error("scan repo delete failed!");
                }

                if (!deleteCloneRepoSuccess || !deleteCodetrackerRepoSuccess || !deleteCommitRepoSucess || !deleteIssueRepoSuccess
                        || !deleteMeasureRepoSucess || !deleteScanRepoSucess || !deleteDependencyRepoSucess || !deleteJiraRepoSuccess) {
                    throw new RunTimeException("delete failed!");
                }

            }
        }
        return true;

    }

    @Override
    public Integer updateRecycleStatus(String token, String repoUuid, Integer recycled) throws Exception {
        UserInfoDTO userInfoDTO = getUserInfoByToken(token);
        final Integer outRecycle = 0;
        final Integer inRecycle = 100000000;

        // 0 表示超级管理员 只有超级管理员能操作
        if (userInfoDTO.getRight() != 0) {
            throw new RunTimeException("this user has no right to change repo recycled");
        }

        if (recycled == 0) {
            //将库放入回收站中
            recycled = inRecycle;
            subRepositoryDao.putRecycledStatus(repoUuid, recycled);
            return subRepositoryDao.getRecycledStatus(repoUuid);
        }

        if (recycled == 100000000) {
            //将库从回收站中拿出
            recycled = outRecycle;
            subRepositoryDao.putRecycledStatus(repoUuid, recycled);
            return subRepositoryDao.getRecycledStatus(repoUuid);
        }

        return null;
    }

    @Override
    public SubRepository getRepoInfoByUrl(String Url) {
        return subRepositoryDao.getSubRepoByUrl(Url);
    }

    @Override
    public Integer updateRecycled(String repoUuid, String serviceName) throws Exception {
        EnumMap<ServicesManager.Services, Character> serviceStatus = new EnumMap<>(ServicesManager.Services.class);

        Integer recycledStatus = subRepositoryDao.getRecycledStatus(repoUuid);
        //recycledStatus = new Scanner(System.in).nextInt();
        //RECYCLED, JIRA, DEPENDENCY, CLONE, MEASURE, CODETRACKER, ISSUE, SCAN, REPOSITORY;
        char[] deleteStatus = String.valueOf(recycledStatus).toCharArray();
        serviceStatus.put(ServicesManager.Services.RECYCLED, deleteStatus[0]);
        serviceStatus.put(ServicesManager.Services.JIRA, deleteStatus[1]);
        serviceStatus.put(ServicesManager.Services.DEPENDENCY, deleteStatus[2]);
        serviceStatus.put(ServicesManager.Services.CLONE, deleteStatus[3]);
        serviceStatus.put(ServicesManager.Services.MEASURE, deleteStatus[4]);
        serviceStatus.put(ServicesManager.Services.CODETRACKER, deleteStatus[5]);
        serviceStatus.put(ServicesManager.Services.ISSUE, deleteStatus[6]);
        serviceStatus.put(ServicesManager.Services.SCAN, deleteStatus[7]);
        serviceStatus.put(ServicesManager.Services.REPOSITORY, deleteStatus[8]);


        //服务顺序是RECYCLED, JIRA, DEPENDENCY, CLONE, MEASURE, CODETRACKER, ISSUE, SCAN, REPOSITORY
        if("JIRA".equals(serviceName)){
            serviceStatus.put(ServicesManager.Services.JIRA, '1');
        }else if("DEPENDENCY".equals(serviceName)){
            serviceStatus.put(ServicesManager.Services.DEPENDENCY, '1');
        }else if("CLONE".equals(serviceName)){
            serviceStatus.put(ServicesManager.Services.CLONE, '1');
        }else if("MEASURE".equals(serviceName)){
            serviceStatus.put(ServicesManager.Services.MEASURE, '1');
        }else if("CODETRACKER".equals(serviceName)){
            serviceStatus.put(ServicesManager.Services.CODETRACKER, '1');
        }else if("ISSUE".equals(serviceName)){
            serviceStatus.put(ServicesManager.Services.ISSUE, '1');
        }else if("SCAN".equals(serviceName)){
            serviceStatus.put(ServicesManager.Services.SCAN, '1');
        }else if("REPOSITORY".equals(serviceName)){
            serviceStatus.put(ServicesManager.Services.REPOSITORY, '1');
        }

       String statusCollection = serviceStatus.get(ServicesManager.Services.RECYCLED).toString() + serviceStatus.get(ServicesManager.Services.JIRA).toString()
               + serviceStatus.get(ServicesManager.Services.DEPENDENCY).toString() + serviceStatus.get(ServicesManager.Services.CLONE).toString()
               + serviceStatus.get(ServicesManager.Services.MEASURE).toString() + serviceStatus.get(ServicesManager.Services.CODETRACKER).toString()
               + serviceStatus.get(ServicesManager.Services.ISSUE).toString() + serviceStatus.get(ServicesManager.Services.SCAN).toString()
               + serviceStatus.get(ServicesManager.Services.REPOSITORY).toString();

        Integer status = Integer.parseInt(statusCollection);
        subRepositoryDao.putRecycledStatus(repoUuid, status);

        return subRepositoryDao.getRecycledStatus(repoUuid);
    }

    @Override
    public Boolean deleteRepoInfo(String token, String repoUuid) throws Exception {
        UserInfoDTO userInfoDTO = getUserInfoByToken(token);
        String accountUuid = userInfoDTO.getUuid();

        if (subRepositoryDao.getRecycledStatus(repoUuid) == 111111111) {
            accountRepositoryDao.deleteRepoAR(accountUuid, repoUuid);
            subRepositoryDao.deleteRepoSR(accountUuid, repoUuid);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Integer updateProjectLifeStatus(String token, String projectName, Integer lifeStatus) throws Exception {
        UserInfoDTO userInfoDTO = getUserInfoByToken(token);

        // 0 表示超级管理员 只有超级管理员能操作
        if (userInfoDTO.getRight() != 0) {
            throw new RunTimeException("this user has no right to change repo recycled");
        }

        if (lifeStatus != 1 && lifeStatus != 2) {
            return 0;
        }else{
            projectDao.updateLifeStatus(projectName, lifeStatus);
            return projectDao.getProjectLifeStatus(projectName);
        }

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
    public void setAccountRepositoryDao(AccountRepositoryDao accountRepositoryDao) {
        this.accountRepositoryDao = accountRepositoryDao;
    }

    @Autowired
    public void setProjectDao(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    @Autowired
    public void setAccountProjectDao(AccountProjectDao accountProjectDao) {
        this.accountProjectDao = accountProjectDao;
    }

    @Autowired
    public void setKafkaTemplate(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

}