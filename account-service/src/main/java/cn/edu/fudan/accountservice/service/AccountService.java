package cn.edu.fudan.accountservice.service;


import cn.edu.fudan.accountservice.domain.Account;
import cn.edu.fudan.accountservice.domain.ResponseBean;
import cn.edu.fudan.accountservice.domain.Tool;

import java.util.List;


public interface AccountService {

    /**
     * login
     *
     * @param username get user name
     * @param password get user password
     * @return ResponseBean
     */
    ResponseBean login(String username, String password);

    /**
     * get user name by token
     *
     * @param userToken get user token
     * @return String
     */
    String getUserNameByToken(String userToken);

    /**
     * is account name exist
     *
     * @param accountName get user account name
     * @return boolean
     */
    boolean isAccountNameExist(String accountName);

    /**
     * is name exist
     *
     * @param name get user name
     * @return boolean
     */
    boolean isNameExist(String name);

    /**
     * is email exist
     *
     * @param email get user email
     * @return boolean
     */
    boolean isEmailExist(String email);

    /**
     * get status by name
     *
     * @param name get user status
     * @return status and name
     */
    Object getStatusByName(List name);


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
}
