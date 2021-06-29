package cn.edu.fudan.measureservice.domain.vo;

import cn.edu.fudan.measureservice.domain.bo.DeveloperCommitStandard;
import cn.edu.fudan.measureservice.domain.bo.DeveloperProjectCcn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName: DeveloperDataCommitStandard
 * @Description: 人员总览界面 开发者提交规范性
 * @Author wjzho
 * @Date 2021/5/20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeveloperDataCommitStandard {
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
     * 开发者提交规范性明细
     */
    private DeveloperCommitStandard detail;
    /**
     * 开发者合法提交次数
     */
    private int developerValidCommitCount;
    /**
     * 开发者包含 Jira 单号的提交次数
     */
    private int developerJiraCommitCount;
    /**
     * 开发者提交规范性
     */
    private double commitStandard;
    /**
     * 开发者提交规范性评级
     */
    private String level;

}
