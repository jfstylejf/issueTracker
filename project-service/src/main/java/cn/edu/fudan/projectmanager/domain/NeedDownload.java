package cn.edu.fudan.projectmanager.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author fancying
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NeedDownload {

    private String projectId;
    private String repoSource;
    private String url;
    private boolean isPrivate;
    private String username;
    private String password;
    private String branch;

}
