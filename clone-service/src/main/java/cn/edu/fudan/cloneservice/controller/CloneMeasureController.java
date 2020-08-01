package cn.edu.fudan.cloneservice.controller;

import cn.edu.fudan.cloneservice.domain.ResponseBean;
import cn.edu.fudan.cloneservice.service.CloneMeasureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author znj
 * @date 2020/5/25
 */
@CrossOrigin
@RestController
public class CloneMeasureController {

    @Autowired
    CloneMeasureService cloneMeasureService;

//    @GetMapping(value = {"/cloneMeasure/insertMeasureClone"})
//    public ResponseBean insertMeasureClone(@RequestParam("repo_id") String repoId, @RequestParam("commit_id") String commitId){
//
//        try{
//            return new ResponseBean(200,"success",cloneMeasureService.insertCloneMeasure(repoId, commitId));
//        }catch (Exception e){
//            e.printStackTrace();
//            return new ResponseBean(401,"failed",null);
//        }
//    }

    @GetMapping(value = {"/cloneMeasure/latestCloneLines"})
    public ResponseBean getLatestCloneLines(@RequestParam("repo_id") String repoId){

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
    @GetMapping(value = {"/cloneMeasure/getMeasureClone"})
    public ResponseBean getMeasureCloneData(@RequestParam(value = "repo_id", required = false, defaultValue = "") String repoId,
                                            @RequestParam("developer") String developer,
                                            @RequestParam("start") String start,
                                            @RequestParam("end") String end){
        try{
            return new ResponseBean(200,"success",cloneMeasureService.getCloneMeasure(repoId,developer,start,end));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(401,"failed",null);
        }
    }

    @DeleteMapping(value = {"/cloneMeasure/{repoId}"})
    public Object deleteScans(@PathVariable("repoId") String repoId) {
        try {
            cloneMeasureService.deleteCloneMeasureByRepoId(repoId);
            return new ResponseBean(200, "clone measure delete success!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "clone measure delete failed", null);
        }
    }

    @GetMapping(value = {"/cloneMeasure/scan"})
    public ResponseBean scan(@RequestParam("repoId") String repoId,
                             @RequestParam("startCommitId") String commitId){
        try{
            cloneMeasureService.scanCloneMeasure(repoId,commitId);
            return new ResponseBean(200,"success",null);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(401,"failed",null);
        }
    }

}
