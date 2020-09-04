package cn.edu.fudan.accountservice.mapper;


import cn.edu.fudan.accountservice.domain.Account;
import cn.edu.fudan.accountservice.domain.Tool;
import org.apache.ibatis.annotations.Param;
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
     * add account
     *
     * @param account Account type
     */
    void addAccount(Account account);

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

}
