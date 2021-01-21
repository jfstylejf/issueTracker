package cn.edu.fudan.issueservice.controller;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.domain.ResponseBean;
import cn.edu.fudan.issueservice.service.IssueMeasureInfoService;
import cn.edu.fudan.issueservice.util.DateTimeUtil;
import cn.edu.fudan.issueservice.util.PagedGridResult;
import cn.edu.fudan.issueservice.util.SegmentationUtil;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.*;

/**
 * description 代码度量--Issue数量相关controller
 * @author fancying
 * create 2019-04-08 16:55
 **/
@Api(value = "issue measurement", tags = {"用于统计issue数量的相关接口"})
@RestController
public class IssueMeasurementController {

    private IssueMeasureInfoService issueMeasureInfoService;

    private RestInterfaceManager restInterfaceManager;

    private final String success = "success", failed = "failed ";

    private final String TOKEN = "token", repos = "repoList", sinceStr = "since", untilStr = "until", developerStr = "developer";

    private final String timeError = "time format error", timeErrorMessage = "The input time format error,should be yyyy-MM-dd.";

    @ApiOperation(value = "获取issueTypeCounts", notes = "@return List<Map.Entry<String, JSONObject>>\n[\n" +
            "        {\n" +
            "            \"String literals should not be duplicated\": {\n" +
            "                \"Ignore\": 0,\n" +
            "                \"Misinformation\": 0,\n" +
            "                \"To_Review\": 0,\n" +
            "                \"Total\": 117,\n" +
            "                \"Default\": 117\n" +
            "            }\n" +
            "        }\n]", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tool", value = "工具名", dataType = "String", required = true, defaultValue = "sonarqube", allowableValues = "sonarqube"),
            @ApiImplicitParam(name = "repo_uuids", value = "多个库的uuid\n库uuid之间用英文逗号,作为分隔符", dataType = "String", required = true),
            @ApiImplicitParam(name = "order", value = "排序方式", dataType = "String", defaultValue = "Default", allowableValues = "Default, Total, Ignore, Misinformation, To_Review"),
            @ApiImplicitParam(name = "commit", value = "commit的uuid", dataType = "String"),
    })
    @GetMapping(value = "/measurement/issue-type-counts")
    public ResponseBean<List<Map.Entry<String, JSONObject>>> getIssueTypeCountsByToolAndRepoUuid(@RequestParam("repo_uuids") String repoUuids,
                                                                                     @RequestParam("tool") String category,
                                                                                     @RequestParam(value = "order", required = false, defaultValue = "Default") String order,
                                                                                     @RequestParam(value = "commit", required = false) String commitUuid) {
        List<String> repoList = SegmentationUtil.splitStringList(repoUuids);
        try{
            return new ResponseBean<>(200, success, issueMeasureInfoService.getNotSolvedIssueCountByToolAndRepoUuid(repoList, category, order, commitUuid));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(500, failed + e.getMessage(),null);
        }
    }

    @ApiOperation(value = "获取开发者日均解决缺陷数量", notes = "@return Map<String,Object>\n{\n" +
            "        \"dayAvgSolvedIssue\": 0.1,\n" +
            "        \"solvedIssuesCount\": 27,\n" +
            "        \"days\": 270.0\n" +
            "    }", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "developer", value = "开发人员姓名", required = true),
            @ApiImplicitParam(name = "repo_uuid", value = "代码库uuid", dataType = "String"),
            @ApiImplicitParam(name = "since", value = "起始时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "until", value = "终止时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "manual_status", value = "缺陷忽略类型", defaultValue = "Default", allowableValues = "Default, Total, Ignore, Misinformation, To_Review"),
            @ApiImplicitParam(name = "tool", value = "工具名", defaultValue = "sonarqube", allowableValues = "sonarqube")
    })
    @GetMapping(value = "/measurement/developer/day-avg-solved-issue")
    public ResponseBean<Map<String,Object>> getDayAvgSolvedIssue(@RequestParam(value = "developer") String developer,
                                            @RequestParam(value = "repo_uuid", required = false) String repoUuid,
                                            @RequestParam(value = "since", required = false) String since,
                                            @RequestParam(value = "until", required = false) String until,
                                            @RequestParam(value = "manual_status",required = false, defaultValue = "Default")String manualStatus,
                                            @RequestParam(value = "tool", required = false, defaultValue = "sonarqube") String tool,
                                            HttpServletRequest httpServletRequest) {
        if(timeError.equals(DateTimeUtil.timeFormatIsLegal(since, false)) || timeError.equals(DateTimeUtil.timeFormatIsLegal(until, true))){
            return new ResponseBean<>(400, timeErrorMessage, null);
        }

        Map<String, Object> query = new HashMap<>(10);

        query.put(repos, repoUuid == null ? null : new ArrayList<String>(){{add(repoUuid);}});
        query.put(developerStr, developer);
        query.put("tool", tool);
        query.put("manual_status", manualStatus);

        try {
            query.put(sinceStr, since != null ? since : restInterfaceManager.getFirstCommitDate(developer));
            query.put(untilStr, until != null ? until : LocalDate.now().toString());
            return new ResponseBean<>(200, success, issueMeasureInfoService.getDayAvgSolvedIssue(query, httpServletRequest.getHeader(TOKEN)));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(500, failed + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "获取开发者能力页面四类（谁引入、谁解决）issue接口", notes = "@return Object\n  [\n" +
            "        {\n" +
            "            \"yuping\": {\n" +
            "                \"quantity\": 0,\n" +
            "                \"min\": 0,\n" +
            "                \"max\": 0,\n" +
            "                \"mid\": 0\n" +
            "            }\n" +
            "        }\n]", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "developer", value = "开发人员姓名"),
            @ApiImplicitParam(name = "repo_uuids", value = "多个库的uuid\n库uuid之间用英文逗号,作为分隔符"),
            @ApiImplicitParam(name = "since", value = "起始时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "until", value = "终止时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "asc", value = "是否升序：1表示升序，0表示降序"),
            @ApiImplicitParam(name = "page", value = "页号", defaultValue = "1"),
            @ApiImplicitParam(name = "ps", value = "页大小\n范围0-100\n为0时只返回issue数量", defaultValue = "10"),
            @ApiImplicitParam(name = "status", value = "issue状态", allowableValues = "living , other_solved , self_solved"),
            @ApiImplicitParam(name = "percent", value = "-1返回数量\n-2返回详情\n不传默认-2", allowableValues = "-1 , -2"),
            @ApiImplicitParam(name = "target", value = "缺陷是谁引入\nself 自己引入,other 他人引入", allowableValues = "self , other"),
    })
    @GetMapping(value = "/codewisdom/issue/lifecycle")
    public ResponseBean<Object> getIssueLifecycleByConditions(HttpServletRequest request,@RequestParam(value = "developers", required = false) String developer,
                                               @RequestParam(value = "repo_uuids", required = false) String repoUuids,
                                               @RequestParam(value = "since", required = false) String since,
                                               @RequestParam(value = "until", required = false) String until,
                                               @RequestParam(value = "tool", required = false, defaultValue = "sonarqube") String tool,
                                               @RequestParam(value = "asc", required = false) Boolean isAsc,
                                               @RequestParam(value = "status") String status,
                                               @RequestParam(value = "percent", required = false, defaultValue = "-2") Double percent,
                                               @RequestParam(value = "target") String target,
                                               @RequestParam(value = "ps", required = false, defaultValue = "10") int ps,
                                               @RequestParam(value = "page", required = false, defaultValue = "1") int page) {
        double numberInfo = -2;
        //handle the requirement
        since = DateTimeUtil.timeFormatIsLegal(since, false);
        until = DateTimeUtil.timeFormatIsLegal(until, true);
        List<String> repoList = SegmentationUtil.splitStringList(repoUuids);
        //init query
        Map<String, Object> query = new HashMap<>(18);
        query.put(repos, repoList);
        query.put(sinceStr, since);
        query.put(untilStr, until);
        query.put("tool", tool);
        //check need detail or just number info
        try {
            if(percent == numberInfo) {
                List<Map<String, JSONObject>> developersLifecycle = new ArrayList<>();
                List<String> developers = isAsc != null ? restInterfaceManager.getDeveloperInRepo(repoUuids, since, until) : SegmentationUtil.splitStringList(developer);
                assert developers != null;
                developers.forEach(producer -> {
                    query.put("producer", producer);
                    developersLifecycle.add(new HashMap<String, JSONObject>(2) {{
                        put(producer, issueMeasureInfoService.getIssuesLifeCycle(status, target, query));
                    }});
                });
                return new ResponseBean<>(200, success, issueMeasureInfoService.handleSortDeveloperLifecycle(developersLifecycle, isAsc, ps, page));
            }
            query.put("producer", developer);
            query.put("ps", ps);
            query.put("start", (page - 1) * ps);
            String token = request.getHeader(TOKEN);
            return new ResponseBean<>(200, success, issueMeasureInfoService.getLifeCycleDetail(status, target, query, token));
        } catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(500, failed + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "开发者能力页面新增、消除、百行引入、百行消除静态缺陷接口", notes = "@return Map<String, Object>\n{\n" +
            "        \"addQuality\": 0.5444311156224048,\n" +
            "        \"loc\": 10837,\n" +
            "        \"solvedIssueCount\": 27,\n" +
            "        \"addedIssueCount\": 59,\n" +
            "        \"solveQuality\": 0.2491464427424564\n" +
            "    }", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "developers", value = "开发人员姓名"),
            @ApiImplicitParam(name = "repo_uuids", value = "代码库uuid\n支持多选\n以英文逗号,分隔"),
            @ApiImplicitParam(name = "manual_status", value = "缺陷忽略类型", defaultValue = "Default", allowableValues = "Default, Total, Ignore, Misinformation, To_Review"),
            @ApiImplicitParam(name = "since", value = "起始时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "until", value = "终止时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "tool", value = "工具名", allowableValues = "sonarqube", defaultValue = "sonarqube"),
            @ApiImplicitParam(name = "asc", value = "是否需要排序"),
            @ApiImplicitParam(name = "all", value = "是否需要all字段", defaultValue = "true")
    })
    @GetMapping(value = {"/codewisdom/issue/developer/code-quality"})
    public ResponseBean<Object> getDeveloperCodeQuality(@RequestParam(value = "repo_uuids",required = false)String repoList,
                                                @RequestParam(value = "developers",required = false)String developer,
                                                @RequestParam(value = "tool",required = false, defaultValue = "sonarqube")String tool,
                                                @RequestParam(value = "manual_status",required = false, defaultValue = "Default")String manualStatus,
                                                @RequestParam(value = "since",required = false)String since,
                                                @RequestParam(value = "until",required = false)String until,
                                                @RequestParam(value = "ps",required = false)Integer ps,
                                                @RequestParam(value = "page",required = false)Integer page,
                                                @RequestParam(value = "asc",required = false)Boolean asc,
                                                @RequestParam(value = "all",required = false ,defaultValue = "false")Boolean needAll,
                                                HttpServletRequest httpServletRequest){
        String token = httpServletRequest.getHeader(TOKEN);

        Map<String, Object> query = new HashMap<>(10);
        List<Map<String, Object>> result = new ArrayList<>();
        if(timeError.equals(DateTimeUtil.timeFormatIsLegal(since, false)) || timeError.equals(DateTimeUtil.timeFormatIsLegal(until, true))){
            return new ResponseBean<>(400, timeErrorMessage, null);
        }

        query.put(sinceStr, since);
        query.put(untilStr, until);
        query.put("tool", tool);
        query.put(repos, repoList);
        query.put("manual_status", manualStatus);

        try {
            if (asc != null) {
                List<String> developers = restInterfaceManager.getDeveloperInRepo(repoList, since, until);
                developers.forEach(r -> {
                    query.put(developerStr, r);
                    result.add(issueMeasureInfoService.getDeveloperCodeQuality(query, needAll, token));
                });
                return new ResponseBean<>(200, success, issueMeasureInfoService.handleSortCodeQuality(result, asc, ps, page));
            }

            List<String> developers = SegmentationUtil.splitStringList(developer);
            if(developers.isEmpty()){
                query.put(developerStr, developer);
                return new ResponseBean<>(200, success, issueMeasureInfoService.getDeveloperCodeQuality(query, needAll, token));
            }

            developers.forEach(r -> {
                query.put(developerStr, r);
                result.add(issueMeasureInfoService.getDeveloperCodeQuality(query, needAll, token));
            });
            return new ResponseBean<>(200, success, result);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(500, failed + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "开发者能力页面自己引入未解决缺陷数接口", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "developers", value = "开发人员姓名,支持多人查询"),
            @ApiImplicitParam(name = "repo_uuids", value = "代码库uuid\n支持多选\n以英文逗号,分隔"),
            @ApiImplicitParam(name = "since", value = "起始时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "until", value = "终止时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "page", value = "分页查询的第几页"),
            @ApiImplicitParam(name = "ps", value = "分页查询每页的大小"),
            @ApiImplicitParam(name = "order", value = "需要排序的字段"),
            @ApiImplicitParam(name = "asc", value = "是否升序：1表示升序，0表示降序"),
            @ApiImplicitParam(name = "tool", value = "工具名", allowableValues = "sonarqube", defaultValue = "sonarqube"),
    })
    @GetMapping(value = {"/codewisdom/issue/developer-data/living-issue-count/self"})
    public ResponseBean<Object> getDeveloperLivingIssueCount(@RequestParam(value = "repo_uuids",required = false)String repoUuids,
                                                                      @RequestParam(value = "developers",required = false)String developers,
                                                                      @RequestParam(value = "tool",required = false, defaultValue = "sonarqube")String tool,
                                                                      @RequestParam(value = "page",required = false, defaultValue = "1")int page,
                                                                      @RequestParam(value = "ps",required = false, defaultValue = "10")int ps,
                                                                      @RequestParam(value = "since",required = false)String since,
                                                                      @RequestParam(value = "until",required = false)String until,
                                                                      @RequestParam(value = "order", required = false, defaultValue = "livingIssueCount") String order,
                                                                      @RequestParam(value = "asc",required = false, defaultValue = "1") Boolean isAsc){

        Map<String, Object> query = new HashMap<>(10);

        String timeError = "time format error";
        if(timeError.equals(DateTimeUtil.timeFormatIsLegal(since, false)) || timeError.equals(DateTimeUtil.timeFormatIsLegal(until, true))){
            return new ResponseBean<>(400, "The input time format error,should be yyyy-MM-dd.", null);
        }
        if (StringUtils.isEmpty(until)) {
            until = DateTimeUtil.timeFormatIsLegal(until, true);
        }
        List<String> repoList = SegmentationUtil.splitStringList(repoUuids);
        List<String> producerList = (developers == null || developers.length() == 0) ? restInterfaceManager.getDeveloperInRepo(repoUuids, since, until) : SegmentationUtil.splitStringList(developers);

        query.put("since", since);
        query.put("until", until);
        query.put("producerList", producerList);
        query.put("tool", tool);
        query.put("repoList", repoList);
        query.put("order", order);
        query.put("asc", isAsc);

        // 是否做分页处理
        Boolean isPagination = developers == null || developers.length() == 0;
        try {
            return new ResponseBean<>(200, success, issueMeasureInfoService.getSelfIntroducedLivingIssueCount(page, ps, order, isAsc, query, isPagination));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(500, failed + e.getMessage(), null);
        }
    }


    @Autowired
    public void setIssueMeasureInfoService(IssueMeasureInfoService issueMeasureInfoService) {
        this.issueMeasureInfoService = issueMeasureInfoService;
    }

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }
}
