package cn.edu.fudan.dependservice.domain;

import lombok.Data;

@Data
public class Group {
    private String commit_id;
    private String repo_uuid;
    private int group_id;
    private int cycle_num;
    private String file;
    private String depend_on;
    private String depend_details;


}
