package cn.edu.fudan.measureservice.domain.metric;

import cn.edu.fudan.measureservice.domain.enums.TagMetricEnum;

/**
 * @ClassName: LivingStaticIssueMetric
 * @Description: 静态缺陷基础基线数据
 * @Author wjzho
 * @Date 2021/6/9
 */

public class LivingStaticIssueBaseMetric extends TagBaseMetric {

    private final static int LIVING_STATIC_ISSUE_INITIAL_BEST_MAX = 0;

    private final static int LIVING_STATIC_ISSUE_INITIAL_BEST_MIN = 0;

    private final static int LIVING_STATIC_ISSUE_INITIAL_BETTER_MAX = 1;

    private final static int LIVING_STATIC_ISSUE_INITIAL_BETTER_MIN = 3;

    private final static int LIVING_STATIC_ISSUE_INITIAL_NORMAL_MAX = 4;

    private final static int LIVING_STATIC_ISSUE_INITIAL_NORMAL_MIN = 5;

    private final static int LIVING_STATIC_ISSUE_INITIAL_WORSE_MAX = 6;

    private final static int LIVING_STATIC_ISSUE_INITIAL_WORSE_MIN = 10;

    private final static int LIVING_STATIC_ISSUE_INITIAL_WORST_MAX = 11;

    private final static int LIVING_STATIC_ISSUE_INITIAL_WORST_MIN = Integer.MIN_VALUE;

    /**
     *  初始化注入静态缺陷基础基线数据
     */
    {
        tagMetricEnum = TagMetricEnum.LivingStaticIssue;
        bestMax = LIVING_STATIC_ISSUE_INITIAL_BEST_MAX;
        bestMin = LIVING_STATIC_ISSUE_INITIAL_BEST_MIN;
        betterMax = LIVING_STATIC_ISSUE_INITIAL_BETTER_MAX;
        betterMin = LIVING_STATIC_ISSUE_INITIAL_BETTER_MIN;
        normalMax = LIVING_STATIC_ISSUE_INITIAL_NORMAL_MAX;
        normalMin = LIVING_STATIC_ISSUE_INITIAL_NORMAL_MIN;
        worseMax = LIVING_STATIC_ISSUE_INITIAL_WORSE_MAX;
        worseMin = LIVING_STATIC_ISSUE_INITIAL_WORSE_MIN;
        worstMax = LIVING_STATIC_ISSUE_INITIAL_WORST_MAX;
        worstMin = LIVING_STATIC_ISSUE_INITIAL_WORST_MIN;
    }


}
