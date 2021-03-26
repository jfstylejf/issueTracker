package cn.edu.fudan.measureservice.controller;

import cn.edu.fudan.measureservice.core.process.JsCodeAnalyzer;
import cn.edu.fudan.measureservice.domain.ResponseBean;
import cn.edu.fudan.measureservice.domain.dto.RepoResourceDTO;
import cn.edu.fudan.measureservice.domain.dto.ScanDTO;
import cn.edu.fudan.measureservice.service.MeasureScanService;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * description:
 *
 * @author fancying
 * create: 2020-06-11 10:41
 **/
@Slf4j
@RestController
public class MeasureScanController {


    private MeasureScanService measureScanService;


    @ApiOperation(value = "接收请求开始扫描 servicePath  + \"/scan\", jsonObject, JSONObject.class 接收scan服务的请求进行扫描", httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "scanDTO", value = "repoUuid branch beginCommit", dataType = "String",required = true),
    })
    /**
     * description 接收请求开始扫描 servicePath  + "/scan", jsonObject, JSONObject.class 接收scan服务的请求进行扫描
     * @param jsonObject : repoUuid branch beginCommit
     */
    @PostMapping(value = {"/measure/{toolName}"})
    public ResponseBean scan(@RequestBody ScanDTO scanDTO,@PathVariable String toolName) {
        String repoUuid = scanDTO.getRepoUuid();
        String branch = scanDTO.getBranch();
        String beginCommit = scanDTO.getBeginCommit();
        // TODO 调用 tool scan 流程
        try {
            measureScanService.scan(RepoResourceDTO.builder().repoUuid(repoUuid).build(), branch, beginCommit);
            return ResponseBean.builder().code(200).build();
        }catch (Exception e) {
            log.error("measure scan failed! message is {}", e.getMessage());
            return ResponseBean.builder().code(500).data(e.getMessage()).build();
        }
    }

    @ApiOperation(value = "获取扫描状态 PathVariable:javancss",notes = "@return Map<String, Object>", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuid", value = "参与库", dataType = "String",required = true,defaultValue = "3ecf804e-0ad6-11eb-bb79-5b7ba969027e"),
    })
    @SuppressWarnings("unchecked")
    @GetMapping(value = {"/measure/{toolName}/scan-status"})
    public ResponseBean<Map<String, Object>> getScanStatus(@RequestParam("repo_uuid") String repoUuid,@PathVariable("toolName") String toolName) {

        try {
            //目前measure服务只有这个扫描工具
            Map<String, Object> result = (Map<String, Object>) measureScanService.getScanStatus(repoUuid);
            return new ResponseBean<>(200,"success", result);
        }catch (Exception e) {
            e.printStackTrace();
            log.error("get scan status failed! message is {}", e.getMessage());
            return new ResponseBean<>(500,"failed "+e.getMessage(),null);
        }
    }


    @ApiOperation(value = "删除扫描状态", httpMethod = "DELETE")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuid", value = "参与库", dataType = "String",required = true,defaultValue = "3ecf804e-0ad6-11eb-bb79-5b7ba969027e"),
    })
    @SuppressWarnings("unchecked")
    @DeleteMapping("/measure/{repoUuid}")
    public ResponseBean deleteRepoMeasureByRepoUuid(@PathVariable("repo_uuid")String repoUuid){
        try{
            measureScanService.delete(repoUuid);
            return new ResponseBean(200,"success",null);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(401,"failed",null);
        }
    }


    @Autowired
    public void setMeasureScanService(MeasureScanService measureScanService) {
        this.measureScanService = measureScanService;
    }

}