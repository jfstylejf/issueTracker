package cn.edu.fudan.measureservice.controller;

import cn.edu.fudan.measureservice.domain.CommitBaseInfoDuration;
import cn.edu.fudan.measureservice.domain.Granularity;
import cn.edu.fudan.measureservice.domain.RepoMeasure;
import cn.edu.fudan.measureservice.domain.ResponseBean;
import cn.edu.fudan.measureservice.service.MeasureRepoService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
public class MeasureRepoController {


    private MeasureRepoService measureRepoService;

    public MeasureRepoController(MeasureRepoService measureRepoService) {
        this.measureRepoService = measureRepoService;
    }

    @ApiOperation(value = "获取一个项目在某个时间段特定时间单位的项目级别的所有度量信息", notes = "@return List<RepoMeasure>", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuid", value = "参与库", dataType = "String",required = true,defaultValue = "3ecf804e-0ad6-11eb-bb79-5b7ba969027e" ),
            @ApiImplicitParam(name = "since", value = "起始时间（yyyy-MM-dd）", dataType = "String", defaultValue = "2019-02-20"),
            @ApiImplicitParam(name = "until", value = "截止时间（yyyy-MM-dd）", dataType = "String",required = true,defaultValue = "当前时间"),
            @ApiImplicitParam(name = "granularity", value = "时间粒度", dataType = "String", required = true,defaultValue = "week")
    })

    @GetMapping("/measure/repository")
    @CrossOrigin
    public ResponseBean<List<RepoMeasure>> getMeasureDataByRepoId(@RequestParam("repo_uuid")String repoUuid,
                                                                  @RequestParam(name="since",required = false)String since,
                                                                  @RequestParam("until")String until,
                                                                  @RequestParam("granularity") Granularity granularity){
        try{
            return new ResponseBean<>(200,"success", measureRepoService.getRepoMeasureByRepoId(repoUuid,since,until,granularity));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(401,"failed "+ e.getMessage(),null);
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
                                                                                   @RequestParam("since")String since,
                                                                                   @RequestParam("until")String until,
                                                                                   @RequestParam(name = "developer",required = false)String developerName
                                                 ){
        try{
            return new ResponseBean<>(200,"success",measureRepoService.getCommitBaseInformationByDuration(repoUuid, since, until, developerName));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(401,"failed "+e.getMessage(),null);
        }

    }

    @ApiOperation(value = "根据repo_id和since、until获取某个时间段内commit次数最多的5位开发者姓名以及对应的commit次数", notes = "@return List<Map<String,Object>> key : counts, developer_name", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuid", value = "参与库", dataType = "String",required = true,defaultValue = "3ecf804e-0ad6-11eb-bb79-5b7ba969027e" ),
            @ApiImplicitParam(name = "since", value = "起始时间（yyyy-MM-dd）", dataType = "String", defaultValue = "2019-02-20"),
            @ApiImplicitParam(name = "until", value = "截止时间（yyyy-MM-dd）", dataType = "String",defaultValue = "当前时间"),
    })
    @SuppressWarnings("unchecked")
    @GetMapping("/measure/developer-rank/commit-count")
    @CrossOrigin
    public ResponseBean<List<Map<String,Object>>> getDeveloperRankByCommitCount(
            @RequestParam("repo_uuid")String repoUuid,
            @RequestParam("since")String since,
            @RequestParam("until")String until){
        try{
            return new ResponseBean<>(200,"success",(List<Map<String,Object>>) measureRepoService.getDeveloperRankByCommitCount(repoUuid, since, until));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(401,"failed "+ e.getMessage(),null);
        }
    }

    @ApiOperation(value = "根据repo_id和since、until获取某个时间段内,该项目中提交代码行数（LOC）最多的前5名开发者的姓名以及对应的LOC", notes = "@return List<Map<String,Object>> key : counts,developer_name", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuid", value = "参与库", dataType = "String",required = true,defaultValue = "3ecf804e-0ad6-11eb-bb79-5b7ba969027e" ),
            @ApiImplicitParam(name = "since", value = "起始时间（yyyy-MM-dd）", dataType = "String", defaultValue = "2019-02-20"),
            @ApiImplicitParam(name = "until", value = "截止时间（yyyy-MM-dd）", dataType = "String",defaultValue = "当前时间"),
    })
    @SuppressWarnings("unchecked")
    @GetMapping("/measure/developer-rank/loc")
    @CrossOrigin
    public ResponseBean<List<Map<String, Object>>> getDeveloperRankByLoc(
            @RequestParam("repo_uuid")String repoUuid,
            @RequestParam("since")String since,
            @RequestParam("until")String until){
        try{
            return new ResponseBean<>(200,"success",(List<Map<String, Object>>) measureRepoService.getDeveloperRankByLoc(repoUuid, since, until));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(401,"failed "+e.getMessage(),null);
        }
    }

    @ApiOperation(value = "获取某段时间内，每天的所有开发者产生的LOC", notes = "@return List<Map<String,Object>> key : commit_date, LOC,commit_count", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuid", value = "参与库", dataType = "String",required = true,defaultValue = "3ecf804e-0ad6-11eb-bb79-5b7ba969027e" ),
            @ApiImplicitParam(name = "since", value = "起始时间（yyyy-MM-dd）", dataType = "String", defaultValue = "2019-02-20"),
            @ApiImplicitParam(name = "until", value = "截止时间（yyyy-MM-dd）", dataType = "String", defaultValue = "当前时间"),
    })
    @SuppressWarnings("unchecked")
    @GetMapping("/measure/repository/commit-count&LOC-daily")
    @CrossOrigin
    public ResponseBean<List<Map<String, Object>>> getCommitCountLOCDaily(
            @RequestParam("repo_uuid")String repoUuid,
            @RequestParam("since")String since,
            @RequestParam("until")String until){
        try{
            return new ResponseBean<>(200,"success",(List<Map<String, Object>>) measureRepoService.getCommitCountLOCDaily(repoUuid, since, until));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(401,"failed "+e.getMessage(),null);
        }
    }


}
