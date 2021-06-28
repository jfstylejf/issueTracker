package cn.edu.fudan.accountservice.mapper;


import cn.edu.fudan.accountservice.domain.Account;
import cn.edu.fudan.accountservice.domain.Tool;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AccountMapper {

    /**
     * login
     *
     * @param accountName get user accountName
     * @param password  get user password
     * @return Account
     */
    Account login(@Param("accountName") String accountName, @Param("password") String password);

    /**
     * get account by email
     *
     *
     * @param email get user accountName
     * @return Account
     */
    Account getAccountByEmail(@Param("email") String email);

    /**
     * get account status by account name
     *
     * @param accountName 用户名
     * @return String 状态
     */
    String getStatusByName(@Param("accountName") String accountName);

    List<Account> getAllAccount();

    /**
     * update statusInfo by account accountName
     *
     * @param statusInfo
     * @return null
     */
    void updateStatusInfo(List<Account> statusInfo);

    /**
     * get account by account accountName
     *
     * @param accountName get account accountName
     * @return Account
     */
    Account getAccountByAccountName(String accountName);

    /**
     * get account by account accountName except admin
     *
     * @param accountName username
     * @return Account
     */
    Account getAccountByAccountNameExceptAdmin(String accountName);

    /**
     * get all account id
     *
     * @return List<String>
     */
    List<String> getAllAccountId();

    void updateToolsEnable(List<Tool> tools);

    List<Tool> getTools();

    String getAccountNameById(String id);

    /**
     *  一次插入多个账户
     * @param accounts 多个账户
     */
    void addAccounts(List<Account> accounts);


    @Select("select uuid,account_right from account where account_name = #{accountName}")
    List<Map<String,Object>> getRightByAccountName(String accountName);

    /**
     * get git accountName
     *
     * @return List<String>
     */
    @Select("SELECT account_gitname FROM account_author;")
    List<String> getOldAccountGitName();

    @Select("select account_name from account where email = #{email}")
    String getAccountName(String email);

    /**
     * 通过accountName获取password
     * @param username 用户名
     * @param encodePassword 新密码
     */
    void setPasswordByUserName(String username, String encodePassword);
}
