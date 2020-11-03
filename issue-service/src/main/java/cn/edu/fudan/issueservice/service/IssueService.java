package cn.edu.fudan.issueservice.service;


import cn.edu.fudan.issueservice.domain.dbo.Issue;
import cn.edu.fudan.issueservice.domain.dto.IssueParam;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

public interface IssueService {

    void deleteIssueByRepoIdAndTool(String repoId, String tool);

    void deleteScanResultsByRepoIdAndTool(String repo_id, String tool);

    Issue getIssueByID(String uuid);

    Map<String, List<Map<String, String>>> getRepoWithIssues(String developer);

    Map<String, Object> getFilteredIssueList(JSONObject requestParam, String userToken);

    Object getSpecificIssues(IssueParam issueParam,String userToken);

    Object getDashBoardInfo(String duration, String project_id, String userToken, String tool);

    Object getStatisticalResults(Integer month, String project_id, String userToken, String tool);

    List<String> getExistIssueTypes(String tool);

    Object getAliveAndEliminatedInfo(String project_id, String tool);

    void updateIssueCount(String time);

    Object getNewTrend(Integer month, String project_id, String userToken, String tool);

    void updatePriority(String issueId, String priority,String token) throws Exception;

    void updateStatus(String issueId, String status,String token);

    /**
     *
     * @param repo_id
     * @param since
     * @param until
     * @param tool
     * @return 项目详情页面的issueCount每日数据
     */
    List<Map<String, Object>> getRepoIssueCounts(String repo_id, String since, String until, String tool);

    /**
     * 根据 工具名 获取该工具对缺陷的相应分类
     * @param toolName
     * @return
     */
    List<String> getIssueCategories(String toolName);

    /**
     * 获取缺陷严重程度列表
     * @return
     */
    List<String> getIssueSeverities();

    /**
     * 获取缺陷的状态列表
     * @return
     */
    List<String> getIssueStatus();

    /**
     * 获取相应条件下，每个缺陷种类的数量
     * @param requestParam
     * @return
     */
    Map<String, Object> getIssueCountWithCategoryByCondition(JSONObject requestParam, String userToken);


    /**
     *获取引入过缺陷的所有开发者姓名列表
     */
    List<String> getIssueIntroducers(List<String> repoUuids);

    /**
     * 返回issue总数
     * @param query
     * @return
     */
    Map<String, Object> getIssueFilterListCount(Map<String, Object> query);

    /**
     * 根据query获取issuesList
     * @param query
     * @return issuesList
     */
    Map<String, Object> getIssueFilterList(Map<String, Object> query, Map<String, Object> issueFilterList);
    /**
     * 返回缺陷详情 由于只有open的issue才有location，所以去除solved issues
     * @param issueFilterList
     */
    Map<String, Object> getIssueFilterListWithDetail(Map<String, Object> query, Map<String, Object> issueFilterList);

    /**
     * 缺陷排序
     * @param query
     * @param issueFilterList
     * @return
     */
    Map<String, Object> getIssueFilterListWithOrder(Map<String, Object> query, Map<String, Object> issueFilterList);

    String test();
}
