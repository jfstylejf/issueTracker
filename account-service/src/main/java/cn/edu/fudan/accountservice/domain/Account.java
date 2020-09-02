package cn.edu.fudan.accountservice.domain;

import java.io.Serializable;

public class Account implements Serializable {

    private String uuid;
    private String accountName;
    private String password;
    private String name;
    private String email;
    private String status;
    /**
     * 用户权限管理，0表示管理员，1表示团队负责人，默认为1
     */
    private int right;
    /**
     * 当前用户所属组，可为空,可属于多个组
     */
    private String groups;

    public String getGroups() {
        return groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getRight() {
        return right;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
