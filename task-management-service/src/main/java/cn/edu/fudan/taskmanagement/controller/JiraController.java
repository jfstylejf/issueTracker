package cn.edu.fudan.taskmanagement.controller;

import cn.edu.fudan.taskmanagement.domain.ResponseBean;
import cn.edu.fudan.taskmanagement.domain.Task;
import cn.edu.fudan.taskmanagement.service.JiraService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author zyh
 * @date 2020/7/2
 */
@RestController
@EnableAutoConfiguration
public class JiraController {

    @Autowired
    JiraService jiraService;

    @GetMapping(value = {"/jira/jql"})
    public ResponseBean<List<Task>> getTaskInfoByJql(@RequestParam(name = "type", required = false, defaultValue = "key") String type, @RequestParam("keyword") String keyword){

        try{
            return new ResponseBean(200,"success",jiraService.getTaskInfoByJql(type, keyword));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(400,"failed",e.getMessage());
        }
    }

    @ApiOperation(value = "jira信息", notes = "@return List<Map<String, Object>>", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuid", value = "库Id", dataType = "String"),
            @ApiImplicitParam(name = "since", value = "开始时间", dataType = "String"),
            @ApiImplicitParam(name = "until", value = "结束时间", dataType = "String"),
            @ApiImplicitParam(name = "developer", value = "开发者", dataType = "String"),
    })
    @CrossOrigin
    @GetMapping(value = {"jira/developer-msg"})
    public ResponseBean<List<Map<String, Object>>> getDeveloperMsg(
            @RequestParam(value = "repo_uuid", required = false) String repoUuidPara,
            @RequestParam(value = "developer", required = false) String developer,
            @RequestParam(value = "since", required = false) String since,
            @RequestParam(value = "until", required = false) String until
    ){

        try{
            return new ResponseBean(200,"success",jiraService.getDeveloperMsg(repoUuidPara, developer, since, until));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(400,"failed",e.getMessage());
        }
    }

    @CrossOrigin
    @PostMapping(value = {"jira/insert-jira-msg"})
    public void insertJiraMsg(
            @RequestParam(value = "repo_uuid", required = false) String repoUuidPara,
            @RequestParam(value = "developer", required = false) String developer,
            @RequestParam(value = "since", required = false) String since,
            @RequestParam(value = "until", required = false) String until
    ){
        jiraService.insertJiraMsg(repoUuidPara, developer, since, until);
    }

    @Scheduled(cron = "0 0 1 * * *")
    @PostMapping(value = {"jira/auto-insert-jira-msg"})
    public void insertJiraMsg(
    ){
        jiraService.insertJiraMsg(null, null, null, null);
    }
}
