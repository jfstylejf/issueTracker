package cn.edu.fudan.dependservice.controller;


import cn.edu.fudan.dependservice.domain.RelationData;
import cn.edu.fudan.dependservice.domain.ResponseBean;
import cn.edu.fudan.dependservice.service.RelationService;
import cn.edu.fudan.dependservice.util.ExcelUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RestController
@CrossOrigin
@Scope("singleton")
public class RelationController {
    private static final String PARAMETER_IS_EMPTY = "parameter is empty";
    private static final String NO_SUCH_PROJECT = "no such project";
    private RelationService relationService;
    private static final String RESPONSE_STATUS_SUCCESS = "success";

    @Autowired
    public void setRelationService(RelationService relationService) {
        this.relationService = relationService;
    }
    //
    @ApiOperation(value = "根据条件筛选依赖关系", httpMethod = "GET", notes = "@return Map{\"code\": String, \"msg\": String, \"data\": RelationData}")
    @GetMapping(value = {"codewisdom/depend/relation"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ps", value = "分页大小", dataType = "String", required = true,defaultValue = "10"),
            @ApiImplicitParam(name = "page", value = "第？页", dataType = "String", required = true, defaultValue = "1" ),
            @ApiImplicitParam(name = "scan_until", value = "时间点", dataType = "String", defaultValue = "now's datetime"),
            @ApiImplicitParam(name = "relation_type", value = "筛选的类型", dataType = "String", defaultValue =""),
            @ApiImplicitParam(name = "project_names", value = "筛选的项目名", dataType = "String", defaultValue ="")
    })
    @CrossOrigin
    public ResponseBean<RelationData> getRelation(@RequestParam(value = "ps") String ps,
                                                        @RequestParam(value = "page") String page,
                                                        @RequestParam(value = "project_names", required = false) String project_names,
//                                                        @RequestParam(value = "repo_uuids", required = false) String repo_uuids,
                                                        @RequestParam(value = "relation_type", required = false) String relation_type,
//                                                        @RequestParam(value = "scan_since", required = false) String scan_since,
                                                        @RequestParam(value = "scan_until", required = false) String scan_until,
                                                        @RequestParam(value = "order", required = false) String order) {
        log.info("get relation controller");
        try {
            if (ps.isEmpty() || page.isEmpty()) {
                return new ResponseBean<>(412, PARAMETER_IS_EMPTY, null);
            }
            RelationData data = relationService.getRelationShips(ps,page,project_names,relation_type,scan_until,order);

            return new ResponseBean<>(200, RESPONSE_STATUS_SUCCESS, data);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(401, e.getMessage(), null);
        }

    }
    @ApiOperation(value = "下载循环依赖关系", httpMethod = "GET", notes = "@return Map{\"code\": String, \"msg\": String, \"data\": RelationData}")
    @GetMapping(value = {"codewisdom/depend/relation/download"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "scan_until", value = "时间点", dataType = "String", required = false, defaultValue = "now's datetime"),
            @ApiImplicitParam(name = "relation_type", value = "筛选的类型", dataType = "String", defaultValue =""),
            @ApiImplicitParam(name = "project_names", value = "筛选的项目名", dataType = "String", defaultValue ="")
    })
    @CrossOrigin
    public void downloadRelation(
                                 @RequestParam(value = "project_names", required = false) String project_names,
//                                                        @RequestParam(value = "repo_uuids", required = false) String repo_uuids,
                                 @RequestParam(value = "relation_type", required = false) String relation_type,
//                                                        @RequestParam(value = "scan_since", required = false) String scan_since,
                                 @RequestParam(value = "scan_until", required = false) String scan_until,
                                 @RequestParam(value = "order", required = false) String order,
                                 HttpServletResponse response) {
        log.info("download relation controller");
        try {
            RelationData data = relationService.getRelationShips(project_names,relation_type,scan_until,order);
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setHeader("Content-Disposition", "attachment; filename=" + "depend.xls");
            response.setHeader("content-type", "application/vnd.ms-excel");
            try (HSSFWorkbook workbook = ExcelUtil.exportExcel(data)) {
                workbook.write(response.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
