package cn.edu.fudan.measureservice.mapper;

import cn.edu.fudan.measureservice.annotation.MethodMeasureAnnotation;
import cn.edu.fudan.measureservice.domain.CommitBase;
import cn.edu.fudan.measureservice.domain.CommitInfoDeveloper;
import cn.edu.fudan.measureservice.domain.RepoMeasure;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RepoMeasureMapper {

    int sameRepoMeasureOfOneCommit(@Param("repo_id")String repo_id,@Param("commit_id")String commit_id);

    RepoMeasure getRepoMeasureByCommit(@Param("repo_id")String repo_id,@Param("commit_id")String commit_id);

    CommitBase getCommitBaseInformation(@Param("repo_id")String repo_id,@Param("commit_id")String commit_id);

    List<CommitInfoDeveloper> getCommitInfoDeveloperListByDuration(@Param("repo_id")String repo_id,@Param("since")String since,@Param("until")String until,@Param("developer_name")String developer_name);

    @Deprecated
    Integer getAddLinesByDuration(@Param("repo_id")String repo_id,@Param("since")String since,@Param("until")String until,@Param("developer_name")String developerName);

    @Deprecated
    Integer getDelLinesByDuration(@Param("repo_id")String repo_id,@Param("since")String since,@Param("until")String until,@Param("developer_name")String developerName);

    @Deprecated
    int getCommitCountsByDuration(@Param("repo_id")String repo_id,@Param("since")String since,@Param("until")String until,@Param("developer_name")String developerName);


    /**
     * 获取该条件内开发者提交次数（去除Merge）
     * @param repoUuid
     * @param since
     * @param until
     * @param developerName
     * @return int developerValidCommitCount
     */
    int getDeveloperValidCommitCount(@Param("repoUuid")String repoUuid,@Param("since")String since,@Param("until")String until,@Param("developer_name")String developerName);


    int getChangedFilesByDuration(@Param("repo_id")String repo_id,@Param("since")String since,@Param("until")String until,@Param("developer_name")String developerName);

    String getStartDateOfRepo(@Param("repo_id")String repo_id);

    void insertOneRepoMeasure(RepoMeasure repoMeasure);

    void delRepoMeasureByrepoUuid(@Param("repo_id")String repo_id);

    void delFileMeasureByrepoUuid(@Param("repo_id")String repo_id);

    /**
     * 根据 开发者名字与repo id获取项目度量列表，都可以为null
     *
     *
     */
    List<RepoMeasure> getRepoMeasureByDeveloperAndrepoUuid(@Param("repo_id")String repo_id,@Param("developer_name")String developer_name ,@Param("since")String since,@Param("until")String until);

    @Deprecated
    int getRepoLOCByDuration(@Param("repo_id")String repo_id,@Param("since")String since,@Param("until")String until,@Param("developer_name")String developerName);

    @Deprecated
    List<Map<String, Object>> getDeveloperListByrepoUuidList(@Param("repoUuidList")List<String> repoUuidList);

    // fixme 只查account,account_name是聚合后名字
    List<Map<String, Object>> getDeveloperDutyTypeListByrepoUuid(@Param("repoUuidList")List<String> repoUuidList);

    String getLastScannedCommitId(@Param("repo_id")String repoUuid);

    @Deprecated
    int getLOCByCondition(@Param("repo_id")String repoUuid,@Param("developer_name")String developerName,@Param("since")String since,@Param("until")String until);

    /**
     * 获取开发者参与库列表（且在sub_repository下）
     * @param developerName 开发者姓名
     * @param since 查询起始时间
     * @param until 查询结束时间
     * @return
     */
    @Deprecated
    List<String> getRepoListByDeveloper(@Param("developer_name")String developerName,@Param("since")String since,@Param("until")String until);

    String getFirstCommitDateByCondition(@Param("repoUuidList")List<String> repoUuidList,@Param("developer")String developerName);

    List<Map<String, Object>> getCommitMsgByCondition(@Param("repo_id")String repoUuid,@Param("developer_name")String developerName,@Param("since")String since,@Param("until")String until);


    int getRepoAge(@Param("repo_id")String repoUuid);

    /**
     * 根据developer查询对应的accout_role
     * @param developer
     *
     * @return
     */
    String getDeveloperType(@Param("developer")String developer);

}
