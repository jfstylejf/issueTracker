package cn.edu.fudan.dependservice.domain;

import lombok.Data;

@Data
public class Commit {
    private String repoUuid;
    private String commitId;
    private String commitTime;
}
