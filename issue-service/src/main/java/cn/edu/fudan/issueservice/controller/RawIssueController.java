package cn.edu.fudan.issueservice.controller;

import cn.edu.fudan.issueservice.domain.ResponseBean;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.service.RawIssueService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;


@Slf4j
@RestController
public class RawIssueController {

    private RawIssueService rawIssueService;

    @Autowired
    public void setRawIssueService(RawIssueService rawIssueService) {
        this.rawIssueService = rawIssueService;
    }


    /**
     *
     * @param issue_id issueid
     * @param page 页数
     * @param size 每页大小
     * @param status rawissue状态
     * @return 返回rawIssue列表
     */
    @ApiOperation(value = "获取rawIssue列表接口", notes = "@return Object", httpMethod = "GET")
    @GetMapping(value = {"/raw-issue-list"})
    @Deprecated
    public ResponseBean<Object> getOnePageRawIssueList(
            @RequestParam("issue_id") String issue_id ,
            @RequestParam("page") Integer page,
            @RequestParam("size") Integer size,
            @RequestParam(value = "status",required = false) String status
    ) {
        try {
            return new ResponseBean<>(200, "success!", rawIssueService.getRawIssueList(issue_id,page,size,status));
        }catch (Exception e){
            return new ResponseBean<>(500, e.getMessage(),null);
        }
    }

    /**
     *
     * fixme 应该由code service提供此接口
     *
     * @param project_id  project_id
     * @param commit_id commit_id
     * @param file_path file_path
     * @return 文件的源代码
     */
    @ApiIgnore
    @ApiOperation(value = "获取文件的源代码接口", notes = "@return Map<String, Object>", httpMethod = "GET")
    @GetMapping(value = {"/raw-issue/code"})
    @Deprecated
    public ResponseBean<Map<String, Object>> getCode(@RequestParam("project_id") String project_id,
                                                     @RequestParam("commit_id") String commit_id,
                                                     @RequestParam("file_path") String file_path) {
        try {
            //需要添加全部的code内容
            return new ResponseBean<>(200, "success!", rawIssueService.getCode(project_id, commit_id, file_path));
        }catch (Exception e){
            return new ResponseBean<>(500, e.getMessage(),null);
        }
    }

    @GetMapping(value = {"/raw-issue"})
    @ApiOperation(value = "根据issue_uuid筛选rawIssue", notes = "@return List<RawIssue>", httpMethod = "GET")
    public ResponseBean<List<RawIssue>> getRawIssueList(@RequestParam("issue_uuid") String issue_id) {
        try {
            List<RawIssue> data = rawIssueService.getRawIssueByIssueId(issue_id);
            return new ResponseBean(200, "success", data);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500, e.getMessage(), null);
        }
    }


    //下面的接口都是其他服务调用的内部接口
    @ApiIgnore
    @ApiOperation(value = "删除rawIssue表记录接口", notes = "@return null", httpMethod = "DELETE")
    @DeleteMapping(value = {"/inner/raw-issue/{category}/{repoId}"})
    @Deprecated
    public ResponseBean<Object> deleteRawIssue(@PathVariable("category")String category ,@PathVariable("repoId") String repoId) {
        try {
            rawIssueService.deleteRawIssueByRepoIdAndTool(repoId,category);
            return new ResponseBean(200, "rawIssue delete success!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, "rawIssue delete failed!", null);
        }
    }

}
