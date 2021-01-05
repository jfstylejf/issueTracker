package cn.edu.fudan.projectmanager.controller;

import cn.edu.fudan.projectmanager.domain.ResponseBean;
import cn.edu.fudan.projectmanager.domain.SubRepository;
import cn.edu.fudan.projectmanager.domain.vo.RepositoryVO;
import cn.edu.fudan.projectmanager.service.AccountRepositoryService;
import cn.edu.fudan.projectmanager.service.ProjectControlService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * description: 提供关于project表的查询服务
 *
 * @author fancying
 * create: 2020-07-29 14:11
 **/
@RestController
public class ProjectQueryController {


    private AccountRepositoryService repoUser;
    private ProjectControlService projectControl;
    private final String TOKEN = "token";

    /**
     * todo issue 得到所有库信息
     * @param recycled {@link cn.edu.fudan.projectmanager.domain.SubRepository} EMPTY RESERVATIONS ALL
     * @return    k projectName v: list [k: repo_id, accountName]
     */
    @ApiOperation(value = " 得到所有库信息", notes = "@return Map < String, List < Map < String, String > > >  k projectName v: list [k: repo_id, accountName]")
    @GetMapping(value = "/project/all")
    public Map<String, List<Map<String, String>>> getProjectAndRepoRelation(@RequestParam(name = "recycled", required = false, defaultValue = "0") int recycled) {
        return repoUser.getProjectAndRepoRelation(recycled);
    }

    /**
     * todo issue 所有项目信息
     * @param
     * @return
     */
    @ApiOperation(value = " 得到所有库信息", notes = "@return List<Map<Integer, String>>")
    @GetMapping(value = {"/project/list"})
    public ResponseBean<List<Map<String, Object>>> getProjectList(HttpServletRequest request) {
        String token = request.getHeader(TOKEN);
        try {
            return new ResponseBean<>(200, "add success", projectControl. getProjectAll(token));
        } catch (Exception e) {
            return new ResponseBean<>(401, "add failed :" + e.getMessage(), null);
        }
    }

    /**
     * fixme measure
     */
    @GetMapping(value = {"/inner/project"})
    public RepositoryVO getProjectByRepoId(@RequestParam("repo_uuid") String repoUuid){
        SubRepository subRepository = repoUser.getProjectInfoByRepoId(repoUuid);
        if (subRepository == null) {
            return null;
        }
        return new RepositoryVO(subRepository);
    }

    /**
     * FIXME issue cn.edu.fudan.common.scan
     */
    @GetMapping(value = "/inner/project/repo-uuid")
    public String getRepoUuid(@RequestParam("project_uuid") String projectUuid) {
        return repoUser.getRepoUuid(projectUuid);
    }

    /**
     * FIXME issue
     * @return
     */
    @GetMapping(value = "/inner/project/repo-ids")
    public List<String> getProjectIds(@RequestParam(name = "account_uuid", required = false) String accountUuid,
                                @RequestParam(name = "recycled", required = false, defaultValue = "0") int recycled) {

        boolean isAll = recycled == SubRepository.ALL;
        List<SubRepository> subRepositories = repoUser.getRepositoryByAccountUuid(accountUuid);
        List<String> result = new ArrayList<>();
        subRepositories.stream().filter(r -> isAll || recycled == r.getRecycled()).forEach(r -> result.add(r.getRepoUuid()));
        return result;
    }

    /**
     * FIXME issue
     *
     * List<Project>
     */
    @GetMapping(value = "/inner/projects")
    public List<RepositoryVO> getRepositoryByAccountId(@RequestParam(name = "account_uuid", required = false) String accountUuid,
                                        @RequestParam(name = "recycled", required = false, defaultValue = "0") int recycled){
        boolean isAll = recycled == SubRepository.ALL;
        List<SubRepository> repositories = repoUser.getRepositoryByAccountUuid(accountUuid);
        List<RepositoryVO> result = new ArrayList<>();
        repositories.stream().filter(r -> isAll || recycled == r.getRecycled()).forEach(r -> result.add(new RepositoryVO(r)));
        return result;
    }


    /**
     * description todo 补足注释
     * @param accountName
     * @return List<Map<String, Object>> key  account_name account_right account_role project_name
     */
    @ApiOperation(value = "根据用户姓名得到对应的信息", notes = "@return List < Map < String, Object > >  key  account_name account_right account_role project_name ")
    @GetMapping(value = "/project/info")
    public List<Map<String, Object>> getProjectInfoByAccountName(@RequestParam String accountName) {
        return repoUser.getProjectInfoByAccountName(accountName);
    }


    @Autowired
    public void setRepoUser(AccountRepositoryService repoUser) {
        this.repoUser = repoUser;
    }
    @Autowired
    public void setProjectControl(ProjectControlService projectControl) {
        this.projectControl = projectControl;
    }
}