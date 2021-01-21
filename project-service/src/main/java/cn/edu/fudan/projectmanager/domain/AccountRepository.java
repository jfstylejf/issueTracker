package cn.edu.fudan.projectmanager.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 记录人员在什么时候添加了库
 *
 * @author fancying
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRepository {

    private String uuid;
    private String repoName;
    private Date importTime;
    private String accountUuid;
    private String subRepositoryUuid;
    private String projectName;

}
