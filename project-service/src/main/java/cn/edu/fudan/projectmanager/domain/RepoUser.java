package cn.edu.fudan.projectmanager.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author fancying
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepoUser {

    private String uuid;
    private String name;
    private Date importTime;
    private String accountUuid;
    private String subRepositoryUuid;
    private String projectName;

}
