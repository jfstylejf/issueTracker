package cn.edu.fudan.measureservice.controller;

import cn.edu.fudan.measureservice.dao.ProjectDao;
import cn.edu.fudan.measureservice.domain.CommitBaseInfoDuration;
import cn.edu.fudan.measureservice.domain.Granularity;
import cn.edu.fudan.measureservice.domain.RepoMeasure;
import cn.edu.fudan.measureservice.domain.ResponseBean;
import cn.edu.fudan.measureservice.domain.metric.RepoTagMetric;
import cn.edu.fudan.measureservice.domain.dto.Query;
import cn.edu.fudan.measureservice.service.MeasureRepoService;
import cn.edu.fudan.measureservice.util.DateTimeUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
public class MeasureRepoController {

    private MeasureRepoService measureRepoService;

    private ProjectDao projectDao;


    @ApiOperation(value = "获取一个项目在某个时间段特定时间单位的项目级别的所有度量信息", notes = "@return List<RepoMeasure>", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuid", value = "参与库", dataType = "String",required = true,defaultValue = "3ecf804e-0ad6-11eb-bb79-5b7ba969027e" ),
            @ApiImplicitParam(name = "since", value = "起始时间（yyyy-MM-dd）", dataType = "String", defaultValue = "2019-02-20"),
            @ApiImplicitParam(name = "until", value = "截止时间（yyyy-MM-dd）", dataType = "String" ,defaultValue = "当前时间"),
            @ApiImplicitParam(name = "granularity", value = "时间粒度", dataType = "String", required = true,defaultValue = "week")
    })

    @GetMapping("/measure/repository")
    @CrossOrigin
    public ResponseBean<List<RepoMeasure>> getMeasureDataByrepoUuid(@RequestParam("repo_uuid")String repoUuid,
                                                                  @RequestParam(name="since",required = false)String since,
                                                                  @RequestParam(value = "until",required = false)String until,
                                                                  @RequestParam("granularity") Granularity granularity){

        try{
            if(until==null || "".equals(until)) {
                until = DateTimeUtil.dtf.format(LocalDate.now().plusDays(1));
            }
            return new ResponseBean<>(HttpStatus.OK.value(),"success", measureRepoService.getRepoMeasureByRepoUuid(repoUuid,since,until,granularity));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(HttpStatus.BAD_REQUEST.value(),"failed "+ e.getMessage(),null);
        }
    }


    @ApiOperation(value = "获取一个repo在一段时间内的某个开发者的commit信息，如果不指定开发者参数，则返回所有开发者commit信息", notes = "@return CommitBaseInfoDuration", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuid", value = "参与库", dataType = "String",required = true,defaultValue = "3ecf804e-0ad6-11eb-bb79-5b7ba969027e" ),
            @ApiImplicitParam(name = "since", value = "起始时间（yyyy-MM-dd）", dataType = "String", defaultValue = "2019-02-20"),
            @ApiImplicitParam(name = "until", value = "截止时间（yyyy-MM-dd）", dataType = "String",defaultValue = "当前时间"),
            @ApiImplicitParam(name = "developer", value = "开发者姓名", dataType = "String",defaultValue = "week")
    })

    @GetMapping("/measure/repository/duration")
    @CrossOrigin
    public ResponseBean<CommitBaseInfoDuration> getCommitBaseInformationByDuration(@RequestParam("repo_uuid")String repoUuid,
                                                                                   @RequestParam(value = "since",required = false)String since,
                                                                                   @RequestParam(value = "until",required = false)String until,
                                                                                   @RequestParam(name = "developer",required = false)String developerName
                                                 ){

        try{
            if(until==null || "".equals(until)) {
                until = DateTimeUtil.dtf.format(LocalDate.now().plusDays(1));
            }
            return new ResponseBean<>(HttpStatus.OK.value(),"success",measureRepoService.getCommitBaseInformationByDuration(repoUuid, since, until, developerName));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(HttpStatus.BAD_REQUEST.value(),"failed "+e.getMessage(),null);
        }

    }

    @ApiOperation(value = "根据repo_id和since、until获取某个时间段内commit次数最多的3位开发者姓名以及对应的commit次数", notes = "@return List<Map<String,Object>> key : counts, developer_name", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuids", value = "参与库", dataType = "String",defaultValue = "defd1c4c-33a4-11eb-8dca-4dbb5f7a5f33" ),
            @ApiImplicitParam(name = "project_name", value = "项目名" ,dataType = "String" ,defaultValue = "测试项目2"),
            @ApiImplicitParam(name = "since", value = "起始时间（yyyy-MM-dd）", dataType = "String", defaultValue = "2019-02-20"),
            @ApiImplicitParam(name = "until", value = "截止时间（yyyy-MM-dd）", dataType = "String",defaultValue = "当前时间"),
    })
    @SuppressWarnings("unchecked")
    @GetMapping("/measure/developer-rank/commit-count")
    @CrossOrigin
    public ResponseBean<List<Map<String,Object>>> getDeveloperRankByCommitCount(
            @RequestParam(value = "repo_uuids",required = false)String repoUuid,
            @RequestParam(value = "project_name",required = false) String projectName,
            @RequestParam(value = "since",required = false)String since,
            @RequestParam(value = "until",required = false)String until,
            HttpServletRequest request){

        try{
            String token = request.getHeader("token");
            if(until==null || "".equals(until)) {
                until = DateTimeUtil.dtf.format(LocalDate.now().plusDays(1));
            }else {
                until = DateTimeUtil.dtf.format(LocalDate.parse(until,DateTimeUtil.dtf).plusDays(1));
            }
            List<String> repoUuidList;
            if(projectName!=null && !"".equals(projectName)) {
                repoUuidList = projectDao.getProjectRepoList(projectName,token);
            }else {
                repoUuidList = projectDao.involvedRepoProcess(repoUuid,token);
            }
            Query query = new Query(null,since,until,null, repoUuidList);
            return new ResponseBean<>(HttpStatus.OK.value(),"success", measureRepoService.getDeveloperRankByCommitCount(query));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(HttpStatus.BAD_REQUEST.value(),"failed "+ e.getMessage(),null);
        }
    }

    @ApiOperation(value = "根据repo_id和since、until获取某个时间段内,该项目中提交代码行数（LOC）最多的前3名开发者的姓名以及对应的LOC", notes = "@return List<Map<String,Object>> key : counts,developer_name", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuids", value = "参与库", dataType = "String",defaultValue = "defd1c4c-33a4-11eb-8dca-4dbb5f7a5f33" ),
            @ApiImplicitParam(name = "project_name", value = "项目名" ,dataType = "String" ,defaultValue = "测试项目2"),
            @ApiImplicitParam(name = "since", value = "起始时间（yyyy-MM-dd）", dataType = "String", defaultValue = "2019-02-20"),
            @ApiImplicitParam(name = "until", value = "截止时间（yyyy-MM-dd）", dataType = "String",defaultValue = "当前时间"),
    })

    @GetMapping("/measure/developer-rank/loc")
    @CrossOrigin
    public ResponseBean<List<Map<String, Object>>> getDeveloperRankByLoc(
            @RequestParam(value = "repo_uuids",required = false)String repoUuid,
            @RequestParam(value = "project_name",required = false) String projectName,
            @RequestParam(value = "since",required = false)String since,
            @RequestParam(value = "until",required = false)String until,
            HttpServletRequest request){

        try{
            String token = request.getHeader("token");
            if(until==null || "".equals(until)) {
                until = DateTimeUtil.dtf.format(LocalDate.now().plusDays(1));
            }else {
                until = DateTimeUtil.dtf.format(LocalDate.parse(until,DateTimeUtil.dtf).plusDays(1));
            }
            List<String> repoUuidList;
            if(projectName!=null && !"".equals(projectName)) {
                repoUuidList = projectDao.getProjectRepoList(projectName,token);
            }else {
                repoUuidList = projectDao.involvedRepoProcess(repoUuid,token);
            }
            Query query = new Query(null,since,until,null,repoUuidList);
            return new ResponseBean<>(HttpStatus.OK.value(),"success", measureRepoService.getDeveloperRankByLoc(query));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(HttpStatus.BAD_REQUEST.value(),"failed "+e.getMessage(),null);
        }
    }

    @ApiOperation(value = "获取某段时间内，每天的所有提交次数和物理行数", notes = "@return List<Map<String,Object>> key : commit_date, LOC,commit_count", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuid", value = "参与库", dataType = "String",defaultValue = "defd1c4c-33a4-11eb-8dca-4dbb5f7a5f33" ),
            @ApiImplicitParam(name = "project_name", value = "项目名" ,dataType = "String" ,defaultValue = "测试项目2"),
            @ApiImplicitParam(name = "since", value = "起始时间（yyyy-MM-dd）", dataType = "String", defaultValue = "2019-02-20"),
            @ApiImplicitParam(name = "until", value = "截止时间（yyyy-MM-dd）", dataType = "String", defaultValue = "当前时间"),
    })
    @GetMapping("/measure/repository/commit-count&LOC-daily")
    @CrossOrigin
    public ResponseBean<List<Map<String, Object>>> getDailyCommitCountAndLOC(
            @RequestParam(value = "repo_uuids",required = false)String repoUuid,
            @RequestParam(value = "project_name",required = false) String projectName,
            @RequestParam(value = "since",required = false)String since,
            @RequestParam(value = "until",required = false)String until,
            HttpServletRequest request){

        try{
            String token = request.getHeader("token");
            until = DateTimeUtil.processUntil(until);
            List<String> repoUuidList;
            if(projectName!=null && !"".equals(projectName)) {
                repoUuidList = projectDao.getProjectRepoList(projectName,token);
            }else {
                repoUuidList = projectDao.involvedRepoProcess(repoUuid,token);
            }
            Query query = new Query(token,since,until,null,repoUuidList);
            return new ResponseBean<>(HttpStatus.OK.value(),"success",measureRepoService.getDailyCommitCountAndLOC(query));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(HttpStatus.BAD_REQUEST.value(),"failed "+e.getMessage(),null);
        }
    }


    /**
     * measure 服务删除接口
     * @param repoUuid 待删除库
     * @return
     */
    @DeleteMapping("/measure/repo/{repo_uuid}")
    @CrossOrigin
    public ResponseBean<String> deleteRepoUselessMsg(@PathVariable("repo_uuid") String repoUuid,
                                                     HttpServletRequest request) {
        try {
            String token = request.getHeader("token");
            measureRepoService.deleteRepoMsg(repoUuid,token);
            return new ResponseBean<>(HttpStatus.OK.value(),"measure delete Success","Completed");
        }catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(HttpStatus.BAD_REQUEST.value(),"measure failed",e.getMessage());
        }
    }

    @ApiOperation(value = "获取对应库下相应维度的基线值", notes = "@return List<RepoTagMetric>", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuid", value = "查询库 id", dataType = "String",  required = false)
    })

    @GetMapping("/measure/repo-metric")
    public ResponseBean<Object> getRepoMetricList(@RequestParam(value = "repo_uuid",required = false) String repoUuid) {
        try {
            List<RepoTagMetric> repoTagMetricList = measureRepoService.getRepoMetricList(repoUuid);
            if(repoTagMetricList != null) {
                return new ResponseBean<>(HttpStatus.OK.value(),"get repoMetric success",repoTagMetricList);
            }else {
                return new ResponseBean<>(HttpStatus.NO_CONTENT.value(),"check the repo , it seems that no related data in database ",null);
            }
        }catch (Exception e) {
            return new ResponseBean<>(HttpStatus.BAD_REQUEST.value(),"measure failed",e.getMessage());
        }
    }

    @PostMapping("/measure/post/repo-metric")
    public ResponseBean<Object> insertRepoMetric(@RequestParam String tag,
                                                 @RequestParam(value = "repo_uuid") String repoUuid,
                                                 @RequestParam(value = "best_min") double bestMin,
                                                 @RequestParam(value = "better_max") double betterMax,
                                                 @RequestParam(value = "better_min") double betterMin,
                                                 @RequestParam(value = "normal_max") double normalMax,
                                                 @RequestParam(value = "normal_min") double normalMin,
                                                 @RequestParam(value = "worse_max") double worseMax,
                                                 @RequestParam(value = "worse_min") double worseMin,
                                                 @RequestParam(value = "worst_max") double worstMax,
                                                 HttpServletRequest request)
    {
        try {
            String token = request.getHeader("token");
            RepoTagMetric repoTagMetric = RepoTagMetric.builder()
                    .repoUuid(repoUuid).tag(tag).updater(token).updateTime(LocalDate.now())
                    .bestMin(bestMin)
                    .betterMax(betterMax).betterMin(betterMin)
                    .normalMax(normalMax).normalMin(normalMin)
                    .worseMax(worseMax).worseMin(worseMin)
                    .worstMax(worstMax)
                    .build();
            measureRepoService.insertRepoTagMetric(repoTagMetric);
            return new ResponseBean<>(HttpStatus.OK.value(),"insert success",null);
        }catch (Exception e) {
            return new ResponseBean<>(HttpStatus.BAD_REQUEST.value(),"insert failed",e.getMessage());
        }
    }


    @Autowired
    public MeasureRepoController(MeasureRepoService measureRepoService) {
        this.measureRepoService = measureRepoService;
    }

    @Autowired
    public void setProjectDao(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }
}
