package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.dbo.Commit;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Beethoven
 */
@Repository
public interface CommitViewMapper {

    /**
     * 获取commit列表
     *
     * @param repoId          repoUuid
     * @param startCommitTime 时间
     * @return commit list
     */
    List<Commit> getCommits(@Param("repo_uuid") String repoId, @Param("start_commit_time") String startCommitTime);

    /**
     * 根据commitId 获取commitView 的相关信息
     *
     * @param repoId   repoUuid
     * @param commitId commitId
     * @return ommitView信息
     */
    Map<String, Object> getCommitViewInfoByCommitId(@Param("repo_uuid") String repoId, @Param("commit_id") String commitId);

    /**
     * 根据commitId获取commit信息
     *
     * @param repoUuid    repoUuid
     * @param startCommit commit
     * @return commit信息
     */
    Commit getCommitByCommitId(String repoUuid, String startCommit);

    /**
     * get commit time by commit id
     *
     * @param commit   commit id
     * @param repoUuid repoUuid
     * @return commit time
     */
    String getCommitTimeByCommitId(String commit, String repoUuid);

    /**
     * get parent commit
     *
     * @param commitTime commitTime
     * @param repoUuid   repoUuid
     * @return
     */
    List<Map<String, Object>> getParentCommits(String commitTime, String repoUuid);

    /**
     * get developer name
     *
     * @param commitId commitId
     * @return developer name
     */
    String getDeveloperByCommitId(String commitId);
}
