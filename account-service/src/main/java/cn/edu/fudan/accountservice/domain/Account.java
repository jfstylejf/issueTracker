package cn.edu.fudan.accountservice.domain;

import cn.edu.fudan.accountservice.util.MD5Util;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * 账户信息
 * @author fancying
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account implements Serializable {

    private transient static final String PASSWORD_POSTFIX = "1234!";

    private String uuid;
    /**
     * 登录时用户名（用户的真实姓名）
     */
    private String accountName;
    private String password;
    private String email;
    /**
     * 用户权限管理，0表示管理员，1表示团队负责人，默认为1
     */
    private int right;
    /** 用户所属部门 */
    private String dep;

    /**
     * todo 后续用int 保存 1 表示在职 0 表示离职
     * 用户在职状态
     */
    private String status = "1";
    /** 用户所属的项目角色 */
    private String role = AccountRoleEnum.DEVELOPER.name();

    public static Account newInstance(String gitName) {
        Account account = new Account();
        account.setAccountName(gitName);
        account.setStatus("1");
        account.setUuid(UUID.randomUUID().toString());
        account.setPassword(MD5Util.md5(gitName  + PASSWORD_POSTFIX));
        return account;
    }
}
