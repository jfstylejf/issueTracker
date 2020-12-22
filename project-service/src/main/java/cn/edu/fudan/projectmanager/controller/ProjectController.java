package cn.edu.fudan.projectmanager.controller;

import cn.edu.fudan.projectmanager.domain.ResponseBean;
import cn.edu.fudan.projectmanager.domain.SubRepository;
import cn.edu.fudan.projectmanager.domain.dto.RepositoryDTO;
import cn.edu.fudan.projectmanager.domain.vo.RepositoryVO;
import cn.edu.fudan.projectmanager.service.ProjectControlService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
     * description: 添加库
     * @param repositoryDTO url isPrivate username password accountName type branch
     * @param request header
     */
    @ApiOperation(value="添加新的库",httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repositoryDTO", value = "库的信息", dataType = "RepositoryDTO", required = true)
    })
    @PostMapping(value = {"/repository"})
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


    @ApiOperation(value="修改项目名称",httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "oldProjectName", value = "旧的项目名", dataType = "String", required = true),
            @ApiImplicitParam(name = "newProjectName", value = "新的项目名", dataType = "String", required = true)
    })
    @PutMapping(value = {"/project"})
    public ResponseBean updateProject(HttpServletRequest request,
                                      @RequestParam("old_project_name") String oldProjectName,
                                      @RequestParam("new_project_name") String newProjectName){
        try {
            projectControl.update(request.getHeader(TOKEN), oldProjectName, newProjectName);
            return new ResponseBean<>(200, "update success", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, "update failed :" + e.getMessage(), null);
        }
    }

    @ApiOperation(value="获取所有库信息",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "recycled", value = "是否被回收", dataType = "int", required = false,defaultValue = "0"),
    })
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


    @DeleteMapping(value = {"/project"})
    public ResponseBean delete(@RequestParam("project_name")String projectName,
                         HttpServletRequest request) {
        try {
            projectControl.deleteProject(projectName,request.getHeader(TOKEN));
            return new ResponseBean(200, "projectName delete success!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "projectName delete failed!", null);
        }
    }


    /**
     * description: 添加项目
     * @param projectName
     * @param request header
     */
    @ApiOperation(value="添加项目",httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectName", value = "项目名称", dataType = "String", required = true)
    })
    @PostMapping(value = {"/project"})
    public ResponseBean addNewProject(HttpServletRequest request, @RequestParam ("project_name") String projectName) {
        String token = request.getHeader(TOKEN);
        try {
            projectControl.addOneProject(token, projectName);
            return new ResponseBean<>(200, "add success", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, "add failed :" + e.getMessage(), null);
        }
    }

    @ApiOperation(value="修改库名称",httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "oldRepoName", value = "旧的库名", dataType = "String", required = true),
            @ApiImplicitParam(name = "newRepoName", value = "新的库名", dataType = "String", required = true)
    })
    @PutMapping(value = {"/repository"})
    public ResponseBean updateRepository(HttpServletRequest request,
                                      @RequestParam("old_repo_name") String oldRepoName,
                                      @RequestParam("new_repo_name") String newRepoName){
        try {
            projectControl.updateRepo(request.getHeader(TOKEN), oldRepoName, newRepoName);
            return new ResponseBean<>(200, "update success", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, "update failed :" + e.getMessage(), null);
        }
    }

    @ApiOperation(value="修改库的所属项目",httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "oldProjectName", value = "旧的项目名", dataType = "String", required = true),
            @ApiImplicitParam(name = "newProjectName", value = "新的项目名", dataType = "String", required = true),
            @ApiImplicitParam(name = "RepoUuid", value = "库的uuid", dataType = "String", required = true)
    })
    @PutMapping(value = {"/repository/project"})
    public ResponseBean updateRepoProject(HttpServletRequest request,
                                          @RequestParam("old_project_name") String oldProjectName,
                                          @RequestParam("new_project_name") String newProjectName,
                                          @RequestParam("repo_uuid") String RepoUuid){
        try {
            projectControl.updateRepoProject(request.getHeader(TOKEN), oldProjectName, newProjectName,RepoUuid);
            return new ResponseBean<>(200, "update success", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, "update failed :" + e.getMessage(), null);
        }
    }

    @ApiOperation(value="删除库",httpMethod = "DELETE")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectUuid", value = "库的uuid", dataType = "String", required = true)
    })
    @DeleteMapping(value = {"/project/{repo_uuid}"})
    public ResponseBean deleteRepo(@PathVariable("repo_uuid")String repoUuid,
                               HttpServletRequest request) {
        try {
            projectControl.deleteRepo(repoUuid,request.getHeader(TOKEN));
            return new ResponseBean(200, "repo delete success!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "repo delete failed!", null);
        }
    }

    @Autowired
    public void setProjectControl(ProjectControlService projectControl) {
        this.projectControl = projectControl;
    }
}
