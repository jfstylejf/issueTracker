package cn.edu.fudan.projectmanager.controller;

import cn.edu.fudan.projectmanager.domain.Project;
import cn.edu.fudan.projectmanager.domain.ResponseBean;
import cn.edu.fudan.projectmanager.service.ProjectService;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@EnableAutoConfiguration
public class ProjectController {

    private ProjectService projectService;

    @Autowired
    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    // url isPrivate username password name type branch
    @PostMapping(value = {"/project"})
    public Object addProject(HttpServletRequest request, @RequestBody JSONObject projectInfo) {
        String userToken = request.getHeader("token");
        try {
            projectService.addOneProject(userToken, projectInfo);
            return new ResponseBean(200, "add success", null);
        } catch (Exception e) {
            return new ResponseBean(401, "add failed :" + e.getMessage(), null);
        }
    }

    // List ： url isPrivate username password name type branch
//    @PostMapping(value = {"/project/addProjectList"})
//    public Object addProjectList(HttpServletRequest request, @RequestBody List<JSONObject> projectListInfo) {
//        String userToken = request.getHeader("token");
//        JSONObject obj = projectService.addProjectList(userToken, projectListInfo);
//        boolean flag = obj.getBoolean("isSuccessful");
//        String lofInfo = obj.getString("logInfo");
//        if (flag){
//            return new ResponseBean(200, "All projects were added successfully!", null);
//        }
//        return new ResponseBean(401, "At least one project was not added successfully. Logging info is showed as follow:/n" + lofInfo, null);
//
//    }

    // List ： url isPrivate username password name type branch
    @PostMapping(value = {"/project/addProjectList"})
    @ResponseBody
    public Object addProjectList(HttpServletRequest request, @RequestBody MultipartFile file) throws IOException {
        String userToken = request.getHeader("token");
        if (file.isEmpty()) {
            return "上传失败，请选择文件";
        }
        List<JSONObject> projectListInfo = projectService.getProjectListInfoFromExcelFile(file);
        JSONObject obj = projectService.addProjectList(userToken, projectListInfo);
        boolean flag = obj.getBoolean("isSuccessful");
        String lofInfo = obj.getString("logInfo");
        if (flag){
            return new ResponseBean(200, "All projects were added successfully!", null);
        }
        return new ResponseBean(401, "At least one project was not added successfully. Logging info is showed as follow:" + lofInfo, null);

    }



    //get project list
    @GetMapping(value = {"/project"})
    public Object query(HttpServletRequest request,
                        @RequestParam(name = "type",required = false,defaultValue = "bug")String type) {
        String userToken = request.getHeader("token");
        return projectService.getProjectList(userToken,type);
    }

    //jeff get project list by module
    @Deprecated
    @GetMapping(value = {"/projectByModule"})
    public Object query(HttpServletRequest request,
                        @RequestParam("module") String module,
                        @RequestParam(name = "type",required = false,defaultValue = "bug")String type) {
        String userToken = request.getHeader("token");
        return projectService.getProjectListByModule(userToken,type,module);
    }

    @GetMapping(value = {"/project/filter"})
    public Object keyWordQuery(HttpServletRequest request,
                               @RequestParam("keyWord") String keyWord,
                               @RequestParam(name = "type",required = false,defaultValue = "bug")String type) {
        String userToken = request.getHeader("token");
        return projectService.getProjectListByKeyWord(userToken, keyWord,type);
    }

    @DeleteMapping(value = {"/project/{projectId}"})
    public Object delete(@PathVariable("projectId") String projectId,
                         @RequestParam(name = "type",required = false,defaultValue = "bug")String type,
                         HttpServletRequest request) {
        try {
            projectService.remove(projectId, type,request.getHeader("token"));
            return new ResponseBean(200, "project delete success!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "project delete failed!", null);
        }
    }

    @GetMapping(value = {"/project/name"})
    public Object getProjectName(HttpServletRequest request,
                               @RequestParam("repoId") String repoId,
                               @RequestParam(name = "category",required = false,defaultValue = "bug")String category) {
        String userToken = request.getHeader("token");
        try {
            return projectService.getProjectByRepoIdAndCategory(userToken, repoId,category).getName();
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "project add failed! No such repo found!", null);
        }

//        return projectService.getProjectByRepoIdAndCategory(userToken, repoId,category).getName();

    }

    @GetMapping(value = {"/project/search"})
    public Object getProjectByCondition(HttpServletRequest request,
                                 @RequestParam(name = "category",required = false)String category,
                                 @RequestParam(name = "name", required = false)String name,
                                 @RequestParam(name = "module", required = false)String module
                                        ){
        String userToken = request.getHeader("token");
        try {
            return new ResponseBean(200, "get success", projectService.getProjectsByCondition(userToken, category,name,module));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "operate failed", null);
        }

    }


    //下面是其它服务调用的内部接口

    @PutMapping(value = {"/inner/project"})
    public Object updateProject(@RequestBody Project project) {
        try {
            projectService.updateProjectStatus(project);
            return new ResponseBean(200, "project update success!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "project update failed!", null);
        }
    }

    @GetMapping(value = {"/inner/project"})
    @CrossOrigin
    public Object getProjectByRepoId(@RequestParam("repo_id") String repo_id){
        return projectService.getProjectByRepoId(repo_id);
    }

    @GetMapping(value = {"/inner/project/{project-id}"})
    public Object getProject(@PathVariable("project-id") String project_id) {
        return projectService.getProjectByID(project_id);
    }

    @GetMapping(value = "/inner/project/repo-id")
    public String getRepoId(@RequestParam("project-id") String projectId) {
        return projectService.getRepoId(projectId);
    }

    @GetMapping(value = "/inner/project/repo-ids")
    public Object getProjectIds(@RequestParam(name = "account_id", required = false) String account_id,
                                @RequestParam("type")String type) {
        return projectService.getRepoIdsByAccountIdAndType(account_id,type);
    }

    @GetMapping(value = "/inner/projects")
    public Object getProjectByAccountId(@RequestParam(name = "account_id", required = false) String account_id){
        return projectService.getProjectByAccountId(account_id);
    }

    @GetMapping(value = "/inner/project/exist")
    public Object exist(@RequestParam("repo_id") String repo_id,@RequestParam("type")String type,@RequestParam("is_first")boolean is_first){
        return projectService.existProjectWithThisRepoIdAndType(repo_id, type,is_first);
    }

    @PutMapping(value = "/inner/project/first-auto-scan")
    public Object updateAutoScan(@RequestParam("repo_id") String repo_id,@RequestParam("type")String type){
        try {
            projectService.updateProjectFirstAutoScan(repo_id, type);
            return new ResponseBean(200, "project update success!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "project update failed!", null);
        }
    }



}
