package cn.edu.fudan.accountservice.service;


import cn.edu.fudan.accountservice.domain.Account;
import cn.edu.fudan.accountservice.domain.AccountVO;
import cn.edu.fudan.accountservice.domain.Tool;
import cn.edu.fudan.accountservice.util.PagedGridResult;

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
    AccountVO login(String username, String password, String email);

    /**
     * passwordReset
     *
     * @param username get user accountName
     * @param password get user password
     * @return null
     */
    boolean passwordReset(String username, String password);

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
     * @param accountName 用户名
     * @return status and accountName
     */
    Map<String, Integer> getStatusByName(List<String> accountName);

    /**
     * update status
     *
     * @param statusInfo
     * @return null
     */
    void updateAccountStatus(List<Account> statusInfo);

    /**
     * 分页获取人员列表
     * @param accountStatus 用户在职状态
     * @param accountName 用户在职状态
     * @return List<Account> 人员列表
     */
    List<Account> getAccountList(String accountStatus, String accountName);

    /**
     * 获取人员列表
     * @param accountStatus 用户在职状态
     * @param accountName 用户在职状态
     * @param page 分页的第几页
     * @param pageSize 每页的大小
     * @param order 排序的字段
     * @param isAsc 是否升序
     * @return PagedGridResult 获取分页后的人员列表
     */
    PagedGridResult getAccountList(String accountStatus, String accountName, Integer page, Integer pageSize, String order, Boolean isAsc);

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
     * @param accountName 用户名
     * @return Account
     */
    Account getAccountByName(String accountName, Boolean needAdmin);

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
    Boolean addNewAccounts(List<String> gitname);

    /**
     *
     * @param repoList 参与的库
     * @param since 起始时间
     * @param until 结束时间
     * @param page 分页的第几页
     * @param pageSize 每页的大小
     * @param order 排序的字段
     * @param isAsc 是否升序
     * @param accountStatus 用户在职状态
     * @return 根据查询条件获取开发者（聚合后）的列表
     */
    PagedGridResult getDevelopers(List<String> repoList, String since, String until, String developers, Integer page, Integer pageSize, String order, Boolean isAsc, String accountStatus);

    /**
     *
     * @param repoList 参与的库
     * @param since 起始时间
     * @param until 结束时间
     * @param accountStatus 用户在职状态
     * @return 获取给定条件下 所有的开发者列表 不进行分页
     */
    List<Map<String, Object>> getDevelopers(List<String> repoList, String since, String until, String accountStatus);

    /**
     * 人员手动聚合
     *
     * @param majorAccountName 主合并人姓名
     * @param subAccountName 被合并人姓名
     * @return List<String>
     */
    List<String> accountMerge(String majorAccountName, String subAccountName, String token) throws Exception;
}
