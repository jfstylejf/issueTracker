package cn.edu.fudan.issueservice.controller;

import cn.edu.fudan.issueservice.domain.ResponseBean;
import cn.edu.fudan.issueservice.service.RawIssueService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author Beethoven
 */
@Slf4j
@RestController
@Api(value = "rawIssue", tags = {"用于统计rawIssue的相关接口"})
public class RawIssueController {

    private RawIssueService rawIssueService;

    private static final String SUCCESS = "success";
    private static final String FAILED = "failed ";

    @ApiOperation(value = "根据issue_uuid筛选rawIssue", notes = "@return List<RawIssue>", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "issue_uuid", value = "issue的uuid", required = true)
    })
    @GetMapping(value = {"/raw-issue"})
    public ResponseBean<List<Map<String, Object>>> getRawIssueList(@RequestParam("issue_uuid") String issueUuid) {
        try {
            return new ResponseBean<>(200, SUCCESS, rawIssueService.getRawIssueByIssueUuid(issueUuid));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(500, FAILED + e.getMessage(), null);
        }
    }

    @Autowired
    public void setRawIssueService(RawIssueService rawIssueService) {
        this.rawIssueService = rawIssueService;
    }
}
