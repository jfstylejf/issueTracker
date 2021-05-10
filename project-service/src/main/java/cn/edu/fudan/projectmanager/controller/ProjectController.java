package cn.edu.fudan.projectmanager.controller;

import cn.edu.fudan.projectmanager.domain.ResponseBean;
import cn.edu.fudan.projectmanager.domain.SubRepository;
import cn.edu.fudan.projectmanager.domain.dto.RepositoryDTO;
import cn.edu.fudan.projectmanager.domain.vo.RepositoryVO;
import cn.edu.fudan.projectmanager.service.AccountRepositoryService;
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
    private AccountRepositoryService accountRepository;
    private final String TOKEN = "token";

    /**
     * description: 添加库
     *
     * @param repositoryDTO url isPrivate username password accountName type branch
     * @param request       header
     */
    @ApiOperation(value = "添加新的库", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repositoryDTO", value = "库的信息", dataType = "RepositoryDTO", required = true)
    })
    @PostMapping(value = {"/repository"})
    public ResponseBean addProject(HttpServletRequest request, @RequestBody RepositoryDTO repositoryDTO) {
        String token = request.getHeader(TOKEN);
        try {
            boolean result = projectControl.addOneRepo(token, repositoryDTO);
            if(!result){
                return new ResponseBean<>(406, "add failed,repo duplicated!", null);
            }
            return new ResponseBean<>(200, "add success", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, "add failed :" + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "在本地添加新的库", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repositoryDTO", value = "库的信息", dataType = "RepositoryDTO", required = true)
    })
    @PostMapping(value = {"/repository/local"})
    public ResponseBean addRepoLocal(HttpServletRequest request, @RequestBody RepositoryDTO repositoryDTO) {
        String token = request.getHeader(TOKEN);
        try {
            projectControl.addOneRepoByLocal(token, repositoryDTO);
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
        Map<String, Boolean> result = projectControl.addRepos(userToken, repositories);
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
                               @RequestParam(value = "empty", required = false, defaultValue = "false") Boolean empty) {
        try {
            projectControl.delete(request.getHeader(TOKEN), subRepoId, empty);
            return new ResponseBean<>(200, "delete success!", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, "delete failed! " + e.getMessage(), null);
        }
    }


    @ApiOperation(value = "修改项目名称", httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "oldProjectName", value = "旧的项目名", dataType = "String", required = true),
            @ApiImplicitParam(name = "newProjectName", value = "新的项目名", dataType = "String", required = true)
    })
    @PutMapping(value = {"/project"})
    public ResponseBean updateProject(HttpServletRequest request,
                                      @RequestParam("old_project_name") String oldProjectName,
                                      @RequestParam("new_project_name") String newProjectName) {
        try {
            projectControl.update(request.getHeader(TOKEN), oldProjectName, newProjectName);
            return new ResponseBean<>(200, "update success", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, "update failed :" + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "删除项目", httpMethod = "DELETE")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "project_name", value = "项目名称", dataType = "String", required = true)
    })
    @DeleteMapping(value = {"/project"})
    public ResponseBean delete(
            HttpServletRequest request,
            @RequestParam("project_name") String projectName) {
        try {
            boolean result = projectControl.deleteProject(request.getHeader(TOKEN), projectName);
            if(!result){
                return new ResponseBean(412, "failed:this project contains repo!", null);
            }
            return new ResponseBean(200, "projectName delete success!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "projectName delete failed!", null);
        }
    }

    /**
     * description: 添加项目
     *
     * @param projectName
     * @param request     header
     */
    @ApiOperation(value = "添加项目", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectName", value = "项目名称", dataType = "String", required = true)
    })
    @PostMapping(value = {"/project"})
    public ResponseBean addNewProject(HttpServletRequest request, @RequestParam("project_name") String projectName) {
        String token = request.getHeader(TOKEN);
        try {
            projectControl.addOneProject(token, projectName);
            return new ResponseBean<>(200, "add success", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, "add failed :" + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "修改库名称", httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "oldRepoName", value = "旧的库名", dataType = "String", required = true),
            @ApiImplicitParam(name = "newRepoName", value = "新的库名", dataType = "String", required = true)
    })
    @PutMapping(value = {"/repository"})
    public ResponseBean updateRepository(HttpServletRequest request,
                                         @RequestParam("old_repo_name") String oldRepoName,
                                         @RequestParam("new_repo_name") String newRepoName) {
        try {
            projectControl.updateRepo(request.getHeader(TOKEN), oldRepoName, newRepoName);
            return new ResponseBean<>(200, "update success", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, "update failed :" + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "修改库的所属项目", httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "oldProjectName", value = "旧的项目名", dataType = "String", required = true),
            @ApiImplicitParam(name = "newProjectName", value = "新的项目名", dataType = "String", required = true),
            @ApiImplicitParam(name = "RepoUuid", value = "库的uuid", dataType = "String", required = true)
    })
    @PutMapping(value = {"/repository/project"})
    public ResponseBean updateRepoProject(HttpServletRequest request,
                                          @RequestParam("old_project_name") String oldProjectName,
                                          @RequestParam("new_project_name") String newProjectName,
                                          @RequestParam("repo_uuid") String RepoUuid) {
        try {
            accountRepository.updateRepoProject(request.getHeader(TOKEN), oldProjectName, newProjectName, RepoUuid);
            return new ResponseBean<>(200, "update success", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, "update failed :" + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "删除库", httpMethod = "DELETE")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuid", value = "库的uuid", dataType = "String", required = false),
            @ApiImplicitParam(name = "uuid", value = "sub库表的uuid", dataType = "String", required = false)
    })
    @DeleteMapping(value = {"/repo"})
    public ResponseBean<Object> deleteRepo(@RequestParam(value = "repo_uuid", required = false) String repoUuid,
                                           @RequestParam(value = "uuid", required = false) String uuid,
                                           HttpServletRequest request) {
        if(repoUuid == null & uuid == null){
            return new ResponseBean<>(412, "repo uuid can not be null!", null);
        }
        try {
            boolean result = projectControl.deleteRepo(request.getHeader(TOKEN), repoUuid, uuid);
            if (!result){
                return new ResponseBean<>(412, "repo is not exist!", null);
            }
            return new ResponseBean<>(200, "repo delete success!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(401, "repo delete failed:" + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "新增项目负责人", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "newLeaderId", value = "新的负责人ID", dataType = "String", required = true),
            @ApiImplicitParam(name = "projectId", value = "项目id", dataType = "Integer", required = true)

    })
    @PostMapping(value = {"/project/leader"})
    public ResponseBean addLeader(HttpServletRequest request,
                                  @RequestParam("newLeaderId") String newLeaderId,
                                  @RequestParam("projectId") Integer projectId) {
        try {
            boolean result = accountRepository.addProjectLeader(request.getHeader(TOKEN), newLeaderId, projectId);
            if (result == false) {
                return new ResponseBean<>(412, "update failed!", null);
            }
            return new ResponseBean<>(200, "update success", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, "update failed :" + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "删除项目负责人", httpMethod = "DELETE")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "LeaderId", value = "负责人ID", dataType = "String", required = true),
            @ApiImplicitParam(name = "projectId", value = "项目id", dataType = "Integer", required = true)

    })
    @DeleteMapping(value = {"/project/leader"})
    public ResponseBean deleteLeader(HttpServletRequest request,
                                     @RequestParam("LeaderId") String LeaderId,
                                     @RequestParam("projectId") Integer projectId) {
        try {
            accountRepository.deleteProjectLeader(request.getHeader(TOKEN), LeaderId, projectId);
            return new ResponseBean<>(200, "update success", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, "update failed :" + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "修改回收站状态", httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "recycled", value = "库的回收状态", dataType = "int", required = true),
            @ApiImplicitParam(name = "repo_uuid", value = "库uuid", dataType = "String", required = true)
    })
    @PutMapping(value = {"/repository/recycle"})
    public ResponseBean<Integer> updateRecycleStatus(HttpServletRequest request,
                                            @RequestParam("repo_uuid") String repoUuid,
                                            @RequestParam("recycled") int recycled) {
        try {
            SubRepository repository = accountRepository.getRepoInfoByRepoId(repoUuid);
            if (repository == null) {
                return new ResponseBean<>(412, "repo not exist", null);
            }
            Integer recycledStatus  = projectControl.updateRecycleStatus(request.getHeader(TOKEN), repoUuid, recycled);
            return new ResponseBean<>(200, "update success", recycledStatus);
        } catch (Exception e) {
            return new ResponseBean<>(401, "update failed :" + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "删除成功回调接口", httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "recycled", value = "库的回收状态", dataType = "int", required = true),
            @ApiImplicitParam(name = "repo_uuid", value = "库的uuid", dataType = "String", required = true)
    })
    @PutMapping(value = {"/repo"})
    public ResponseBean<Integer> updateRecycled(HttpServletRequest request,
                                            @RequestParam("repo_uuid") String repoUuid,
                                            @RequestParam("service_name") String serviceName) throws Exception {
        try {
            SubRepository repository = accountRepository.getRepoInfoByRepoId(repoUuid);
            if (repository == null) {
                return new ResponseBean<>(412, "repo not exist", null);
            }
            if(serviceName == null){
                return new ResponseBean<>(412, "please input service name!", null);
            }
            Integer recycledStatus = projectControl.updateRecycled(request.getHeader(TOKEN), repoUuid, serviceName);
            return new ResponseBean<>(200, "update success", recycledStatus);
        } catch (Exception e) {
            return new ResponseBean<>(401, "update failed :" + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "硬删除project服务中的repo信息", httpMethod = "DELETE")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repoUuid", value = "库的UUID", dataType = "String", required = true)
    })
    @DeleteMapping(value = {"/repo/project"})
    public ResponseBean<Boolean> deleteRepoInfo(HttpServletRequest request,
                                                @RequestParam("repo_uuid") String repoUuid) {
        try {
            Boolean result = projectControl.deleteRepoInfo(request.getHeader(TOKEN), repoUuid);
            if(!result){
                return new ResponseBean<>(412, "repo can not be delete!", null);
            }
            return new ResponseBean<>(200, "delete success", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, "delete failed :" + e.getMessage(), null);
        }
    }

    @Autowired
    public void setAccountRepository(AccountRepositoryService accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Autowired
    public void setProjectControl(ProjectControlService projectControl) {
        this.projectControl = projectControl;
    }
}
