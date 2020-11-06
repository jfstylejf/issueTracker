
package cn.edu.fudan.issueservice.controller;

import cn.edu.fudan.issueservice.domain.ResponseBean;
import cn.edu.fudan.issueservice.domain.statistics.CodeQualityResponse;
import cn.edu.fudan.issueservice.service.IssueMeasureInfoService;
import cn.edu.fudan.issueservice.service.IssueRankService;
import cn.edu.fudan.issueservice.util.DateTimeUtil;
import cn.edu.fudan.issueservice.util.SegmentationUtil;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description 代码度量--Issue数量相关controller
 * @author fancying
 * create 2019-04-08 16:55
 **/
@Api(value = "issue measurement", tags = {"用于统计issue数量的相关接口"})
@RestController
public class IssueMeasurementController {

    private IssueMeasureInfoService issueMeasureInfoService;
    private IssueRankService issueRankService;

    @Autowired
    public void setIssueMeasureInfoService(IssueMeasureInfoService issueMeasureInfoService) {
        this.issueMeasureInfoService = issueMeasureInfoService;
    }

    @Autowired
    public void setIssueRankService(IssueRankService issueRankService) {
        this.issueRankService = issueRankService;
    }

    /**
     *获取某个文件相关的issue数据
     * use {@link IssueOuterController#filterIssues2}
     */
    @ApiOperation(value = "获取某个文件相关的issue数据", notes = "@return JSONObject", httpMethod = "GET")
    @GetMapping(value = {"/issue/file"})
    @Deprecated
    @ApiIgnore
    public ResponseBean<JSONObject> getIssueInfoOfSpecificFile(@RequestBody JSONObject jsonObject) {
        try {
            String repoId = jsonObject.getString ("repoId");
            String commitId = jsonObject.getString ("commitId");
            String tool = jsonObject.getString ("tool");
            String filePath = jsonObject.getString ("filePath");
            return new ResponseBean(200, "success!", issueMeasureInfoService.getIssueInfoOfSpecificFile(repoId, commitId, tool, filePath) );
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, e.getMessage(), null);
        }
    }


    /**
     * todo 需要根据用户权限来查看所有的repo
     * quantity
     * {@link IssueMeasurementController#getDeveloperCodeQuality}
     */
    @ApiOperation(value = "根据条件获取代码质量", notes = "@return Issue", httpMethod = "GET")
    @GetMapping(value = {"/measurement/code-quality"})
    @Deprecated
    @ApiIgnore
    public ResponseBean<CodeQualityResponse> getCodeQuality(@RequestParam(value = "developer",required = false) String developer,
                                                            @RequestParam(value = "timeGranularity", required = false) String timeGranularity,
                                                            @RequestParam(value = "since",required = false) String since,
                                                            @RequestParam(value = "until",required = false) String until,
                                                            @RequestParam(value = "repoId",required = false) String repoId,
                                                            @RequestParam(value = "tool",required = false, defaultValue = "sonarqube") String tool,
                                                            @RequestParam(value = "page",required = false,defaultValue = "1") int page,
                                                            @RequestParam(value = "ps",required = false,defaultValue = "10") int ps) {
        try {
            return new ResponseBean(200, "success!", issueMeasureInfoService.getQualityChangesByCondition(developer,timeGranularity,since,until,repoId,tool,page,ps) );
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, e.getMessage(), null);
        }
    }

    @ApiOperation(value = "获取issueTypeCounts", notes = "@return List <JSONObject>\n[\n" +
            "        {\n" +
            "            \"Total\": 189,\n" +
            "            \"Issue_Type\": \"String literals should not be duplicated\"\n" +
            "        }\n]", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tool", value = "工具名", dataType = "String", required = true, defaultValue = "sonarqube", allowableValues = "sonarqube"),
            @ApiImplicitParam(name = "repo_uuid", value = "代码库uuid", dataType = "String", required = true),
            @ApiImplicitParam(name = "commit", value = "commit的uuid", dataType = "String"),
    })
    @GetMapping(value = "/measurement/issue-type-counts")
    public ResponseBean<List<JSONObject>> getIssueTypeCountsByCategoryAndRepoId(@RequestParam("repo_uuid") String repoId,
                                                                                @RequestParam("tool") String category,
                                                                                @RequestParam(value="commit",required = false) String commitId) {
        try{
            return new ResponseBean<>(200,"success", issueMeasureInfoService.getNotSolvedIssueCountByCategoryAndRepoId(repoId, category,commitId));
        }catch (Exception e){
            return new ResponseBean<>(500, e.getMessage(),null);
        }
    }

    /**
     * use {@link IssueOuterController#filterIssues2}
     */
    @ApiOperation(value = "issue数量统计接口", notes = "根据条件统计相应的issue数量", httpMethod = "GET")
    @GetMapping(value = "/measurement/issueCount")
    @Deprecated
    @ApiIgnore
    public ResponseBean<Integer> getIssueCountByConditions(@RequestParam(value = "developer",required = false) String developer,
                                            @RequestParam(value = "repoId") String repoId,
                                            @RequestParam(value = "since",required = false) String since,
                                            @RequestParam(value = "until",required = false) String until,
                                            @RequestParam(value = "tool",required = false) String tool,
                                            @RequestParam(value = "general_category",required = false) String generalCategory) {

        try {
            return new ResponseBean(200, "success!", issueMeasureInfoService.getIssueCountByConditions(developer,repoId, since,until,tool,generalCategory));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "failed!" + e.getMessage(), null);
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
            @ApiImplicitParam(name = "tool", value = "工具名", defaultValue = "sonarqube", allowableValues = "sonarqube")
    })
    @GetMapping(value = "/measurement/developer/day-avg-solved-issue")
    public ResponseBean<Map<String,Object>> getDayAvgSolvedIssue(@RequestParam(value = "developer") String developer,
                                            @RequestParam(value = "repo_uuid",required = false) String repoId,
                                            @RequestParam(value = "since",required = false) String since,
                                            @RequestParam(value = "until",required = false) String until,
                                            @RequestParam(value = "tool",required = false) String tool) {

        try {
            return new ResponseBean(200, "success!", issueMeasureInfoService.getDayAvgSolvedIssue(developer,repoId, since,until,tool));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "failed!" + e.getMessage(), null);
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

        try {
            return new ResponseBean(200, "success!", issueMeasureInfoService.getIssueLifecycle(developer, repoIdList, since, until,
                    tool, status, percent, type, target));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "failed: " + e.getMessage(), Collections.emptyList());
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
            @ApiImplicitParam(name = "since", value = "起始时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "until", value = "终止时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "tool", value = "工具名", allowableValues = "sonarqube", defaultValue = "sonarqube"),
    })
    @GetMapping(value = {"/codewisdom/issue/developer/code-quality"})
    public ResponseBean<Map<String, JSONObject>> getDeveloperCodeQuality(@RequestParam(value = "repo_uuids",required = false)String repoList,
                                                @RequestParam(value = "developer",required = false)String developer,
                                                @RequestParam(value = "tool",required = false,defaultValue = "sonarqube")String tool,
                                                @RequestParam(value = "since",required = false)String since,
                                                @RequestParam(value = "until",required = false)String until){

        Map<String, Object> query = new HashMap<>(10);

        query.put("since", DateTimeUtil.timeFormatIsLegal(since, false));
        query.put("until", DateTimeUtil.timeFormatIsLegal(until, true));
        query.put("developer", developer);
        query.put("tool", tool);
        query.put("repoList", repoList);

        try {
            //fixme end_commit 改为 solve_commit 有误差
            return new ResponseBean(200, "success!", issueMeasureInfoService.getDeveloperCodeQuality(query));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "failed!" + e.getMessage(), null);
        }
    }

    /**
     * use {@link IssueOuterController#filterIssues2}
     */
    @Deprecated
    @ApiIgnore
    @ApiOperation(value = "获取开发者open或solved缺陷数量", notes = "@return Map<String, Integer>", httpMethod = "GET")
    @GetMapping(value = {"/codewisdom/issue/developer/quantity"})
    public ResponseBean<Map<String, Integer>> getDeveloperQuantity(@RequestParam(value = "repo-id-list",required = false)String repoIdList,
                                                @RequestParam(value = "tool",required = false,defaultValue = "sonarqube")String tool,
                                                @RequestParam(value = "status",required = false)String status,
                                                @RequestParam(value = "is-add",required = false,defaultValue = "true")Boolean isAdd,
                                                @RequestParam(value = "since",required = false)String since,
                                                @RequestParam(value = "until",required = false)String until){
        try {
            return new ResponseBean(200, "success!", issueMeasureInfoService.getDeveloperQuantity(repoIdList, tool, status, isAdd, since, until));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "failed!" + e.getMessage(), null);
        }
    }
}
