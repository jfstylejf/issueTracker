package cn.edu.fudan.measureservice.domain.bo;

import java.time.LocalDate;

/**
 * @ClassName: RepoTagMetric
 * @Description: 该库相应 tag 的衡量标准
 * @Author wjzho
 * @Date 2021/6/8
 */

public class RepoTagMetric {

    /**
     * 库 id
     */
    private String repoUuid;
    /**
     * 衡量指标
     */
    private String tag;
    /**
     * 最新更新时间
     */
    private LocalDate updateTime;
    /**
     * 最新更新账号
     */
    private String updater;
    /**
     * 该指标最好标准的上界
     */
    private int bestMax;
    /**
     * 该指标最好标准的下届
     */
    private int bestMin;
    /**
     * 该指标较好标准的上界
     */
    private int betterMax;
    /**
     * 该指标较好标准的下届
     */
    private int betterMin;
    /**
     * 该指标一般标准的上界
     */
    private int normalMax;
    /**
     * 该指标一般标准的下届
     */
    private int normalMin;
    /**
     * 该指标较差标准的上界
     */
    private int worseMax;
    /**
     * 该指标较差标准的下届
     */
    private int worseMin;
    /**
     * 该指标最差标准的上界
     */
    private int worstMax;
    /**
     * 该指标最差标准的下届
     */
    private int worstMin;
}
