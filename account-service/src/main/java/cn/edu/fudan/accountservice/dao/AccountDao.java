package cn.edu.fudan.accountservice.dao;

import cn.edu.fudan.accountservice.domain.Account;
import cn.edu.fudan.accountservice.domain.Tool;
import cn.edu.fudan.accountservice.mapper.AccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class AccountDao {


    private AccountMapper accountMapper;

    @Autowired
    public void setAccountMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    public Account login(String accountName, String password) {
        return accountMapper.login(accountName, password);
    }

    public Account getAccountByAccountName(String accountName) {
        return accountMapper.getAccountByAccountName(accountName);
    }

    public boolean isAccountNameExist(String accountName) {
        return getAccountByAccountName(accountName) != null;
    }

    public boolean isNameExist(String name) {
        return accountMapper.getAccountIdByName(name) != null;
    }

    public boolean isEmailExist(String email) {
        return accountMapper.getAccountByEmail(email) != null;
    }

    public List<Map<String,String>> getStatusByName(List name){return accountMapper.getStatusByName(name);}

    public List<Account> getAccountStatus(){ return accountMapper.getAccountStatus(); }

    public void updateAccountStatus(List<Account> statusInfo){
        accountMapper.updateStatusInfo(statusInfo);
    }

    public void addAccount(Account account) {
        accountMapper.addAccount(account);
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
}
