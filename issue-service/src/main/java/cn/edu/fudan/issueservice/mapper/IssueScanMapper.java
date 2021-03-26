package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.dbo.IssueScan;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * @author lsw
 *
 */
@Repository
public interface IssueScanMapper {

    /**
     * 插入issueScan
     * @param scan issueScan
     */
    void insertOneScan(IssueScan scan);

    /**
     * 删除issueScan
     * @param repoId repoUuid
     * @param tool tool
     */
    void deleteIssueScanByRepoIdAndTool(@Param("repo_id")String repoId,@Param("tool")String tool);

    /**
     * 获取issueScan
     * @param repoId repoUuid
     * @param statusList statusList
     * @param tool tool
     * @return issueScan
     */
    List<IssueScan> getIssueScanByRepoIdAndStatusAndTool(@Param("repo_id") String repoId, @Param("statusList")List<String> statusList, @Param("tool") String tool);

    /**
     * 获取issueScan
     * @param repoId repoUuid
     * @param commitId commitId
     * @param tool tool
     * @param since since
     * @param until until
     * @return issueScan
     */
    List<IssueScan> getIssueScanByRepoIdAndCommitIdAndTool(@Param("repo_id") String repoId, @Param("commit_id")String commitId, @Param("tool") String tool,
                                                           @Param("since")String since, @Param("until") String until);

    /**
     * 获取issueScan
     * @param repoId repoUuid
     * @param tool tool
     * @return issueScan
     */
    IssueScan getLatestIssueScanByRepoIdAndTool(@Param("repo_id") String repoId,  @Param("tool") String tool);

    /**
     * 获取扫描过的issueScan记录
     * @param repoUuid repoUuid
     * @param tool tool
     * @return 扫描过的issueScan记录
     */
    List<String> getScannedCommitList(String repoUuid, String tool);

    /**
     * 获取扫描起始commit
     * @param repoUuid  repoUuid
     * @return 扫描起始commit
     */
    @Select("select startCommit from scan where repo_id = #{repoUuid}")
    String getStartCommitByRepoUuid(String repoUuid);
}
