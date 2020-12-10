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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.List;

/**
 * @author Beethoven
 */
@Api(value = "issue ignore", tags = {"用于忽略issue的相关接口"})
@RestController
public class IssueIgnoreController {

    private IssueIgnoreService issueIgnoreService;

    private final String success = "success";

    private final String failed = "failed ";

    @ApiOperation(value = "插入IssueIgnore记录", notes = "@return String", httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tool", value = "工具名", dataType = "String", required = true, allowableValues = "sonarqube"),
    })
    @PutMapping(value = "issue/ignore/{tool}")
    public ResponseBean<String> ignoreIssues(@PathVariable("tool")String tool, @RequestBody List<IgnoreRecord> ignoreRecords){

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try{
            for(IgnoreRecord ignoreRecord : ignoreRecords) {
                if(!IgnoreTypeEnum.statusInEnum(ignoreRecord.getTag())){
                    return new ResponseBean<>(401, "issue tag error!", null);
                }
                ignoreRecord.setUuid(UUID.randomUUID().toString());
                ignoreRecord.setIgnoreTime(df.format(new Date()));
            }
            return new ResponseBean<>(200, success, issueIgnoreService.insertIssueIgnoreRecords(ignoreRecords));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(500,failed + e.getMessage(), null);
        }
    }

    @DeleteMapping("issue/ignore/{tool}")
    public ResponseBean<String> deleteIssueIgnoreRecord(@PathVariable("tool")String tool,
                                                        @RequestParam("issue_uuid")String issueUuid,
                                                        @RequestParam("ignore_uuid")String ignoreUuid){
        try{
            issueIgnoreService.deleteIssueIgnoreRecord(tool, issueUuid, ignoreUuid);
            return new ResponseBean<>(200, success, "delete issue ignore record success");
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(500, failed + e.getMessage(), null);
        }
    }

    @Autowired
    public void setIssueIgnoreService(IssueIgnoreService issueIgnoreService) {
        this.issueIgnoreService = issueIgnoreService;
    }
}
