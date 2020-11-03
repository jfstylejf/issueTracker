package cn.edu.fudan.scanservice.controller;

import cn.edu.fudan.scanservice.domain.dto.ResponseBean;
import cn.edu.fudan.scanservice.service.ToolManagementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;


/**
 * description: 工具配置
 *
 * @author fancying
 * create: 2020-04-25 11:26
 **/
@Api(value = "ToolManagementController", tags = {"扫描工具的管理接口"})
@RestController
public class ToolManagementController {

    private ToolManagementService  toolManagementService;

    @ApiIgnore
    @PatchMapping(value = "/tool")
    @Deprecated
    public ResponseBean modifyDisplayData(){
        return ResponseBean.builder()
                .code(HttpStatus.OK.value())
                .msg(HttpStatus.OK.name())
                .data(toolManagementService.getAllTools()).build();
    }


    /**
     * 设置是否启用工具
     */
    @ApiOperation(value = "设置是否启用工具", notes = "设置是否启用工具", httpMethod = "PATCH")
    @PatchMapping(value = "/tool/{id}/status")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",
                    value = "tool id:\n1代表findbugs\n2代表sonarqube\n3代表sage-cpu\n4代表codeTracker\n5代表javancss",  required = true, allowableValues = "1,2,3,4,5"),
            @ApiImplicitParam(name = "enabled", value = "设置工具启用状态\n0代表关闭\n1代表开启", required = true, allowableValues = "0,1")
    })
    public ResponseBean modifyToolStatus(@PathVariable("id") int id, @RequestParam("enabled") int enabled){
        return ResponseBean.builder()
                .code(HttpStatus.OK.value())
                .msg(HttpStatus.OK.name())
                .data(toolManagementService.modifyToolStatus(id, enabled)).build();
    }


    @Autowired
    public void setToolManagementService(ToolManagementService toolManagementService) {
        this.toolManagementService = toolManagementService;
    }
}