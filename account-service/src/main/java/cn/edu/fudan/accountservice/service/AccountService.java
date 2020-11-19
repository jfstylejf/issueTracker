package cn.edu.fudan.accountservice.service;


import cn.edu.fudan.accountservice.domain.Account;
import cn.edu.fudan.accountservice.domain.Tool;
import cn.edu.fudan.common.http.ResponseEntity;

import java.util.List;
import java.util.Map;


public interface AccountService {

    /**
     * login
     *
     * @param username get user accountName
     * @param password get user password
     * @return ResponseEntity
     */
    ResponseEntity login(String username, String password);

    /**
     * is account accountName exist
     *
     * @param accountName get user account accountName
     * @return boolean
     */
    boolean isAccountNameExist(String accountName);

    /**
     * is email exist
     *
     * @param email get user email
     * @return boolean
     */
    boolean isEmailExist(String email);

    /**
     * get status by accountName
     *
     * @param name get user status
     * @return status and accountName
     */
    Object getStatusByName(List name);

    /**
     * update status
     *
     * @param statusInfo
     * @return null
     */
    void updateAccountStatus(List<Account> statusInfo);

    List<Account> getAccountStatus();

    /**
     * auth by token
     *
     * @param userToken get user token
     * @return boolean
     */
    boolean authByToken(String userToken);

    /**
     * get account by token
     *
     * @param userToken get user token
     * @return Account
     */
    Account getAccountByToken(String userToken);

    /**
     * addAccount
     *
     * @param account get Account object
     */
    void addAccount(Account account);

    /**
     * get all account id
     *
     * @return List<String>
     */
    List<String> getAllAccountId();

    List<String> getGroupsByAccountName(String accountName);

    void updateToolsEnable(List<Tool> tools);

    List<Tool> getTools();

    String getAccountNameById(String accountId);

    Map<String,Object> getRightByToken(String userToken);

    /**
     * 查询 account_author表中的所有 gitname
     * @param gitname  新增用户的git 名字
     */
    void addNewAccounts(List<String> gitname);


}
