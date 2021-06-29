package cn.edu.fudan.measureservice.domain.metric;

import cn.edu.fudan.measureservice.domain.enums.TagMetricEnum;

/**
 * @ClassName: CodeStabilityBaseMetric
 * @Description: 代码稳定性基础基线数据
 * @Author wjzho
 * @Date 2021/6/10
 */

public class CodeStabilityBaseMetric extends TagBaseMetric {

    private final static double CODE_STABILITY_INITIAL_BEST_MAX = 1.00;

    private final static double CODE_STABILITY_INITIAL_BEST_MIN = 0.80;

    private final static double CODE_STABILITY_INITIAL_BETTER_MAX = 0.79;

    private final static double CODE_STABILITY_INITIAL_BETTER_MIN = 0.60;

    private final static double CODE_STABILITY_INITIAL_NORMAL_MAX = 0.59;

    private final static double CODE_STABILITY_INITIAL_NORMAL_MIN = 0.40;

    private final static double CODE_STABILITY_INITIAL_WORSE_MAX = 0.39;

    private final static double CODE_STABILITY_INITIAL_WORSE_MIN = 0.20;

    private final static double CODE_STABILITY_INITIAL_WORST_MAX = 0.19;

    private final static double CODE_STABILITY_INITIAL_WORST_MIN = 0.00;

    /**
     * 初始化注入代码稳定性基础数据
     */
    {
        tagMetricEnum = TagMetricEnum.CodeStability;
        bestMax = CODE_STABILITY_INITIAL_BEST_MAX;
        bestMin = CODE_STABILITY_INITIAL_BEST_MIN;
        betterMax = CODE_STABILITY_INITIAL_BETTER_MAX;
        betterMin = CODE_STABILITY_INITIAL_BETTER_MIN;
        normalMax = CODE_STABILITY_INITIAL_NORMAL_MAX;
        normalMin = CODE_STABILITY_INITIAL_NORMAL_MIN;
        worseMax = CODE_STABILITY_INITIAL_WORSE_MAX;
        worseMin = CODE_STABILITY_INITIAL_WORSE_MIN;
        worstMax = CODE_STABILITY_INITIAL_WORST_MAX;
        worstMin = CODE_STABILITY_INITIAL_WORST_MIN;
    }


}
