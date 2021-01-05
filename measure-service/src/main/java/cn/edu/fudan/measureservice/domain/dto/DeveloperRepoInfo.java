package cn.edu.fudan.measureservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wjzho
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeveloperRepoInfo {
    String developer;
    RepoInfo repoInfo;
    String firstCommitDate;
}
