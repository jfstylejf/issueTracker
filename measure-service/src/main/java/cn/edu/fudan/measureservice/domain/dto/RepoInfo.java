package cn.edu.fudan.measureservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wjzho
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepoInfo {
    String projectName;
    String repoName;
    String repoUuid;
    int involvedDeveloperNumber;
}
