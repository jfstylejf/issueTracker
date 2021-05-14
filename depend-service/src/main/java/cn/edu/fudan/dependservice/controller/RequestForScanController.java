package cn.edu.fudan.dependservice.controller;

import cn.edu.fudan.dependservice.component.ScanProcessor;
import cn.edu.fudan.dependservice.domain.*;
import cn.edu.fudan.dependservice.service.ScanService;
import cn.edu.fudan.dependservice.service.StatusService;
import cn.edu.fudan.dependservice.util.TimeUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@Slf4j
@RestController
@EnableAsync
public class RequestForScanController {

    ScanService scanService;

    @Autowired
    public void setScanService(ScanService scanService) {
        this.scanService = scanService;
    }


    @Autowired
    ScanProcessor scanProcessor;
    @Autowired
    StatusService statusService;
    private List<ScanRepo> toScanList;
    @Autowired
    public void setToScanList(){
        this.toScanList=new Vector<>();
    }
    @ApiOperation(value = "被scan服务调用来开启依赖分析的扫描", httpMethod = "POST", notes = "@return Map{\"code\": String, \"msg\": String, \"data\":null}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "scanBody", value = "开发人员信息列表", dataType = "ScanBody", required = true)
    })
    @PostMapping(value = {"dependency/dependency"})
    public ResponseBean<String> scantest(@RequestBody ScanBody scanBody) {
        log.info("scan by scan" );
        log.info("scanBody.getRepo_uuid():"+scanBody.getRepoUuid());
        if(canInvoke(scanBody.getRepoUuid())){
            ScanRepo scanRepo = initScanRepo(scanBody);
            scanService.scanOneRepo(scanRepo,toScanList);
            String data ="invoke success";
            return new ResponseBean<>(200,"success",data);
        }else {
            String data ="invoke fail";
            return new ResponseBean<>(200,"success",data);
        }

    }
    public boolean canInvoke(String repouuid){
        if(statusService.canScan(repouuid)){
            return true;
        }else{
            ScanRepo scanRepo =new ScanRepo();
            scanRepo.setRepoUuid(repouuid);
            ScanStatus scanStatus=new ScanStatus();
            scanStatus.setStatus("fail");
            scanStatus.setTs_start(0);
            scanStatus.setTs_end(0);
            scanStatus.setScanTime("0");
            scanStatus.setMsg("CAN NOT SCAN");
            scanStatus.setEndScanTime(TimeUtil.getCurrentDateTime());
            scanRepo.setScanStatus(scanStatus);
            scanService.canNotScan(scanRepo);
            return false;
        }
    }

    @ApiOperation(value = "获取扫描状态", httpMethod = "GET", notes = "@return Map{\"code\": String, \"msg\": String, \"data\":ScanStatus}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuid", value = "repouuid", dataType = "String", required = true)
    })
    @GetMapping(value = {"dependency/dependency/scan-status"})
    public ResponseBean<ScanStatus> getScanStatus(@RequestParam(value = "repo_uuid") String repoUuid) {
        // get if in scanning by processor
        // get from data base ,最近的一次成功失败
        // todo get scanrepo from
        ScanStatus scanStatus=getFromWaitQueue(repoUuid);
        if(scanStatus==null){
            scanStatus =scanProcessor.getScanStatus(repoUuid);
        }
        if(scanStatus!=null){
            scanStatus.setStatus("scanning");
            String scanTime =String.valueOf((System.currentTimeMillis()-scanStatus.getTs_start())/1000);
            scanStatus.setScanTime(scanTime);
            return new ResponseBean<>(200,"success",scanStatus);
        }
        //go to database for status
        scanStatus=statusService.getScanStatus(repoUuid);
        log.info("scanStatus-> {}",scanStatus.getStatus());

        return new ResponseBean<>(200,"success",scanStatus);
    }
    public ScanStatus getFromWaitQueue(String repouuid){
        for(ScanRepo s:toScanList){
            if(s.getRepoUuid().equals(repouuid)){
                return s.getScanStatus();
            }
        }
        return null;
    }
    public ScanRepo initScanRepo(ScanBody scanBody){
        ScanRepo scanRepo=new ScanRepo();
        scanRepo.setBranch(scanBody.getBranch());
        scanRepo.setRepoUuid(scanBody.getRepoUuid());
        ScanStatus scanStatus =new ScanStatus();
        scanRepo.setToScanDate(TimeUtil.getCurrentDate());
        scanStatus.setStartScanTime(TimeUtil.getCurrentDateTime());
        scanStatus.setTs_start(System.currentTimeMillis());
        scanRepo.setScanStatus(scanStatus);
        return scanRepo;
    }


}



