package cn.edu.fudan.measureservice.domain.bo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 开发者人员列表入库数据
 * @author wjzho
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeveloperLevel {
    private String developerName;
    private double efficiency;
    private double quality;
    private double contribution;
    private double totalLevel;
    private int involvedRepoCount;
    private String dutyType;
}
