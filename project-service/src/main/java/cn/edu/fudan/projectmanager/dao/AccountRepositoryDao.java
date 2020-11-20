package cn.edu.fudan.projectmanager.dao;

import cn.edu.fudan.projectmanager.domain.AccountRepository;
import cn.edu.fudan.projectmanager.mapper.AccountRepositoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * @author fancying
 */
@Repository
public class AccountRepositoryDao {


    private AccountRepositoryMapper accountRepositoryMapper;

    public void insertAccountRepository(AccountRepository accountRepository){
        insertAccountRepositories(Collections.singletonList(accountRepository));
    }

    public void insertAccountRepositories(List<AccountRepository> accountRepositories){
        accountRepositoryMapper.insertAccountRepositories(accountRepositories);
    }


    public Boolean hasRepo(String accountUuid, String url) {
        return accountRepositoryMapper.getRepoCount(accountUuid, url) == 1;
    }

    public void updateRepoName(String accountUuid, String oldName, String newName) {
        accountRepositoryMapper.updateRepoName(accountUuid, oldName, newName);
    }

    public void deleteRelation(String subRepoUuid) {
        accountRepositoryMapper.deleteRelation(subRepoUuid);
    }

    @Autowired
    public void setAccountRepositoryMapper(AccountRepositoryMapper accountRepositoryMapper) {
        this.accountRepositoryMapper = accountRepositoryMapper;
    }
}