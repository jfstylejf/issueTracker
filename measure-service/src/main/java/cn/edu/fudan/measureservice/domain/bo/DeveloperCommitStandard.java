package cn.edu.fudan.measureservice.domain.bo;

import cn.edu.fudan.measureservice.domain.enums.LevelEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author wjzho
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeveloperCommitStandard implements Serializable {
    /**
     * 开发者
     */
    private String developerName;
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
     * 开发者综合等级
     */
    private LevelEnum level;

}
