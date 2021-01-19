package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author Beethoven
 */
@Repository
public interface RawIssueMapper {

    /**
     * 插入rawIssues
     * @param list rawIssue list
     */
    void insertRawIssueList(List<RawIssue> list);

    /**
     * 删除rawIssues
     * @param list rawIssue list
     */
    void deleteRawIssueByIds(@Param("list")List<String> list);

    /**
     * 获取rawIssues
     * @param repoId repoUuid
     * @param tool tool
     * @param commitId commitId
     * @return rawIssues
     */
    List<RawIssue> getRawIssueByCommitIDAndTool(@Param("repo_id") String repoId, @Param("tool") String tool, @Param("commit_id") String commitId);

    /**
     * 获取rawIssues
     * @param repoUuids repoUuids
     * @param tool tool
     * @param commitId commitId
     * @return rawIssues
     */
    List<RawIssue> getRawIssueByRepoList(@Param("repoUuids") List<String> repoUuids, @Param("tool") String tool, @Param("commit_id") String commitId);


    /**
     * 获取rawIssues
     * @param issueId issueUuid
     * @return rawIssues
     */
    List<Map<String, Object>> getRawIssueByIssueId(@Param("issueId") String issueId);

    /**
     * 获取rawIssues
     * @param map 条件
     * @return rawIssues
     */
    List<RawIssue> getRawIssueListByIssueId(Map<String, Object> map);

    /**
     * rawIssue数量
     * @param issueId issueUuid
     * @param status status
     * @return rawIssue数量
     */
    Integer getNumberOfRawIssuesByIssueIdAndStatus(@Param("issueId") String issueId,@Param("list") List<String> status);

    /**
     * 获取rawIssues
     * @param repoId repoUuid
     * @param tool tool
     * @return rawIssue list
     */
    List<RawIssue> getRawIssueByRepoIdAndTool(@Param("repo_id") String repoId, @Param("tool") String tool);

    /**
     * adder
     * @param issueId issueUuid
     * @return adder
     */
    String getAdderOfOneIssue(@Param("issue_id") String issueId);

    /**
     * solver
     * @param issueId issueUuid
     * @return solver
     */
    String getLastSolverOfOneIssue(@Param("issue_id") String issueId);

    /**
     * lastSolvedInfo
     * @param issueId issueUuid
     * @return lastSolvedInfo
     */
    Map<String, Object> getLastSolvedInfoOfOneIssue(@Param("issue_id") String issueId);

    /**
     * 根据commit和issueUuid筛选对应的rawIssue
     * @param issueUuid issueUuid
     * @param commit commit
     * @return rawIssue
     */
    String getRawIssueUuidByIssueUuidAndCommit(String issueUuid, String commit);

    /**
     * 获取commit根据rawIssueUuid
     * @param rawIssueUuid rawIssueUuid
     * @return commit
     */
    String getCommitByRawIssueUuid(String rawIssueUuid);
}
