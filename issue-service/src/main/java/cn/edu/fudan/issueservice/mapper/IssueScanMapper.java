package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.dbo.IssueScan;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * @author lsw
 *
 */
@Repository
public interface IssueScanMapper {

    void insertOneScan(IssueScan scan);

    void deleteIssueScanByRepoIdAndTool(@Param("repo_id")String repoId,@Param("tool")String tool);

    List<IssueScan> getIssueScanByRepoIdAndStatusAndTool(@Param("repo_id") String repoId, @Param("statusList")List<String> statusList, @Param("tool") String tool);

    List<IssueScan> getIssueScanByRepoIdAndCommitIdAndTool(@Param("repo_id") String repoId, @Param("commit_id")String commitId, @Param("tool") String tool,
                                                           @Param("since")String since, @Param("until") String until);

    IssueScan getLatestIssueScanByRepoIdAndTool(@Param("repo_id") String repoId,  @Param("tool") String tool);
}
