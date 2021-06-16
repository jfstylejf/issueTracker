package cn.edu.fudan.measureservice.domain.enums;

/**
 * @ClassName: LevelEnum
 * @Description: 人员画像评级
 * @Author wjzho
 * @Date 2021/5/12
 */

public enum LevelEnum {
    // 评级 : 好
    Best("best"),
    // 评级 : 较好
    Better("better"),
    // 评级 : 一般
    Normal("normal"),
    // 评级 : 较差
    Worse("worse"),
    // 评级 : 差
    Worst("worst"),
    // 评价 : 不做评价
    NoNeedToEvaluate("nothing");

    private final String type;

    LevelEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
