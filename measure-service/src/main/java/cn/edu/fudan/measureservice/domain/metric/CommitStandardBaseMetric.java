package cn.edu.fudan.measureservice.domain.metric;

import cn.edu.fudan.measureservice.domain.enums.TagMetricEnum;

/**
 * @ClassName: CommitStandardBaseMetric
 * @Description: 提交规范性初始基线数据
 * @Author wjzho
 * @Date 2021/6/10
 */

public class CommitStandardBaseMetric extends TagBaseMetric{

    private final static double COMMIT_STANDARD_INITIAL_BEST_MAX = 1.00;

    private final static double COMMIT_STANDARD_INITIAL_BEST_MIN = 0.80;

    private final static double COMMIT_STANDARD_INITIAL_BETTER_MAX = 0.79;

    private final static double COMMIT_STANDARD_INITIAL_BETTER_MIN = 0.60;

    private final static double COMMIT_STANDARD_INITIAL_NORMAL_MAX = 0.59;

    private final static double COMMIT_STANDARD_INITIAL_NORMAL_MIN = 0.40;

    private final static double COMMIT_STANDARD_INITIAL_WORSE_MAX = 0.39;

    private final static double COMMIT_STANDARD_INITIAL_WORSE_MIN = 0.20;

    private final static double COMMIT_STANDARD_INITIAL_WORST_MAX = 0.19;

    private final static double COMMIT_STANDARD_INITIAL_WORST_MIN = 0.00;

    /**
     * 初始化提交规范性基线数据
     */
    {
        tagMetricEnum = TagMetricEnum.CommitStandard;
        bestMax = COMMIT_STANDARD_INITIAL_BEST_MAX;
        bestMin = COMMIT_STANDARD_INITIAL_BEST_MIN;
        betterMax = COMMIT_STANDARD_INITIAL_BETTER_MAX;
        betterMin = COMMIT_STANDARD_INITIAL_BETTER_MIN;
        normalMax = COMMIT_STANDARD_INITIAL_NORMAL_MAX;
        normalMin = COMMIT_STANDARD_INITIAL_NORMAL_MIN;
        worseMax = COMMIT_STANDARD_INITIAL_WORSE_MAX;
        worseMin = COMMIT_STANDARD_INITIAL_WORSE_MIN;
        worstMax = COMMIT_STANDARD_INITIAL_WORST_MAX;
        worstMin = COMMIT_STANDARD_INITIAL_WORST_MIN;
    }


}
