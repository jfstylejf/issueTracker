package cn.edu.fudan.accountservice.dao;

import cn.edu.fudan.accountservice.domain.Account;
import cn.edu.fudan.accountservice.domain.Tool;
import cn.edu.fudan.accountservice.mapper.AccountAuthorMapper;
import cn.edu.fudan.accountservice.mapper.AccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class AccountDao {


    private AccountMapper accountMapper;
    private AccountAuthorMapper accountAuthorMapper;

    public Account login(String accountName, String password) {
        return accountMapper.login(accountName, password);
    }

    public Account getAccountByAccountName(String accountName) {
        return accountMapper.getAccountByAccountName(accountName);
    }

    public Account getAccountByAccountNameExceptAdmin(String accountName) {
        return accountMapper.getAccountByAccountNameExceptAdmin(accountName);
    }

    public boolean isAccountNameExist(String accountName) {
        return getAccountByAccountName(accountName) != null;
    }

    public boolean isEmailExist(String email) {
        return accountMapper.getAccountByEmail(email) != null;
    }

    public List<Map<String,String>> getStatusByName(List name){return accountMapper.getStatusByName(name);}

    public List<Account> getAccountStatus(){ return accountMapper.getAllAccount(); }

    public void updateAccountStatus(List<Account> statusInfo){
        accountMapper.updateStatusInfo(statusInfo);
    }

    public List<String> getAllAccountId() {
        return accountMapper.getAllAccountId();
    }

    public void updateToolsEnable(List<Tool> tools){
        accountMapper.updateToolsEnable(tools);
    }

    public List<Tool> getTools(){ return accountMapper.getTools(); }

    public String getAccountNameById(String uuid){
        return accountMapper.getAccountNameById(uuid);
    }

    public Map<String,Object> getRightByAccountName(String accountName) {
        List<Map<String,Object>> rightList = accountMapper.getRightByAccountName(accountName);
        Map<String,Object> accountInfo = rightList.get(0);

        Integer accountRight = (Integer) accountInfo.get("account_right");
        accountInfo.put("right", accountRight);
        accountInfo.remove("account_right");
        return accountInfo;
    }

    public List<String> getAccountGitname() {
        return accountMapper.getOldAccountGitName();
    }

    public void addAccounts(List<Account> accounts) {
        accountMapper.addAccounts(accounts);
    }

    public void addAccount(Account account) {
        addAccounts(Collections.singletonList(account));
    }

    public String getAccountName(String email) {
        return accountMapper.getAccountName(email);
    }

    public void setPasswordByUserName(String username, String encodePassword){accountMapper.setPasswordByUserName(username, encodePassword); }

    public void resetSubAccount(String subAccountName, String majorAccountName, String majorAccountUuid){
        accountAuthorMapper.resetSubAccount(subAccountName, majorAccountName, majorAccountUuid);}

    public List<String> getGitnameByAccountName(String majorAccountName){
        return accountAuthorMapper.getGitnameByAccountName(majorAccountName);
    }

    @Autowired
    public void setAccountMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    @Autowired
    public void setAccountAuthorMapper(AccountAuthorMapper accountAuthorMapper) {
        this.accountAuthorMapper = accountAuthorMapper;
    }
}
