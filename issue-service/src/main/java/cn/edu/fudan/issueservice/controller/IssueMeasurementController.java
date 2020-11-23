
package cn.edu.fudan.issueservice.controller;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.domain.ResponseBean;
import cn.edu.fudan.issueservice.service.IssueMeasureInfoService;
import cn.edu.fudan.issueservice.util.DateTimeUtil;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

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

    private final String success = "success";

    private final String failed = "failed\n";

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
            @ApiImplicitParam(name = "repo_uuid", value = "代码库uuid", dataType = "String", required = true),
            @ApiImplicitParam(name = "order", value = "排序方式", dataType = "String", defaultValue = "Default", allowableValues = "Default, Total, Ignore, Misinformation, To_Review"),
            @ApiImplicitParam(name = "commit", value = "commit的uuid", dataType = "String"),
    })
    @GetMapping(value = "/measurement/issue-type-counts")
    public ResponseBean<List<Map.Entry<String, JSONObject>>> getIssueTypeCountsByToolAndRepoUuid(@RequestParam("repo_uuid") String repoUuid,
                                                                                     @RequestParam("tool") String category,
                                                                                     @RequestParam(value = "order", required = false, defaultValue = "Default") String order,
                                                                                     @RequestParam(value = "commit", required = false) String commitUuid) {
        try{
            return new ResponseBean<>(200, success, issueMeasureInfoService.getNotSolvedIssueCountByToolAndRepoUuid(repoUuid, category, order, commitUuid));
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
                                            @RequestParam(value = "tool", required = false, defaultValue = "sonarqube") String tool) {
        String timeError = "time format error";
        if(timeError.equals(DateTimeUtil.timeFormatIsLegal(since, false)) || timeError.equals(DateTimeUtil.timeFormatIsLegal(until, true))){
            return new ResponseBean<>(400, "The input time format error,should be yyyy-MM-dd.", null);
        }

        Map<String, Object> query = new HashMap<>(10);

        query.put("repoList", repoUuid == null ? null : new ArrayList<String>(){{add(repoUuid);}});
        query.put("developer", developer);
        query.put("tool", tool);
        query.put("manual_status", manualStatus);

        try {
            query.put("since", since != null ? since : restInterfaceManager.getFirstCommitDate(developer));
            query.put("until", until != null ? until : LocalDate.now().toString());
            return new ResponseBean<>(200, success, issueMeasureInfoService.getDayAvgSolvedIssue(query));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(401, failed + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "获取开发者能力页面四类（谁引入、谁解决）issue接口", notes = "@return Object\n {\n" +
            "        \"min\": 0.0,\n" +
            "        \"avg\": 156.52678571428572,\n" +
            "        \"upperQuartile\": 9.0,\n" +
            "        \"quantity\": 112.0,\n" +
            "        \"max\": 435.0,\n" +
            "        \"multiple\": 346.0,\n" +
            "        \"mid\": 88.0,\n" +
            "        \"lowerQuartile\": 346.0\n" +
            "    }", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "author", value = "开发人员姓名"),
            @ApiImplicitParam(name = "repo_uuids", value = "多个库的uuid\n库uuid之间用英文逗号,作为分隔符"),
            @ApiImplicitParam(name = "since", value = "起始时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "until", value = "终止时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "status", value = "issue状态\n不传默认all", allowableValues = "all , Open , Solved"),
            @ApiImplicitParam(name = "percent", value = "-1返回数量\n-2返回详情\n不传默认-2", allowableValues = "-1 , -2"),
            @ApiImplicitParam(name = "type", value = "issue的类型"),
            @ApiImplicitParam(name = "target", value = "缺陷时谁引入\nself 自己引入,other 他人引入", defaultValue = "all", allowableValues = "self , other , all"),
    })
    @GetMapping(value = "/codewisdom/issue/lifecycle")
    public ResponseBean<Object> getIssueLifecycleByConditions(@RequestParam(value = "author",required = false) String developer,
                                               @RequestParam(value = "repo_uuids",required = false) String repoIdList,
                                               @RequestParam(value = "since",required = false) String since,
                                               @RequestParam(value = "until",required = false) String until,
                                               @RequestParam(value = "tool",required = false) String tool,
                                               @RequestParam(value = "status",required = false,defaultValue = "all") String status,
                                               @RequestParam(value = "percent",required = false,defaultValue = "-2") Double percent,
                                               @RequestParam(value = "type",required = false) String type,
                                               @RequestParam(value = "target",required = false,defaultValue = "all") String target) {

        since = DateTimeUtil.timeFormatIsLegal(since, false);
        until = DateTimeUtil.timeFormatIsLegal(until, true);

        //repoList是最后sql中查询用的repoId列表
        List<String> repoList = new ArrayList<>();
        if(StringUtils.isEmpty(repoIdList)) {
            repoList = null;
        }else {
            String[] repoIdArray = repoIdList.split(",");
            //先把前端给的repo加入到repoList
            repoList.addAll(Arrays.asList(repoIdArray));
        }

        try {
            return new ResponseBean<>(200, success, issueMeasureInfoService.getIssueLifecycle(developer, repoList, since, until, tool, status, percent, type, target));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(401, failed + e.getMessage(), Collections.emptyList());
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
            @ApiImplicitParam(name = "developer", value = "开发人员姓名"),
            @ApiImplicitParam(name = "repo_uuids", value = "代码库uuid\n支持多选\n以英文逗号,分隔"),
            @ApiImplicitParam(name = "manual_status", value = "缺陷忽略类型", defaultValue = "Default", allowableValues = "Default, Total, Ignore, Misinformation, To_Review"),
            @ApiImplicitParam(name = "since", value = "起始时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "until", value = "终止时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "tool", value = "工具名", allowableValues = "sonarqube", defaultValue = "sonarqube"),
    })
    @GetMapping(value = {"/codewisdom/issue/developer/code-quality"})
    public ResponseBean<Map<String, JSONObject>> getDeveloperCodeQuality(@RequestParam(value = "repo_uuids",required = false)String repoList,
                                                @RequestParam(value = "developer",required = false)String developer,
                                                @RequestParam(value = "tool",required = false, defaultValue = "sonarqube")String tool,
                                                @RequestParam(value = "manual_status",required = false, defaultValue = "Default")String manualStatus,
                                                @RequestParam(value = "since",required = false)String since,
                                                @RequestParam(value = "until",required = false)String until){

        Map<String, Object> query = new HashMap<>(10);

        query.put("since", DateTimeUtil.timeFormatIsLegal(since, false));
        query.put("until", DateTimeUtil.timeFormatIsLegal(until, true));
        query.put("developer", developer);
        query.put("tool", tool);
        query.put("repoList", repoList);
        query.put("manual_status", manualStatus);

        try {
            //fixme end_commit 改为 solve_commit 有误差
            return new ResponseBean<>(200, success, issueMeasureInfoService.getDeveloperCodeQuality(query));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(401, failed + e.getMessage(), null);
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
