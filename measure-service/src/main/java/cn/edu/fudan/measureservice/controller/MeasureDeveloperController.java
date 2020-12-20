package cn.edu.fudan.measureservice.controller;

import cn.edu.fudan.measureservice.domain.ResponseBean;
import cn.edu.fudan.measureservice.domain.dto.Query;
import cn.edu.fudan.measureservice.portrait.DeveloperMetrics;
import cn.edu.fudan.measureservice.portrait.DeveloperPortrait;
import cn.edu.fudan.measureservice.service.MeasureDeveloperService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MeasureDeveloperController {


    private final MeasureDeveloperService measureDeveloperService;

    private final static String split = ",";

    private final static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public MeasureDeveloperController(MeasureDeveloperService measureDeveloperService) {
        this.measureDeveloperService = measureDeveloperService;
    }

/**
 *   @ApiOperation注解 value添加该api的说明，用note表示该api的data返回类型，httpMethod表示请求方式
 *   @ApiImplicitParams 参数列表集合注解
 *   @ApiImplicitParam 参数说明接口 对应api中@RequestParam作解释说明 可显示在swagger2-ui页面上
 *   name表示参数名 value表示对参数的中文解释 dataType表示该参数类型 required表示该参数是否必须 defaultValue提供测试样例
 *   具体@ApiImplicitParam其他属性ctrl+click 点进源码
 */

    @ApiOperation(value = "开发者工作量数据接口", notes = "@return Map<String, Object> key : delLines, changedFiles, addLines, commitCount, developerName", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "developer", value = "开发人员姓名", dataType = "String",  defaultValue = "yuping"),
            @ApiImplicitParam(name = "repo_uuid", value = "repo_uuid", dataType = "String", defaultValue = "" ),
            @ApiImplicitParam(name = "since", value = "起始时间（yyyy-MM-dd）", dataType = "String", defaultValue = "2019-02-20"),
            @ApiImplicitParam(name = "until", value = "截止时间（yyyy-MM-dd）", dataType = "String", defaultValue = "当前时间"),
    })

    @SuppressWarnings("unchecked")
    @GetMapping("/measure/developer/work-load")
    @CrossOrigin
    public ResponseBean<Map<String, Object>> getDeveloperWorkLoad(
            @RequestParam(value="developer",required = false)String developer,
            @RequestParam(value="since",required = false)String since,
            @RequestParam(value="until",required = false)String until,
            @RequestParam(value = "project_name",required = false) String projectName,
            @RequestParam(value ="repo_uuid" ,required = false)String repoUuid,
            HttpServletRequest request
    ){
        try{
            if(until==null || "".equals(until)) {
                until = dtf.format(LocalDate.now().plusDays(1));
            }else {
                until = dtf.format(LocalDate.parse(until,dtf));
            }
            String token = request.getHeader("token");
            List<String> repoUuidList;
            if(repoUuid!=null && !"".equals(repoUuid)) {
                repoUuidList = Arrays.asList(repoUuid.split(split));
            }else {
                repoUuidList = null;
            }
            Query query = new Query(token,since,until,developer,repoUuidList);
            return new ResponseBean<>(200,"success",(Map<String, Object>) measureDeveloperService.getDeveloperWorkLoad(query,projectName));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(401,"failed" + e.getMessage(),null);
        }
    }


    @ApiOperation(value = "开发者雷达图度量基础数据接口 , 需要废弃", notes = "@return DeveloperMetrics", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "developer", value = "开发人员姓名", dataType = "String",required = true, defaultValue = "yuping"),
            @ApiImplicitParam(name = "repo_uuids", value = "repo_uuid", dataType = "String", required = true, defaultValue = "3ecf804e-0ad6-11eb-bb79-5b7ba969027e" ),
            @ApiImplicitParam(name = "since", value = "起始时间（yyyy-MM-dd）", dataType = "String", defaultValue = "2019-02-20"),
            @ApiImplicitParam(name = "until", value = "截止时间（yyyy-MM-dd）", dataType = "String", defaultValue = "当前时间"),
            @ApiImplicitParam(name = "tool", value = "扫描工具", dataType = "String", defaultValue = "sonarqube"),
            @ApiImplicitParam(name = "token", value = "token", dataType = "String",required = true,defaultValue = "ec15d79e36e14dd258cfff3d48b73d35")
    })

    @GetMapping("/measure/portrait")
    @Deprecated
    public ResponseBean<DeveloperMetrics> getPortrait(@RequestParam(value = "repo_uuids")String repoUuidList,
                                                      @RequestParam(value = "developer")String developer,
                                                      @RequestParam(value = "since", required = false, defaultValue = "")String since,
                                                      @RequestParam(value = "until", required = false, defaultValue = "")String until,
                                                      @RequestParam(value = "tool", required = false, defaultValue = "sonarqube")String tool,
                                                      HttpServletRequest request){
        try{
            if(until==null || "".equals(until)) {
                until = dtf.format(LocalDate.now().plusDays(1));
            }
            String token = request.getHeader("token");
            return new ResponseBean<>(200,"success",(DeveloperMetrics) measureDeveloperService.getPortrait(repoUuidList,developer,since,until,token,tool));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(401,"failed "+ e.getMessage() ,null);
        }
    }

    @ApiOperation(value = "获取开发者所参与所有库的画像", notes = "@return DeveloperPortrait", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "developer", value = "开发人员姓名", dataType = "String",required = true,defaultValue = "yuping"),
            @ApiImplicitParam(name = "since", value = "起始时间（yyyy-MM-dd）", dataType = "String", defaultValue = "2019-02-20"),
            @ApiImplicitParam(name = "until", value = "截止时间（yyyy-MM-dd）", dataType = "String", defaultValue = "当前时间"),
            @ApiImplicitParam(name = "token", value = "token", dataType = "String", required = true,defaultValue = "ec15d79e36e14dd258cfff3d48b73d35")
    })

    @GetMapping("/measure/portrait-level")
    public ResponseBean<DeveloperPortrait> getPortraitLevel(@RequestParam("developer")String developer,
                                                            @RequestParam(value = "since", required = false)String since,
                                                            @RequestParam(value = "until", required = false)String until,
                                                            HttpServletRequest request){

        try{
            if(until==null || "".equals(until)) {
                until = dtf.format(LocalDate.now().plusDays(1));
            }
            String token = request.getHeader("token");
            return new ResponseBean<>(200,"success",(DeveloperPortrait) measureDeveloperService.getPortraitLevel(developer,since,until,token));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(401,"failed "+e.getMessage(),null);
        }
    }

    @ApiOperation(value = "获取开发者能力：质量，效率，贡献等相关数据", notes = "@return List<cn.edu.fudan.measureservice.portrait2.DeveloperPortrait>", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "developer", value = "开发者姓名", dataType = "String", required = true, defaultValue = "yuping"),
            @ApiImplicitParam(name = "repo_uuids", value = "多个repo_uuid之间用‘，’分割", dataType = "String", defaultValue = "3ecf804e-0ad6-11eb-bb79-5b7ba969027e"),
            @ApiImplicitParam(name = "since", value = "起始时间（yyyy-MM-dd）", dataType = "String", defaultValue = "2019-02-20"),
            @ApiImplicitParam(name = "until", value = "截止时间（yyyy-MM-dd）", dataType = "String", defaultValue = "当前时间"),
            @ApiImplicitParam(name = "token", value = "token", dataType = "String", required = true, defaultValue = "ec15d79e36e14dd258cfff3d48b73d35")
    })

    @SuppressWarnings("unchecked")
    @GetMapping("/measure/portrait/competence")
    @CrossOrigin
    public ResponseBean<List<cn.edu.fudan.measureservice.portrait2.DeveloperPortrait>> getPortraitCompetence(@RequestParam(value = "developer" )String developer,
                                              @RequestParam(value = "repo_uuids",required = false)String repoUuidList,
                                              @RequestParam(value = "since", required = false)String since,
                                              @RequestParam(value = "until", required = false)String until,
                                              HttpServletRequest request){

        try{
            if(until==null || "".equals(until)) {
                until = dtf.format(LocalDate.now().plusDays(1));
            }
            String token = request.getHeader("token");
            return new ResponseBean<>(200,"success",(List<cn.edu.fudan.measureservice.portrait2.DeveloperPortrait>) measureDeveloperService.getPortraitCompetence(developer,repoUuidList,since,until,token));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(401,"failed "+ e.getMessage(),null);
        }
    }

    @ApiOperation(value = "开发者提交规范性", notes = "@return Object", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "developer", value = "开发人员姓名，传入developer返回该开发者不规范明细，不传时返回所有开发者提交规范性", dataType = "String",defaultValue = "yuping"),
            @ApiImplicitParam(name = "since", value = "起始时间（yyyy-MM-dd）", dataType = "String", defaultValue = "2019-02-20"),
            @ApiImplicitParam(name = "until", value = "截止时间（yyyy-MM-dd）", dataType = "String", defaultValue = "当前时间"),
            @ApiImplicitParam(name = "repo_uuids", value = "多个repo_uuid之间用‘，’分割", dataType = "String", defaultValue = "3ecf804e-0ad6-11eb-bb79-5b7ba969027e"),
            @ApiImplicitParam(name = "page", value = "页数,仅在查看不规范明细时需传入page", dataType = "int", defaultValue = "1"),
            @ApiImplicitParam(name = "ps", value = "每页显示个数,仅在查看不规范明细时需传入size", dataType = "int", defaultValue = "10"),
    })

    @GetMapping("/measure/commit-standard")
    @CrossOrigin
    public ResponseBean<Object> getCommitStandard(@RequestParam(value = "developer",required = false)String developer,
                                          @RequestParam(value = "project_name",required = false) String projectName,
                                          @RequestParam(value = "repo_uuids",required = false)String repoUuidList,
                                          @RequestParam(value = "since", required = false)String since,
                                          @RequestParam(value = "until", required = false)String until,
                                          @RequestParam(required = false, defaultValue = "1")int page,
                                          @RequestParam(required = false, defaultValue = "10")int ps,
                                          HttpServletRequest request){

        try{
            if(until==null || "".equals(until)) {
                until = dtf.format(LocalDate.now().plusDays(1));
            }else {
                until = dtf.format(LocalDate.parse(until,dtf).plusDays(1));
            }
            String token = request.getHeader("token");
            String condition ;
            Query query = new Query(token,since,until,developer,Arrays.asList(repoUuidList.split(split)));
            if(developer==null) {
                condition = "1";
                query.setDeveloper(null);
                return new ResponseBean<>(200,"success", measureDeveloperService.getCommitStandard(query,projectName,condition));
            }else {
                condition = "2";
                List<Map<String,Object>> invalidCommitList = measureDeveloperService.getCommitStandard(query,projectName,condition);
                Map<String,Object> map = new HashMap<>();
                map.put("totalCount",invalidCommitList.size());
                map.put("invalidCommitList",invalidCommitList.subList((page - 1) * ps , page * ps > invalidCommitList.size() ? invalidCommitList.size() : page * ps ));
                return  new ResponseBean<>(200,"success",map);
            }
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(401,"failed",e.getMessage());
        }
    }

    @ApiOperation(value = "返回用户画像页面得代码行数数据，包括所有项目和单个项目的 To codeTracker", notes = "@return Map<String,Object>", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "developer", value = "开发人员姓名", dataType = "String",defaultValue = "yuping"),
            @ApiImplicitParam(name = "repo_uuids", value = "多个repo_uuid之间用‘，’分割", dataType = "String",defaultValue = "3ecf804e-0ad6-11eb-bb79-5b7ba969027e"),
            @ApiImplicitParam(name = "since", value = "起始时间（yyyy-MM-dd）", dataType = "String", defaultValue = "2019-02-20"),
            @ApiImplicitParam(name = "until", value = "截止时间（yyyy-MM-dd）", dataType = "String", defaultValue = "当前时间")
    })

    @SuppressWarnings("unchecked")
    @GetMapping("/measure/statement")
    @Deprecated
    public ResponseBean<Map<String,Object>> getStatementByCondition(@RequestParam(value = "repo_uuids", required = false)String repUuidList,
                                                @RequestParam(value = "developer", required = false)String developer,
                                                @RequestParam(value = "since", required = false)String since,
                                                @RequestParam(value = "until", required = false)String until){

        try{
            if(until!=null && !"".equals(until)) {
                until = dtf.format(LocalDate.now().plusDays(1));
            }
            return new ResponseBean<>(200,"success",(Map<String,Object>) measureDeveloperService.getStatementByCondition(repUuidList,developer,since,until));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(401,"failed "+e.getMessage(),null);
        }
    }


    @ApiOperation(value = "开发者最新动态", notes = "@return List<Map<String, Object>> key : repo_id,jira_info,commit_time,message,developer_unique_name,commit_id", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "developer", value = "开发人员姓名", dataType = "String",required = true,defaultValue = "yuping"),
            @ApiImplicitParam(name = "repo_uuids", value = "多个repo_uuid之间用‘，’分割", dataType = "String",defaultValue = "3ecf804e-0ad6-11eb-bb79-5b7ba969027e"),
            @ApiImplicitParam(name = "since", value = "起始时间（yyyy-MM-dd）", dataType = "String", defaultValue = "2019-02-20"),
            @ApiImplicitParam(name = "until", value = "截止时间（yyyy-MM-dd）", dataType = "String", defaultValue = "当前时间")
    })

    @SuppressWarnings("unchecked")
    @GetMapping("/measure/developer/recent-news")
    @CrossOrigin
    public ResponseBean<List<Map<String, Object>>> getDeveloperRecentNews(@RequestParam(value = "repo_uuids", required = false)String repoUuidList,
                                               @RequestParam(value = "developer")String developer,
                                               @RequestParam(value = "since", required = false)String since,
                                               @RequestParam(value = "until", required = false)String until){

        try{
            if(until!=null && !"".equals(until)) {
                until = dtf.format(LocalDate.now().plusDays(1));
            }
            return new ResponseBean<>(200,"success",(List<Map<String, Object>>)measureDeveloperService.getDeveloperRecentNews(repoUuidList,developer,since,until));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(401,"failed "+e.getMessage(),null);
        }
    }

    @ApiOperation(value = "人员列表", notes = "@return List<Map<String, Object>> key: involveRepoCount, totalLevel, efficiency, developer_name, DutyType, value, quality", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuids", value = "多个repo_uuid之间用‘，’分割", dataType = "String",defaultValue = "3ecf804e-0ad6-11eb-bb79-5b7ba969027e"),
            @ApiImplicitParam(name = "token", value = "token", dataType = "String",required = true,defaultValue = "ec15d79e36e14dd258cfff3d48b73d35")
    })

    @SuppressWarnings("unchecked")
    @GetMapping("/measure/developer-list")
    @CrossOrigin
    public ResponseBean<List<Map<String, Object>>> getDeveloperList(@RequestParam(value = "repo_uuids", required = false)String repoUuidList,
                                                                    @RequestParam(value = "since" , required = false ) String since,
                                                                    @RequestParam(value = "until" , required = false ) String until,
                                                                    @RequestParam(value = "developer" , required = false ,defaultValue = "") String developer,
                                                                    @RequestParam(required = false, defaultValue = "1")int page,
                                                                    @RequestParam(required = false, defaultValue = "10")int ps,
                                                                    @RequestParam(required = false, defaultValue = "")String order,
                                                                    @RequestParam(required = false, defaultValue = "true")boolean asc ,
                                                                    HttpServletRequest request){
        try{
            LocalDate localDate;
            if(until!=null && !"".equals(until)) {
                localDate = LocalDate.parse(until, dtf);
            }else {
                localDate = LocalDate.now();
                localDate = localDate.plusDays(1);
            }
            until = dtf.format(localDate);
            String token = request.getHeader("token");
            Query query;
            if(repoUuidList!=null) {
                query = new Query(token,since,until,developer, Arrays.asList(repoUuidList.split(split)));
            }else {
                query = new Query(token,since,until,developer,null);
            }
            return new ResponseBean<>(200,"success",(List<Map<String, Object>>) measureDeveloperService.getDeveloperList(repoUuidList,token));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(401,"failed "+ e.getMessage(),null);
        }
    }




}
