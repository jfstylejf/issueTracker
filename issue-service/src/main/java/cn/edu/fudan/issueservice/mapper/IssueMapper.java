package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.dbo.Issue;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;


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
     * get specific issue count
     *
     * @param map get issue map
     * @return int
     */
    int getSpecificIssueCount(Map<String, Object> map);

    /**
     * get specific issues
     *
     * @param map get issue map
     * @return List<Issue>
     */
    List<Issue> getSpecificIssues(Map<String, Object> map);

    /**
     * get issue count
     *
     * @param map get issue map
     * @return Integer
     */

    List<Map<String, Object>> getIssueCount(Map<String, Object> map);

    /**
     * get issue count
     *
     * @param map get issue map
     * @return Integer
     */

    List<Map<String, Object>> getIssuesCount(Map<String, Object> map);


    /**
     * get issues by end commit
     *
     * @param repo_id get issue repo id
     * @param tool get issue category
     * @param commit_id get issue commit id
     * @return List<Issue>
     */
    List<Issue> getIssuesByEndCommit(@Param("repo_id") String repo_id, @Param("tool")String tool,@Param("commit_id") String commit_id);

    /**
     *
     * @param developer 开发者
     * @return 返回开发者参与并且有引入过issue的项目的repo_id
     */
    List<String> getRepoWithIssues(@Param("developer") String developer);


    /**
     * get issue list
     *
     * @param map get issue map
     * @return List<Issue>
     */
    List<Issue> getIssueList(Map<String, Object> map);

    /**
     * get issue with more info list
     *
     * @param map get issue map
     * @return List<Map<String, Object>>
     */
    List<Map<String, Object>> getIssueWithAdder(Map<String, Object> map);

    /**
     * get exist issue types
     *
     * @param tool get issue tool
     * @return List<Issue>
     */
    List<String> getExistIssueTypes(@Param("tool")String tool);

    /**
     * get issue ids by repo id and category
     *
     * @param repoId get issue repo id
     * @param tool get issue category
     * @return List<Issue>
     */
    List<String> getIssueIdsByRepoIdAndTool(@Param("repo_id")String repoId,@Param("tool")String tool);

    /**
     * get avg eliminated time
     *
     * @param list get issue repo list
     * @param repoId get issue repo id
     * @param tool get issue category
     * @return Double
     */
    Double getAvgEliminatedTime(@Param("list")List<String> list, @Param("repo_id")String repoId, @Param("tool")String tool);

    /**
     * get max dlive time
     *
     * @param list get issue repo list
     * @param repoId get issue repo id
     * @param tool get issue category
     * @return Long
     */
    Long getMaxAliveTime(@Param("list")List<String> list, @Param("repo_id")String repoId, @Param("tool")String tool);

    /**
     * update one issue priority
     *
     * @param issueId get issue id
     * @param priority get issue priority
     */
    void updateOneIssuePriority(@Param("uuid")String issueId, @Param("priority") int priority);


    /**
     * update one issue status
     * @param status
     * @param manualStatus
     */
    void updateOneIssueStatus(@Param("uuid")String issueId,@Param("status")String status, @Param("manual_status") String manualStatus);



    /**
     * get max issue display id
     *
     * @param repoId get issue repo id
     * @return Integer
     */
    Integer getMaxIssueDisplayId(@Param("repo_id") String repoId);

    /**
     * get ignored count in mapped issues
     *
     * @param ignoreId get issue ignore id
     * @param list get issue ignore list
     * @return int
     */
    int getIgnoredCountInMappedIssues(@Param("ignoreId")String ignoreId, @Param("list")List<String> list);


    // just for update old data
    /**
     * update issue display id
     *
     * @param uuid get issue uuid
     * @param displayId get issue display id
     */
    void updateIssueDisplayId(@Param("uuid")String uuid, @Param("display_id") int displayId);

    /**
     * get all repo id
     *
     * @return List<String>
     */
    List<String> getAllRepoId();

    /**
     * get all issue id by repo id
     *
     * @param repoId get issue repo id
     * @return List<String>
     */
    List<String> getAllIssueIdByRepoId(@Param("repo_id") String repoId);

    /**
     * get not solved issue list by type and repo id
     *
     * @param repoId get issue repo id
     * @param type get issue type
     * @return List<String>
     */
    List<String> getNotSolvedIssueListByTypeAndRepoId(@Param("repo_id") String repoId,@Param("type")  String type);

    /**
     * get not solved issue all list by category and repo id
     *
     * @param repoId get issue repo id
     * @param tool get issue type
     * @return List<Issue>
     */
    List<Issue> getNotSolvedIssueAllListByToolAndRepoId(@Param("repo_id") String repoId,@Param("tool")  String tool);

    /**
     * batch update issue list priority
     *
     * @param issueUuid get issue uuid
     * @param priority get issue priority
     */
    void batchUpdateIssueListPriority(@Param("list")List<String> issueUuid,@Param("priority") int priority);

    /**
     * get number newInstance new issue by duration
     *
     * @param repoId get issue repo id
     * @param start get issue start
     * @param end get issue end
     * @return int
     */
    int getNumberOfNewIssueByDuration(@Param("repo_id") String repoId,@Param("start") String start,@Param("end") String end);

    /**
     * get number newInstance eliminate issue by duration
     *
     * @param repoId get issue repo id
     * @param start get issue start
     * @param end get issue end
     * @return int
     */
    int getNumberOfEliminateIssueByDuration(@Param("repo_id") String repoId,@Param("start") String start,@Param("end") String end);

    /**
     * get commit new issue
     *
     * @param start get issue start
     * @param end get issue end
     * @param repoId get issue repo id
     * @return List<WeakHashMap<Object, Object>>
     */
    List<WeakHashMap<Object, Object>> getCommitNewIssue(@Param("start") String start, @Param("end") String end, @Param("repo_id") String repoId);


    List<Issue> getIssuesByIssueIds(List<String> issueIds);



    /**
     * get all commit by repo id
     *
     * @param repoId
     * @return commit id list
     */
    List<String> getCommitIds(@Param("repo_id") String repoId,@Param("since") String since,@Param("until") String until);

    /**
     * 获取指定repo 与 category 存在无效消除的 issue 列表
     * @param repoId
     * @param tool
     * @return
     */
    List<Issue> getHaveNotAdoptEliminateIssuesByToolAndRepoId(@Param("repo_id") String repoId,@Param("tool")String tool);

    /**
     * 获取指定repo category的 issue 列表， 且status不等于statusList 中任何一个。
     * @param repoId
     * @param tool
     * @param statusList
     * @return
     */
    List<Issue> getIssueByRepoIdAndToolAndStatusList(@Param("repo_id")String repoId, @Param("tool")String tool,
                                                         @Param("status_list")List<String> statusList);

    /**
     * 获取指定缺陷id列表的 缺陷集
     * @param issueIdList
     * @return
     */
    List<Issue> getIssuesByIds(@Param("issueId_list")List<String> issueIdList);


    /**
     *
     * @param repoIdList repoIdList
     * @param type 缺陷类型
     * @param tool 缺陷检测工具
     * @param since 起始时间
     * @param until 结束时间
     * @param developer 开发者
     * @param rawIssueStatus 开发者对rawIssue的操作状态
     * @param issueStatus issue表的status
     * @return 根据rawIssue、commit_view、issue  三表来查询issue信息
     */
    List<Map<String, Object>> getIssueByRawIssueCommitViewIssueTable(@Param("repoIdList")List<String> repoIdList,@Param("type")String type,@Param("tool")String tool,@Param("since")String since,@Param("until")String until,@Param("developer")String developer,@Param("rawIssueStatus")String rawIssueStatus,@Param("issueStatus")String issueStatus);


    /**
     *
     * @param repoIdList repoIdList
     * @param type 缺陷类型
     * @param tool 缺陷检测工具
     * @param since 起始时间
     * @param until 结束时间
     * @param developer 开发者
     * @param rawIssueStatus 开发者对rawIssue的操作状态
     * @return 如果status为solved，表示这个developer所解决的(在RawIssue中解决)issue  并且issue的最终状态为Solved
     */
    List<Map<String, Object>> getSolvedIssueLifeCycle(@Param("repoIdList")List<String> repoIdList,@Param("type")String type,@Param("tool")String tool,@Param("since")String since,@Param("until")String until,@Param("developer")String developer,@Param("status")String rawIssueStatus);

    /**
     *
     * @param repoIdList repoIdList
     * @param type 缺陷类型
     * @param tool 缺陷检测工具
     * @param since 起始时间
     * @param until 结束时间
     * @param developer 开发者
     * @param rawIssueStatus 开发者对rawIssue的操作状态
     * @return 如果status为solved，表示这个developer以外的其他开发者所解决的(在RawIssue中解决)issue 并且issue的最终状态为Solved
     */
    List<Map<String, Object>> getSolvedIssueLifeCycleByOtherSolved(@Param("repoIdList")List<String> repoIdList,@Param("type")String type,@Param("tool")String tool,@Param("since")String since,@Param("until")String until,@Param("developer")String developer,@Param("status")String rawIssueStatus);

    /**
     *
     * @param repoIdList repoIdList
     * @param type 缺陷类型
     * @param tool 缺陷检测工具
     * @param since 起始时间
     * @param until 结束时间
     * @param developer 开发者
     * @param rawIssueStatus 开发者对rawIssue的操作状态
     * @param issueStatus issue表的status
     * @return 根据rawIssue、commit_view、issue  三表来查询issue信息
     */
    List<Map<String, Object>> getOpenIssueLifeCycle(@Param("repoIdList")List<String> repoIdList,@Param("type")String type,@Param("tool")String tool,@Param("since")String since,@Param("until")String until,@Param("developer")String developer,@Param("rawIssueStatus")String rawIssueStatus,@Param("issueStatus")String issueStatus);


    /**
     *
     * @param issueIdList issueid list
     * @return 获取issue统计 包括 quantity lifecycle
     */
    List<Map<String, Object>> getIssueStatisticByIssueIdList(@Param("issueId_list")List<String> issueIdList,@Param("order")String order,@Param("asc")String asc);

    /**
     * 根据所有issue uuid查所有引入者
     *
     * @param repoList
     * @param tool
     * @param since
     * @param until
     * @param status
     * @return
     */
    List<String> getAdderByIssue(@Param("repoList")List<String> repoList,@Param("tool") String tool,@Param("since") String since,@Param("until") String until,@Param("status") String status);

    /**
     * 返回所有解决的issueId
     * @param repoList
     * @param tool
     * @param since
     * @param until
     * @param status
     * @return
     */
    List<String> getSolvedIssue(List<String> repoList, String tool, String since, String until, String status);

    /**
     * 返回筛选后issues数量
     * @param query
     * @return
     */
    int getIssueFilterListCount(Map<String, Object> query);

    /**
     * 根据条件筛选issue
     * @param query
     * @return issue列表
     */
    List<Map<String, Object>> getIssueFilterList(Map<String, Object> query);



    void test(String uuid, String s);

    List<String> getIssuetest();

    /**
     *
     * @param query
     * @return 根据rawIssue、commit_view、issue  三表来查询issue信息
     */
    List<Map<String, Object>> getIssueByRawIssuesCommitViewIssueTable(Map<String, Object> query);

    int getSolvedIssueFilterListCount(Map<String, Object> query);

    List<Map<String, Object>> getSolvedIssueFilterList(Map<String, Object> query);
}
