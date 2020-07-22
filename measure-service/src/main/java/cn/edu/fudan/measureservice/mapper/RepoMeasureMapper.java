package cn.edu.fudan.measureservice.mapper;

import cn.edu.fudan.measureservice.domain.CommitBase;
import cn.edu.fudan.measureservice.domain.CommitInfoDeveloper;
import cn.edu.fudan.measureservice.domain.RepoMeasure;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RepoMeasureMapper {

    int sameMeasureOfOneCommit(@Param("repo_id")String repo_id,@Param("commit_id")String commit_id);

    RepoMeasure getRepoMeasureByCommit(@Param("repo_id")String repo_id,@Param("commit_id")String commit_id);

    CommitBase getCommitBaseInformation(@Param("repo_id")String repo_id,@Param("commit_id")String commit_id);

    List<CommitInfoDeveloper> getCommitInfoDeveloperListByDuration(@Param("repo_id")String repo_id,@Param("since")String since,@Param("until")String until,@Param("developer_name")String developer_name);

    Integer getAddLinesByDuration(@Param("repo_id")String repo_id,@Param("since")String since,@Param("until")String until,@Param("developer_name")String developerName);

    Integer getDelLinesByDuration(@Param("repo_id")String repo_id,@Param("since")String since,@Param("until")String until,@Param("developer_name")String developerName);

    int getCommitCountsByDuration(@Param("repo_id")String repo_id,@Param("since")String since,@Param("until")String until,@Param("developer_name")String developerName);

    int getChangedFilesByDuration(@Param("repo_id")String repo_id,@Param("since")String since,@Param("until")String until,@Param("developer_name")String developerName);

    String getStartDateOfRepo(@Param("repo_id")String repo_id);

    void insertOneRepoMeasure(RepoMeasure repoMeasure);

    void delRepoMeasureByRepoId(@Param("repo_id")String repo_id);

    void delFileMeasureByRepoId(@Param("repo_id")String repo_id);

    /**
     * 根据 开发者名字与repo id获取项目度量列表，都可以为null
     *
     *
     */
    List<RepoMeasure> getRepoMeasureByDeveloperAndRepoId(@Param("repo_id")String repo_id,@Param("developer_name")String developer_name,@Param("counts")int counts,@Param("since")String since,@Param("until")String until);

    List<Map<String, Object>> getDeveloperRankByCommitCount(@Param("repo_id")String repo_id, @Param("since")String since, @Param("until")String until);

    List<Map<String, Object>> getDeveloperRankByLoc(@Param("repo_id")String repo_id, @Param("since")String since, @Param("until")String until);

    int getRepoLOCByDuration(@Param("repo_id")String repo_id,@Param("since")String since,@Param("until")String until,@Param("developer_name")String developerName);

    List<Map<String, Object>> getDeveloperListByRepoId(@Param("repo_id")String repo_id);

    String getLastScannedCommitId(@Param("repo_id")String repoId);

    int getLOCByCondition(@Param("repo_id")String repoId,@Param("developer_name")String developerName,@Param("since")String beginDate,@Param("until")String endDate);

    List<Map<String, Object>> getCommitDays(@Param("repo_id")String repoId,@Param("developer_name")String developerName,@Param("since")String beginDate,@Param("until")String endDate);

    List<String> getRepoListByDeveloper(@Param("developer_name")String developerName);

    String getFirstCommitDateByCondition(@Param("repo_id") String repo_id,@Param("developer")String developerName);

    String getLastCommitDateOfOneRepo(@Param("repo_id") String repo_id,@Param("developer")String developerName);

    List<Map<String, Object>> getCommitMsgByCondition(@Param("repo_id")String repoId,@Param("developer_name")String developerName,@Param("since")String beginDate,@Param("until")String endDate);

    List<String> getCommitMsgByRepoId(@Param("repo_id")String repoId,@Param("developer")String developerName);

    int getCommitNumByRepoId(@Param("repo_id")String repoId,@Param("developer")String developerName);

}
