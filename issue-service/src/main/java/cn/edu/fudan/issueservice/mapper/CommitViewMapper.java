package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.dbo.Commit;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.Map;

/**
 * @author Beethoven
 */
@Repository
public interface CommitViewMapper {

    /**
     * 获取commit列表
     * @param repoId repoUuid
     * @param startCommitTime 时间
     * @return commit list
     */
    LinkedList<Commit> getCommits(@Param("repo_id") String repoId, @Param("start_commit_time") String startCommitTime);

    /**
     * 根据commitId 获取commitView 的相关信息
     * @param repoId repoUuid
     * @param commitId commitId
     * @return ommitView信息
     */
    Map<String, Object> getCommitViewInfoByCommitId(@Param("repo_id") String repoId, @Param("commit_id") String commitId);

    /**
     * 根据commitId获取commit信息
     * @param repoUuid repoUuid
     * @param startCommit commit
     * @return commit信息
     */
    Commit getCommitByCommitId(String repoUuid, String startCommit);
}
