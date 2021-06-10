package cn.edu.fudan.dependservice.controller;


import cn.edu.fudan.dependservice.domain.GroupData;
import cn.edu.fudan.dependservice.domain.ResponseBean;
import cn.edu.fudan.dependservice.service.GroupService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@Slf4j
@RestController
@CrossOrigin
@Scope("singleton")
public class GroupController {
    private static final String PARAMETER_IS_EMPTY = "parameter is empty";
    private static final String NO_SUCH_PROJECT = "no such project";
    private GroupService groupService;
    private static final String RESPONSE_STATUS_SUCCESS = "success";

    @Autowired
    public void setRelationService(GroupService groupService) {
        this.groupService = groupService;
    }
    //
    @ApiOperation(value = "根据条件筛选依赖组", httpMethod = "GET", notes = "@return Map{\"code\": String, \"msg\": String, \"data\": RelationData}")
    @GetMapping(value = {"codewisdom/depend/group"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ps", value = "分页大小", dataType = "String", required = true,defaultValue = "10"),
            @ApiImplicitParam(name = "page", value = "第？页", dataType = "String", required = true, defaultValue = "1" ),
            @ApiImplicitParam(name = "scan_until", value = "时间点", dataType = "String", defaultValue = "now's datetime"),
            @ApiImplicitParam(name = "project_names", value = "筛选的项目名", dataType = "String", defaultValue ="")
    })
    @CrossOrigin
    public ResponseBean<GroupData> getGroup(@RequestParam(value = "ps") String ps,
                                            @RequestParam(value = "page") String page,
                                            @RequestParam(value = "project_names", required = false) String project_names,
                                            @RequestParam(value = "scan_until", required = false) String scan_until,
                                            @RequestParam(value = "order", required = false) String order) {
        log.info("get group controller");
        try {
            if (ps.isEmpty() || page.isEmpty()) {
                return new ResponseBean<>(412, PARAMETER_IS_EMPTY, null);
            }
            GroupData data = groupService.getGroups(ps,page,project_names,scan_until,order);

            return new ResponseBean<>(200, RESPONSE_STATUS_SUCCESS, data);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(401, e.getMessage(), null);
        }

    }

}
