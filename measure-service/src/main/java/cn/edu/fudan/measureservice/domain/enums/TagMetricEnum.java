package cn.edu.fudan.measureservice.domain.enums;

/**
 * @ClassName: TagMetricEnum
 * @Description: 雷达图相应的衡量指标
 * @Author wjzho
 * @Date 2021/6/8
 */

public enum TagMetricEnum {

    // 工作量指标
    WorkLoad("工作量"),
    // 留存缺陷数
    LivingStaticIssue("留存静态缺陷"),
    // 代码稳定性
    CodeStability("代码稳定性"),
    // 圈复杂度
    CyclomaticComplexity("圈复杂度"),
    // 提交规范性
    CommitStandard("提交规范性"),
    // 克隆行
    CloneLine("克隆行"),
    // 超大方法个数
    BigMethodNum("超大方法个数"),
    // 设计贡献
    DesignContribution("设计贡献");


    private String tag;

    TagMetricEnum(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

}
