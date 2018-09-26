package cn.edu.fudan.issueservice.controller;

import cn.edu.fudan.issueservice.domain.Issue;
import cn.edu.fudan.issueservice.domain.ResponseBean;
import cn.edu.fudan.issueservice.service.IssueService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class IssueController {


    private IssueService issueService;

    @Autowired
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }


    @GetMapping(value = {"/issue"})
    public Object getIssues(@RequestParam("project_id") String project_id,
                            @RequestParam("page") Integer page,
                            @RequestParam("size") Integer size) {
        return issueService.getIssueList(project_id, page, size);
    }

    @GetMapping(value = {"/issue/issue-types"})
    public Object getExistIssueTypes() {
        return issueService.getExistIssueTypes();
    }

    //参数超过三个不宜采用requestParameter、post请求（参数封装成一个类自动解析）、get+requestBody
    @PostMapping(value = {"/issue/filter"})
    public Object filterIssues(@RequestBody JSONObject requestParam) {
        return issueService.getFilteredIssueList(requestParam);
    }

    @GetMapping(value = {"/issue/dashboard"})
    public Object getDashBoardInfo(@RequestParam("duration") String duration,
                                   @RequestParam(name = "project_id", required = false) String project_id,
                                   HttpServletRequest request) {
        String userToken = request.getHeader("token");
        return issueService.getDashBoardInfo(duration, project_id, userToken);
    }

    @GetMapping(value = {"/issue/statistical-results"})
    public Object getStatisticalResults(@RequestParam("month") Integer month,
                                        @RequestParam(name = "project_id", required = false) String project_id,
                                        HttpServletRequest request) {
        String userToken = request.getHeader("token");
        return issueService.getStatisticalResults(month, project_id, userToken);
    }

    //下面都是供其他服务调用的内部接口

    @PostMapping(value = {"/inner/issue"})
    public Object addIssues(@RequestBody List<Issue> issueList) {
        try {
            issueService.insertIssueList(issueList);
            return new ResponseBean(200, "issues add success!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "issues add failed!", null);
        }
    }

    @DeleteMapping(value = {"/inner/issue/{repoId}"})
    public Object deleteIssues(@PathVariable("repoId") String repoId) {
        try {
            issueService.deleteIssueByRepoId(repoId);
            return new ResponseBean(200, "issues delete success!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "issues delete failed!", null);
        }
    }

    @PutMapping(value = {"/inner/issue"})
    public Object updateIssues(@RequestBody List<Issue> issueList) {
        try {
            issueService.batchUpdateIssue(issueList);
            return new ResponseBean(200, "issues update success!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "issues update failed!", null);
        }
    }

    @PostMapping(value = {"/inner/issue/mapping"})
    public Object mapping(@RequestBody JSONObject requestParam) {
        try {
            String repo_id = requestParam.getString("repo_id");
            String pre_commit_id = requestParam.getString("pre_commit_id");
            String current_commit_id = requestParam.getString("current_commit_id");
            issueService.startMapping(repo_id, pre_commit_id, current_commit_id);
            return new ResponseBean(200, "issues mapping success!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "issues mapping failed!", null);
        }
    }

    @GetMapping(value = "/inner/issue/dashbaord")
    public Object updateIssueCount(@RequestParam ("time") String time){
        try {
            issueService.updateIssueCount(time);
            return new ResponseBean(200, "update success!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, " update failed!", null);
        }
    }

}
