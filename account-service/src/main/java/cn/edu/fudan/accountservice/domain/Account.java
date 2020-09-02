package cn.edu.fudan.accountservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account implements Serializable {

    private String uuid;
    /**
     * 登录时用户名（gitlab的用户名）
     */
    private String accountName;
    private String password;
    /**
     * 登陆后用户真实姓名
     */
    private String name;
    private String email;
    /**
     * 用户权限管理，0表示管理员，1表示团队负责人，默认为1
     */
    private int right;
    /** 用户所属部门 */
    private String dep;
    /** 用户使用的git用户名 */
    private String gitname;
    /** 用户在职状态 */
    private String status;
    /** 用户所属的项目角色 */
    private String role;
}
