package cn.edu.fudan.cloneservice.controller;

import cn.edu.fudan.cloneservice.domain.CloneMeasure;
import cn.edu.fudan.cloneservice.domain.CloneMessage;
import cn.edu.fudan.cloneservice.domain.ResponseBean;
import cn.edu.fudan.cloneservice.service.CloneMeasureService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author znj yp wgc
 * @date 2020/10/23
 */
@CrossOrigin
@RestController
public class CloneMeasureController {

    @Autowired
    CloneMeasureService cloneMeasureService;

    @ApiOperation(value = "最新版本克隆行数信息", notes = "@return cloneMeasure", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name="repo_uuid", value = "repo_uuid", dataType = "String")
    })
    @GetMapping(value = {"/cloneMeasure/latestCloneLines"})
    public ResponseBean<CloneMeasure> getLatestCloneLines(@RequestParam("repo_uuid") String repoId){

        try{
            return new ResponseBean(200,"success",cloneMeasureService.getLatestCloneMeasure(repoId));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(401,"failed",null);
        }
    }

    /**
     * fixme 暂时只有一个用户没有做鉴权操作
     */
    @ApiOperation(value = "获得克隆信息有关的度量", notes = "@return cloneMeasure", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name="repo_uuid", value = "repo_uuid", dataType = "String",required = false,defaultValue = ""),
            @ApiImplicitParam(name = "developer", value = "developer",required = false),
            @ApiImplicitParam(name = "start", value = "开始时间"),
            @ApiImplicitParam(name = "end", value = "结束时间")
    })
    @GetMapping(value = {"/cloneMeasure"})
    public ResponseBean<Object> getMeasureCloneData(@RequestParam(value = "repo_uuid", required = false, defaultValue = "") String repoId,
                                                    @RequestParam(value = "developer", required = false) String developer,
                                                    @RequestParam(value = "start", required = false) String start,
                                                    @RequestParam(value = "end", required = false) String end,
                                                    @RequestParam(value = "page", required = false, defaultValue = "1") String page,
                                                    @RequestParam(value = "size", required = false, defaultValue = "5") String size,
                                                    @RequestParam(value = "desc", required = false) Boolean isDesc){
        try{
            List<CloneMessage> result = cloneMeasureService.getCloneMeasure(repoId, developer, start, end, page, size, isDesc);
            Object data = result;
            if (! StringUtils.isEmpty(developer) && result.size() > 0) {
                data = result.get(0);
            }
            return new ResponseBean(200,"success", data);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(401,"failed",null);
        }
    }


}
