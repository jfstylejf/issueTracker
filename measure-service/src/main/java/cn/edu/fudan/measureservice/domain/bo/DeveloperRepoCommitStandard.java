package cn.edu.fudan.measureservice.domain.bo;

import cn.edu.fudan.measureservice.domain.enums.LevelEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: DeveloperRepoCommitStandard
 * @Description: 开发者单个库的提交规范性相关数据
 * @Author wjzho
 * @Date 2021/6/11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeveloperRepoCommitStandard  {

    /**
     * 开发者
     */
    private String developerName;
    /**
     * 代码库 id
     */
    private String repoUuid;
    /**
     * 库名称
     */
    private String repoName;
    /**
     * 开发者提交次数（不含Merge）
     */
    private int developerValidCommitCount;
    /**
     * 开发者包含Jira单号的提交次数
     */
    private int developerJiraCommitCount;
    /**
     * 开发者不规范的提交次数
     */
    private int developerInvalidCommitCount;
    /**
     * 开发者提交规范性
     */
    private double commitStandard;
    /**
     * 开发者该库等级
     */
    private int level;


}
