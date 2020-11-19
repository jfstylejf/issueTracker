package cn.edu.fudan.accountservice.service.impl;

import cn.edu.fudan.accountservice.dao.AccountDao;
import cn.edu.fudan.accountservice.domain.*;
import cn.edu.fudan.accountservice.mapper.AccountAuthorMapper;
import cn.edu.fudan.accountservice.service.AccountService;
import cn.edu.fudan.accountservice.util.Base64Util;
import cn.edu.fudan.accountservice.util.MD5Util;
import cn.edu.fudan.common.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
public class AccountServiceImpl implements AccountService {

    private StringRedisTemplate stringRedisTemplate;
    private AccountAuthorMapper accountAuthorMapper;

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
    public AccountVO login(String username, String encodedPassword) {
        //Base64解密
        String password = Base64Util.decodePassword(encodedPassword);
        //首次登录或token过期重新登录，返回新的token
        String encodePassword = MD5Util.md5(username + password);
        Account account = accountDao.login(username, encodePassword);
        if (account != null) {
            String userToken = MD5Util.md5(encodePassword);
            int userRight = account.getRight();
            stringRedisTemplate.opsForValue().set("login:" + userToken, username);
            //token保存7天
            stringRedisTemplate.expire("login:" + userToken, 7, TimeUnit.DAYS);
            return new AccountVO(username, userToken, userRight);
        } else {
            return null;
        }
    }

    @Override
    public boolean isAccountNameExist(String accountName) {
        return accountDao.isAccountNameExist(accountName);
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
            String authorName = m.get("accountName");
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
        if (account.getAccountName() == null || account.getPassword() == null || account.getEmail() == null) {
            throw new RuntimeException("param loss");
        }
        if (isAccountNameExist(account.getAccountName())) {
            throw new RuntimeException("accountName has been used!");
        }
        if (isEmailExist(account.getEmail())) {
            throw new RuntimeException("email has been used!");
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
        if(right == AccountRoleEnum.ADMIN.getRight()){
            accountDao.updateToolsEnable(tools);
        }

    }

    @Override
    public List<Tool> getTools(){
        return accountDao.getTools
                ();
    }

    @Override
    public String getAccountNameById(String accountId) {

        return accountDao.getAccountNameById(accountId);
    }

    @Override
    public Map<String,Object> getRightByToken(String userToken) {
        String username = stringRedisTemplate.opsForValue().get("login:" + userToken);
        return accountDao.getRightByAccountName(username);
    }

    @Override
    public void addNewAccounts(List<String> gitNames) {
        List<Account> accounts = gitNames.stream().
                filter(gitName -> !accountDao.getAccountGitname().contains(gitName)).
                map(Account::newInstance).
                collect(Collectors.toList());
        accountDao.addAccounts(accounts);
        accountAuthorMapper.batchInsertAccountAuthor(accounts.stream().map(AccountAuthor::newInstanceOf).collect(Collectors.toList()));

        // todo 查询新增人员在哪个项目 后续更新account_project 表
    }

    @Autowired
    public void setAccountAuthorMapper(AccountAuthorMapper accountAuthorMapper) {
        this.accountAuthorMapper = accountAuthorMapper;
    }
}
