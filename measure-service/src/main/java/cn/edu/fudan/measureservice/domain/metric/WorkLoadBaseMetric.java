package cn.edu.fudan.measureservice.domain.metric;

import cn.edu.fudan.measureservice.domain.enums.TagMetricEnum;

/**
 * @ClassName: WorkLoadBaseMetric
 * @Description: 工作量基本基线数据
 * @Author wjzho
 * @Date 2021/6/9
 */

public class WorkLoadBaseMetric extends TagBaseMetric {

    private final static int WORK_LOAD_INITIAL_BEST_MAX = Integer.MAX_VALUE;

    private final static int WORK_LOAD_INITIAL_BEST_MIN = 15000;

    private final static int WORK_LOAD_INITIAL_BETTER_MAX = 14999;

    private final static int WORK_LOAD_INITIAL_BETTER_MIN = 12000;

    private final static int WORK_LOAD_INITIAL_NORMAL_MAX = 11999;

    private final static int WORK_LOAD_INITIAL_NORMAL_MIN = 8000;

    private final static int WORK_LOAD_INITIAL_WORSE_MAX = 7999;

    private final static int WORK_LOAD_INITIAL_WORSE_MIN = 5000;

    private final static int WORK_LOAD_INITIAL_WORST_MAX = 4999;

    private final static int WORK_LOAD_INITIAL_WORST_MIN = 1000;


    {
        tagMetricEnum = TagMetricEnum.WorkLoad;
        bestMax = WORK_LOAD_INITIAL_BEST_MAX;
        bestMin = WORK_LOAD_INITIAL_BEST_MIN;
        betterMax = WORK_LOAD_INITIAL_BETTER_MAX;
        betterMin = WORK_LOAD_INITIAL_BETTER_MIN;
        normalMax = WORK_LOAD_INITIAL_NORMAL_MAX;
        normalMin = WORK_LOAD_INITIAL_NORMAL_MIN;
        worseMax = WORK_LOAD_INITIAL_WORSE_MAX;
        worseMin = WORK_LOAD_INITIAL_WORSE_MIN;
        worstMax = WORK_LOAD_INITIAL_WORST_MAX;
        worstMin = WORK_LOAD_INITIAL_WORST_MIN;
    }

}
