package cn.edu.fudan.cloneservice.util;

/**
 * @ClassName: GranularityEnum
 * @Description:
 * @Author wjzho
 * @Date 2021/4/2
 */

public enum GranularityEnum {
    /**
     * 按天聚合
     */
    Day("day"),
    /**
     * 按周聚合
     */
    Week("week"),
    /**
     * 按月聚合
     */
    Month("month"),
    /**
     * 按年聚合
     */
    Year("year");

    private final String type;

    GranularityEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
