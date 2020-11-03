package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.IssueCountDeveloper;
import cn.edu.fudan.issueservice.domain.dto.IssueCountPo;
import cn.edu.fudan.issueservice.domain.ScanResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ScanResultMapper {

    void addOneScanResult(ScanResult scanResult);

    void deleteScanResultsByRepoIdAndCategory(@Param("repo_id")String repo_id,@Param("category") String category);

    List<IssueCountPo> getScanResultsGroupByDay(@Param("list") List<String> repoIds, @Param("category") String category, @Param("start") String start, @Param("end") String end);

    List<IssueCountPo> getScanResultsEachCommit(@Param("repo_id")String repo_id,@Param("category") String category, @Param("start") String start, @Param("end") String end);

    List<IssueCountDeveloper> getScanResultsEachDeveloper(@Param("repo_id")String repo_id, @Param("category") String category, @Param("start") String start, @Param("end") String end);

    IssueCountPo getMergedScanResult(@Param("list") List<String> repoIds, @Param("category") String category, @Param("start") String start, @Param("end") String end);

    int getNewIssueCountByCommit(@Param("repo_id") String repo_id,@Param("commit_id") String commit_id,@Param("category") String category);

    int getEliminateIssueCountByCommit(@Param("repo_id") String repo_id,@Param("commit_id") String commit_id,@Param("category") String category);

    List<ScanResult> getScanResultByCondition(@Param("repo_id") String repoId,@Param("since") String since,@Param("until") String until,@Param("category") String category,@Param("developer") String developer);

    List<Map<String, Object>> getRepoIssueCounts(@Param("repo_id")String repo_id, @Param("since")String since, @Param("until")String until,@Param("category") String category,@Param("developer")String developer);

}
