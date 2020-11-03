package cn.edu.fudan.scanservice.controller;

import cn.edu.fudan.scanservice.domain.dto.ResponseBean;
import cn.edu.fudan.scanservice.domain.dto.ScanRequestMessage;
import cn.edu.fudan.scanservice.service.ScanInfoService;
import cn.edu.fudan.scanservice.service.ScanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * description: receive scan request and
 *             query data about scan eg scan status, scan commits
 *
 * @author fancying
 */
@Api(value = "scan", tags = {"扫描相关接口"})
@RestController
public class ScanController {

    private ScanService scanService;
    private ScanInfoService scanInfoService;

    /**
     * description receive scan request form front
     *
     * @param requestParam  repoId branch commitId startTime
     *                      Unfixed parameters are used to handle various situations
     */
    @ApiOperation(value = "前端调用工具进行扫描", notes = "前端调用工具进行扫描", httpMethod = "POST")
    @PostMapping(value = {"/scan"})
    public ResponseBean scan(@RequestBody ScanRequestMessage requestParam) {
        String repoId;
        String branch;
        String commitId;
        String startTime;
        try {
            if (requestParam.getRepoId() == null && requestParam.getBranch() == null) {
                return new ResponseBean(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(), null);
            }
            repoId = requestParam.getRepoId();
            branch = requestParam.getBranch();
            commitId = requestParam.getCommitId();
            startTime = requestParam.getStartTime();
            if (commitId != null) {
                scanService.scan(repoId, branch, commitId);
            } else if (startTime != null) {
                scanService.scan(repoId, branch, DateTime.parse(startTime).toDate());
            } else {
                scanService.scan(repoId, branch);
            }

            return new ResponseBean(HttpStatus.OK.value(), HttpStatus.OK.name(), null);
        } catch (Exception e) {
            return new ResponseBean(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
        }
    }

    @ApiOperation(value = "获取各个工具的扫描状态", notes = "获取各个工具的扫描状态", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_id", value = "库Id", dataType = "String", required = true, defaultValue = "3ecf804e-0ad6-11eb-bb79-5b7ba969027e")
    })
    @GetMapping(value = {"/scan/status"})
    @CrossOrigin
    public ResponseBean getScanStatus(@RequestParam(name = "repo_id") String repoId) {

        try {
            return new ResponseBean(HttpStatus.OK.value(), HttpStatus.OK.name(), scanInfoService.getAllScanStatus(repoId));
        } catch (Exception e) {
            return new ResponseBean(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
        }

    }


    @Autowired
    public void setScanInfoService(ScanInfoService scanInfoService) {
        this.scanInfoService = scanInfoService;
    }

    @Autowired
    public void setScanService(ScanService scanService) {
        this.scanService = scanService;
    }
}