package cn.edu.fudan.measureservice.domain.vo;

import cn.edu.fudan.measureservice.domain.bo.DeveloperProjectCcn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName: DeveloperDataCcn
 * @Description: 人员总览开发者圈复杂度
 * @Author wjzho
 * @Date 2021/5/12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeveloperDataCcn {
    /**
     * 开发者姓名
     */
   private String developerName;
    /**
     * 起始时间
     */
   private String since;
    /**
     * 截至时间
     */
   private String until;
    /**
     * 开发者各项目圈复杂度修改明细
     * note : 粒度到项目，若之后需要看库，则再新加
     */
   private List<DeveloperProjectCcn> developerProjectCcnList;
    /**
     * 开发者指定时间段内的总修改圈复杂度
     */
   private int totalDiffCcn;
    /**
     * 开发者圈复杂度评级
     */
   private String level;
}
