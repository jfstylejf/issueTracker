/**
 * @description: 代码度量信息
 * @author: fancying
 * @create: 2019-04-01 22:11
 **/
package cn.edu.fudan.issueservice.service;


import cn.edu.fudan.issueservice.domain.IssueCountDeveloper;
import cn.edu.fudan.issueservice.domain.IssueCountMeasure;
import cn.edu.fudan.issueservice.domain.dto.IssueCountPo;
import cn.edu.fudan.issueservice.domain.statistics.CodeQualityResponse;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

public interface IssueMeasureInfoService {

    JSONObject getIssueInfoOfSpecificFile(String repoId, String commitId, String tool, String filePath);

    //时间粒度  快照（commit）
    //空间粒度  开发者，文件，包，项目
    int numberOfRemainingIssue(String repoId, String commit, String spaceType, String detail);

    //时间粒度  日，周，月
    //空间粒度  开发者，项目
    int numberOfNewIssue(String duration, String spaceType, String detail);

    //时间粒度  快照（commit）
    int numberOfNewIssueByCommit(String repoId,String commitId,String spaceType,String category);

    //时间粒度  日，周，月
    //空间粒度  开发者，项目
    int numberOfEliminateIssue(String duration, String spaceType, String detail);

    //时间粒度  快照（commit）
    int numberOfEliminateIssueByCommit(String repoId,String commitId,String spaceType,String category);

    List<IssueCountPo> getIssueCountEachCommit(String repoId,String category,String since,String until);

    IssueCountMeasure getIssueCountMeasureByRepo(String repoId,String category,String since,String until);

    List<IssueCountDeveloper> getIssueCountMeasureByDeveloper(String repoId,String category,String since,String until);

    List<JSONObject> getNotSolvedIssueCountByCategoryAndRepoId(String repoId, String category,String commitId);

    /**
     *  根据条件获取返回的代码质量
     * @param developer   开发者
     * @param timeGranularity 时间粒度
     * @param since       从什么时候开始统计
     * @param until       统计到什么时候
     * @param repoId      repo的唯一id
     * @param tool        什么工具的分析结果
     * @param page        返回第几页的结果
     * @param ps          每页结果的大小
     * @return  返回符合条件的代码质量
     */
    CodeQualityResponse getQualityChangesByCondition(String developer, String timeGranularity, String since, String until, String repoId, String tool, int page, int ps);


    /**
     *  根据条件获取缺陷数量
     * @param developer 开发者
     * @param repoId repoId
     * @param since 起始时间
     * @param until 结束时间
     * @param tool 缺陷检测工具
     * @param generalCategory 缺陷类别
     * @return  根据条件获取缺陷数量
     */
    Integer getIssueCountByConditions(String developer,String repoId,String since,String until,String tool,String generalCategory);

    /**
     *  根据条件获取开发者日均解决缺陷数量
     * @param developer 开发者
     * @param repoId repoId
     * @param since 起始时间
     * @param until 结束时间
     * @param tool 缺陷检测工具
     * @return  根据条件获取开发者日均解决缺陷数量
     */
    Map<String,Object> getDayAvgSolvedIssue(String developer, String repoId, String since, String until, String tool);


    /**
     *
     * @param developer 开发者
     * @param repoId repoId
     * @param date 起止时间
     * @param tool 缺陷检测工具
     * @return 返回要求的缺陷数量
     */
    Map<String, Integer> getIssueQuantityByConditions(String developer,String repoId,String date,String tool);

    /**
     *
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
    Object getIssueLifecycle(String developer,String repoIdList,String since,String until,String tool,String status,Double percent,String type,String target);

    /**
     *
     * @param query
     * @return
     */
    Map<String, JSONObject>  getDeveloperCodeQuality(Map<String, Object> query);

    /**
     *
     * @param repoIdList repoIdList
     * @param tool  缺陷检测工具
     * @param status add/solve
     * @param isAdd 是否是由这些开发者引入的缺陷
     * @param since 开始时间
     * @param until  结束时间
     * @return 每个开发者对应条件下的数量
     */
    Map<String, Integer> getDeveloperQuantity(String repoIdList,String tool,String status,Boolean isAdd, String since,String until);

    /**
     * 获取扫描各个状态的数量
     * @param repoId repoid
     * @param tool 缺陷工具
     * @param date 时间段
     * @return 获取扫描各个状态的数量
     */
    JSONObject getScanStatusCount(String repoId, String tool, String date);


    /**
     * 清空缓存
     */
    void clearCache();

}