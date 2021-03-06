package cn.edu.fudan.issueservice.controller;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.domain.ResponseBean;
import cn.edu.fudan.issueservice.domain.enums.IssuePriorityEnums.*;
import cn.edu.fudan.issueservice.domain.enums.ToolEnum;
import cn.edu.fudan.issueservice.domain.vo.IssueFilterInfoVO;
import cn.edu.fudan.issueservice.domain.vo.IssueFilterSidebarVO;
import cn.edu.fudan.issueservice.exception.UrlException;
import cn.edu.fudan.issueservice.service.IssueService;
import cn.edu.fudan.issueservice.util.DateTimeUtil;
import cn.edu.fudan.issueservice.util.ExcelUtil;
import cn.edu.fudan.issueservice.util.StringsUtil;
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

    private static final String SUCCESS = "success";
    private static final String FAILED = "failed ";
    private static final String TOKEN = "token";
    private static final String TIME_FORMAT_ERROR = "time format error";
    private static final String SINCE = "since";
    private static final String UNTIL = "until";
    private static final String STATUS = "status";
    private static final String REPO_LIST = "repoList";
    private static final String PRIORITY = "priority";
    private static final String TOOLS = "tools";
    private static final String MANUAL_STATUS = "manualStatus";
    private static final String PRODUCER = "producer";
    private static final String UUID = "uuid";

    @ApiOperation(value = "根据缺陷扫描工具获取该工具下扫描的所有的issue类型", notes = "@return  List < String > ", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tool", value = "工具名", defaultValue = "sonarqube", allowableValues = "sonarqube , ESLint")
    })
    @GetMapping(value = {"/issue/issue-types"})
    public ResponseBean<List<String>> getExistIssueTypes(@RequestParam(name = "tool", defaultValue = "sonarqube") String tool) {
        if (!ToolEnum.toolIsLegal(tool)) {
            return new ResponseBean<>(400, FAILED + "tool is illegal!", null);
        }
        try {
            return new ResponseBean<>(200, SUCCESS, issueService.getExistIssueTypes(tool));
        } catch (Exception e) {
            return new ResponseBean<>(500, FAILED + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "获取issue紧急程度的类型", notes = "@return  List < String >", httpMethod = "GET")
    @GetMapping(value = {"/issue/issue-severities"})
    public ResponseBean<List<String>> getIssueSeverities() {
        try {
            return new ResponseBean<>(200, SUCCESS, issueService.getIssueSeverities());
        } catch (Exception e) {
            return new ResponseBean<>(500, FAILED + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "获取缺陷的状态列表", notes = "@return List < String >", httpMethod = "GET")
    @GetMapping(value = {"/issue/issue-status"})
    public ResponseBean<List<String>> getIssueStatus() {
        try {
            return new ResponseBean<>(200, SUCCESS, issueService.getIssueStatus());
        } catch (Exception e) {
            return new ResponseBean<>(500, FAILED + e.getMessage(), null);
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
    @GetMapping(value = "/issue/developer/repo-with-issues")
    public ResponseBean<Map<String, List<Map<String, String>>>> getRepoWithIssues(@RequestParam(value = "developer") String developer) {
        try {
            return new ResponseBean<>(200, SUCCESS, issueService.getRepoWithIssues(developer));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(500, FAILED + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "根据条件获取issue列表", notes = "@return Map<String, Object>\n{\n\"total\": 59,\n" +
            "        \"totalPage\": 59,\n\"issueList\": [{}],\n\"issueListSortByType\":[\"\":{}]\n}", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "project_name", value = "项目名\n支持多项目用英文逗号,作为分隔符"),
            @ApiImplicitParam(name = "repo_uuids", value = "多个库的uuid\n库uuid之间用英文逗号,作为分隔符"),
            @ApiImplicitParam(name = "since", value = "起始时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "until", value = "终止时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "tool", value = "工具名\n默认查全部工具", allowableValues = "sonarqube , ESLint"),
            @ApiImplicitParam(name = "types", value = "issue类型\n传null默认查询所有类型"),
            @ApiImplicitParam(name = "page", value = "页号", defaultValue = "1"),
            @ApiImplicitParam(name = "ps", value = "页大小\n范围0-100\n为0时只返回issue数量", defaultValue = "10"),
            @ApiImplicitParam(name = "status", value = "issue状态\n传null默认查询所有状态", allowableValues = "Open , Solved , Misinformation , To_Review , Ignore"),
            @ApiImplicitParam(name = "introducer", value = "issue引入者\n传null默认查询所有引入者\n支持多选用英文逗号,作为分隔符"),
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
    public ResponseBean<Map<String, Object>> filterIssues(HttpServletRequest request, @RequestParam(value = "project_names", required = false) String projectNames, @RequestParam(value = "repo_uuids", required = false) String repoUuids,
                                                          @RequestParam(value = "since", required = false) String since, @RequestParam(value = "until", required = false) String until,
                                                          @RequestParam(value = "tool", required = false) String toolName, @RequestParam(value = "types", required = false) String type,
                                                          @RequestParam(value = "page", required = false, defaultValue = "1") int page, @RequestParam(value = "ps", required = false, defaultValue = "10") int ps,
                                                          @RequestParam(value = "status", required = false) String status, @RequestParam(value = "introducer", required = false) String introducers,
                                                          @RequestParam(value = "solver", required = false) String solver, @RequestParam(value = "category", required = false) String category,
                                                          @RequestParam(value = "priority", required = false) String priority, @RequestParam(value = "file_paths", required = false) String filesPath,
                                                          @RequestParam(value = "url", required = false) String url, @RequestParam(value = "commit", required = false) String commit,
                                                          @RequestParam(value = "detail", required = false, defaultValue = "false") Boolean detail, @RequestParam(value = "asc", required = false, defaultValue = "false") Boolean asc,
                                                          @RequestParam(value = "order", required = false, defaultValue = "no") String order, @RequestParam(value = "issue_uuids", required = false) String issueUuids,
                                                          @RequestParam(value = "manual_status", required = false, defaultValue = "Default") String manualStatus) {
        String userToken = request.getHeader(TOKEN);

        Map<String, Object> query = new HashMap<>(32);

        if (ps < 0 || ps > 100) {
            return new ResponseBean<>(400, FAILED + "page size should in [0,100]!", null);
        }

        if (TIME_FORMAT_ERROR.equals(DateTimeUtil.timeFormatIsLegal(since, false)) || TIME_FORMAT_ERROR.equals(DateTimeUtil.timeFormatIsLegal(until, true))) {
            return new ResponseBean<>(400, FAILED + "The input time format error,should be yyyy-MM-dd.", null);
        }

        if (!StringUtils.isEmpty(priority)) {
            query.put(PRIORITY, Objects.requireNonNull(JavaIssuePriorityEnum.getPriorityEnum(priority)).getRank());
        }

        //根据url、projectName和reposUuid获得 repoUuid列表
        try {
            List<String> repoList = getRepoListByUrlProjectNamesRepoUuids(url, projectNames, repoUuids, userToken);
            query.put(REPO_LIST, repoList);
        } catch (UrlException e) {
            return new ResponseBean<>(400, FAILED + e.getMessage(), null);
        }
        //处理查询条件
        String[] queryName = {STATUS, "filesPath", "issueUuids"};
        String[] spiltStrings = {status, filesPath, issueUuids};
        StringsUtil.splitString(queryName, spiltStrings, query);
        if (!StringUtils.isEmpty(type)) {
            query.put("types", new ArrayList<String>() {{
                add(type);
            }});
        }
        query.put(SINCE, since);
        query.put(UNTIL, until);
        query.put("category", category);
        query.put("toolName", toolName);
        query.put("developer", StringsUtil.splitStringList(introducers));
        query.put("commit", commit);
        query.put("start", (page - 1) * ps);
        query.put("solver", solver);
        query.put("ps", ps);
        query.put("asc", asc);
        query.put("detail", detail);
        query.put("order", order);
        query.put("manual_status", manualStatus);
        //step1 ps = 0 only return total(because fetch time --)  or  ps != 0 do select;
        Map<String, Object> issueFilterList = issueService.getIssueFilterListCount(query);
        if (ps == 0) {
            return new ResponseBean<>(200, SUCCESS, issueFilterList);
        }
        //step2 select issueList (always(since,until,status,types,filesPath,repoList,priority,toolName,start,ps,category) and
        //                        options(commit ? do select commit : pass)(solver ? select introducer and solver : select introducer))
        issueFilterList = issueService.getIssueFilterList(query, issueFilterList);
        //step3 final check detail
        issueFilterList = issueService.getIssueFilterListWithDetail(query, issueFilterList);

        return new ResponseBean<>(200, SUCCESS, issueFilterList);
    }

    @ApiOperation(value = "缺陷总览侧边栏", notes = "@return IssueFilterSidebar", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "project_names", value = "项目名\n项目名之间用英文逗号,作为分隔符"),
            @ApiImplicitParam(name = "repo_uuids", value = "多个库的uuid\n库uuid之间用英文逗号,作为分隔符"),
            @ApiImplicitParam(name = "since", value = "起始时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "until", value = "终止时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "tools", value = "工具名\n工具名之间用英文逗号,作为分隔符", defaultValue = "sonarqube,ESLint,TscanCode", allowableValues = "sonarqube , ESLint , TscanCode"),
            @ApiImplicitParam(name = "status", value = "issue状态\n传null默认查询所有状态", allowableValues = "Open , Solved , Misinformation , To_Review , Ignore"),
            @ApiImplicitParam(name = "producer", value = "issue引入者\n传null默认查询所有引入者"),
            @ApiImplicitParam(name = "priority", value = "优先级", allowableValues = "Low , Urgent , Normal , High , Immediate"),
            @ApiImplicitParam(name = "issue_uuids", value = "多个issue的uuid\n用英文逗号,作为分隔符"),
            @ApiImplicitParam(name = "manual_status", value = "manual status", defaultValue = "Default", allowableValues = "Ignore , Default")
    })
    @GetMapping(value = "issue/filter/sidebar")
    public ResponseBean<List<IssueFilterSidebarVO>> getIssueFilterSidebar(@RequestParam(value = "project_names", required = false) String projectNames,
                                                                          @RequestParam(value = "repo_uuids", required = false) String repoUuids,
                                                                          @RequestParam(value = "tools", required = false, defaultValue = "sonarqube,ESLint,TscanCode") String tools,
                                                                          @RequestParam(value = "since", required = false) String since,
                                                                          @RequestParam(value = "until", required = false) String until,
                                                                          @RequestParam(value = "status", required = false) String status,
                                                                          @RequestParam(value = "manual_status", required = false, defaultValue = "Default") String manualStatus,
                                                                          @RequestParam(value = "priority", required = false) String priority,
                                                                          @RequestParam(value = "introducer", required = false) String introducer,
                                                                          @RequestParam(value = "issue_uuids", required = false) String issueUuids,
                                                                          HttpServletRequest httpServletRequest) throws UrlException {
        Map<String, Object> query = new HashMap<>(16);
        if (TIME_FORMAT_ERROR.equals(DateTimeUtil.timeFormatIsLegal(since, false)) || TIME_FORMAT_ERROR.equals(DateTimeUtil.timeFormatIsLegal(until, true))) {
            return new ResponseBean<>(400, TIME_FORMAT_ERROR, null);
        }

        String token = httpServletRequest.getHeader(TOKEN);
        List<String> repoList = getRepoListByUrlProjectNamesRepoUuids(null, projectNames, repoUuids, token);

        query.put(UUID, StringsUtil.splitStringList(issueUuids));
        query.put(TOOLS, StringsUtil.splitStringList(tools));
        query.put(SINCE, since);
        query.put(UNTIL, until);
        query.put(STATUS, status);
        query.put(MANUAL_STATUS, manualStatus);
        query.put(REPO_LIST, repoList);
        query.put(PRIORITY, priority == null ? null : Objects.requireNonNull(JavaIssuePriorityEnum.getPriorityEnum(priority)).getRank());
        query.put(PRODUCER, StringsUtil.splitStringList(introducer));
        try {
            return new ResponseBean<>(200, SUCCESS, issueService.getIssuesFilterSidebar(query));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(500, FAILED + e.getMessage(), null);
        }
    }

    private List<String> getRepoListByUrlProjectNamesRepoUuids(String url, String projectNames, String repoUuids, String userToken) throws UrlException {

        List<String> repoList = new ArrayList<>();

        if (url != null && url.length() > 0) {
            String repoUuid = restInterfaceManager.getRepoUuidByUrl(url, userToken);
            if (repoUuid == null) {
                throw new UrlException("url input error!");
            }
            repoList.add(repoUuid);
            return repoList;
        }

        if (!StringUtils.isEmpty(repoUuids)) {
            repoList = Arrays.asList(repoUuids.split(","));
            return repoList;
        }

        if (!StringUtils.isEmpty(projectNames)) {
            return restInterfaceManager.getAllRepoByProjectNames(userToken, StringsUtil.splitStringList(projectNames));
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
            @ApiImplicitParam(name = "project_names", value = "项目名\n项目名之间用英文逗号,作为分隔符"),
            @ApiImplicitParam(name = "repo_uuids", value = "多个库的uuid\n库uuid之间用英文逗号,作为分隔符"),
            @ApiImplicitParam(name = "since", value = "起始时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "until", value = "终止时间\n格式要求: yyyy-MM-dd"),
            @ApiImplicitParam(name = "tools", value = "工具名\n工具名之间用英文逗号,作为分隔符", defaultValue = "sonarqube,ESLint,TscanCode", allowableValues = "sonarqube , ESLint, TscanCode"),
            @ApiImplicitParam(name = "status", value = "issue状态\n传null默认查询所有状态", allowableValues = "Open , Solved , Misinformation , To_Review , Ignore"),
            @ApiImplicitParam(name = "producer", value = "issue引入者\n传null默认查询所有引入者"),
            @ApiImplicitParam(name = "priority", value = "优先级", allowableValues = "Low , Urgent , Normal , High , Immediate"),
            @ApiImplicitParam(name = "types", value = "issue种类"),
            @ApiImplicitParam(name = "manual_status", value = "manual status", allowableValues = "Ignore , Default"),
    })
    @GetMapping("/issue/filter/download")
    @ResponseBody
    public void downloadExcel(HttpServletRequest request, @RequestParam(value = "project_names", required = false) String projectNames,
                              @RequestParam(value = "repo_uuids", required = false) String repoUuids,
                              @RequestParam(value = "tools", required = false, defaultValue = "sonarqube,ESLint,TscanCode") String tools,
                              @RequestParam(value = "since", required = false) String since,
                              @RequestParam(value = "until", required = false) String until,
                              @RequestParam(value = "introducer", required = false) String introducer,
                              @RequestParam(value = "status", required = false) String status,
                              @RequestParam(value = "manual_status", required = false) String manualStatus,
                              @RequestParam(value = "priority", required = false) String priority,
                              @RequestParam(value = "types", required = false) String type,
                              @RequestParam(value = "issue_uuids", required = false) String issueUuids, HttpServletResponse response) throws UrlException {

        Map<String, Object> query = new HashMap<>(16);

        String token = request.getHeader(TOKEN);

        if (TIME_FORMAT_ERROR.equals(DateTimeUtil.timeFormatIsLegal(since, false)) || TIME_FORMAT_ERROR.equals(DateTimeUtil.timeFormatIsLegal(until, true))) {
            return;
        }

        List<String> repoList = getRepoListByUrlProjectNamesRepoUuids(null, projectNames, repoUuids, token);

        query.put(UUID, StringsUtil.splitStringList(issueUuids));
        query.put(TOOLS, StringsUtil.splitStringList(tools));
        query.put(SINCE, since);
        query.put(UNTIL, until);
        query.put(STATUS, status);
        query.put(MANUAL_STATUS, manualStatus);
        query.put(REPO_LIST, repoList);
        query.put(PRIORITY, priority == null ? null : Objects.requireNonNull(JavaIssuePriorityEnum.getPriorityEnum(priority)).getRank());
        query.put(PRODUCER, StringsUtil.splitStringList(introducer));
        query.put("type", type);

        List<IssueFilterInfoVO> issuesOverview = issueService.getIssuesOverview(query, token);

        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Connection, User-Agent, Cookie, token, Content-Disposition");
        response.setHeader("Content-Disposition", "attachment; filename=" + "issues.xls");
        response.setHeader("content-type", "application/vnd.ms-excel");

        try (HSSFWorkbook workbook = ExcelUtil.exportExcel(issuesOverview)) {
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
            @ApiImplicitParam(name = "tool", value = "工具名", defaultValue = "sonarqube", allowableValues = "sonarqube , ESLint")
    })
    @GetMapping(value = {"/issue/repository/issue-count"})
    public ResponseBean<List<Map<String, Object>>> getNewTrend(@RequestParam("repo_uuids") String repoUuids,
                                                               @RequestParam("since") String since,
                                                               @RequestParam("until") String until,
                                                               @RequestParam(name = "tool", required = false, defaultValue = "sonarqube") String tool) {
        List<String> repoList = StringsUtil.splitStringList(repoUuids);
        try {
            return new ResponseBean<>(200, SUCCESS, issueService.getRepoIssueCounts(repoList, since, until, tool));
        } catch (Exception e) {
            return new ResponseBean<>(500, FAILED + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "获取所有引入过缺陷的开发者姓名列表", notes = "@return  List < String >", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuids", value = "代码库uuid\n支持多库筛选以英文,分隔\n不传默认全部库")
    })
    @GetMapping(value = {"/issue/issue-introducers"})
    public ResponseBean<List<String>> getIssueIntroducers(@RequestParam(value = "repo_uuids", required = false) String repoUuids) {

        List<String> repoUuidList = StringsUtil.splitStringList(repoUuids);

        try {
            return new ResponseBean<>(200, SUCCESS, issueService.getIssueIntroducers(repoUuidList));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(500, FAILED + e.getMessage(), null);
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
