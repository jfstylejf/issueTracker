package cn.edu.fudan.issueservice.service;


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
}