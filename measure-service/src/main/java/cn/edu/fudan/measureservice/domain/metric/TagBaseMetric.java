package cn.edu.fudan.measureservice.domain.metric;

import cn.edu.fudan.measureservice.domain.enums.TagMetricEnum;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: TagBaseMetric
 * @Description: 各维度基本基线数据
 * @Author wjzho
 * @Date 2021/6/9
 */
@Data
public class TagBaseMetric {


    protected TagMetricEnum tagMetricEnum;

    protected double bestMax;

    protected double bestMin;

    protected double betterMax;

    protected double betterMin;

    protected double normalMax;

    protected double normalMin;

    protected double worseMax;

    protected double worseMin;

    protected double worstMax;

    protected double worstMin;


    /**
     * 初始化并获取相应维度的基础数据
     * @return
     */
    public Map<String,TagBaseMetric> getTagBaseMetricMap() {
        Map<String,TagBaseMetric> tagBaseMetricMap = new HashMap<>();
        // 获取 工作量初始基线数据
        WorkLoadBaseMetric workLoadBaseMetric = new WorkLoadBaseMetric();
        tagBaseMetricMap.put(TagMetricEnum.WorkLoad.name(),workLoadBaseMetric);
        //获取 静态缺陷初始基础数据
        LivingStaticIssueBaseMetric livingStaticIssueBaseMetric = new LivingStaticIssueBaseMetric();
        tagBaseMetricMap.put(TagMetricEnum.LivingStaticIssue.name(),livingStaticIssueBaseMetric);
        // 获取 代码稳定性初始基础数据
        CodeStabilityBaseMetric codeStabilityBaseMetric = new CodeStabilityBaseMetric();
        tagBaseMetricMap.put(TagMetricEnum.CodeStability.name(),codeStabilityBaseMetric);
        // 获取 提交规范性初始基础数据
        CommitStandardBaseMetric commitStandardBaseMetric = new CommitStandardBaseMetric();
        tagBaseMetricMap.put(TagMetricEnum.CommitStandard.name(),commitStandardBaseMetric);
        // 获取 圈复杂度初始基础数据
        CyclomaticComplexityBaseMetric cyclomaticComplexityBaseMetric = new CyclomaticComplexityBaseMetric();
        tagBaseMetricMap.put(TagMetricEnum.CyclomaticComplexity.name(),cyclomaticComplexityBaseMetric);
        // 获取 克隆行数初始基础数据
        CloneLineBaseMetric cloneLineBaseMetric = new CloneLineBaseMetric();
        tagBaseMetricMap.put(TagMetricEnum.CloneLine.name(),cloneLineBaseMetric);
        // 获取 超大方法数初始基础数据
        BigMethodNumBaseMetric bigMethodNumBaseMetric = new BigMethodNumBaseMetric();
        tagBaseMetricMap.put(TagMetricEnum.BigMethodNum.name(),bigMethodNumBaseMetric);
        // 获取 设计贡献初始基础数据
        DesignContributionBaseMetric designContributionBaseMetric = new DesignContributionBaseMetric();
        tagBaseMetricMap.put(TagMetricEnum.DesignContribution.name(),designContributionBaseMetric);

        return tagBaseMetricMap;
    }

}
