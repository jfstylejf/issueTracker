package cn.edu.fudan.measureservice.domain.metric;

import cn.edu.fudan.measureservice.domain.enums.TagMetricEnum;

/**
 * @ClassName: CloneLineBaseMetric
 * @Description: 初始化注入克隆行基线数据
 * @Author wjzho
 * @Date 2021/6/10
 */

public class CloneLineBaseMetric extends TagBaseMetric{

    private final static double CLONE_LINE_INITIAL_BEST_MAX = 0;

    private final static double CLONE_LINE_INITIAL_BEST_MIN = 0.05;

    private final static double CLONE_LINE_INITIAL_BETTER_MAX = 0.06;

    private final static double CLONE_LINE_INITIAL_BETTER_MIN = 0.10;

    private final static double CLONE_LINE_INITIAL_NORMAL_MAX = 0.11;

    private final static double CLONE_LINE_INITIAL_NORMAL_MIN = 0.15;

    private final static double CLONE_LINE_INITIAL_WORSE_MAX = 0.16;

    private final static double CLONE_LINE_INITIAL_WORSE_MIN = 0.20;

    private final static double CLONE_LINE_INITIAL_WORST_MAX = 0.21;

    private final static double CLONE_LINE_INITIAL_WORST_MIN = 1;

    /**
     * 初始化克隆行基线数据
     */
    {
        tagMetricEnum = TagMetricEnum.CloneLine;
        bestMax = CLONE_LINE_INITIAL_BEST_MAX;
        bestMin = CLONE_LINE_INITIAL_BEST_MIN;
        betterMax = CLONE_LINE_INITIAL_BETTER_MAX;
        betterMin = CLONE_LINE_INITIAL_BETTER_MIN;
        normalMax = CLONE_LINE_INITIAL_NORMAL_MAX;
        normalMin = CLONE_LINE_INITIAL_NORMAL_MIN;
        worseMax = CLONE_LINE_INITIAL_WORSE_MAX;
        worseMin = CLONE_LINE_INITIAL_WORSE_MIN;
        worstMax = CLONE_LINE_INITIAL_WORST_MAX;
        worstMin = CLONE_LINE_INITIAL_WORST_MIN;
    }

}
