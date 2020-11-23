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
     * @param repoUuid repoUuid
     * @param tool tool
     * @param order order
     * @param commitUuid commitId
     * @return 未解决的Issue
     */
    List<Map.Entry<String, JSONObject>> getNotSolvedIssueCountByToolAndRepoUuid(String repoUuid, String tool, String order, String commitUuid);

    /**
     *  根据条件获取开发者日均解决缺陷数量
     * @param query 条件
     * @return  根据条件获取开发者日均解决缺陷数量
     */
    Map<String,Object> getDayAvgSolvedIssue(Map<String, Object> query);

    /**
     * 缺陷类型的生命周期
     * @param developer 开发者
     * @param repoIdList repoIdList
     * @param since 时间段
     * @param until 终止时间
     * @param tool 缺陷检测工具
     * @param status 缺陷状态 ： living self-solved other-solved all
     * @param percent 0-100 -1[全部显示列表] -2[平均] -3[重数] -4 [最大 最小 平均 中位数 上四分位 下四分位 众数]  [default -4]
     * @param type 缺陷类型
     * @param target self other all [default all]
     * @return 缺陷类型的生命周期
     */
    Object getIssueLifecycle(String developer,List<String> repoIdList,String since,String until,String tool,String status,Double percent,String type,String target);

    /**
     * 返回developer code quality
     * @param query 条件
     * @return developer code quality
     */
    Map<String, JSONObject> getDeveloperCodeQuality(Map<String, Object> query);

    /**
     * 清空缓存
     */
    void clearCache();
}