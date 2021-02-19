package cn.edu.fudan.projectmanager.service.impl;

import cn.edu.fudan.projectmanager.component.RestInterfaceManager;
import cn.edu.fudan.projectmanager.dao.AccountProjectDao;
import cn.edu.fudan.projectmanager.dao.AccountRepositoryDao;
import cn.edu.fudan.projectmanager.dao.ProjectDao;
import cn.edu.fudan.projectmanager.dao.SubRepositoryDao;
import cn.edu.fudan.projectmanager.domain.AccountRoleEnum;
import cn.edu.fudan.projectmanager.domain.Project;
import cn.edu.fudan.projectmanager.domain.SubRepository;
import cn.edu.fudan.projectmanager.domain.dto.UserInfoDTO;
import cn.edu.fudan.projectmanager.exception.RunTimeException;
import cn.edu.fudan.projectmanager.mapper.AccountMapper;
import cn.edu.fudan.projectmanager.service.AccountRepositoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description: 项目和库和人关系实现
 *
 * @author Richy
 * create: 2021-01-14 15:14
 **/

@Service
@Slf4j
public class AccountRepositoryServiceImpl implements AccountRepositoryService {

    private static Map<String, UserInfoDTO> userInfos = new ConcurrentHashMap<>(32);
    private RestInterfaceManager rest;

    private AccountRepositoryDao accountRepositoryDao;
    private SubRepositoryDao subRepositoryDao;
    private ProjectDao projectDao;
    private AccountProjectDao accountProjectDao;
    private AccountMapper accountMapper;

    /**
     * @return k projectName v: list [k: repo_id, name]
     */
    @Override
    public Map<String, List<Map<String, String>>> getProjectAndRepoRelation(int recycled) {
        List<Map<String, Object>> projects = subRepositoryDao.getAllProjectRepoRelation();
        boolean isAll = recycled == SubRepository.ALL;

        Map<String, List<Map<String, String>>> result = new HashMap<>(8);
        for (Map<String, Object> project : projects) {

            int recycledStatus = (int) project.get("recycled");
            if (!isAll && recycled != recycledStatus) {
                continue;
            }

            String projectName = (String) project.get("project_name");
            if (StringUtils.isEmpty(projectName)) {
                projectName = "unnamed";
            }
            if (!result.keySet().contains(projectName)) {
                result.put(projectName, new ArrayList<>(4));
            }
            List<Map<String, String>> v = result.get(projectName);
            Map<String, String> p = new HashMap<>(4);
            p.put("repo_id", (String) project.get("repo_uuid"));
            p.put("name", (String) project.get("repo_name"));
            v.add(p);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getProjectAll(String token) throws Exception {
        List<Project> projectList = projectDao.getProjectList();
        List<Map<String, Object>> results = new ArrayList<>();
        projectList.forEach(project -> {
            Map<String, Object> entity = new HashMap<>();
            entity.put("projectId", project.getId());
            entity.put("projectName", project.getProjectName());
            entity.put("leaders", accountProjectDao.getLeaderListByProjectId(project.getId()));
            results.add(entity);
        });
        return results;
    }

    @Override
    public SubRepository getRepoInfoByRepoId(String repoUuid) {
        return subRepositoryDao.getSubRepoByRepoUuid(repoUuid);
    }

    @Override
    public String getRepoUuidByUuid(String uuid) throws Exception {
        return subRepositoryDao.getSubRepoByUuid(uuid).getRepoUuid();
    }

    @Override
    public void updateRepoProject(String token, String oldProjectName, String newProjectName, String RepoUuid) throws Exception {
        UserInfoDTO userInfoDTO = getUserInfoByToken(token);
        String accountUuid = userInfoDTO.getUuid();

        if (StringUtils.isEmpty(oldProjectName) || StringUtils.isEmpty(newProjectName) || oldProjectName.equals(newProjectName)) {
            return;
        }
        // 0 表示超级管理员 只有超级管理员能操作
        if (userInfoDTO.getRight() != 0) {
            throw new RunTimeException("this user has no right to change project Name");
        }
        //该repo的所有projectName 都会改变 只有超级管理员才会有此权限
        log.warn("projectName changed by {}! old projectName is {}, new projectName is {}", userInfoDTO.getUuid(), oldProjectName, newProjectName);
        accountRepositoryDao.updateRepoProjectAR(accountUuid, oldProjectName, newProjectName, RepoUuid);
        subRepositoryDao.updateRepoProjectSR(accountUuid, oldProjectName, newProjectName, RepoUuid);
    }

    @Override
    public boolean addProjectLeader(String token, String newLeaderId, Integer projectId) throws Exception {
        UserInfoDTO userInfoDTO = getUserInfoByToken(token);
        String accountUuid = userInfoDTO.getUuid();

        if (accountProjectDao.isProjectLeaderExist(newLeaderId, projectId) == true) {
            return false;
        }

        if (StringUtils.isEmpty(newLeaderId) || StringUtils.isEmpty(projectId)) {
            return false;
        }
        // 0 表示超级管理员 只有超级管理员能操作
        if (userInfoDTO.getRight() != 0) {
            throw new RunTimeException("this user has no right to change project Leader");
        }
        //只有超级管理员才会有此权限
        log.warn("project leader changed by {}! new leader is {}", userInfoDTO.getUuid(), newLeaderId);
        accountProjectDao.addProjectLeaderAP(accountUuid, newLeaderId, projectId);
        return true;
    }

    @Override
    public void deleteProjectLeader(String token, String LeaderId, Integer projectId) throws Exception {
        UserInfoDTO userInfoDTO = getUserInfoByToken(token);
        String accountUuid = userInfoDTO.getUuid();

        if (StringUtils.isEmpty(LeaderId) || StringUtils.isEmpty(projectId)) {
            return;
        }
        // 0 表示超级管理员 只有超级管理员能操作
        if (userInfoDTO.getRight() != 0) {
            throw new RunTimeException("this user has no right to delete project Leader");
        }
        //只有超级管理员才会有此权限
        log.warn("project leader deleted by {}!", userInfoDTO.getUuid());
        accountProjectDao.deleteProjectLeaderAP(accountUuid, LeaderId, projectId);
    }

    @Override
    public List<SubRepository> getRepoByAccountUuid(String accountUuid) throws Exception {
        String accountName = rest.getAccountName(accountUuid);
        if(accountName == null){
            return null;
        }
        return subRepositoryDao.getAllSubRepoByAccountUuid(accountUuid);
    }

    @Override
    public List<Map<String, Object>> getProjectInfoByAccountName(String accountName) throws Exception {
        // 根据accountName 查询权限
        int accountRight = accountMapper.queryRightByAccountName(accountName);
        if (accountRight == AccountRoleEnum.ADMIN.getRight()) {
            accountName = null;
        }
        return accountMapper.getProjectInfoByAccountName(accountName);
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
    public void setAccountMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }
}