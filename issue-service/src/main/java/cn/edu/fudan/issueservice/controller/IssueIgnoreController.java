package cn.edu.fudan.issueservice.controller;

import cn.edu.fudan.issueservice.domain.ResponseBean;
import cn.edu.fudan.issueservice.domain.dbo.IgnoreRecord;
import cn.edu.fudan.issueservice.domain.enums.IgnoreTypeEnum;
import cn.edu.fudan.issueservice.service.IssueIgnoreService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

/**
 * @author Beethoven
 */
@Api(value = "issue ignore", tags = {"用于忽略issue的相关接口"})
@RestController
public class IssueIgnoreController {

    private IssueIgnoreService issueIgnoreService;

    private static final String SUCCESS = "success";
    private static final String FAILED = "failed ";

    @ApiOperation(value = "插入IssueIgnore记录", notes = "@return String", httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tool", value = "工具名", dataType = "String", required = true, allowableValues = "sonarqube"),
    })
    @PutMapping(value = "issue/ignore/{tool}")
    public ResponseBean<String> ignoreIssues(@PathVariable("tool") String tool, @RequestBody List<IgnoreRecord> ignoreRecords) {
        if (ignoreRecords.isEmpty()) {
            return new ResponseBean<>(200, SUCCESS, null);
        }
        try {
            for (IgnoreRecord ignoreRecord : ignoreRecords) {
                if (!IgnoreTypeEnum.isStatusRight(ignoreRecord.getTag())) {
                    return new ResponseBean<>(400, "issue tag error!", null);
                }
                if (!ignoreRecord.getTool().equals(tool)) {
                    return new ResponseBean<>(400, FAILED, "tool in url path or tool in record error!");
                }
                ignoreRecord.setUuid(UUID.randomUUID().toString());
                ignoreRecord.setIgnoreTime(ignoreRecord.getIgnoreTime());
            }
            return new ResponseBean<>(200, SUCCESS, issueIgnoreService.insertIssueIgnoreRecords(ignoreRecords));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(500, FAILED + e.getMessage(), null);
        }
    }

    @Autowired
    public void setIssueIgnoreService(IssueIgnoreService issueIgnoreService) {
        this.issueIgnoreService = issueIgnoreService;
    }
}
