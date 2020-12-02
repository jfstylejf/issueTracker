package cn.edu.fudan.issueservice.controller;

import cn.edu.fudan.issueservice.domain.ResponseBean;
import cn.edu.fudan.issueservice.service.IssueService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Beethoven
 */
@Api(value = "issue inner", tags = {"用于删除issue的相关接口"})
@RestController
public class IssueInnerController {

    private IssueService issueService;

    @Autowired
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    /**
     * todo 检查是否删除了和issue scan result 相关的数据
     */
    @ApiOperation(value = "根据repoUuid和tool删除对应issue", notes = "@return String", httpMethod = "DELETE")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tool", value = "工具名", dataType = "String", required = true, allowableValues = "sonarqube"),
            @ApiImplicitParam(name = "repo-uuid", value = "代码库uuid", dataType = "String", required = true),

    })
    @DeleteMapping(value = {"/inner/issue/{tool}/{repo-uuid}"})
    public ResponseBean<String> deleteIssues(@PathVariable("tool")String tool,@PathVariable("repo-uuid") String repoUuid) {
        try {
            issueService.deleteIssueByRepoIdAndTool(repoUuid,tool);
            return new ResponseBean<>(200, "success", "issues delete success!");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(401, "failed " + e.getMessage(), "issues delete failed!");
        }
    }
}
