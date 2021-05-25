package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.common.domain.po.scan.RepoScan;
import cn.edu.fudan.issueservice.domain.dbo.IssueRepo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

/**
 * @author beethoven
 */
@Repository
public interface IssueRepoMapper {

    /**
     * 插入issueRepo
     *
     * @param issueRepo issueRepo
     */
    void insertOneIssueRepo(@Param("issueRepo") RepoScan issueRepo);

    /**
     * 更新issueRepo
     *
     * @param issueRepo issueRepo
     */
    void updateIssueRepo(@Param("issueRepo") RepoScan issueRepo);

    /**
     * 获取issueRepo
     *
     * @param repoId repoUuid
     * @param nature nature
     * @param tool   tool
     * @return issueRepo list
     */
    List<IssueRepo> getIssueRepoByCondition(@Param("repo_uuid") String repoId, @Param("nature") String nature, @Param("tool") String tool);

    /**
     * 删除issueRepo
     *
     * @param repoId repoUuid
     * @param nature nature
     * @param tool   tool
     */
    void deleteIssueRepoByCondition(@Param("repo_uuid") String repoId, @Param("nature") String nature, @Param("tool") String tool);

    /**
     * 返回没扫描commit数
     *
     * @param repoUuid repoUuid
     * @param tool     tool
     * @return 没扫描commit数
     */
    List<HashMap<String, Integer>> getNotScanCommitsCount(@Param("repoUuid") String repoUuid, @Param("tool") String tool);

    /**
     * 获取main issueRepo
     *
     * @param repoUuid repoUuid
     * @param tool     tool
     * @return main issueRepo
     */
    IssueRepo getMainIssueRepo(String repoUuid, String tool);
}
