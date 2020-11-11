package cn.edu.fudan.projectmanager.controller;

import cn.edu.fudan.projectmanager.domain.ResponseBean;
import cn.edu.fudan.projectmanager.domain.SubRepository;
import cn.edu.fudan.projectmanager.domain.dto.RepositoryDTO;
import cn.edu.fudan.projectmanager.domain.vo.RepositoryVO;
import cn.edu.fudan.projectmanager.service.ProjectControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author fancying
 */
@RestController
public class ProjectController {

    private ProjectControlService projectControl;
    private final String TOKEN = "token";

    /**
     * description: 添加项目
     * @param repositoryDTO url isPrivate username password name type branch
     * @param request header
     */
    @PostMapping(value = {"/project"})
    public ResponseBean addProject(HttpServletRequest request, @RequestBody RepositoryDTO repositoryDTO) {
        String token = request.getHeader(TOKEN);
        try {
            projectControl.addOneRepo(token, repositoryDTO);
            return new ResponseBean<>(200, "add success", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, "add failed :" + e.getMessage(), null);
        }
    }

    /**
     * 暂时不可用
     */
    @PostMapping(value = {"/project/multiple"})
    @ResponseBody
    public ResponseBean<Map<String, Boolean>> addProjectList(HttpServletRequest request, @RequestBody MultipartFile file) throws IOException {
        String userToken = request.getHeader(TOKEN);
        if (file.isEmpty()) {
            return new ResponseBean<>(400, "上传失败，请选择文件!", null);
        }
        List<RepositoryDTO> repositories = analysisFile(file);
        Map<String, Boolean> result =  projectControl.addRepos(userToken, repositories);
        if (result.values().contains(Boolean.FALSE)) {
            return new ResponseBean<>(400, "not all repo add success", result);
        }
        return new ResponseBean<>(200, "add success", result);

//        List<JSONObject> projectListInfo = projectService.getProjectListInfoFromExcelFile(file);
//        JSONObject obj = projectService.addProjectList(userToken, projectListInfo);
//        boolean flag = obj.getBoolean("isSuccessful");
//        String lofInfo = obj.getString("logInfo");
//        if (flag){
//            return new ResponseBean<>(200, "All projects were added successfully!", null);
//        }
//        return new ResponseBean<>(401, "At least one projectName was not added successfully. Logging info is showed as follow:" + lofInfo, null);
    }

    /**
     * TODO
     */
    private List<RepositoryDTO> analysisFile(MultipartFile file) {
        return new ArrayList<>(0);
    }

    /**
     * 只给超级管理员提供
     */
    @DeleteMapping(value = {"/project/{sub_repo_uuid}"})
    public ResponseBean delete(HttpServletRequest request, @PathVariable("sub_repo_uuid") String subRepoId,
                               @RequestParam(value = "empty", required = false,defaultValue = "false") Boolean empty) {
        try {
            projectControl.delete(request.getHeader(TOKEN), subRepoId, empty);
            return new ResponseBean<>(200, "delete success!", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, "delete failed! " + e.getMessage() , null);
        }
    }



    @PutMapping(value = {"/project"})
    public ResponseBean updateProject(HttpServletRequest request, @RequestParam("old_name") String oldName,
                                      @RequestParam("new_name") String newName,
                                      @RequestParam(value = "type", required = false, defaultValue = "repository") String type){
        try {
            projectControl.update(request.getHeader(TOKEN), oldName, newName, type);
            return new ResponseBean<>(200, "update success", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, "update failed :" + e.getMessage(), null);
        }
    }


    @GetMapping(value = {"/project"})
    public ResponseBean<List<RepositoryVO>> query(HttpServletRequest request,
                              @RequestParam(name = "recycled",required = false, defaultValue = "0") int recycled) {
        String userToken = request.getHeader(TOKEN);
        List<SubRepository> subRepositories = projectControl.query(userToken);
        List<RepositoryVO> repositoryVos = new ArrayList<>(subRepositories.size());
        subRepositories.stream().filter(s -> s.getRecycled() == recycled).
                forEach(s -> repositoryVos.add(new RepositoryVO(s)));

        return new ResponseBean<>(200, "success", repositoryVos);
    }

//    //jeff get projectName list by module
//    @Deprecated
//    @GetMapping(value = {"/projectByModule"})
//    public ResponseBean query(HttpServletRequest request,
//                        @RequestParam("module") String module,
//                        @RequestParam(name = "type",required = false,defaultValue = "bug")String type) {
//        String userToken = request.getHeader(TOKEN);
//        return projectService.getProjectListByModule(userToken,type,module);
//    }
//
//    @Deprecated
//    @GetMapping(value = {"/project/filter"})
//    public ResponseBean keyWordQuery(HttpServletRequest request,
//                               @RequestParam(name = "key_word", required = false )String keyWord,
//                               @RequestParam(name = "repo",required = false) String module,
//                               @RequestParam(name = "type",required = false, defaultValue = "bug")String type,
//                               @RequestParam(name = "isRecycled",required = false, defaultValue = "0") int isRecycled) {
//        String userToken = request.getHeader(TOKEN);
//        return projectService.getProjectListByKeyWord(userToken, module, keyWord, type, isRecycled);
//    }

//    @GetMapping(value = {"/project/name"})
//    public ResponseBean getProjectName(HttpServletRequest request,
//                                 @RequestParam("repoUuid") String repoId,
//                                 @RequestParam(name = "category",required = false,defaultValue = "bug")String category) {
//        String userToken = request.getHeader(TOKEN);
//        try {
//            return projectService.getProjectByRepoIdAndCategory(userToken, repoId,category).getName();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseBean(401, "projectName add failed! No such repo found!", null);
//        }
//
//    }
//
//    @GetMapping(value = {"/project/search"})
//    public ResponseBean getProjectByCondition(HttpServletRequest request,
//                                        @RequestParam(name = "category",required = false)String category,
//                                        @RequestParam(name = "name", required = false)String name,
//                                        @RequestParam(name = "module", required = false)String module
//    ){
//        String userToken = request.getHeader(TOKEN);
//        try {
//            return new ResponseBean(200, "get success", projectService.getProjectsByCondition(userToken, category,name,module));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseBean(401, "operate failed", null);
//        }
//
//    }
//
//    @GetMapping(value = {"/project/recycle"})
//    public ResponseBean projectRecycle(HttpServletRequest request,
//                                 @RequestParam(name = "projectId")String projectId,
//                                 @RequestParam(name = "isRecycled", required = false,defaultValue = "0") int isRecycled){
//        String userToken = request.getHeader(TOKEN);
//        try {
//            projectService.recycle(projectId,userToken,isRecycled);
//            return new ResponseBean(200, "success", null);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseBean(401, "failed", null);
//        }
//    }

//    @DeleteMapping(value = {"/project/{projectId}"})
//    public ResponseBean delete(@PathVariable("projectId") String projectId,
//                         @RequestParam(name = "type",required = false,defaultValue = "bug")String type,
//                         HttpServletRequest request) {
//        try {
//            // projectService.remove(projectId, type,request.getHeader(TOKEN));
//            return new ResponseBean(200, "projectName delete success!", null);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseBean(401, "projectName delete failed!", null);
//        }
//    }




    @Autowired
    public void setProjectControl(ProjectControlService projectControl) {
        this.projectControl = projectControl;
    }
}
