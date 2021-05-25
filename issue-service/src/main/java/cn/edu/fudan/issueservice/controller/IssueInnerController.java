package cn.edu.fudan.issueservice.controller;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.domain.ResponseBean;
import cn.edu.fudan.issueservice.domain.enums.IgnoreTypeEnum;
import cn.edu.fudan.issueservice.domain.enums.IssueStatusEnum;
import cn.edu.fudan.issueservice.service.IssueService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Beethoven
 */
@Api(value = "issue inner", tags = {"用于修改issue信息的相关接口"})
@RestController
public class IssueInnerController {

    private IssueService issueService;
    private RestInterfaceManager restInterfaceManager;

    private static final String SUCCESS = "success";
    private static final String FAILED = "failed ";
    private static final String TOKEN = "token";

    @Autowired
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @ApiOperation(value = "根据repoUuid和tool删除对应issue", notes = "@return String", httpMethod = "DELETE")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo-uuid", value = "代码库uuid", dataType = "String", required = true),
    })
    @DeleteMapping(value = {"/issue/{repo-uuid}"})
    public ResponseBean<String> deleteIssues(@PathVariable("repo-uuid") String repoUuid) {
        try {
            String tool = restInterfaceManager.getToolByRepoUuid(repoUuid);
            issueService.deleteIssueByRepoIdAndTool(repoUuid, tool);
            return new ResponseBean<>(200, SUCCESS, "issues delete success!");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(500, FAILED + e.getMessage(), "issues delete failed!");
        }
    }

    @ApiOperation(value = "修改Issue的状态", notes = "@return null", httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "issue-uuid", value = "issue的uuid", required = true),
            @ApiImplicitParam(name = "status", value = "issue状态", allowableValues = "Open , Solved"),
            @ApiImplicitParam(name = "manual_status", value = "issue是否被忽略", allowableValues = "Ignore , Default"),
    })
    @PutMapping(value = "/issue/status/{issue-uuid}")
    public ResponseBean<Object> updateStatus(@PathVariable("issue-uuid") String issueUuid,
                                             @RequestParam(value = "status", required = false) String status,
                                             @RequestParam(value = "manual_status", required = false) String manualStatus) {
        try {
            if (!StringUtils.isEmpty(status) && !IssueStatusEnum.isStatusRight(status)) {
                return new ResponseBean<>(400, FAILED, "input issue status error!");
            }
            if (!StringUtils.isEmpty(manualStatus) && !IgnoreTypeEnum.isStatusRight(manualStatus)) {
                return new ResponseBean<>(400, FAILED, "input issue manual status error!");
            }
            issueService.updateStatus(issueUuid, status, manualStatus);
            return new ResponseBean<>(200, SUCCESS, "issues update status success!");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(500, FAILED + e.getMessage(), "issues update status failed!");
        }
    }

    @ApiOperation(value = "修改Issue的优先级", notes = "@return null", httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "issue-uuid", value = "issue的uuid", required = true),
            @ApiImplicitParam(name = "priority", value = "issue的优先级", required = true, allowableValues = "Low , Urgent , Normal , High , Immediate"),
    })
    @PutMapping(value = "/issue/priority/{issue-uuid}")
    public ResponseBean<Object> updatePriority(@PathVariable("issue-uuid") String issueUuid, @RequestParam("priority") String priority, HttpServletRequest request) {
        try {
            String userToken = request.getHeader(TOKEN);
            issueService.updatePriority(issueUuid, priority, userToken);
            return new ResponseBean<>(200, SUCCESS, "issues update priority success!");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(500, FAILED + e.getMessage(), "issues update priority failed!");
        }
    }

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }
}
