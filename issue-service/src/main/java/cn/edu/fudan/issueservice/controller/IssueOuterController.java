package cn.edu.fudan.issueservice.controller;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.domain.ResponseBean;
import cn.edu.fudan.issueservice.domain.enums.JavaIssuePriorityEnum;
import cn.edu.fudan.issueservice.exception.UrlException;
import cn.edu.fudan.issueservice.service.IssueService;
import cn.edu.fudan.issueservice.util.DateTimeUtil;
import cn.edu.fudan.issueservice.util.ExcelUtil;
import cn.edu.fudan.issueservice.util.SegmentationUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author Beethoven
 */
@Api(value = "issue outer", tags = {"用于请求issue信息的相关接口"})
@RestController
public class IssueOuterController {

    private IssueService issueService;

    private RestInterfaceManager restInterfaceManager;

    private final String success = "success";

    private final String failed = "failed ";

    private final String TOKEN = "token";

    private final String timeError = "time format error";

    @ApiOperation(value = "根据缺陷扫描工具获取该工具下扫描的所有的issue类型", notes = "@return  List < String > ", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tool", value = "工具名", defaultValue = "sonarqube", allowableValues = "sonarqube")
    })
    @GetMapping(value = {"/issue/issue-types"})
    public ResponseBean<List<String>> getExistIssueTypes(@RequestParam(name = "tool",defaultValue = "sonarqube")String tool) {
        try{
            return new ResponseBean<>(200, success, issueService.getExistIssueTypes(tool));
        }catch (Exception e){
            return new ResponseBean<>(500, failed + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "获取issue紧急程度的类型", notes = "@return  List < String >", httpMethod = "GET")
    @GetMapping(value = {"/issue/issue-severities"})
    public ResponseBean<List<String>> getIssueSeverities() {
        try{
            return new ResponseBean<>(200, success, issueService.getIssueSeverities());
        }catch (Exception e){
            return new ResponseBean<>(500, failed + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "获取缺陷的状态列表", notes = "@return List < String >", httpMethod = "GET")
    @GetMapping(value = {"/issue/issue-status"})
    public ResponseBean<List<String>> getIssueStatus() {
        try{
            return new ResponseBean<>(200, success, issueService.getIssueStatus());
        }catch (Exception e){
            return new ResponseBean<>(500, failed + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "返回开发者参与并且有引入过issue的repoUuid", notes = "@return Map<String, List<Map<String, String>>>\n" +
            "{\n\"项目名\": [\n" +
            "            {\n" +
            "                \"repoUuid\": \"3ecf804e-0ad6-11eb-bb79-5b7ba969027e\",\n" +
            "                \"repoName\": \"/IssueTracker-Master\",\n" +
            "                \"projectName\": \"项目名\",\n" +
            "                \"branch\": \"zhonghui20191012\"\n" +
            "            }\n" +
            "        ]\n}", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "developer", value = "开发者姓名", required = true)
    })
    @GetMapping(value = {"/issue/developer/repo-with-issues"})
    public ResponseBean<Map<String, List<Map<String, String>>>> getRepoWithIssues(@RequestParam(name = "developer")String developer) {
        try {
            return new ResponseBean<>(200, success, issueService.getRepoWithIssues(developer));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(500, failed + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "根据条件获取issue列表", notes = "@return Map<String, Object>\n{\n\"total\": 59,\n" +
            "        \"totalPage\": 59,\n\"issueList\": [{}],\n\"issueListSortByType\":[\"\":{}]\n}", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "project_name", value = "项目名"),
            @ApiImplicitParam(name = "repo_uuids", value = "多个库的uuid\n库uuid之间用英文逗号,作为分隔符"),
            @ApiImplicitParam(name = "since", value = "起始时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "until", value = "终止时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "tool", value = "工具名", defaultValue = "sonarqube", allowableValues = "sonarqube"),
            @ApiImplicitParam(name = "types", value = "issue类型\n传null默认查询所有类型"),
            @ApiImplicitParam(name = "page", value = "页号", defaultValue = "1"),
            @ApiImplicitParam(name = "ps", value = "页大小\n范围0-100\n为0时只返回issue数量", defaultValue = "10"),
            @ApiImplicitParam(name = "status", value = "issue状态\n传null默认查询所有状态", allowableValues = "Open , Solved , Misinformation , To_Review , Ignore"),
            @ApiImplicitParam(name = "introducer", value = "issue引入者\n传null默认查询所有引入者"),
            @ApiImplicitParam(name = "solver", value = "issue解决者\n传null默认查询所有解决者"),
            @ApiImplicitParam(name = "category", value = "issue种类", allowableValues = "Code smell , Bug"),
            @ApiImplicitParam(name = "priority", value = "优先级", allowableValues = "Low , Urgent , Normal , High , Immediate"),
            @ApiImplicitParam(name = "file_paths", value = "文件路径\n支持多个文件筛选\n用英文逗号,作为分隔符"),
            @ApiImplicitParam(name = "url", value = "代码库url\n如果传url优先按url所指的代码库查询issue信息"),
            @ApiImplicitParam(name = "commit", value = "指定commit版本\n如果传null默认显示最新commit"),
            @ApiImplicitParam(name = "detail", value = "是否显示issue的详情\n默认不显示", allowableValues = "true , false"),
            @ApiImplicitParam(name = "asc", value = "是否升序\n默认false", allowableValues = "true , false"),
            @ApiImplicitParam(name = "order", value = "排序方式\n默认为no,不需要排序", allowableValues = "no , quantity , open , solved"),
            @ApiImplicitParam(name = "issue_uuids", value = "多个issue的uuid\n用英文逗号,作为分隔符"),
            @ApiImplicitParam(name = "manual_status", value = "缺陷是否被忽略", defaultValue = "Default", allowableValues = "Ignore , Misinformation , To_Review , Default")
    })
    @GetMapping(value = {"/issue/filter"})
    public ResponseBean<Map<String, Object>> filterIssues(HttpServletRequest request, @RequestParam(value = "project_name",required = false) String projectName,
                                                           @RequestParam(value = "repo_uuids", required = false) String repoUuids,
                                                           @RequestParam(value = "since",required = false) String since,
                                                           @RequestParam(value = "until",required = false) String until,
                                                           @RequestParam(value = "tool",required = false, defaultValue = "sonarqube") String toolName,
                                                           @RequestParam(value = "types",required = false) String type,
                                                           @RequestParam(value = "page",required = false,defaultValue = "1") int page,
                                                           @RequestParam(value = "ps",required = false,defaultValue = "10") int ps,
                                                           @RequestParam(value = "status",required = false) String status,
                                                           @RequestParam(value = "introducer",required = false) String introducer,
                                                           @RequestParam(value = "solver",required = false) String solver,
                                                           @RequestParam(value = "category",required = false) String category,
                                                           @RequestParam(value = "priority",required = false) String priority,
                                                           @RequestParam(value = "file_paths",required = false) String filesPath,
                                                           @RequestParam(value = "url", required = false) String url,
                                                           @RequestParam(value = "commit", required = false) String commit,
                                                           @RequestParam(value = "detail", required = false, defaultValue = "false") Boolean detail,
                                                           @RequestParam(value = "asc", required = false, defaultValue = "false") Boolean asc,
                                                           @RequestParam(value = "order", required = false, defaultValue = "no") String order,
                                                           @RequestParam(value = "issue_uuids", required = false) String issueUuids,
                                                           @RequestParam(value = "manual_status", required = false, defaultValue = "Default") String manualStatus) {
        String userToken = request.getHeader(TOKEN);

        Map<String, Object> query = new HashMap<>(32);

        if(ps < 0 || ps > 100){
            return new ResponseBean<>(400, failed + "page size should in [0,100]!", null);
        }

        if(timeError.equals(DateTimeUtil.timeFormatIsLegal(since, false)) || timeError.equals(DateTimeUtil.timeFormatIsLegal(until, true))){
            return new ResponseBean<>(400, failed + "The input time format error,should be yyyy-MM-dd.", null);
        }

        if(!StringUtils.isEmpty(priority)){
            query.put("priority", Objects.requireNonNull(JavaIssuePriorityEnum.getPriorityEnum(priority)).getRank());
        }

        //根据url、projectName和reposUuid获得 repoUuid列表
        try {
            List<String> repoList = getRepoListByUrlProjectNameRepoUuids(url, projectName, repoUuids, userToken);
            query.put("repoList", repoList);
        } catch (UrlException e) {
            return new ResponseBean<>(400, failed + e.getMessage(), null);
        }
        //处理查询条件
        String[] queryName = {"status", "filesPath", "issueUuids"};
        String[] spiltStrings = {status, filesPath, issueUuids};
        SegmentationUtil.splitString(queryName, spiltStrings, query);
        if(!StringUtils.isEmpty(type)){
            query.put("types", new ArrayList<String>(){{add(type);}});
        }
        query.put("since", since);
        query.put("until", until);
        query.put("category", category);
        query.put("toolName", toolName);
        query.put("developer", introducer);
        query.put("commit", commit);
        query.put("start", (page - 1) * ps);
        query.put("solver", solver);
        query.put("ps", ps);
        query.put("asc", asc);
        query.put("detail", detail);
        query.put("order", order);
        query.put("manual_status", manualStatus);
        //fixme end_commit 改为 solve_commit 有误差
        //step1 ps = 0 only return total(because fetch time --)  or  ps != 0 do select;
        Map<String, Object> issueFilterList = issueService.getIssueFilterListCount(query);
        if(ps == 0){
            return new ResponseBean<>(200, success, issueFilterList);
        }
        //step2 select issueList (always(since,until,status,types,filesPath,repoList,priority,toolName,start,ps,category) and
        //                        options(commit ? do select commit : pass)(solver ? select introducer and solver : select introducer))
        issueFilterList = issueService.getIssueFilterList(query, issueFilterList);
        //step3 final check detail
        issueFilterList = issueService.getIssueFilterListWithDetail(query, issueFilterList);
        //step3 final check asc and order,do this need much time.
        issueFilterList = issueService.getIssueFilterListWithOrder(query, issueFilterList);

        return new ResponseBean<>(200, success, issueFilterList);
    }

    private List<String> getRepoListByUrlProjectNameRepoUuids(String url, String projectName, String repoUuids, String userToken) throws UrlException {

        List<String> repoList = new ArrayList<>();

        if(url != null && url.length() > 0) {
            String repoUuid= restInterfaceManager.getRepoUuidByUrl(url, userToken);
            if(repoUuid == null){
                throw new UrlException("url input error!");
            }
            repoList.add(repoUuid);
            return repoList;
        }

        if(!StringUtils.isEmpty(repoUuids)){
            repoList = Arrays.asList(repoUuids.split(","));
            return repoList;
        }

        if (!StringUtils.isEmpty(projectName)) {
            JSONArray repo = restInterfaceManager.getAllRepo(userToken).getJSONArray(projectName);
            for (int i = 0; i < repo.size(); i++) {
                String tempRepo = repo.getJSONObject(i).getString("repo_id");
                repoList.add(tempRepo);
            }
            return repoList;
        }

        JSONObject allProject = restInterfaceManager.getAllRepo(userToken);
        for (String str : allProject.keySet()) {
            JSONArray repo = allProject.getJSONArray(str);
            for (int i = 0; i < repo.size(); i++) {
                String tempRepo = repo.getJSONObject(i).getString("repo_id");
                repoList.add(tempRepo);
            }
        }

        return repoList;
    }

    @ApiOperation(value = "下载缺陷总览excel", notes = "@return issues.xls", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "project_name", value = "项目名"),
            @ApiImplicitParam(name = "repo_uuids", value = "多个库的uuid\n库uuid之间用英文逗号,作为分隔符"),
            @ApiImplicitParam(name = "since", value = "起始时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "until", value = "终止时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "tool", value = "工具名", defaultValue = "sonarqube", allowableValues = "sonarqube"),
            @ApiImplicitParam(name = "status", value = "issue状态\n传null默认查询所有状态", allowableValues = "Open , Solved , Misinformation , To_Review , Ignore"),
            @ApiImplicitParam(name = "introducer", value = "issue引入者\n传null默认查询所有引入者"),
            @ApiImplicitParam(name = "priority", value = "优先级", allowableValues = "Low , Urgent , Normal , High , Immediate"),
    })
    @GetMapping("/issue/filter/download")
    @ResponseBody
    public void downloadExcel(HttpServletRequest request, @RequestParam(value = "project_name",required = false) String projectName,
                              @RequestParam(value = "repo_uuids", required = false) String repoUuids,
                              @RequestParam(value = "tool",required = false, defaultValue = "sonarqube") String toolName,
                              @RequestParam(value = "since", required = false) String since,
                              @RequestParam(value = "until", required = false) String until,
                              @RequestParam(value = "introducer", required = false) String introducer,
                              @RequestParam(value = "status", required = false) String status,
                              @RequestParam(value = "priority", required = false) String priority, HttpServletResponse response) {

        Map<String, Object> query = new HashMap<>(16);

        String userToken = request.getHeader(TOKEN);
        Map<String, String> allRepoToRepoName = restInterfaceManager.getAllRepoToRepoName(userToken);

        try {
            List<String> repoList = getRepoListByUrlProjectNameRepoUuids(null, projectName, repoUuids, userToken);
            query.put("repoList", repoList);
        } catch (UrlException e) {
            e.printStackTrace();
            return;
        }

        if(timeError.equals(DateTimeUtil.timeFormatIsLegal(since, false)) || timeError.equals(DateTimeUtil.timeFormatIsLegal(until, true))){
            return;
        }

        String[] queryName = {"status"};
        String[] spiltStrings = {status};
        SegmentationUtil.splitString(queryName, spiltStrings, query);

        query.put("since", since);
        query.put("until", until);
        query.put("toolName", toolName);
        query.put("introducer", introducer);
        query.put("priority", priority);

        Map<String, Object> issueFilterList = issueService.getIssueFilterListCount(query);
        issueFilterList = issueService.getIssueFilterList(query, issueFilterList);

        response.setHeader("content-type", "application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + "issues.xls");

        try (HSSFWorkbook workbook = ExcelUtil.exportExcel(issueFilterList, allRepoToRepoName)){
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ApiOperation(value = "项目详情页面的issueCount数据图的接口", notes = "@return List<Map<String, Object>>\n[\n" +
            "        {\n" +
            "            \"date\": \"2020-06-10\",\n" +
            "            \"newIssueCount\": 0,\n" +
            "            \"eliminatedIssueCount\": 0,\n" +
            "            \"remainingIssueCount\": \"176\"\n" +
            "        }\n]", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "since", value = "起始时间\n格式要求: yyyy-MM-dd", required = true),
            @ApiImplicitParam(name = "until", value = "终止时间\n格式要求: yyyy-MM-dd", required = true),
            @ApiImplicitParam(name = "repo_uuids", value = "多个库的uuid\n库uuid之间用英文逗号,作为分隔符", required = true),
            @ApiImplicitParam(name = "tool", value = "工具名", defaultValue = "sonarqube", allowableValues = "sonarqube"),
    })
    @GetMapping(value = {"/issue/repository/issue-count"})
    public ResponseBean<List<Map<String, Object>>> getNewTrend(@RequestParam("repo_uuids")String repoUuids,
                              @RequestParam("since")String since,
                              @RequestParam("until")String until,
                              @RequestParam(name="tool", required = false, defaultValue = "sonarqube")String tool) {
        List<String> repoList = SegmentationUtil.splitStringList(repoUuids);
        try{
            return new ResponseBean<>(200,success, issueService.getRepoIssueCounts(repoList, since, until, tool));
        }catch (Exception e){
            return new ResponseBean<>(500, failed + e.getMessage(),null);
        }
    }

    @ApiOperation(value = "修改Issue的优先级", notes = "@return null", httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "issue-uuid", value = "issue的uuid", required = true),
            @ApiImplicitParam(name = "priority", value = "issue的优先级", required = true, allowableValues = "Low , Urgent , Normal , High , Immediate"),
    })
    @PutMapping(value = "/issue/priority/{issue-uuid}")
    public ResponseBean<Object> updatePriority(@PathVariable("issue-uuid")String issueUuid, @RequestParam ("priority")String priority,HttpServletRequest request) {
        try {
            String userToken = request.getHeader(TOKEN);
            issueService.updatePriority(issueUuid,priority,userToken);
            return new ResponseBean<>(200, success, "issues update priority success!");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(500, failed + e.getMessage(), "issues update priority failed!");
        }
    }

    @ApiOperation(value = "修改Issue的状态", notes = "@return null", httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "issue-uuid", value = "issue的uuid", required = true),
            @ApiImplicitParam(name = "status", value = "issue状态", required = true, allowableValues = "Open , Solved"),
    })
    @PutMapping(value = "/issue/status/{issue-uuid}")
    public ResponseBean<Object> updateStatus(@PathVariable("issue-uuid")String issueUuid, @RequestParam ("status")String status,HttpServletRequest request) {
        try {
            String userToken = request.getHeader(TOKEN);
            issueService.updateStatus(issueUuid,status,userToken);
            return new ResponseBean<>(200, success, "issues update status success!");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(500, failed + e.getMessage(), "issues update status failed!");
        }
    }

    @ApiOperation(value = "获取所有引入过缺陷的开发者姓名列表", notes = "@return  List < String >", httpMethod = "GET")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "repo_uuids", value = "代码库uuid\n支持多库筛选以英文,分隔\n不传默认全部库")
    })
    @GetMapping(value = {"/issue/issue-introducers"})
    public ResponseBean<List<String>> getIssueIntroducers(@RequestParam(value = "repo_uuids", required = false)String repoUuids) {

        List<String> repoUuidList = SegmentationUtil.splitStringList(repoUuids);

        try {
            return new ResponseBean<>(200, success, issueService.getIssueIntroducers(repoUuidList));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(500, failed + e.getMessage(), null);
        }
    }

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    @Autowired
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }
}
