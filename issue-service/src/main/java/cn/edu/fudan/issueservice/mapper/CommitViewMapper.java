package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.dbo.Commit;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CommitViewMapper {

    /**
     * 获取相应条件的 commit数量
     * @param repoId
     * @param startCommitTime
     * @return
     */
    Integer getCommitCount(@Param("repo_id") String repoId, @Param("start_commit_time") String startCommitTime);

    /**
     *
     */
    List<Commit> getCommits(@Param("repo_id") String repoId, @Param("start_commit_time") String startCommitTime);

    /**
     * 获取所给repo的开发者列表
     * @param repoIdList repoIdList
     * @return 获取所给repo的开发者列表
     */
    List<String> getDevelopersByRepoIdList(@Param("repoIdList")List<String> repoIdList);

    /**
     *
     * @param commitId commitId
     * @return 根据commitId 获取commitView 的相关信息
     */
    Map<String, Object> getCommitViewInfoByCommitId(@Param("repo_id") String repoId, @Param("commit_id") String commitId);
}
