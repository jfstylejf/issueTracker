package cn.edu.fudan.issueservice.service;

import cn.edu.fudan.issueservice.util.PagedGridResult;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * @author beethoven
 */
public interface IssueMeasureInfoService {

    /**
     * 获取未解决的Issue
     * @param repoList repoUuids
     * @param tool tool
     * @param order order
     * @param commitUuid commitId
     * @return 未解决的Issue
     */
    List<Map.Entry<String, JSONObject>> getNotSolvedIssueCountByToolAndRepoUuid(List<String> repoList, String tool, String order, String commitUuid);

    /**
     *  根据条件获取开发者日均解决缺陷数量
     * @param query 条件
     * @param token token
     * @return  根据条件获取开发者日均解决缺陷数量
     */
    Map<String,Object> getDayAvgSolvedIssue(Map<String, Object> query, String token);

    /**
     * 返回developer code quality
     * @param query 条件
     * @param needAll needAll
     * @param token token
     * @return developer code quality
     */
    Map<String, Object> getDeveloperCodeQuality(Map<String, Object> query, Boolean needAll, String token);

    /**
     * 清空缓存
     */
    void clearCache();

    /**
     * 缺陷类型的生命周期
     * @param status 缺陷状态 living self-solved other-solved
     * @param target self other
     * @param query condition
     * @return 缺陷类型的生命周期
     */
    JSONObject getIssuesLifeCycle(String status, String target, Map<String, Object> query);

    /**
     * 缺陷类型的生命周期详情
     * @param status 缺陷状态 living self-solved other-solved
     * @param target self other
     * @param query condition
     * @param token token
     * @return 缺陷类型的生命周期详情
     */
    List<JSONObject> getLifeCycleDetail(String status, String target, Map<String, Object> query, String token);

    /**
     * handleSortDeveloperLifecycle
     * @param developersLifecycle developersLifecycle
     * @param isAsc isAsc
     * @param ps ps
     * @param page page
     * @return 排序后developersLifecycle
     */
    List<Map<String, JSONObject>> handleSortDeveloperLifecycle(List<Map<String, JSONObject>> developersLifecycle, Boolean isAsc, int ps, int page);

    /**
     * 对code quality排序
     * @param result result
     * @param asc asc
     * @param ps  ps
     * @param page  page
     * @return 排序后的code quality
     */
    Map<String, Object> handleSortCodeQuality(List<Map<String, Object>> result, Boolean asc, int ps, int page);

    /**
     *
     * @param query 查询条件
     * @return producer, livingIssueCount
     */
    Object getSelfIntroducedLivingIssueCount(int page, int ps, String order, Boolean isAsc, Map<String, Object> query, Boolean isPagination);


}