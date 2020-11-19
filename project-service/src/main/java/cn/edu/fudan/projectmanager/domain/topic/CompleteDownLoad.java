package cn.edu.fudan.projectmanager.domain.topic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author fancying
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteDownLoad {

    private String repo_id;
    private String projectId;
    private String language;
    private String status;
    private String description;

    private String downloadStatus;

    public String getSubRepositoryUuid() {
        if (subRepositoryUuid == null) {
            subRepositoryUuid = projectId;
            return projectId;
        }
        return subRepositoryUuid;
    }

    private String subRepositoryUuid;
    private Date latestCommitTime;
    private Date till_commiit_time;



    // todo RepoManager 的字段做一下修改
}
