package cn.edu.fudan.issueservice.controller;

import cn.edu.fudan.issueservice.domain.ResponseBean;
import cn.edu.fudan.issueservice.service.LocationService;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final String SUCCESS = "success";
    private static final String FAILED = "failed ";

    @ApiOperation(value = "获取某个方法中缺陷数，为前端显示追溯链使用", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "meta_uuid", value = "方法uuid", dataType = "String", required = true)
    })
    @GetMapping("/issue/method")
    public ResponseBean<JSONObject> getIssueIntroducers(@RequestParam(value = "meta_uuid") String metaUuid,
                                                        HttpServletRequest httpServletRequest) {
        try {
            String token = httpServletRequest.getHeader("token");
            return new ResponseBean<>(200, SUCCESS, locationService.getMethodTraceHistory(metaUuid, token));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(500, FAILED + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "获取某个方法中缺陷数，为后端codeTracker使用", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "method_name", value = "方法名", dataType = "String", required = true),
            @ApiImplicitParam(name = "file_path", value = "文件路径", dataType = "String", required = true),
            @ApiImplicitParam(name = "repo_uuid", value = "库uuid", dataType = "String", required = true),
            @ApiImplicitParam(name = "tool", value = "工具名", defaultValue = "sonarqube", allowableValues = "sonarqube , ESLint")
    })
    @GetMapping("/issue/method-count")
    public ResponseBean<Integer> getIssueCountInMethod(@RequestParam(value = "method_name") String methodName,
                                                       @RequestParam(value = "file_path") String filePath,
                                                       @RequestParam(value = "repo_uuid") String repoUuid,
                                                       @RequestParam(value = "tool") String tool) {
        try {
            return new ResponseBean<>(200, SUCCESS, locationService.getIssueCountsInMethod(methodName, filePath, repoUuid, tool));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(500, FAILED + e.getMessage(), null);
        }
    }

    @Autowired
    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }
}

