package cn.edu.fudan.issueservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * description:
 *
 * @author fancying
 * create: 2020-10-20 14:39
 **/
@Getter
@ToString
public class RepoInfo {

    public RepoInfo(@NotNull String projectName, String repoName, String branch, String repoUuid) {
        this.projectName = projectName;
        this.repoName = repoName;
        this.branch = branch;
        this.repoUuid = repoUuid;
    }

    private String projectName;
    private String repoName;
    private String branch;
    private String repoUuid;



}