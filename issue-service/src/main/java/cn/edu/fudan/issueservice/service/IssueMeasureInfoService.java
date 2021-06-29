package cn.edu.fudan.issueservice.service;

import cn.edu.fudan.issueservice.domain.vo.DeveloperLivingIssueVO;
import cn.edu.fudan.issueservice.domain.vo.IssueTopVO;
import cn.edu.fudan.issueservice.domain.vo.PagedGridResult;
import cn.edu.fudan.issueservice.exception.MeasureServiceException;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * @author beethoven
 */
public interface IssueMeasureInfoService {

    /**
     * 获取未解决的Issue
     *
     * @param repoList repoUuids
     * @param tool     tool
     * @param order    order
     * @return 未解决的Issue
     */
    List<Map.Entry<String, JSONObject>> getNotSolvedIssueCountByToolAndRepoUuid(List<String> repoList, String tool, String order);

    /**
     * 获取未解决的Issue
     *
     * @param repoList   repoUuids
     * @param tool       tool
     * @param order      order
     * @param commitUuid commitUuid
     * @return 未解决的Issue
     */
    List<Map.Entry<String, JSONObject>> getNotSolvedIssueCountByCommit(List<String> repoList, String tool, String order, String commitUuid);

    /**
     * 根据条件获取开发者日均解决缺陷数量
     *
     * @param query 条件
     * @param token token
     * @return 根据条件获取开发者日均解决缺陷数量
     * @throws MeasureServiceException MeasureServiceException
     */
    Map<String, Object> getDayAvgSolvedIssue(Map<String, Object> query, String token) throws MeasureServiceException;

    /**
     * 返回developer code quality
     *
     * @param query   条件
     * @param needAll needAll
     * @param token   token
     * @return developer code quality
     * @throws MeasureServiceException MeasureServiceException
     */
    Map<String, Object> getDeveloperCodeQuality(Map<String, Object> query, Boolean needAll, String token) throws MeasureServiceException;

    /**
     * 清空缓存
     */
    void clearCache();

    /**
     * 缺陷类型的生命周期
     *
     * @param status 缺陷状态 living self-solved other-solved
     * @param target self other
     * @param query  condition
     * @return 缺陷类型的生命周期
     */
    JSONObject getIssuesLifeCycle(String status, String target, Map<String, Object> query);

    /**
     * 缺陷类型的生命周期详情
     *
     * @param status 缺陷状态 living self-solved other-solved
     * @param target self other
     * @param query  condition
     * @param token  token
     * @return 缺陷类型的生命周期详情
     */
    List<JSONObject> getLifeCycleDetail(String status, String target, Map<String, Object> query, String token);

    /**
     * handleSortDeveloperLifecycle
     *
     * @param developersLifecycle developersLifecycle
     * @param isAsc               isAsc
     * @param ps                  ps
     * @param page                page
     * @return 排序后developersLifecycle
     */
    List<Map<String, JSONObject>> handleSortDeveloperLifecycle(List<Map<String, JSONObject>> developersLifecycle, Boolean isAsc, int ps, int page);

    /**
     * 对code quality排序
     *
     * @param result result
     * @param asc    asc
     * @param ps     ps
     * @param page   page
     * @return 排序后的code quality
     */
    Map<String, Object> handleSortCodeQuality(List<Map<String, Object>> result, Boolean asc, int ps, int page);

    /**
     * 返回自己引入未解决缺陷数
     *
     * @param page         page
     * @param ps           ps
     * @param order        order
     * @param isAsc        isAsc
     * @param query        query
     * @param isPagination isPagination
     * @param producerList producerList
     * @return 自己引入未解决缺陷数
     */
    Object getSelfIntroducedLivingIssueCount(int page, int ps, String order, Boolean isAsc, Map<String, Object> query, Boolean isPagination, List<String> producerList);

    /**
     * get developer introduce issue top 5
     *
     * @param developer developer
     * @param order     order
     * @return issue top 5
     */
    List<IssueTopVO> getDeveloperIntroduceIssueTop5(String developer, String order);

    /**
     * 指定某些项目的留存缺陷数的趋势统计图数据
     *
     * @param since      since
     * @param until      until
     * @param projectIds 项目ids
     * @param interval   时间粒度
     * @param showDetail 是否展示细节
     * @return 指定某些项目的留存缺陷数的趋势统计图数据
     */
    Object getLivingIssueTendency(String since, String until, String projectIds, String interval, String showDetail);

    /**
     * get developer list living issue count
     *
     * @param since      since
     * @param until      until
     * @param repoUuids  repoUuids
     * @param developers developers
     * @param page       page
     * @param ps         ps
     * @param asc        asc
     * @return living issue count
     * @throws MeasureServiceException MeasureServiceException
     */
    PagedGridResult<DeveloperLivingIssueVO> getDeveloperListLivingIssue(String since, String until, List<String> repoUuids, List<String> developers, int page, int ps, Boolean asc) throws MeasureServiceException;
}
