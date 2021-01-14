package cn.edu.fudan.projectmanager.dao;

import cn.edu.fudan.projectmanager.mapper.AccountMapper;
import cn.edu.fudan.projectmanager.mapper.ProjectMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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

    public void addProjectLeaderAP(String accountUuid, String newLeaderId, String projectId) {
        accountMapper.addProjectLeaderAP(accountUuid, newLeaderId, projectId);
    }

    @Autowired
    public void setAccountMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }
}