package cn.edu.fudan.measureservice.domain.metric;

import cn.edu.fudan.measureservice.domain.enums.TagMetricEnum;

/**
 * @ClassName: BigMethodNumBaseMetric
 * @Description: 初始化注入超大文件数基线数据
 * @Author wjzho
 * @Date 2021/6/10
 */

public class BigMethodNumBaseMetric extends TagBaseMetric{

    private final static int BIG_METHOD_NUM_INITIAL_BEST_MAX = 0;

    private final static int BIG_METHOD_NUM_INITIAL_BEST_MIN = 0;

    private final static int BIG_METHOD_NUM_INITIAL_BETTER_MAX = 1;

    private final static int BIG_METHOD_NUM_INITIAL_BETTER_MIN = 2;

    private final static int BIG_METHOD_NUM_INITIAL_NORMAL_MAX = 3;

    private final static int BIG_METHOD_NUM_INITIAL_NORMAL_MIN = 5;

    private final static int BIG_METHOD_NUM_INITIAL_WORSE_MAX = 6;

    private final static int BIG_METHOD_NUM_INITIAL_WORSE_MIN = 9;

    private final static int BIG_METHOD_NUM_INITIAL_WORST_MAX = 10;

    private final static int BIG_METHOD_NUM_INITIAL_WORST_MIN = Integer.MAX_VALUE;

    /**
     * 初始化超大文件数基线数据
     */
    {
        tagMetricEnum = TagMetricEnum.BigMethodNum;
        bestMax = BIG_METHOD_NUM_INITIAL_BEST_MAX;
        bestMin = BIG_METHOD_NUM_INITIAL_BEST_MIN;
        betterMax = BIG_METHOD_NUM_INITIAL_BETTER_MAX;
        betterMin = BIG_METHOD_NUM_INITIAL_BETTER_MIN;
        normalMax = BIG_METHOD_NUM_INITIAL_NORMAL_MAX;
        normalMin = BIG_METHOD_NUM_INITIAL_NORMAL_MIN;
        worseMax = BIG_METHOD_NUM_INITIAL_WORSE_MAX;
        worseMin = BIG_METHOD_NUM_INITIAL_WORSE_MIN;
        worstMax = BIG_METHOD_NUM_INITIAL_WORST_MAX;
        worstMin = BIG_METHOD_NUM_INITIAL_WORST_MIN;
    }

}
