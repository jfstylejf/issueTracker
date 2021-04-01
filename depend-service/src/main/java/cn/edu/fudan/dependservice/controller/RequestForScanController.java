package cn.edu.fudan.dependservice.controller;

import cn.edu.fudan.dependservice.domain.DependencyInfo;
import cn.edu.fudan.dependservice.domain.ResponseBean;
import cn.edu.fudan.dependservice.domain.ScanBody;
import cn.edu.fudan.dependservice.domain.ScanStatus;
import cn.edu.fudan.dependservice.service.DependencyService;
import cn.edu.fudan.dependservice.service.TempProcess;
import cn.edu.fudan.dependservice.utill.DateHandler;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RequestForScanController {
    private DependencyService dependencyService;

    @Autowired
    TempProcess tempProcess;

    @Autowired
    public void setDependencyService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }
    @ApiOperation(value = "被scan服务调用来开启依赖分析的扫描", httpMethod = "POST", notes = "@return Map{\"code\": String, \"msg\": String, \"data\": List<Map>}")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "since", value = "起始时间(yyyy-MM-dd)", required = true, dataType = "String", defaultValue = "1990-01-01"),
//            @ApiImplicitParam(name = "until", value = "截止时间(yyyy-MM-dd)", required = true, dataType = "String", defaultValue = "当天"),
//            @ApiImplicitParam(name = "projectIds", value = "项目id", dataType = "String"),
//            @ApiImplicitParam(name = "interval", value = "间隔类型", dataType = "String", defaultValue = "week"),
//            @ApiImplicitParam(name = "showDetail", value = "是否展示detail", dataType = "String", defaultValue = "false")
//    })
    @PostMapping(value = {"dependency/dependency"})
    public ResponseBean<String> startScan(@RequestBody ScanBody scanBody) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                tempProcess.scan(scanBody.getRepo_uuid(),scanBody.getBranch(),scanBody.getBegin_commit());

            }
        }).start();
        return new ResponseBean<>(200,"successs",null);


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



