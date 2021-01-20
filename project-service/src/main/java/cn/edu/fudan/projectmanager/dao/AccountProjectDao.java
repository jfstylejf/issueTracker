package cn.edu.fudan.projectmanager.dao;

import cn.edu.fudan.projectmanager.domain.Account;
import cn.edu.fudan.projectmanager.mapper.AccountMapper;
import cn.edu.fudan.projectmanager.mapper.ProjectMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * description:
 *
 * @author Richy
 * create: 2020-11-25 10:44
 **/
@Repository
public class AccountProjectDao {
    private AccountMapper accountMapper;

    public void updateProjectNameAP(String accountUuid,String oldProjectName,String newProjectName) {
        accountMapper.updateProjectNameAP(accountUuid, oldProjectName, newProjectName);
    }

    public void addProjectLeaderAP(String accountUuid, String newLeaderId, Integer projectId) {
        accountMapper.addProjectLeaderAP(accountUuid, newLeaderId, projectId);
    }

    public boolean isProjectLeaderExist(String newLeaderId, Integer projectId) {
        return accountMapper.getProjectLeader(newLeaderId, projectId) != null;
    }

    public void deleteProjectLeaderAP(String accountUuid, String LeaderId, Integer projectId) {
        accountMapper.deleteProjectLeaderAP(accountUuid, LeaderId, projectId);
    }

    public List<Map<String, String>> getLeaderListByProjectId(Integer projectId) {
        return accountMapper.getLeaderListByProjectId(projectId);
    }

    @Autowired
    public void setAccountMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }
}