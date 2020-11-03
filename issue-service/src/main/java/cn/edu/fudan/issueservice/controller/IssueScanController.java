package cn.edu.fudan.issueservice.controller;

import cn.edu.fudan.issueservice.domain.ResponseBean;
import cn.edu.fudan.issueservice.domain.dbo.IssueRepo;
import cn.edu.fudan.issueservice.domain.dto.RepoResourceDTO;
import cn.edu.fudan.issueservice.domain.dto.ScanRequestDTO;
import cn.edu.fudan.issueservice.service.IssueScanService;
import cn.edu.fudan.issueservice.core.ToolInvoker;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * description: issue 工具调用
 *
 * @author fancying
 * create: 2020-05-19 21:03
 **/
@Api(value = "issue scan", tags = {"用于控制issue扫描的相关接口"})
@Slf4j
@RestController
public class IssueScanController {

    private ToolInvoker toolInvoker;

    private IssueScanService issueScanService;

    @ApiOperation(value = "控制扫描开始接口", notes = "@return String", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tool", value = "扫描工具名", dataType = "String", required = true, defaultValue = "sonarqube", allowableValues = "sonarqube"),
            @ApiImplicitParam(name = "scanRequestDTO", value = "所需扫描库Id,分支名,起始commit",dataType = "ScanRequestDTO", required = true)
    })
    @PostMapping(value = {"/issue/{tool}"})
    public ResponseBean<String> scan(@PathVariable("tool") String tool, @RequestBody ScanRequestDTO scanRequestDTO) {
        String repoId = scanRequestDTO.getRepoUuid();
        String branch = scanRequestDTO.getBranch();
        String beginCommit = scanRequestDTO.getBeginCommit();
        // TODO 调用 tool scan 流程
        try {
            RepoResourceDTO repoResourceDTO = RepoResourceDTO.builder ().repoId (repoId).build ();
            String result = issueScanService.prepareForScan (repoResourceDTO, branch, beginCommit, tool);
            return new ResponseBean<>(200,"success!",result);
        }catch (Exception e) {
            e.printStackTrace ();
            log.error("invoke tool:[{}] failed! message is {}", tool, e.getMessage());
            return new ResponseBean<>(500, "invoke tool failed!", e.getMessage());
        }


    }

    /**
     * 根据工具和repoId返回当前扫描的状态
     */
    @ApiOperation(value = "根据工具和repoId返回当前扫描的状态", notes = "@return Map<String, Object>\n{\n" +
            "        \"uuid\": \"3f9aee42-ca85-415b-8e92-5a4257f87368\",\n" +
            "        \"repoId\": \"3ecf804e-0ad6-11eb-bb79-5b7ba969027e\",\n" +
            "        \"branch\": \"zhonghui20191012\",\n" +
            "        \"tool\": \"sonarqube\",\n" +
            "        \"startCommit\": \"e12f6cee85c89d14b9de8d94577fe8844d7b3c25\",\n" +
            "        \"endCommit\": \"7697c69d749dad14f37e1a6072b0090cb869caf2\",\n" +
            "        \"totalCommitCount\": 459,\n" +
            "        \"scannedCommitCount\": 459,\n" +
            "        \"scanTime\": 83524,\n" +
            "        \"status\": \"complete\",\n" +
            "        \"nature\": \"main\",\n" +
            "        \"startScanTime\": \"2020-10-14 17:46:10\",\n" +
            "        \"endScanTime\": \"2020-10-15 17:02:44\"\n" +
            "    }", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tool", value = "工具名", required = true, defaultValue = "sonarqube", allowableValues = "sonarqube"),
            @ApiImplicitParam(name = "repo_uuid", value = "代码库uuid", required = true)
    })
    @GetMapping(value = {"/issue/{tool}/scan-status"})
    public ResponseBean<IssueRepo> scanStatus(@PathVariable("tool") String tool, @RequestParam("repo_uuid") String repoUuid) {
        try {
            IssueRepo issueRepo = issueScanService.getScanStatus (repoUuid, tool);
            return new ResponseBean<>(200, "success!", issueRepo);
        }catch (Exception e) {
            log.error("invoke tool:[{}] failed! message is {}", tool, e.getMessage());
            return new ResponseBean<>(500, e.getMessage(), null);
        }

    }

    /**
     * 根据工具和repoId停止相应的扫描
     */
    @ApiOperation(value = "根据工具和repoId停止相应的扫描", notes = "@return String", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tool", value = "工具名", required = true, defaultValue = "sonarqube", allowableValues = "sonarqube"),
            @ApiImplicitParam(name = "repo_uuid", value = "代码库uuid", required = true)
    })
    @GetMapping(value = {"/issue/{tool}/stop"})
    public ResponseBean<String> stopScan(@PathVariable("tool") String tool, @RequestParam("repo_uuid") String repoUuid) {
        try {
            issueScanService.stopScan (repoUuid, tool);
            return new ResponseBean<>(200, "stop success!", null);
        }catch (Exception e) {
            log.error("invoke tool:[{}] failed! message is {}", tool, e.getMessage());
            return new ResponseBean<>(500, "invoke tool failed!", e.getMessage());
        }
    }

    /**
     * 根据工具和repoId 获取某个项目未扫描commit的数量
     */
    @ApiOperation(value = "根据工具和repoId 获取某个项目未扫描commit的数量", notes = "@return Integer", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tool", value = "工具名", required = true, defaultValue = "sonarqube", allowableValues = "sonarqube"),
            @ApiImplicitParam(name = "repo_uuid", value = "代码库uuid", required = true),
            @ApiImplicitParam(name = "branch", value = "分支名", required = true)
    })
    @GetMapping(value = {"/issue/{tool}/stock"})
    public ResponseBean<Integer> getStockCommit(@PathVariable("tool") String tool, @RequestParam("repo_uuid") String repoUuid, @RequestParam("branch") String branch) {
        try {
            RepoResourceDTO repoResourceDTO = RepoResourceDTO.builder ().repoId (repoUuid).build ();
            int stockCommitCount =  issueScanService.getStockCommit (repoResourceDTO, tool, branch);
            return new ResponseBean<>(200, "success!", stockCommitCount);
        }catch (Exception e) {
            log.error("get tool:[{}] stock failed! message is {}", tool, e.getMessage());
            return new ResponseBean<>(500, e.getMessage(), null);
        }
    }

    /**
     * 根据工具和repoId 获取某个项目未扫描commit的信息
     */
    @ApiOperation(value = "根据工具和repoId 获取某个项目未扫描commit的信息", notes = "@return Map<String, Object>\n{\n" +
            "        \"totalCount\": 0,\n" +
            "        \"commitList\": [],\n" +
            "        \"pageCount\": 0\n" +
            "    }", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tool", value = "工具名", required = true, defaultValue = "sonarqube", allowableValues = "sonarqube"),
            @ApiImplicitParam(name = "repo_uuid", value = "代码库uuid", required = true),
            @ApiImplicitParam(name = "page", value = "页号\n默认为第1页"),
            @ApiImplicitParam(name = "size", value = "页大小\n默认一页10条"),
            @ApiImplicitParam(name = "is_whole", value = "是否需要全部\n默认false", allowableValues = "false , true")
    })
    @GetMapping(value = {"/issue/{tool}/commit-list"})
    public ResponseBean<Map<String, Object>> getStockCommit(@RequestParam(name = "repo_uuid") String repoUuid,
                                       @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
                                       @RequestParam(name = "size", required = false, defaultValue = "10") Integer size,
                                       @RequestParam(name = "is_whole", required = false, defaultValue = "false") Boolean is_whole,
                                       @PathVariable(name="tool")String tool) {
        try {
            Map<String, Object> object = issueScanService.getCommits(repoUuid, page, size, is_whole,tool);
            return new ResponseBean<>(200, "success!", object);
        }catch (Exception e) {
            log.error("invoke tool:[{}] failed! repoId:{} !message is {}", tool, repoUuid, e.getMessage());
            return new ResponseBean<>(500, e.getMessage(), null);
        }
    }



    @Autowired
    public void setToolInvoker(ToolInvoker toolInvoker) {
        this.toolInvoker = toolInvoker;
    }

    @Autowired
    public void setIssueScanService(IssueScanService issueScanService) {
        this.issueScanService = issueScanService;
    }

}