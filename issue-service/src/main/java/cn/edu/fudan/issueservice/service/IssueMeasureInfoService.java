package cn.edu.fudan.issueservice.service;


import cn.edu.fudan.issueservice.util.PagedGridResult;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * @description: 代码度量信息
 * @author: fancying
 * @create: 2019-04-01 22:11
 **/
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
     * @return  根据条件获取开发者日均解决缺陷数量
     */
    Map<String,Object> getDayAvgSolvedIssue(Map<String, Object> query);

    /**
     * 返回developer code quality
     * @param query 条件
     * @param codeQuality is codeQuality
     * @return developer code quality
     */
    Map<String, JSONObject> getDeveloperCodeQuality(Map<String, Object> query, boolean codeQuality);

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
     * 返回自己引入未解决issue
     * @param page page
     * @param ps ps
     * @param order order
     * @param isAsc isAsc
     * @param query query
     * @return 项目总览人员列表中自己引入未解决issue
     */
    PagedGridResult getSelfIntroducedLivingIssueCount(int page, int ps, String order, Boolean isAsc, Map<String, Object> query);

    /**
     * 对lifecycle排序
     * @param developersLifecycle developersLifecycle
     * @param isAsc isAsc
     * @return 排序后
     */
    List<Map<String, JSONObject>> handleSortDeveloperLifecycle(List<Map<String, JSONObject>> developersLifecycle, Boolean isAsc);
}