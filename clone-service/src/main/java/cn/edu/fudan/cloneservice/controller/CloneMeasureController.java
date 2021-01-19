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

import java.util.*;

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
            return new ResponseBean<>(200,"success",cloneMeasureService.getLatestCloneMeasure(repoId));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(401,"failed",null);
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
    public ResponseBean<Object> getMeasureCloneData(@RequestParam(value = "repo_uuids", defaultValue = "") String repoId,
                                                    @RequestParam(value = "developers", required = false) String developers,
                                                    @RequestParam(value = "since", required = false) String start,
                                                    @RequestParam(value = "until", required = false) String end,
                                                    @RequestParam(value = "order",required = false, defaultValue = "") String order,
                                                    @RequestParam(value = "page", required = false, defaultValue = "1") String page,
                                                    @RequestParam(value = "ps", required = false, defaultValue = "5") String size,
                                                    @RequestParam(value = "asc", required = false) Boolean isAsc){
        try{
            List<CloneMessage> result = cloneMeasureService.getCloneMeasure(repoId, developers, start, end, page, size, isAsc, order);
            if (!StringUtils.isEmpty(developers) && !result.isEmpty()) {
                return new ResponseBean<>(200,"success", result);
            }
            else{
                List<CloneMessage> cloneMessageSorted = cloneMeasureService.sortByOrder(result, order);
                List<CloneMessage> cloneMessageList = new ArrayList<>();
                if (page != null && size != null) {
                    int pageDigit = Integer.parseInt(page);
                    int sizeDigit = Integer.parseInt(size);
                    if (isAsc != null && !isAsc) {
                        Collections.reverse(cloneMessageSorted);
                    }
                    int index = (pageDigit - 1) * sizeDigit;
                    while ((index < cloneMessageSorted.size()) && (index < pageDigit * sizeDigit)) {
                        cloneMessageList.add(cloneMessageSorted.get(index));
                        index += 1;
                    }
                }
                Map<String, Object> data = new HashMap<>();
                data.put("page", page);
                data.put("total", cloneMessageSorted.size()/Integer.parseInt(size) + 1);
                data.put("records", cloneMessageSorted.size());
                data.put("rows", cloneMessageList);
                return new ResponseBean<>(200, "success", data);
            }
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(401,"failed",null);
        }
    }


}
