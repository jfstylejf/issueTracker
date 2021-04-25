package cn.edu.fudan.dependservice.controller;

import cn.edu.fudan.dependservice.component.ScanProcessor;
import cn.edu.fudan.dependservice.domain.*;
import cn.edu.fudan.dependservice.service.DependencyService;
import cn.edu.fudan.dependservice.service.TempProcess;
import cn.edu.fudan.dependservice.utill.DateHandler;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

//@Async("taskExecutor")
@Slf4j
@RestController
@EnableAsync
public class RequestForScanController {
//    private DependencyService dependencyService;
    private long waitTime;

    @Autowired
    ScanProcessor scanProcessor;

    private List<ScanRepo> toScanList;
    @Autowired
    public void setToScanList(){
        // Array list is not  thread safy
        this.toScanList=new Vector<>();
    }

//    @Autowired
//    public void setDependencyService(DependencyService dependencyService) {
//        this.dependencyService = dependencyService;
//    }
    @ApiOperation(value = "被scan服务调用来开启依赖分析的扫描", httpMethod = "POST", notes = "@return Map{\"code\": String, \"msg\": String, \"data\": List<Map>}")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "since", value = "起始时间(yyyy-MM-dd)", required = true, dataType = "String", defaultValue = "1990-01-01"),
//            @ApiImplicitParam(name = "until", value = "截止时间(yyyy-MM-dd)", required = true, dataType = "String", defaultValue = "当天"),
//            @ApiImplicitParam(name = "projectIds", value = "项目id", dataType = "String"),
//            @ApiImplicitParam(name = "interval", value = "间隔类型", dataType = "String", defaultValue = "week"),
//            @ApiImplicitParam(name = "showDetail", value = "是否展示detail", dataType = "String", defaultValue = "false")
//    })
//    @PostMapping(value = {"dependency/dependency"})
    public ResponseBean<String> startScan(@RequestBody ScanBody scanBody) {
        scanProcessor.scan(new ScanRepo());

        ScanRepo scanRepo = new ScanRepo();
        scanBody.setRepo_uuid(scanBody.getRepo_uuid());
        toScanList.add(scanRepo);

        return new ResponseBean<>(200,"successs",null);


    }
    @PostMapping(value = {"dependency/dependency"})
    public ResponseBean<String> scanByScan(@RequestBody ScanBody scanBody) {
//        scanOneRepo();
        ScanRepo scanRepo = new ScanRepo();
        scanRepo.setBranch(scanBody.getBranch());
        scanRepo.setRepoUuid(scanBody.getBranch());
        new Thread(new Runnable() {
            @Override
            public void run() {
                scanOneRepo(scanRepo);
            }
        }).start();
        return new ResponseBean<>(200,"successs",null);
    }
    //todo why can not async ,
    @Async("taskExecutor")
    public void scanOneRepo(ScanRepo scanRepo){
        try {
            toScanList.add(scanRepo);
            int size =toScanList.size();
            //many thread have a same wait time

            Thread.sleep(10*1000);
            if(toScanList.size()==size){
                List<ScanRepo> scanRepoList=new ArrayList<>(toScanList);
                toScanList.clear();
                scanProcessor.scan(scanRepoList);
            }

        }catch (InterruptedException e){
            e.printStackTrace();
        }
        log.info(Thread.currentThread().getName());
        log.info("end ");
    }
    @GetMapping(value = {"dependency/dependency/scan-status"})
    public ResponseBean<ScanStatus> getScanStatus(@RequestParam(value = "repo_uuid") String repoUuid) {
        ScanStatus ss= new ScanStatus();
        ss.setScanTime("40");
        ss.setStatus("complete");
        ss.setStartScanTime("2021-02-27 00:00:00");
        ss.setEndScanTime("2021-02-27 00:00:40");
        return new ResponseBean<>(200,"successs",ss);
    }


}



