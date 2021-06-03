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
     * @param since 查询起始时间
     * @param until 查询结束时间
     * @return List<Map<String,Object>> key : repo_id , developer, commit_time , commit_id , message
     */
    List<Map<String,String>> getProjectValidJiraCommitMsg(@Param("repoUuidList")List<String> repoUuidList,@Param("since")String since,@Param("until")String until);


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


}
