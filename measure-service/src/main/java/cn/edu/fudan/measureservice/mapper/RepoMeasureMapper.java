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

    /**
     * 获取开发者参与库的合法提交次数（去除Merge）
     * @param repoUuidList
     * @param since
     * @param until
     * @param developerName
     * @return
     */
    List<Map<String,Object>> getValidCommitCountsByAllRepo(@Param("repoUuidList")List<String> repoUuidList,@Param("since")String since,@Param("until")String until,@Param("developer_name")String developerName);

    /**
     * 获取开发者不规范提交明细
     * @param repoUuidList
     * @param since
     * @param until
     * @param developerName
     * @return List<Map<String,Object>  key : developer,commit_time,commit_id,commit_msg
     */
    List<Map<String,Object>> getInvalidCommitMsg(@Param("repoUuidList")List<String> repoUuidList,@Param("since")String since,@Param("until")String until,@Param("developer_name")String developerName);

    /**
     * 获取该条件内开发者提交次数（去除Merge）
     * @param repoUuid
     * @param since
     * @param until
     * @param developerName
     * @return int developerValidCommitCount
     */
    int getDeveloperValidCommitCount(@Param("repoUuid")String repoUuid,@Param("since")String since,@Param("until")String until,@Param("developer_name")String developerName);

    /**
     * 获取开发者所有commit中关联有jira任务的个数
     * @param repoUuidList
     * @param since
     * @param until
     * @param developerName
     * @return
     */
    int getJiraCountByCondition(@Param("repoUuidList")List<String> repoUuidList,@Param("since")String since,@Param("until")String until,@Param("developer_name")String developerName);

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

    List<Map<String, Object>> getDeveloperRankByCommitCount(@Param("repo_id")String repo_id, @Param("since")String since, @Param("until")String until);

    List<Map<String, Object>> getDeveloperRankByLoc(@Param("repo_id")String repo_id, @Param("since")String since, @Param("until")String until);

    int getRepoLOCByDuration(@Param("repo_id")String repo_id,@Param("since")String since,@Param("until")String until,@Param("developer_name")String developerName);

    List<Map<String, Object>> getDeveloperListByrepoUuidList(@Param("repoUuidList")List<String> repoUuidList);

    List<Map<String, Object>> getDeveloperDutyTypeListByrepoUuid(@Param("repoUuidList")List<String> repoUuidList);

    String getLastScannedCommitId(@Param("repo_id")String repoUuid);

    int getLOCByCondition(@Param("repo_id")String repoUuid,@Param("developer_name")String developerName,@Param("since")String since,@Param("until")String until);

    List<Map<String, Object>> getCommitDays(@Param("repo_id")String repoUuid,@Param("developer_name")String developerName,@Param("since")String since,@Param("until")String until);

    List<String> getRepoListByDeveloper(@Param("developer_name")String developerName,@Param("since")String since,@Param("until")String until);

    String getFirstCommitDateByCondition(@Param("repoUuidList")List<String> repoUuidList,@Param("developer")String developerName);

    List<Map<String, Object>> getCommitMsgByCondition(@Param("repo_id")String repoUuid,@Param("developer_name")String developerName,@Param("since")String since,@Param("until")String until);

    Map<String, Object> getWorkLoadByCondition(@Param("repoUuidList")List<String> repoUuidList,@Param("developer_name")String developerName,@Param("since")String since,@Param("until")String until);

    int getRepoAge(@Param("repo_id")String repoUuid);

    /**
     * 根据developer查询对应的accout_role
     * @param developer
     *
     * @return
     */
    String getDeveloperType(@Param("developer")String developer);

    /**
     * 根据项目返回参与的开发者列表
     * @param repoUuidList
     * @return
     */
    List<String> getDeveloperList(@Param("repoUuidList")List<String> repoUuidList,@Param("since") String since,@Param("until") String until);

    /**
     * 获取该情况下的开发者参与Jira任务数
     * @param developer
     * @param repoUuid
     * @param since
     * @param until
     * @return int jiraCount
     */
    int getJiraCount(@Param("developer")String developer,@Param("repoUuid")String repoUuid,@Param("since")String since,@Param("until")String until);

}
