package cn.edu.fudan.accountservice.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description: 记录真实姓名与git名字的对应关系
 *
 * @author fancying
 * create: 2020-11-12 11:20
 **/
@Data
@NoArgsConstructor
public class AccountAuthor {

    int id;
    String accountUuid;
    private String name;
    private String accountGitName;

    public static AccountAuthor newInstanceOf(Account account) {
        AccountAuthor accountAuthor = new AccountAuthor();
        accountAuthor.setAccountUuid(account.getUuid());
        accountAuthor.setAccountGitName(account.getGitname());
        accountAuthor.setName(account.getName());
        return accountAuthor;
    }
}