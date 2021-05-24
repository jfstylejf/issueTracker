package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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
     *
     * @param list rawIssue list
     */
    void insertRawIssueList(List<RawIssue> list);

    /**
     * 删除rawIssues
     *
     * @param list rawIssue list
     */
    void deleteRawIssueByIds(@Param("list") List<String> list);

    /**
     * 获取rawIssues
     *
     * @param repoId repoUuid
     * @param tool   tool
     * @return rawIssue list
     */
    List<String> getRawIssueByRepoIdAndTool(@Param("repo_uuid") String repoId, @Param("tool") String tool);

    /**
     * 获取commit根据rawIssueUuid
     *
     * @param rawIssueUuid rawIssueUuid
     * @return commit
     */
    String getCommitByRawIssueUuid(String rawIssueUuid);

    /**
     * rawIssue
     *
     * @param issueUuid issueUuid
     * @return rawIssue
     */
    RawIssue getLastVersionRawIssue(String issueUuid);

    /**
     * 根据uuid获取rawIssue
     *
     * @param uuids uuids
     * @return rawIssue list
     */
    List<Map<String, Object>> getRawIssueByUuids(@Param("uuids") List<String> uuids);

    /**
     * get latest version rawIssue uuids
     *
     * @param issueUuids issueUuids
     * @return rawIssue uuids
     */
    List<String> getLatestVersionRawIssueUuids(@Param("issueUuids") List<String> issueUuids);

    /**
     * raw issue count
     *
     * @param repoUuid repoUuid
     * @param tool     tool
     * @return count
     */
    @Select("select count(*) from raw_issue where repo_uuid = #{repoUuid} and tool = #{tool}")
    int getRawIssueCount(String repoUuid, String tool);
}
