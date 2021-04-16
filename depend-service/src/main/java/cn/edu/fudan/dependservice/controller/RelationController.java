package cn.edu.fudan.dependservice.controller;


import cn.edu.fudan.dependservice.domain.*;
import cn.edu.fudan.dependservice.service.DependencyService;
import cn.edu.fudan.dependservice.service.RelationService;
import cn.edu.fudan.dependservice.utill.DateHandler;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin
public class RelationController {
    private static final String PARAMETER_IS_EMPTY = "parameter is empty";
    private static final String NO_SUCH_PROJECT = "no such project";
    private RelationService relationService;

    private static final String RESPONSE_STATUS_SUCCESS = "success,but fake";

    @Autowired
    public void setRelationService(RelationService relationService) {
        this.relationService = relationService;
    }

    @ApiOperation(value = "获取循环依赖中依赖关系的细节", httpMethod = "GET", notes = "@return Map{\"code\": String, \"msg\": String, \"data\": List<Map>}")
    @GetMapping(value = {"codewisdom/depend/relation"})
    @CrossOrigin
    public ResponseBean<List<RelationView>> getRelation(@RequestParam(value = "ps") String ps,
                                                        @RequestParam(value = "page") String page,
                                                        @RequestParam(value = " project_names", required = false) String project_names,
                                                        @RequestParam(value = " repo_uuids", required = false) String repo_uuids,
                                                        @RequestParam(value = " relation_type", required = false) String relation_type,
                                                        @RequestParam(value = " scan_since", required = false) String scan_since,
                                                        @RequestParam(value = "scan_until", required = false) String scan_until) {
        try {
            if (ps.isEmpty() || page.isEmpty()) {
                return new ResponseBean<>(412, PARAMETER_IS_EMPTY, null);
            }
//            List<DependencyInfo> data = dependencyService.getDependencyNum(dates.get(0), dates.get(1), projectIds, interval, showDetail, "method");
            List<RelationView> data = relationService.getRelationShips();

            return new ResponseBean<>(200, RESPONSE_STATUS_SUCCESS, data);
        } catch (Exception e) {
            return new ResponseBean<>(401, e.getMessage(), null);
        }

    }

    @ApiOperation(value = "获取循环依赖中依赖关系的细节", httpMethod = "GET", notes = "@return Map{\"code\": String, \"msg\": String, \"data\": List<Map>}")


    @GetMapping(value = {"/dependency/detail"})
    @CrossOrigin
    public ResponseBean<List<DependencyInfo>> getDependencyRelation(@RequestParam(value = "ps") String ps,
                                                                    @RequestParam(value = "page") String page,
                                                                    @RequestParam(value = "asc", required = false) String asc,
                                                                    @RequestParam(value = "order", required = false) String order) {
        try {
            if (ps.isEmpty() || page.isEmpty()) {
                return new ResponseBean<>(412, PARAMETER_IS_EMPTY, null);
            }
            if (asc == null) {
                asc = "true";
            }
            if (order == null) {
                order = "group_id";
            }
//            List<DependencyInfo> data = dependencyService.getDependencyNum(dates.get(0), dates.get(1), projectIds, interval, showDetail, "method");
            List<DependencyInfo> data = null;
//                dependencyService.getDependencyNumWithDate(dates.get(0), dates.get(1), projectIds, interval, showDetail, "method");

            if (data.get(0).getProjectName() == null) {
                return new ResponseBean<>(412, NO_SUCH_PROJECT, null);
            }
            return new ResponseBean<>(200, RESPONSE_STATUS_SUCCESS, data);
        } catch (Exception e) {
            return new ResponseBean<>(401, e.getMessage(), null);
        }

    }


}
