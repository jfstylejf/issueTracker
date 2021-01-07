package cn.edu.fudan.issueservice.controller;

import cn.edu.fudan.issueservice.domain.ResponseBean;
import cn.edu.fudan.issueservice.service.LocationService;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Beethoven
 */
@RestController
@Api(value = "issue method trace", tags = {"用于方法追溯相关接口"})
public class MethodTraceController {

    private LocationService locationService;

    public ResponseBean<JSONObject> getIssueIntroducers(@RequestParam(value = "meta_uuid")String metaUuid) {
        try {
            return new ResponseBean<>(200, "success", locationService.getMethodTraceHistory(metaUuid));
        }catch (Exception e){
            return new ResponseBean<>(500, "failed " + e.getMessage(), null);
        }
    }

    @Autowired
    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }
}

