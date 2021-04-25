package cn.edu.fudan.measureservice.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author wjzho
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeveloperCommitStandard implements Serializable {
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
    private double commitStandard;
    /**
     * 开发者
     */
    private List<Map<String,String>> developerJiraCommitInfo;
    private List<Map<String,String>> developerInvalidCommitInfo;
}
