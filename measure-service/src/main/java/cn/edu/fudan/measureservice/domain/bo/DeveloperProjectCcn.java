package cn.edu.fudan.measureservice.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName: DeveloperProjectCcn
 * @Description: 开发者单个项目的圈复杂度
 * @Author wjzho
 * @Date 2021/5/12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeveloperProjectCcn {
    /**
     * 开发者姓名
     */
    private String developerName;
    /**
     * 起始时间
     */
    private String since;
    /**
     * 截止时间
     */
    private String until;
    /**
     * 项目名
     */
    private String projectName;
    /**
     * 参与项目库
     */
    private List<DeveloperRepoCcn> developerRepoCcnList;
    /**
     * 开发者在该项目的总修改圈复杂度
     */
    private int projectDiffCcn;
}
