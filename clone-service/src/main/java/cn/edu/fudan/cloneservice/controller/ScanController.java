package cn.edu.fudan.cloneservice.controller;

import cn.edu.fudan.cloneservice.component.RestInterfaceManager;
import cn.edu.fudan.cloneservice.domain.ResponseBean;
import cn.edu.fudan.cloneservice.domain.clone.CloneRepo;
import cn.edu.fudan.cloneservice.service.CloneMeasureService;
import cn.edu.fudan.cloneservice.service.ScanService;
import cn.edu.fudan.cloneservice.task.CPUCloneScanOperation;
import com.alibaba.fastjson.JSONObject;
import com.company.model.Clone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static org.reflections.Reflections.log;

/**
 * @author zyh yp wgc
 * @date 2020/10/23
 */
@CrossOrigin
@RestController
public class ScanController {

    private ScanService scanService;

    private CloneMeasureService cloneMeasureService;

    @PostMapping(value = {"/clone/saga-cpu"})
    public Object scan(@RequestBody JSONObject requestParam) {
        try {
            String repoId = requestParam.getString("repoUuid");
            String beginCommit = requestParam.getString("beginCommit");
            String branch = requestParam.getString("branch");
            scanService.cloneScan(repoId, beginCommit, branch);
            return new ResponseBean<>(200, "scan msg send success!", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, e.getMessage(), null);
        }
    }

    @GetMapping(value = {"/clone/saga-cpu/scan-status"})
    public ResponseBean<CloneRepo> getCloneRepo(@RequestParam("repo_uuid") String repoId) {
        try {
            CloneRepo result = scanService.getLatestCloneRepo(repoId);
            if(result == null){
                result = new CloneRepo();
                result.setStatus("failed");
            }
            return new ResponseBean<>(200, "scan msg send success!", result);
        } catch (Exception e) {
            return new ResponseBean<>(401, e.getMessage(), null);
        }
    }

    @DeleteMapping("/cloneScan/{repo_uuid}")
    public Object deleteCloneScan(@PathVariable("repo_uuid") String repoId){
        try {
            scanService.deleteCloneScan(repoId);
            return new ResponseBean<>(HttpStatus.OK.value(), "scan msg send success!", null);
        } catch (Exception e) {
            return new ResponseBean<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
        }
    }

    @PostMapping(value = {"/clone/scanCloneMeasure"})
    public Object scanCloneMeasure(@RequestBody JSONObject requestParam) {
        try {
            String repoId = requestParam.getString("repoUuid");
            String commit = requestParam.getString("commit");
            String repoPath = requestParam.getString("repoPath");
            cloneMeasureService.insertCloneMeasure(repoId, commit, repoPath);
            return new ResponseBean<>(200, "scan msg send success!", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, e.getMessage(), null);
        }
    }

    @Autowired
    public void setScanService(ScanService scanService) {
        this.scanService = scanService;
    }

    @Autowired
    public void setCloneMeasureService(CloneMeasureService cloneMeasureService){this.cloneMeasureService = cloneMeasureService;}
}
