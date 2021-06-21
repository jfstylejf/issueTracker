package cn.edu.fudan.measureservice.domain.metric;

import cn.edu.fudan.measureservice.domain.enums.TagMetricEnum;

/**
 * @ClassName: CyclomaticComplexityBaseMetric
 * @Description: 初始化注入圈复杂度基线数据
 * @Author wjzho
 * @Date 2021/6/10
 */

public class CyclomaticComplexityBaseMetric extends TagBaseMetric{

    private final static int CYCLOMATIC_COMPLEXITY_INITIAL_BEST_MAX = Integer.MIN_VALUE;

    private final static int CYCLOMATIC_COMPLEXITY_INITIAL_BEST_MIN = 0;

    private final static int CYCLOMATIC_COMPLEXITY_INITIAL_BETTER_MAX = 1;

    private final static int CYCLOMATIC_COMPLEXITY_INITIAL_BETTER_MIN = 10;

    private final static int CYCLOMATIC_COMPLEXITY_INITIAL_NORMAL_MAX = 11;

    private final static int CYCLOMATIC_COMPLEXITY_INITIAL_NORMAL_MIN = 30;

    private final static int CYCLOMATIC_COMPLEXITY_INITIAL_WORSE_MAX = 31;

    private final static int CYCLOMATIC_COMPLEXITY_INITIAL_WORSE_MIN = 50;

    private final static int CYCLOMATIC_COMPLEXITY_INITIAL_WORST_MAX = 51;

    private final static int CYCLOMATIC_COMPLEXITY_INITIAL_WORST_MIN = Integer.MAX_VALUE;

    /**
     * 初始化圈复杂度基线数据
     */
    {
        tagMetricEnum = TagMetricEnum.CyclomaticComplexity;
        bestMax = CYCLOMATIC_COMPLEXITY_INITIAL_BEST_MAX;
        bestMin = CYCLOMATIC_COMPLEXITY_INITIAL_BEST_MIN;
        betterMax = CYCLOMATIC_COMPLEXITY_INITIAL_BETTER_MAX;
        betterMin = CYCLOMATIC_COMPLEXITY_INITIAL_BETTER_MIN;
        normalMax = CYCLOMATIC_COMPLEXITY_INITIAL_NORMAL_MAX;
        normalMin = CYCLOMATIC_COMPLEXITY_INITIAL_NORMAL_MIN;
        worseMax = CYCLOMATIC_COMPLEXITY_INITIAL_WORSE_MAX;
        worseMin = CYCLOMATIC_COMPLEXITY_INITIAL_WORSE_MIN;
        worstMax = CYCLOMATIC_COMPLEXITY_INITIAL_WORST_MAX;
        worstMin = CYCLOMATIC_COMPLEXITY_INITIAL_WORST_MIN;
    }


}
