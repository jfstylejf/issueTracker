package cn.edu.fudan.measureservice.controller;

import cn.edu.fudan.measureservice.domain.ResponseBean;
import cn.edu.fudan.measureservice.domain.dto.RepoResourceDTO;
import cn.edu.fudan.measureservice.service.MeasureScanService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    /**
     * description 接收请求开始扫描 servicePath + toolName + "/scan", jsonObject, JSONObject.class 接收scan服务的请求进行扫描
     * @param jsonObject : repoId branch beginCommit
     */
    @PostMapping(value = {"/measure/{toolName}"})
    public ResponseBean scan(@RequestBody JSONObject jsonObject,@PathVariable String toolName) {
        String repoId = "repoId";
        String branch = "branch";
        String beginCommit = "beginCommit";
        repoId = jsonObject.getString(repoId);
        branch = jsonObject.getString(branch);
        beginCommit = jsonObject.getString(beginCommit);

        // TODO 调用 tool scan 流程
        try {
            // 调用javancss工具进行扫描 目前measure服务只有这个扫描工具
            if (toolName.equals("javancss")){
                measureScanService.scan(RepoResourceDTO.builder().repoId(repoId).build(), branch, beginCommit, toolName);
            }
            return ResponseBean.builder().code(200).build();
        }catch (Exception e) {
            log.error("measure scan failed! message is {}", e.getMessage());
            return ResponseBean.builder().code(500).data(e.getMessage()).build();
        }
    }


    @GetMapping(value = {"/measure/{toolName}/scan-status"})
    public ResponseBean getScanStatus(@RequestParam String repoId,@PathVariable String toolName) {

        try {
            //目前measure服务只有这个扫描工具
            Object result = null;
            if (toolName.equals("javancss")){
                result = measureScanService.getScanStatus(repoId);
            }
            return ResponseBean.builder().code(200).data(result).build();
        }catch (Exception e) {
            log.error("get scan status failed! message is {}", e.getMessage());
            return ResponseBean.builder().code(500).data(e.getMessage()).build();
        }
    }

    @DeleteMapping("/measure/{repoId}")
    public ResponseBean deleteRepoMeasureByRepoId(@PathVariable("repoId")String repoId){
        try{
            measureScanService.delete(repoId);
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