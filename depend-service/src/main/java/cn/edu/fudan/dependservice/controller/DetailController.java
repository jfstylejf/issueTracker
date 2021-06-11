package cn.edu.fudan.dependservice.controller;


import cn.edu.fudan.dependservice.domain.*;
import cn.edu.fudan.dependservice.service.GroupService;
import com.alibaba.fastjson.JSONObject;
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
public class DetailController {
    private GroupService groupService;

    @Autowired
    public void setRelationService(GroupService groupService) {
        this.groupService = groupService;
    }

    private static final String RESPONSE_STATUS_SUCCESS = "success";

    @ApiOperation(value = "依赖图", httpMethod = "GET", notes = "@return Map{\"code\": String, \"msg\": String, \"data\": RelationData}")

    @GetMapping(value = {"depend/detail"})

    @ApiImplicitParams({
            @ApiImplicitParam(name = "group_id", value = "group_id", dataType = "String", required = true, defaultValue = "10"),
            @ApiImplicitParam(name = "repo_uuid", value = "repo_uuid", dataType = "String", required = true, defaultValue = "1"),
            @ApiImplicitParam(name = "commit_id", value = "commit_id", dataType = "String", defaultValue = "now's datetime"),
    })
    @CrossOrigin
    public ResponseBean<JSONObject> getGroupDetail(
            @RequestParam(value = "group_id") String groupId,
            @RequestParam(value = "repo_uuid", required = false) String repouuId,
            @RequestParam(value = "commit_id", required = false) String commitId) {
        try {
            JSONObject res = groupService.getGroupDetail(groupId, commitId, repouuId);
            JSONObject data = res;
            return new ResponseBean<>(200, RESPONSE_STATUS_SUCCESS, data);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(401, e.getMessage(), null);
        }

    }
}
