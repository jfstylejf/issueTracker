package cn.edu.fudan.accountservice.service.impl;

import cn.edu.fudan.accountservice.dao.AccountDao;
import cn.edu.fudan.accountservice.domain.Account;
import cn.edu.fudan.accountservice.domain.AccountInfo;
import cn.edu.fudan.accountservice.domain.ResponseBean;
import cn.edu.fudan.accountservice.domain.Tool;
import cn.edu.fudan.accountservice.service.AccountService;
import cn.edu.fudan.accountservice.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
public class AccountServiceImpl implements AccountService {

    private StringRedisTemplate stringRedisTemplate;


    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private AccountDao accountDao;

    @Autowired
    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public ResponseBean login(String username, String password) {
        //首次登录或token过期重新登录，返回新的token
        String encodePassword = MD5Util.md5(username + password);
        Account account = accountDao.login(username, encodePassword);
        if (account != null) {
            String userToken = MD5Util.md5(encodePassword);
            int userRight = account.getRight();
            stringRedisTemplate.opsForValue().set("login:" + userToken, username);
            stringRedisTemplate.expire("login:" + userToken, 30, TimeUnit.DAYS);//token保存7天
            return new ResponseBean(200, "登录成功!", new AccountInfo(username, userToken, userRight));
        } else {
            return new ResponseBean(401, "用户名或密码错误!", null);
        }
    }

    @Override
    public String getUserNameByToken(String userToken) {
        return stringRedisTemplate.opsForValue().get("login:" + userToken);
    }

    @Override
    public boolean isAccountNameExist(String accountName) {
        return accountDao.isAccountNameExist(accountName);
    }

    @Override
    public boolean isNameExist(String name) {
        return accountDao.isNameExist(name);
    }

    @Override
    public boolean isEmailExist(String email) {
        return accountDao.isEmailExist(email);
    }

    @Override
    public Object getStatusByName(List name) {
        List<Map<String,String>> result = accountDao.getStatusByName(name);
        Map<String, Integer> nameStatus = new HashMap<>(8);
        for(Map<String,String> m : result){
            String authorName = m.get("name");
            String authorStatus = m.get("account_status");
            nameStatus.put(authorName,Integer.valueOf(authorStatus));
        }
        return nameStatus;
    }

    @Override
    public void updateAccountStatus(List<Account> statusInfo) {
        if(statusInfo.size()!=0){
            accountDao.updateAccountStatus(statusInfo);
        }
        return;
    }

    @Override
    public List<Account> getAccountStatus(){
        return accountDao.getAccountStatus();
    }

    @Override
    public boolean authByToken(String userToken) {
        return stringRedisTemplate.opsForValue().get("login:" + userToken) != null;
    }

    @Override
    public Account getAccountByToken(String userToken) {
        if(userToken != null){
            String username = stringRedisTemplate.opsForValue().get("login:" + userToken);
            return accountDao.getAccountByAccountName(username);
        }
        return null;
    }

    @Override
    public void addAccount(Account account) {
        if (account.getAccountName() == null || account.getPassword() == null || account.getEmail() == null || account.getName() == null) {
            throw new RuntimeException("param loss");
        }
        if (isAccountNameExist(account.getAccountName())) {
            throw new RuntimeException("accountName has been used!");
        }
        if (isEmailExist(account.getEmail())) {
            throw new RuntimeException("email has been used!");
        }

        if (isNameExist(account.getName())) {
            throw new RuntimeException("nickName has been used!");
        }
        account.setUuid(UUID.randomUUID().toString());
        account.setPassword(MD5Util.md5(account.getAccountName() + account.getPassword()));
        accountDao.addAccount(account);
    }

    @Override
    public List<String> getAllAccountId() {
        return accountDao.getAllAccountId();
    }

    @Override
    public List<String> getGroupsByAccountName(String accountName) {
//        String group = accountDao.getAccountByAccountName(accountName).getGroups();
//        if(null!=group){
//            List<String> groups = Arrays.asList(group.split(",")).stream().map(s -> (s.trim())).collect(Collectors.toList());
//            return groups;
//        }
        return null;
    }

    @Override
    public void updateToolsEnable(List<Tool> tools) {
        String accountName = tools.get(0).getAccountName();
        int right = accountDao.getAccountByAccountName(accountName).getRight();
        if(right == 1){
            accountDao.updateToolsEnable(tools);
        }

    }

    @Override
    public List<Tool> getTools(){
        return accountDao.getTools();
    }

    @Override
    public String getAccountNameById(String accountId) {

        return accountDao.getAccountNameById(accountId);
    }
}
