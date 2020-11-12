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
     * get account id by name
     *
     * @param name get user name
     * @return String type
     */
    String getAccountIdByName(@Param("name") String name);

    /**
     * get account by email
     *
     *
     * @param email get user name
     * @return Account
     */
    Account getAccountByEmail(@Param("email") String email);

    /**
     * get account by status
     *
     * @param name get user name
     * @return Account
     */
    List<Map<String,String>> getStatusByName(@Param("name_list") List<String> name);

    List<Account> getAccountStatus();

    /**
     * update statusInfo by account name
     *
     * @param statusInfo
     * @return null
     */
    void updateStatusInfo(List<Account> statusInfo);

    /**
     * get account by account name
     *
     * @param accountName get account name
     * @return Account
     */
    Account getAccountByAccountName(String accountName);

    /**
     * get all account id
     *
     * @return List<String>
     */
    List<String> getAllAccountId();

    void updateToolsEnable(List<Tool> tools);

    List<Tool> getTools();

    String getAccountNameById(String id);

    @Select("select uuid,account_right from account where account_name = #{accountName}")
    List<Map<String,Object>> getRightByAccountName(String accountName);

    /**
     * get git name
     *
     * @return List<String>
     */
    List<String> getOldAccountGitname();

    /**
     * 一次插入多个账户
     * @param accounts 多个账户
     */
    void addAccounts(List<Account> accounts);
}
