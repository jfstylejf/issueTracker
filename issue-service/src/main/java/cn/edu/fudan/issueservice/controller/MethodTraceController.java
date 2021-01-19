package cn.edu.fudan.issueservice.controller;

import cn.edu.fudan.issueservice.domain.ResponseBean;
import cn.edu.fudan.issueservice.service.LocationService;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


/**
 * @author Beethoven
 */
@RestController
@Api(value = "issue method trace", tags = {"用于方法追溯相关接口"})
public class MethodTraceController {

    private LocationService locationService;

    @ApiOperation(value = "获取某个方法中缺陷数", notes = "", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "meta_uuid", value = "方法uuid", dataType = "String", required = true)
    })
    @GetMapping("/issue/method")
    public ResponseBean<JSONObject> getIssueIntroducers(@RequestParam(value = "meta_uuid")String metaUuid,
                                                        HttpServletRequest httpServletRequest) {
        try {
            String token = httpServletRequest.getHeader("token");
            return new ResponseBean<>(200, "success", locationService.getMethodTraceHistory(metaUuid, token));
        }catch (Exception e){
            return new ResponseBean<>(500, "failed " + e.getMessage(), null);
        }
    }

    @Autowired
    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }
}

