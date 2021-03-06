package cn.edu.fudan.measureservice.mapper;

import cn.edu.fudan.measureservice.domain.CommitBase;
import cn.edu.fudan.measureservice.domain.CommitInfoDeveloper;
import cn.edu.fudan.measureservice.domain.RepoMeasure;
import cn.edu.fudan.measureservice.domain.metric.RepoTagMetric;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
// fixme 删除前代无用代码，补全javaDoc
public interface RepoMeasureMapper {

    /**
     * 查询 repo_measure 中是否存在这条 commit 信息
     * @param commitId 查询 commit_id
     * @return 存在的记录数
     */
    int containCommitIdOrNot(@Param("commit_id")String commitId);

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

    /**
     * 插入 repo_measure 一条 commit 相关数据
     * @param repoMeasure 插入参数信息
     */
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

    /**
     * 查询该库最后一次扫描的提交id
     * @param repoUuid 查询库 id
     * @return 该库最新的提交 id
     */
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


    List<Map<String, Object>> getCommitMsgByCondition(@Param("repo_id")String repoUuid,@Param("developer_name")String developerName,@Param("since")String since,@Param("until")String until);

    /**
     * fixme 移到 projectMapper
     * 查询库生存时间
     * @param repoUuid 查询库
     * @return 库生存时间
     */
    int getRepoAge(@Param("repo_id")String repoUuid);

    /**
     * fixme 移到accountMapper
     * 根据developer查询对应的accout_role
     * @param developer
     *
     * @return
     */
    String getDeveloperType(@Param("developer")String developer);


    /**
     * 返回所查询库下 repo_measure 表中的信息条数
     * @param repoUuid 查询库
     * @return int countNum
     */
    int getRepoMeasureMsgNumByRepo(@Param("repoUuid") String repoUuid);

    /**
     * 删除repo_measure表中所属repoUuid的数据
     * @param repoUuid 删除库
     */
    void deleteRepoMeasureMsg(@Param("repoUuid") String repoUuid);



    /**
     * 获取项目下包含库的合法提交信息 （不含Merge）
     * @param repoUuidList 查询库列表
     * @param developer 查询开发者
     * @param since 查询起始时间
     * @param until 查询结束时间
     * @return List<Map<String,Object>> key : repo_id , developer, commit_time , commit_id , message , is_compliance
     */
    List<Map<String,Object>> getProjectValidCommitMsg(@Param("developer") String developer,@Param("repoUuidList")List<String> repoUuidList,@Param("since")String since,@Param("until")String until);


    /**
     * 获取项目下包含库的合法 JIRA 提交信息 （不含Merge）
     * @param repoUuidList 查询库列表
     * @param developer 查询开发者
     * @param since 查询起始时间
     * @param until 查询结束时间
     * @return List<Map<String,Object>> key : repo_id , developer, commit_time , commit_id , message
     */
    List<Map<String,Object>> getProjectValidJiraCommitMsg(@Param("developer") String developer,@Param("repoUuidList")List<String> repoUuidList,@Param("since")String since,@Param("until")String until);


    /**
     * 获取分页查询查询库下的合法提交信息 （不含Merge）
     * @param developer 查询开发者
     * @param repoUuidList 查询库列表
     * @param since 查询起始时间
     * @param until 查询结束时间
     * @param size 查询大小
     * @param beginIndex 查询起始位置
     * @return List<Map<String,Object>> key : repo_id , developer, commit_time , commit_id , message, is_compliance
     */
    List<Map<String,Object>> getProjectValidCommitMsgWithPage(@Param("developer") String developer,@Param("repoUuidList")List<String> repoUuidList,@Param("since")String since,@Param("until")String until,@Param("size") int size ,@Param("beginIndex") int beginIndex);


    /**
     * 获取分页查询查询库下的合法 JIRA 提交信息 （不含Merge）
     * @param repoUuidList 查询库列表
     * @param developer 查询开发者
     * @param since 查询起始时间
     * @param until 查询结束时间
     * @param size 查询大小
     * @param beginIndex 查询起始位置
     * @return List<Map<String,Object>> key : repo_id , developer, commit_time , commit_id , message, is_compliance
     */
    List<Map<String,Object>> getProjectValidJiraCommitMsgWithPage(@Param("developer") String developer,@Param("repoUuidList")List<String> repoUuidList,@Param("since")String since,@Param("until")String until,@Param("size") int size ,@Param("beginIndex") int beginIndex);

    /**
     * 获取分页查询查询库下的合法 非JIRA 提交信息 （不含Merge）
     * @param repoUuidList 查询库列表
     * @param developer 查询开发者
     * @param since 查询起始时间
     * @param until 查询结束时间
     * @param size 查询大小
     * @param beginIndex 查询起始位置
     * @return List<Map<String,Object>> key : repo_id , developer, commit_time , commit_id , message, is_compliance
     */
    List<Map<String,Object>> getProjectValidNotJiraCommitMsgWithPage(@Param("developer") String developer,@Param("repoUuidList")List<String> repoUuidList,@Param("since")String since,@Param("until")String until,@Param("size") int size ,@Param("beginIndex") int beginIndex);


    /**
     * 获取获取对应库下相应衡量指标的基线列表
     * @param repoUuid 查询库
     * @return 对应库下衡量指标的基线列表
     */
    List<RepoTagMetric> getRepoTagMetricList(@Param("repoUuid") String repoUuid);

    /**
     * 获取对应库对应维度的基线数据
     * @param repoUuid 查询库
     * @param tag 查询维度
     * @return 该库下该指标的基线数据
     */
    RepoTagMetric getRepoTagMetric(@Param("repoUuid") String repoUuid, @Param("tag") String tag);

    /**
     * 查询相应库的维度数据是否有记录，并返回记录数
     * @param repoUuid 查询库
     * @param tag 查询维度标签
     * @return 记录个数
     */
    int containsRepoTagMetricOrNot(@Param("repoUuid") String repoUuid, @Param("tag") String tag);

    /**
     * 插入库维度基线数据
     * @param repoTagMetric 对应的库下该维度数据基线
     */
    void insertRepoTagMetric(RepoTagMetric repoTagMetric);

    /**
     * 更新库维度基线数据
     * @param repoTagMetric 对应的库下该维度数据基线
     */
    void updateRepoTagMetric(RepoTagMetric repoTagMetric);

    /**
     * 获取这个库下的扫描起始 commit
     * @param repoUuid 查询库 id
     * @return 起始commitId
     */
    String getRepoStartCommit(@Param("repoUuid") String repoUuid);

    /**
     * 获取开发者当前库下的提交次数（repo_measure中）
     * @param developer 查询开发者
     * @param repoUuid 查询库 id
     * @param since 查询起始时间
     * @param until 查询截止时间
     * @return 开发者当前库下提交次数
     */
    int getRepoDeveloperCommitCount(@Param("developer") String developer, @Param("repoUuid") String repoUuid, @Param("since") String since, @Param("until") String until);

}
