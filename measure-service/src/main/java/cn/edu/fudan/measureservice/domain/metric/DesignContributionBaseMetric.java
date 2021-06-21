package cn.edu.fudan.measureservice.domain.metric;

import cn.edu.fudan.measureservice.domain.enums.TagMetricEnum;

/**
 * @ClassName: DesignContributionBaseMetric
 * @Description: 设计贡献基础基线数据
 * @Author wjzho
 * @Date 2021/6/15
 */

public class DesignContributionBaseMetric extends TagBaseMetric{

    private final static int DESIGN_CONTRIBUTION_INITIAL_BEST_MAX = Integer.MAX_VALUE;

    private final static int DESIGN_CONTRIBUTION_INITIAL_BEST_MIN = 200;

    private final static int DESIGN_CONTRIBUTION_INITIAL_BETTER_MAX = 199;

    private final static int DESIGN_CONTRIBUTION_INITIAL_BETTER_MIN = 150;

    private final static int DESIGN_CONTRIBUTION_INITIAL_NORMAL_MAX = 149;

    private final static int DESIGN_CONTRIBUTION_INITIAL_NORMAL_MIN = 100;

    private final static int DESIGN_CONTRIBUTION_INITIAL_WORSE_MAX = 99;

    private final static int DESIGN_CONTRIBUTION_INITIAL_WORSE_MIN = 50;

    private final static int DESIGN_CONTRIBUTION_INITIAL_WORST_MAX = 49;

    private final static int DESIGN_CONTRIBUTION_INITIAL_WORST_MIN = 0;


    {
        tagMetricEnum = TagMetricEnum.DesignContribution;
        bestMax = DESIGN_CONTRIBUTION_INITIAL_BEST_MAX;
        bestMin = DESIGN_CONTRIBUTION_INITIAL_BEST_MIN;
        betterMax = DESIGN_CONTRIBUTION_INITIAL_BETTER_MAX;
        betterMin = DESIGN_CONTRIBUTION_INITIAL_BETTER_MIN;
        normalMax = DESIGN_CONTRIBUTION_INITIAL_NORMAL_MAX;
        normalMin = DESIGN_CONTRIBUTION_INITIAL_NORMAL_MIN;
        worseMax = DESIGN_CONTRIBUTION_INITIAL_WORSE_MAX;
        worseMin = DESIGN_CONTRIBUTION_INITIAL_WORSE_MIN;
        worstMax = DESIGN_CONTRIBUTION_INITIAL_WORST_MAX;
        worstMin = DESIGN_CONTRIBUTION_INITIAL_WORST_MIN;
    }

}
