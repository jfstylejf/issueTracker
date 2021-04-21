package cn.edu.fudan.dependservice.domain;

import lombok.Data;

@Data
public class RelationShip {
    private String projectName;
    private String commit_id;
    private String repo_uuid;
    private int group_id;
    private String file;
    private String depend_on;
    private String depend_details;

}
