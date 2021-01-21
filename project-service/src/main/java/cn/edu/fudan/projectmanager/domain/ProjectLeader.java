package cn.edu.fudan.projectmanager.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description: account_project
 *
 * @author Richy
 * create: 2021-01-20 17:39
 **/

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectLeader {

    private String accountUuid;
    private String accountName;
    private String projectName;
    private Integer projectId;
    private String accountRole;
}