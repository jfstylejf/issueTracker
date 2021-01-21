package cn.edu.fudan.projectmanager.domain;

import lombok.Getter;

/**
 * description: 人员类型和权限
 *
 * @author fancying
 * create: 2020-11-11 16:49
 **/
@Getter
public enum AccountRoleEnum {

    /**
     * 0 超级管理员 对所有的项目均可看
     * 1 只能看项目的所有数据
     */
    ADMIN(0),
    LEADER(1),
    MANAGER(1),
    DEVELOPER(2);

    private int right;

    AccountRoleEnum(int right) {
        this.right = right;
    }

}
