package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.dbo.Issue;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


@Repository
public interface IssueMapper {

    /**
     * insertIssueList
     *
     * @param list get issue list
     */
    void insertIssueList(List<Issue> list);

    /**
     * delete issue by repo id and category
     *
     * @param repoId get issue repo id
     * @param tool get issue tool
     */
    void deleteIssueByRepoIdAndTool(@Param("repo_id")String repoId,@Param("tool")String tool);

    /**
     * batch update issue
     *
     * @param list get issue list
     */
    void batchUpdateIssue(List<Issue> list);

    /**
     * get issue by id
     *
     * @param uuid get issue uuid
     * @return Issue
     */
    Issue getIssueByID(String uuid);

    /**
     * 返回开发者参与并且有引入过issue的项目的repoUuid
     * @param developer 开发者
     * @return 返回开发者参与并且有引入过issue的项目的repo_id
     */
    List<String> getRepoWithIssues(@Param("developer") String developer);

    /**
     * get exist issue types
     *
     * @param tool get issue tool
     * @return List<Issue>
     */
    List<String> getExistIssueTypes(@Param("tool")String tool);

    /**
     * update one issue priority
     *
     * @param issueId get issue id
     * @param priority get issue priority
     */
    void updateOneIssuePriority(@Param("uuid")String issueId, @Param("priority") int priority);

    /**
     * update one issue status
     * @param issueId issueUuid
     * @param status status
     * @param manualStatus manualStatus
     */
    void updateOneIssueStatus(@Param("uuid")String issueId,@Param("status")String status, @Param("manual_status") String manualStatus);


    /**
     * get max issue display id
     * @param repoId get issue repo id
     * @return Integer
     */
    Integer getMaxIssueDisplayId(@Param("repo_id") String repoId);

    /**
     * get not solved issue all list by category and repo id
     *
     * @param repoUuids get issue repo id
     * @param tool get issue type
     * @return List<Issue>
     */
    List<Issue> getNotSolvedIssueAllListByToolAndRepoId(@Param("repoUuids") List<String> repoUuids,@Param("tool")  String tool);

    /**
     * 获取指定repocategory的issue列表且status不等于statusLis中任何一个。
     * @param repoId repoUuid
     * @param tool tool
     * @param statusList statusList
     * @return 获取指定repocategory的issue列表且status不等于statusLis中任何一个
     */
    List<Issue> getIssueByRepoIdAndToolAndStatusList(@Param("repo_id")String repoId, @Param("tool")String tool, @Param("status_list")List<String> statusList);

    /**
     * 获取指定缺陷id列表的缺陷集
     * @param issueIdList repoUuidList
     * @return 获取指定缺陷id列表的缺陷集
     */
    List<Issue> getIssuesByIds(@Param("issueId_list")List<String> issueIdList);

    /**
     * 如果status为solved，表示这个developer所解决的(在RawIssue中解决)issue并且issue的最终状态为Solved
     * @param repoIdList repoIdList
     * @param type 缺陷类型
     * @param tool 缺陷检测工具
     * @param since 起始时间
     * @param until 结束时间
     * @param developer 开发者
     * @param rawIssueStatus 开发者对rawIssue的操作状态
     * @return 如果status为solved，表示这个developer所解决的(在RawIssue中解决)issue并且issue的最终状态为Solved
     */
    List<Map<String, Object>> getSolvedIssueLifeCycle(@Param("repoIdList")List<String> repoIdList,@Param("type")String type,@Param("tool")String tool,@Param("since")String since,@Param("until")String until,@Param("developer")String developer,@Param("status")String rawIssueStatus);

    /**
     * 如果status为solved，表示这个developer以外的其他开发者所解决的(在RawIssue中解决)issue并且issue的最终状态为Solved
     * @param repoIdList repoIdList
     * @param type 缺陷类型
     * @param tool 缺陷检测工具
     * @param since 起始时间
     * @param until 结束时间
     * @param developer 开发者
     * @param rawIssueStatus 开发者对rawIssue的操作状态
     * @return 如果status为solved，表示这个developer以外的其他开发者所解决的(在RawIssue中解决)issue并且issue的最终状态为Solved
     */
    List<Map<String, Object>> getSolvedIssueLifeCycleByOtherSolved(@Param("repoIdList")List<String> repoIdList,@Param("type")String type,@Param("tool")String tool,@Param("since")String since,@Param("until")String until,@Param("developer")String developer,@Param("status")String rawIssueStatus);

    /**
     * 根据rawIssue、commit_view、issue三表来查询issue信息
     * @param repoIdList repoIdList
     * @param type 缺陷类型
     * @param tool 缺陷检测工具
     * @param since 起始时间
     * @param until 结束时间
     * @param developer 开发者
     * @param rawIssueStatus 开发者对rawIssue的操作状态
     * @param issueStatus issue表的status
     * @return 根据rawIssue、commit_view、issue三表来查询issue信息
     */
    List<Map<String, Object>> getOpenIssueLifeCycle(@Param("repoIdList")List<String> repoIdList,@Param("type")String type,@Param("tool")String tool,@Param("since")String since,@Param("until")String until,@Param("developer")String developer,@Param("rawIssueStatus")String rawIssueStatus,@Param("issueStatus")String issueStatus);

    /**
     * 返回筛选后issues数量
     * @param query 条件
     * @return 筛选后issues数量
     */
    int getIssueFilterListCount(Map<String, Object> query);

    /**
     * 根据条件筛选issue
     * @param query 条件
     * @return issue列表
     */
    List<Map<String, Object>> getIssueFilterList(Map<String, Object> query);

    /**
     * 返回解决issues数量
     * @param query 条件
     * @return 返回解决issues数量
     */
    int getSolvedIssueFilterListCount(Map<String, Object> query);

    /**
     * 返回解决issues列表
     * @param query 条件
     * @return 返回解决issues列表
     */
    List<Map<String, Object>> getSolvedIssueFilterList(Map<String, Object> query);

    /**
     * update issue manual status
     * @param repoUuid 所在repo库
     * @param issueUuid issueUuid
     * @param manualStatus 要改成的 目标状态 Ignore, Misinformation, To review, Default
     * @param issueType 要忽略的issue 类型
     * @param tool issue 的检测工具
     * @param currentTime 当前更新记录的时间
     */
    void updateIssueManualStatus(@Param("repoUuid")String repoUuid, @Param("issueUuid")String issueUuid, @Param("manualStatus")String manualStatus,
                                 @Param("issueType")String issueType, @Param("tool")String tool, @Param("currentTime") String currentTime);
}
