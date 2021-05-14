package cn.edu.fudan.measureservice.domain.enums;

/**
 * @ClassName: LevelEnum
 * @Description: 人员画像评级
 * @Author wjzho
 * @Date 2021/5/12
 */

public enum LevelEnum {
    // 评级 : 高
    High("high"),
    // 评级 : 中
    Medium("medium"),
    // 评级 : 低
    Low("low");

    private final String type;

    LevelEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
