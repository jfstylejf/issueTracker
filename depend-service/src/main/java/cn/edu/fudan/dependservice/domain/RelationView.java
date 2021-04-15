package cn.edu.fudan.dependservice.domain;

import lombok.Data;

@Data
public class RelationView {
    private String projectName;
    private String repoUuid;
    private String groupId;
    private String sourceFile;
    private String targetFile;
    private String relationType;
    private String commit_id;
    private int id;
}
