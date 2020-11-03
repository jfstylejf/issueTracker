package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@Repository
public interface RawIssueMapper {


    void insertRawIssueList(List<RawIssue> list);

    void deleteRawIssueByRepoIdAndTool(@Param("repo_id") String repo_id,@Param("tool")String tool);

    void deleteRawIssueByIds(@Param("list")List<String> list);

    void batchUpdateIssueId(List<RawIssue> list);

    Integer getIssueCountBeforeSpecificTime(@Param("account_id") String account_id, @Param("specificTime") String specificTime);

    List<RawIssue> getRawIssueByCommitIDAndTool(@Param("repo_id") String repo_id,
                                                    @Param("tool") String tool,
                                                    @Param("commit_id") String commit_id);

    List<RawIssue> getRawIssueByCommitIDAndFile(@Param("repo_id") String repo_id,
                                                @Param("commit_id")String commit_id,
                                                @Param("tool") String tool,
                                                @Param("file_name")String file);

    List<RawIssue> getRawIssueByIssueId(@Param("issueId") String issueId);

    List<String> getTypesByCommit(@Param("tool")String tool,@Param("commit_id")String commit_id);

    Integer getNumberOfRemainingIssueBaseFile(@Param("repo_id") String repoId,@Param("commit_id") String commit,@Param("file_name") String fileName);

    Integer getNumberOfRemainingIssue(@Param("repo_id") String repoId, @Param("commit_id") String commit);

    List<WeakHashMap<String, String>> getRankOfFileBaseIssueQuantity(@Param("repo_id")String repoId, @Param("commit_id")String commitId);

    List<WeakHashMap<String, String>> getRankOfFileBaseDensity(@Param("repo_id")String repoId, @Param("commit_id")String commitId);

    Integer getNumberOfRemainingIssueBasePackage(@Param("repo_id")String repoId, @Param("commit_id")String commitId,@Param("package_name") String packageName);

    Integer getNumberOfRawIssuesByIssueId(@Param("issueId") String issueId);

    List<RawIssue> getRawIssueListByIssueId(Map<String, Object> map);

    List<RawIssue> getChangedRawIssues(@Param("issueId") String issueId);

    String getRawIssueCommitTimeByRepoIdAndTool(@Param("repo_id") String repoId,
                                                @Param("commit_id")String commitId,
                                                @Param("tool") String tool
                                                );


    String getCommitIdWhichBeforeDesignatedTime(@Param("repo_id") String repoId,
                                                    @Param("commit_time")String commitTime,
                                                    @Param("tool") String tool
    );
    List<String> getRawIssueIdByCommitId(@Param("repo_id") String repoId,@Param("commit_id") String commit, @Param("tool")String tool);

    String getLatestScannedCommitId(@Param("repo_id") String repo_id,@Param("tool")String tool);

    Integer getNumberOfRawIssuesByIssueIdAndStatus(@Param("issueId") String issueId,@Param("list") List status);


    List<RawIssue> getRawIssueByRepoIdAndTool(@Param("repo_id") String repoId,
                                                @Param("tool") String tool);

    List<Map<String, Object>> getRawIssuesByCondition(@Param("developer") String developer,@Param("repo_id") String repoId,@Param("since") String since,@Param("until") String until,@Param("tool") String tool,@Param("status") String status,@Param("type") String type);

    List<Map<String, Object>> getIssueIdAndGroupCountFromRawIssue(@Param("developer") String developer,@Param("repo_id") String repoId,@Param("since") String since,@Param("until") String until,@Param("tool") String tool,@Param("status") String status);

    String getAdderOfOneIssue(@Param("issue_id") String issueId);

    String getLastSolverOfOneIssue(@Param("issue_id") String issueId);

    Map<String, Object> getLastSolvedInfoOfOneIssue(@Param("issue_id") String issueId);

    List<String> getIssueIntroducers(@Param("repoUuids")List<String> repoUuids);

    /**
     * 根据commit和issueUuid筛选对应的rawIssue
     * @param issueUuid
     * @param commit
     * @return
     */
    String getRawIssueUuidByIssueUuidAndCommit(String issueUuid, String commit);
}
