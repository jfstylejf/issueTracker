package cn.edu.fudan.cloneservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private double bestMax;
    /**
     * 该指标最好标准的下届
     */
    private double bestMin;
    /**
     * 该指标较好标准的上界
     */
    private double betterMax;
    /**
     * 该指标较好标准的下届
     */
    private double betterMin;
    /**
     * 该指标一般标准的上界
     */
    private double normalMax;
    /**
     * 该指标一般标准的下届
     */
    private double normalMin;
    /**
     * 该指标较差标准的上界
     */
    private double worseMax;
    /**
     * 该指标较差标准的下届
     */
    private double worseMin;
    /**
     * 该指标最差标准的上界
     */
    private double worstMax;
    /**
     * 该指标最差标准的下届
     */
    private double worstMin;

    public String getLevel(double cloneRate){

        cloneRate = BigDecimal.valueOf(cloneRate).setScale(2, RoundingMode.UP).doubleValue();
        if(bestMax <= cloneRate && cloneRate <= bestMin){
            return "best";
        }else if(betterMax <= cloneRate && cloneRate <= betterMin){
            return "better";
        }else if(normalMax <= cloneRate && cloneRate <= normalMin){
            return "normal";
        }else if(worseMax <= cloneRate && cloneRate <= worseMin){
            return "worse";
        }else if(worstMax <= cloneRate && cloneRate <= worstMin){
            return "worst";
        }else{
            return "wrong";
        }
    }
}
