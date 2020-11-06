package cn.edu.fudan.cloneservice.controller;

import cn.edu.fudan.cloneservice.domain.ResponseBean;
import cn.edu.fudan.cloneservice.domain.clone.CloneRepo;
import cn.edu.fudan.cloneservice.service.ScanService;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author zyh yp wgc
 * @date 2020/10/23
 */
@CrossOrigin
@RestController
public class ScanController {

    @Autowired
    private ScanService scanService;

    @ApiOperation(value = "克隆扫描", notes = "将clone信息扫描入库", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name="repo_uuid", value = "repo_uuid", dataType = "String"),
            @ApiImplicitParam(name = "begin_commit", value = "开始于哪个commit",dataType = "String"),
            @ApiImplicitParam(name = "branch", value = "分支")
    })
    @PostMapping(value = {"/clone/saga-cpu"})
    public Object scan(@RequestBody JSONObject requestParam) {
        try {
            String repoId = requestParam.getString("repo_uuid");
            String beginCommit = requestParam.getString("begin_commit");
            String branch = requestParam.getString("branch");
            scanService.cloneScan(repoId, beginCommit, branch);
            return new ResponseBean(200, "scan msg send success!", null);
        } catch (Exception e) {
            return new ResponseBean(401, e.getMessage(), null);
        }
    }

    @ApiOperation(value = "返回扫描状态", notes = "@return cloneScan", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name="repo_uuid", value = "repo_uuid", dataType = "String", required = true)
    })
    @GetMapping(value = {"/clone/saga-cpu/scan-status"})
    public ResponseBean<CloneRepo> getCloneRepo(@RequestParam("repo_uuid") String repoId) {
        try {
            return new ResponseBean(200, "scan msg send success!", scanService.getLatestCloneRepo(repoId));
        } catch (Exception e) {
            return new ResponseBean(401, e.getMessage(), null);
        }
    }

    @ApiOperation(value = "根据repoId删除所有与clone有关的表中的信息", notes = "void", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name="repo_uuid", value = "repo_uuid", dataType = "String")
    })
    @DeleteMapping("/cloneScan/{repo_uuid}")
    public void deleteCloneScan(@PathVariable("repo_uuid") String repoId){
        scanService.deleteCloneScan(repoId);
    }

}
