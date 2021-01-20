package cn.edu.fudan.projectmanager.controller;

import cn.edu.fudan.projectmanager.domain.ResponseBean;
import cn.edu.fudan.projectmanager.domain.SubRepository;
import cn.edu.fudan.projectmanager.domain.vo.RepositoryVO;
import cn.edu.fudan.projectmanager.service.AccountRepositoryService;
import cn.edu.fudan.projectmanager.service.ProjectControlService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
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


    private AccountRepositoryService accountRepository;
    private ProjectControlService projectControl;
    private final String TOKEN = "token";

    /**
     * todo issue 得到所有库信息
     *
     * @param recycled {@link cn.edu.fudan.projectmanager.domain.SubRepository} EMPTY RESERVATIONS ALL
     * @return k projectName v: list [k: repo_id, accountName]
     */
    @ApiOperation(value = " 得到所有项目和库的关系", notes = "@return Map < String, List < Map < String, String > > >  k projectName v: list [k: repo_id, accountName]")
    @GetMapping(value = "/project/all")
    public ResponseBean<Map<String, List<Map<String, String>>>> getProjectAndRepoRelation(@RequestParam(name = "recycled", required = false, defaultValue = "0") int recycled) throws Exception {
        try {
            return new ResponseBean<>(200, "get info success", accountRepository.getProjectAndRepoRelation(recycled));
        } catch (Exception e) {
            return new ResponseBean<>(401, "get info failed :" + e.getMessage(), null);
        }
    }

    @ApiOperation(value = "获取所有库信息", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "recycled", value = "是否被回收", dataType = "int", required = false, defaultValue = "0"),
    })
    @GetMapping(value = {"/project"})
    public ResponseBean<List<SubRepository>> query(HttpServletRequest request,
                                                   @RequestParam(name = "recycled", required = false, defaultValue = "0") int recycled) {
        String userToken = request.getHeader(TOKEN);
        List<SubRepository> subRepositories = projectControl.query(userToken);
        List<RepositoryVO> repositoryVos = new ArrayList<>(subRepositories.size());
        subRepositories.stream().filter(s -> s.getRecycled() == recycled).
                forEach(s -> repositoryVos.add(new RepositoryVO(s)));

        return new ResponseBean<>(200, "success", subRepositories);
    }

    /**
     * todo issue 所有项目信息
     *
     * @param
     * @return
     */
    @ApiOperation(value = " 得到所有项目名和ID", notes = "@return List<Map<String, Object>>")
    @GetMapping(value = {"/project/list"})
    public ResponseBean<List<Map<String, Object>>> getProjectList(HttpServletRequest request) {
        String token = request.getHeader(TOKEN);
        try {
            return new ResponseBean<>(200, "add success", accountRepository.getProjectAll(token));
        } catch (Exception e) {
            return new ResponseBean<>(401, "add failed :" + e.getMessage(), null);
        }
    }

    /**
     * fixme measure
     */
    @ApiOperation(value = "通过库uuid获取指定库的信息", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repoUuid", value = "库对应的uuid", dataType = "String", required = true)
    })
    @GetMapping(value = {"/inner/project"})
    public ResponseBean<RepositoryVO> getProjectByRepoId(@RequestParam("repo_uuid") String repoUuid) throws Exception {
        SubRepository subRepository = accountRepository.getRepoInfoByRepoId(repoUuid);
        if (subRepository == null) {
            return new ResponseBean<>(412, "get repo failed!", null);
        }
        try {
            return new ResponseBean<>(200, "get repo success", new RepositoryVO(subRepository));
        } catch (Exception e) {
            return new ResponseBean<>(401, "get repo failed :" + e.getMessage(), null);
        }
    }

    /**
     * FIXME issue
     * <p>
     * List<Project>
     */
    @ApiOperation(value = "通过人员ID获取多个库的信息", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accountUuid", value = "引入人uuid", dataType = "String", required = false),
            @ApiImplicitParam(name = "recycled", value = "回收状态", dataType = "int", required = false)
    })
    @GetMapping(value = "/inner/projects")
    public ResponseBean<List<RepositoryVO>> getRepositoryByAccountId(@RequestParam(name = "account_uuid", required = false) String accountUuid,
                                                                     @RequestParam(name = "recycled", required = false, defaultValue = "0") int recycled) throws Exception {
        boolean isAll = recycled == SubRepository.ALL;
        List<SubRepository> repositories = accountRepository.getRepoByAccountUuid(accountUuid);
        List<RepositoryVO> result = new ArrayList<>();
        repositories.stream().filter(r -> isAll || recycled == r.getRecycled()).forEach(r -> result.add(new RepositoryVO(r)));
        try {
            return new ResponseBean<>(200, "get repo success", result);
        } catch (Exception e) {
            return new ResponseBean<>(401, "get repo failed :" + e.getMessage(), null);
        }
    }

    /**
     * FIXME issue scan
     */
    @ApiOperation(value = "通过sub_repository表uuid获取库uuid", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repoUuid", value = "库对应的uuid", dataType = "String", required = true)
    })
    @GetMapping(value = "/inner/repo/repo-uuid")
    public ResponseBean<String> getRepoByRepoUuid(@RequestParam("repo-uuid") String uuid) throws Exception {
        try {
            return new ResponseBean<>(200, "get repo success", accountRepository.getRepoUuidByUuid(uuid));
        } catch (Exception e) {
            return new ResponseBean<>(401, "get repo failed :" + e.getMessage(), null);
        }
    }

    /**
     * FIXME issue
     *
     * @return
     */
    @ApiOperation(value = "获取某人import的库", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accountUuid", value = "人员uuid", dataType = "String", required = false),
            @ApiImplicitParam(name = "recycled", value = "回收状态", dataType = "int", required = false)
    })
    @GetMapping(value = "/inner/project/repo-ids")
    public ResponseBean<List<String>> getProjectIds(@RequestParam(name = "account_uuid", required = false) String accountUuid,
                                                    @RequestParam(name = "recycled", required = false, defaultValue = "0") int recycled) throws Exception {

        boolean isAll = recycled == SubRepository.ALL;
        List<SubRepository> subRepositories = accountRepository.getRepoByAccountUuid(accountUuid);
        List<String> result = new ArrayList<>();
        subRepositories.stream().filter(r -> isAll || recycled == r.getRecycled()).forEach(r -> result.add(r.getRepoUuid()));
        try {
            return new ResponseBean<>(200, "get repo success", result);
        } catch (Exception e) {
            return new ResponseBean<>(401, "get repo failed :" + e.getMessage(), null);
        }
    }

    /**
     * description todo 补足注释
     *
     * @param accountName
     * @return List<Map < String, Object>> key  account_name account_right account_role project_name
     */
    @ApiOperation(value = "根据用户姓名获得其参与项目", notes = "@return List < Map < String, Object > >", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accountName", value = "人员姓名", dataType = "String", required = true)
    })
    @GetMapping(value = "/project/info")
    public ResponseBean<List<Map<String, Object>>> getProjectInfoByAccountName(@RequestParam String accountName) throws Exception {
        try {
            return new ResponseBean<>(200, "get info success", accountRepository.getProjectInfoByAccountName(accountName));
        } catch (Exception e) {
            return new ResponseBean<>(401, "get info failed :" + e.getMessage(), null);
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