package cn.edu.fudan.cloneservice.controller;

import cn.edu.fudan.cloneservice.domain.ResponseBean;
import cn.edu.fudan.cloneservice.service.ScanService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author zyh
 * @date 2020/5/27
 */
@CrossOrigin
@RestController
public class ScanController {

    @Autowired
    private ScanService scanService;

    @GetMapping("/cloneScan")
    public void cloneScan(@RequestParam("repo_id") String repoId,
                     @RequestParam("commit_id") String commitId){
        scanService.cloneScan(repoId, commitId, null);
    }

    @PostMapping(value = {"/clone/saga-cpu"})
    public Object scan(@RequestBody JSONObject requestParam) {
        try {
            String repoId = requestParam.getString("repoId");
            String beginCommit = requestParam.getString("beginCommit");
            String branch = requestParam.getString("branch");
            scanService.cloneScan(repoId, beginCommit, branch);
            return new ResponseBean(200, "scan msg send success!", null);
        } catch (Exception e) {
            return new ResponseBean(401, e.getMessage(), null);
        }
    }

    @GetMapping(value = {"/clone/saga-cpu/scan-status"})
    public Object getCloneRepo(@RequestParam("repoId") String repoId) {
        try {
            return new ResponseBean(200, "scan msg send success!", scanService.getLatestCloneRepo(repoId));
        } catch (Exception e) {
            return new ResponseBean(401, e.getMessage(), null);
        }
    }

    @DeleteMapping("/cloneScan/{repoId}")
    public void deleteCloneScan(@PathVariable("repoId") String repoId){
        scanService.deleteCloneScan(repoId);
    }

}
