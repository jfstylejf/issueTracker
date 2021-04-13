package cn.edu.fudan.dependservice.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class RepoUuidsInfo implements Serializable {
    private String repoUuid;
    private String repoName;
    private String language;
    private String branch;

    public RepoUuidsInfo() {

    }

}