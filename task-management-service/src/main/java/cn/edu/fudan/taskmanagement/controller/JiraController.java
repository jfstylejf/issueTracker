package cn.edu.fudan.taskmanagement.controller;

import cn.edu.fudan.taskmanagement.domain.JiraCount;
import cn.edu.fudan.taskmanagement.domain.ResponseBean;
import cn.edu.fudan.taskmanagement.domain.Task;
import cn.edu.fudan.taskmanagement.mapper.RepoCommitMapper;
import cn.edu.fudan.taskmanagement.service.JiraService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author zyh
 * @date 2020/7/2
 */
@RestController
@EnableAutoConfiguration
public class JiraController {

    @Autowired
    JiraService jiraService;

    @Autowired
    RepoCommitMapper repoCommitMapper;

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


    @GetMapping(value = {"/jira/data/completed-jira-num"})
    public ResponseBean<Object> getJiraCount(@RequestParam(value = "project_ids", defaultValue = "") String projectIds,
                                             @RequestParam(value = "project_names", required = false, defaultValue = "") String projectNames,
                                             @RequestParam(value = "repo_uuids", defaultValue = "") String repoId,
                                             @RequestParam(value = "developers", required = false) String developers,
                                             @RequestParam(value = "since", required = false, defaultValue = "2000-01-01") String start,
                                             @RequestParam(value = "until", required = false) String end,
                                             @RequestParam(value = "order", required = false, defaultValue = "") String order,
                                             @RequestParam(value = "page", required = false, defaultValue = "1") String page,
                                             @RequestParam(value = "ps", required = false, defaultValue = "5") String size,
                                             @RequestParam(value = "asc", required = false) Boolean isAsc,
                                             HttpServletRequest httpServletRequest) {
        try {
            String token = httpServletRequest.getHeader("token");
            projectIds = getProjectIds(projectIds, projectNames);

            if (StringUtils.isEmpty(start)) {
                start = "2000-01-01";
            }
            if (StringUtils.isEmpty(end)) {
                Date today = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                end = simpleDateFormat.format(today);
            }

            List<JiraCount> result = jiraService.getJiraCountList(developers, "done", projectIds, repoId, start, end, token);
            if (StringUtils.isEmpty(developers)) {
                return new ResponseBean<>(200, "developers required", result);
            } else {
                Collections.sort(result);
                Map<String, Object> data = getPagingMap(page, size, isAsc, result);
                return new ResponseBean<>(200, "success", data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(401, "failed", null);
        }
    }

    private String getProjectIds(@RequestParam(value = "project_ids", required = false, defaultValue = "") String projectIds, @RequestParam(value = "project_names", required = false, defaultValue = "") String projectNames) {
        List<String> projectIdList = new ArrayList<>();
        if (!StringUtils.isEmpty(projectNames)) {
            Arrays.asList(projectNames.split(",")).forEach(a -> projectIdList.add(repoCommitMapper.getProjectIdByProjectName(a)));
            if (!projectIdList.isEmpty()) {
                projectIds = projectIdList.get(0);
                for (int i = 1; i < projectIdList.size(); i++) {
                    projectIds = projectIds + "," + projectIdList.get(i);
                }
            }
        }
        return projectIds;
    }

    private <T> Map<String, Object> getPagingMap(@RequestParam(value = "page", required = false, defaultValue = "1") String page, @RequestParam(value = "ps", required = false, defaultValue = "5") String size, @RequestParam(value = "asc", required = false) Boolean isAsc, List<T> source) {
        List<T> target = new ArrayList<>();
        if (page != null && size != null) {
            int pageDigit = Integer.parseInt(page);
            int sizeDigit = Integer.parseInt(size);
            if (isAsc != null && !isAsc) {
                Collections.reverse(source);
            }
            int index = (pageDigit - 1) * sizeDigit;
            while ((index < source.size()) && (index < pageDigit * sizeDigit)) {
                target.add(source.get(index));
                index += 1;
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        data.put("total", source.size() / Integer.parseInt(size) + 1);
        data.put("records", source.size());
        data.put("rows", target);
        return data;
    }
}
