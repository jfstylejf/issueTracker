package cn.edu.fudan.measureservice.domain.enums;

/**
 * @ClassName: DutyStatusEnum
 * @Description: 在职状态
 * @Author wjzho
 * @Date 2021/6/3
 */

public enum DutyStatusEnum {
    // 在职
    InPosition("在职"),
    //离职
    Resign("离职");

    private final String status;

    DutyStatusEnum(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
