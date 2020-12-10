package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.dbo.Issue;
import com.alibaba.fastjson.JSONObject;
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

    /**
     * 获取自己引入自己解决的issue
     * @param query condition
     * @return issue date list
     */
    List<Integer> getSelfIntroduceSelfSolvedIssueInfo(Map<String, Object> query);

    /**
     * 获取他人引入自己解决的issue
     * @param query condition
     * @return issue date list
     */
    List<Integer> getOtherIntroduceSelfSolvedIssueInfo(Map<String, Object> query);

    /**
     * 获取自己引入未解决的issue
     * @param query condition
     * @return issue date list
     */
    List<Integer> getSelfIntroduceLivingIssueInfo(Map<String, Object> query);

    /**
     * 获取自己引入他人解决的issue
     * @param query condition
     * @return issue date list
     */
    List<Integer> getSelfIntroduceOtherSolvedIssueInfo(Map<String, Object> query);

    /**
     * 获取自己引入自己解决的issue detail
     * @param query condition
     * @return issue detail list
     */
    List<JSONObject> getSelfIntroduceSelfSolvedIssueDetail(Map<String, Object> query);

    /**
     * 获取他人引入自己解决的issue detail
     * @param query condition
     * @return issue detail list
     */
    List<JSONObject> getOtherIntroduceSelfSolvedIssueDetail(Map<String, Object> query);

    /**
     * 获取自己引入未解决的issue detail
     * @param query condition
     * @return issue detail list
     */
    List<JSONObject> getSelfIntroduceLivingIssueDetail(Map<String, Object> query);

    /**
     * 获取自己引入他人解决的issue detail
     * @param query condition
     * @return issue detail list
     */
    List<JSONObject> getSelfIntroduceOtherSolvedIssueDetail(Map<String, Object> query);

    /**
     * issueIntroducers
     * @param repoUuids repoUuid list
     * @return issueIntroducers
     */
    List<String> getIssueIntroducers(List<String> repoUuids);
}
