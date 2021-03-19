package cn.edu.fudan.dependservice.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class DependencyDetailInfo implements Serializable {
    private String projectName;
    private String repoName;
    private String repoUuid;
    private String branch;
    private String fileName;
    private String metaFileUuid;
    private String methodName;
    private String metaMethodUuid;
    private String ccn;
    private String changeTimes;

    public DependencyDetailInfo() {

    }

}
