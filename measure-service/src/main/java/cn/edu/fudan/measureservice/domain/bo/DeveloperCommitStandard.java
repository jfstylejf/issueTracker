package cn.edu.fudan.measureservice.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author wjzho
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeveloperCommitStandard {
    private String developerName;
    private int developerValidCommitCount;
    private int developerJiraCommitCount;
    private double commitStandard;
    private List<Map<String,String>> developerJiraCommitInfo;
    private List<Map<String,String>> developerInvalidCommitInfo;
}
