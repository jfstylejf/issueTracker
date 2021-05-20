package cn.edu.fudan.dependservice.controller;


import cn.edu.fudan.dependservice.domain.DependencyInfo;
import cn.edu.fudan.dependservice.domain.ResponseBean;
import cn.edu.fudan.dependservice.service.DependencyService;
import cn.edu.fudan.dependservice.util.DateHandler;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin
public class RequestController {
    private static final String PARAMETER_IS_EMPTY = "parameter is empty";
    private static final String NO_SUCH_PROJECT = "no such project";
    private DependencyService dependencyService;
    private static final String RESPONSE_STATUS_SUCCESS = "success";

    @Autowired
    public void setDependencyService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    @ApiOperation(value = "获取在循环依赖中文件的数量", httpMethod = "GET", notes = "@return Map{\"code\": String, \"msg\": String, \"data\": List<Map>}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "since", value = "起始时间(yyyy-MM-dd)", required = true, dataType = "String"),
            @ApiImplicitParam(name = "until", value = "截止时间(yyyy-MM-dd)", required = true, dataType = "String"),
            @ApiImplicitParam(name = "project_ids", value = "项目id, 以逗号间隔", dataType = "String",defaultValue = "所有的project"),
            @ApiImplicitParam(name = "interval", value = "间隔类型", dataType = "String", defaultValue = "week"),
            @ApiImplicitParam(name = "detail", value = "是否展示detail", dataType = "String", defaultValue = "false")
    })
    @GetMapping(value = {"/dependency/test"})
    @CrossOrigin
    @Deprecated
    public ResponseBean<List<DependencyInfo>> getFileNumInCycle(@RequestParam(value = "since") String beginDate,
                                                              @RequestParam(value = "until") String endDate,
                                                              @RequestParam(value = "project_ids", required = false) String project_ids,
                                                              @RequestParam(value = "interval", required = false) String interval,
                                                              @RequestParam(value = "detail", required = false) String showDetail) {
        String projectIds=project_ids;

        try {
            if (beginDate.isEmpty() || endDate.isEmpty()) {
                return new ResponseBean<>(412, PARAMETER_IS_EMPTY, null);
            }
            if (interval == null) {
                interval = "week";
            }
            if (showDetail == null) {
                showDetail = "false";
            }
            List<String> dates = DateHandler.handleParamDate(beginDate, endDate);
            List<DependencyInfo> data = dependencyService.getDependencyNumWithDate(dates.get(0), dates.get(1), projectIds, interval, showDetail, "method");

            if (data.get(0).getProjectName() == null) {
                return new ResponseBean<>(412, NO_SUCH_PROJECT, null);
            }
            return new ResponseBean<>(200, RESPONSE_STATUS_SUCCESS, data);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(401, e.getMessage(), null);
        }

    }
    @ApiOperation(value = "获取在循环依赖中文件的数量", httpMethod = "GET", notes = "@return Map{\"code\": String, \"msg\": String, \"data\": List<Map>}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "since", value = "起始时间(yyyy-MM-dd)", required = true, dataType = "String"),
            @ApiImplicitParam(name = "until", value = "截止时间(yyyy-MM-dd)", required = true, dataType = "String"),
            @ApiImplicitParam(name = "project_ids", value = "项目id, 以逗号间隔", dataType = "String",defaultValue = "所有的project"),
            @ApiImplicitParam(name = "interval", value = "间隔类型", dataType = "String", defaultValue = "week"),
            @ApiImplicitParam(name = "detail", value = "是否展示detail", dataType = "String", defaultValue = "false")
    })
    @GetMapping(value = {"/dependency"})
    @CrossOrigin
    public ResponseBean<List<DependencyInfo>> getFileNumInCycle2(@RequestParam(value = "since") String beginDate,
                                                                @RequestParam(value = "until") String endDate,
                                                                @RequestParam(value = "project_ids", required = false) String project_ids,
                                                                @RequestParam(value = "interval", required = false) String interval,
                                                                @RequestParam(value = "detail", required = false) String showDetail) {
        String projectIds=project_ids;

        try {
            if (beginDate.isEmpty() || endDate.isEmpty()) {
                return new ResponseBean<>(412, PARAMETER_IS_EMPTY, null);
            }
            if (interval == null) {
                interval = "week";
            }
            if (showDetail == null) {
                showDetail = "false";
            }
            List<String> dates = DateHandler.handleParamDate(beginDate, endDate);
            List<DependencyInfo> data = dependencyService.getDependencyNum2(dates.get(0), dates.get(1), projectIds, interval, showDetail, "method");

            if (data.get(0).getProjectName() == null) {
                return new ResponseBean<>(412, NO_SUCH_PROJECT, null);
            }
            return new ResponseBean<>(200, RESPONSE_STATUS_SUCCESS, data);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(401, e.getMessage(), null);
        }

    }

}
