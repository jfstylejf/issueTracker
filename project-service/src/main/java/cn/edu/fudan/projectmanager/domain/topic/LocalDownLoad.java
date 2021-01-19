package cn.edu.fudan.projectmanager.domain.topic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description:
 *
 * @author Richy
 * create: 2021-01-05 11:10
 **/

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalDownLoad {

    private int flag;
    private String projectId;
    private String url;
    private String username;
    private String branch;
    private String repoSource;
    private String repoName;
}