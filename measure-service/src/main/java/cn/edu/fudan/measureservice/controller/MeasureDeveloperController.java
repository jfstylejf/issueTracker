package cn.edu.fudan.measureservice.controller;

import cn.edu.fudan.measureservice.dao.ProjectDao;
import cn.edu.fudan.measureservice.domain.ResponseBean;
import cn.edu.fudan.measureservice.domain.bo.DeveloperCommitStandard;
import cn.edu.fudan.measureservice.domain.bo.DeveloperWorkLoad;
import cn.edu.fudan.measureservice.domain.dto.DeveloperRepoInfo;
import cn.edu.fudan.measureservice.domain.dto.Query;
import cn.edu.fudan.measureservice.domain.vo.DeveloperCommitStandardFrontend;
import cn.edu.fudan.measureservice.domain.vo.DeveloperWorkLoadFrontend;
import cn.edu.fudan.measureservice.portrait.DeveloperMetrics;
import cn.edu.fudan.measureservice.domain.bo.DeveloperPortrait;
import cn.edu.fudan.measureservice.service.MeasureDeveloperService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
public class MeasureDeveloperController {


    private final MeasureDeveloperService measureDeveloperService;

    private ProjectDao projectDao;

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
            @ApiImplicitParam(name = "developers", value = "项目总览传入人员列表", dataType = "List<String>",defaultValue = ""),
            @ApiImplicitParam(name = "repo_uuids", value = "库列表", dataType = "String", defaultValue = "" ),
            @ApiImplicitParam(name = "since", value = "起始时间（yyyy-MM-dd）", dataType = "String", defaultValue = "2019-02-20"),
            @ApiImplicitParam(name = "until", value = "截止时间（yyyy-MM-dd）", dataType = "String", defaultValue = "当前时间"),
            @ApiImplicitParam(name = "project_name", value = "项目名称(目前项目和repo_uuids不能一起传)", dataType = "String", defaultValue = ""),
            @ApiImplicitParam(name = "page", value = "页数,仅在查看不规范明细时需传入page", dataType = "int", defaultValue = "1"),
            @ApiImplicitParam(name = "ps", value = "每页显示个数,仅在查看不规范明细时需传入size", dataType = "int", defaultValue = "10"),
            @ApiImplicitParam(name = "asc", value = "排序形式", dataType = "Boolean", defaultValue = "true")
    })

    @SuppressWarnings("unchecked")
    @GetMapping("/measure/developer/work-load")
    @CrossOrigin
    public ResponseBean<Object> getDeveloperWorkLoad(
            @RequestParam(value="developer",required = false)String developer,
            @RequestParam(value = "developers",required = false) List<String> developers,
            @RequestParam(value="since",required = false)String since,
            @RequestParam(value="until",required = false)String until,
            @RequestParam(value = "project_name",required = false) String projectName,
            @RequestParam(value ="repo_uuids" ,required = false)String repoUuid,
            @RequestParam(required = false, defaultValue = "1")int page,
            @RequestParam(required = false, defaultValue = "5")int ps,
            @RequestParam(required = false, defaultValue = "")String order,
            @RequestParam(required = false, defaultValue = "true")boolean asc,
            HttpServletRequest request
    ){
        try{
            until = timeProcess(until);
            String token = request.getHeader("token");
            List<String> repoUuidList;
            if(projectName!=null && !"".equals(projectName)) {
                repoUuidList = projectDao.getProjectRepoList(projectName,token);
            }else {
                repoUuidList = projectDao.involvedRepoProcess(repoUuid,token);
            }
            Query query = new Query(token,since,until,developer,repoUuidList);
            List<DeveloperWorkLoad> developerWorkLoadList = measureDeveloperService.getDeveloperWorkLoad(query,developers);
            if (order!=null && !"".equals(order)) {
                Collections.sort(developerWorkLoadList, (o1, o2) -> {
                    if(asc) {
                        return o1.getTotalLoc()-o2.getTotalLoc();
                    }else {
                        return o2.getTotalLoc()-o1.getTotalLoc();
                    }
                });
                int totalPage = developerWorkLoadList.size() % ps == 0 ? developerWorkLoadList.size()/ps : developerWorkLoadList.size()/ps + 1;
                List<DeveloperWorkLoad> selectedDeveloperWorkLoadList = developerWorkLoadList.subList((page-1)*ps,page * ps > developerWorkLoadList.size() ? developerWorkLoadList.size() : page*ps);
                DeveloperWorkLoadFrontend developerWorkLoadFrontend = new DeveloperWorkLoadFrontend(page,totalPage,developerWorkLoadList.size(),selectedDeveloperWorkLoadList);
                return new ResponseBean<>(200,"success",developerWorkLoadFrontend);
            }else {
                return new ResponseBean<>(200,"success",developerWorkLoadList);
            }
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
    // fixme
    public ResponseBean<DeveloperMetrics> getPortrait(@RequestParam(value = "repo_uuids")String repoUuid,
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
            List<String> repoUuidList = projectDao.involvedRepoProcess(repoUuid,token);
            Query query = new Query(token,since,until,developer,repoUuidList);
            return new ResponseBean<>(200,"success",null);
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
    public ResponseBean<DeveloperPortrait> getDeveloperPortrait(@RequestParam("developer")String developer,
                                                                @RequestParam(value = "repo_uuids",required = false) String repoUuid,
                                                                @RequestParam(value = "project_name",required = false) String projectName,
                                                                @RequestParam(value = "since", required = false)String since,
                                                                @RequestParam(value = "until", required = false)String until,
                                                                HttpServletRequest request){

        try{
            until = timeProcess(until);
            String token = request.getHeader("token");
            List<String> repoUuidList;
            if(projectName!=null && !"".equals(projectName)) {
                repoUuidList = projectDao.getProjectRepoList(projectName,token);
            }else {
                repoUuidList = projectDao.involvedRepoProcess(repoUuid,token);
            }
            // todo 再根据开发者实际参与的库筛选一遍
            Query query = new Query(token,since,until,developer,repoUuidList);
            //fixme
            Map<String,List<DeveloperRepoInfo>> developerRepoInfos = projectDao.getDeveloperRepoInfoList(query);
            return new ResponseBean<>(200,"success", measureDeveloperService.getDeveloperPortrait(query,developerRepoInfos));
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

    @ApiOperation(value = "开发者提交规范性", notes = "@return List<DeveloperCommitStandard>", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "developer", value = "开发人员姓名", dataType = "String",defaultValue = "yuping"),
            @ApiImplicitParam(name = "developers", value = "项目总览传入人员列表", dataType = "List<String>",defaultValue = ""),
            @ApiImplicitParam(name = "since", value = "起始时间（yyyy-MM-dd）", dataType = "String", defaultValue = "2019-02-20"),
            @ApiImplicitParam(name = "until", value = "截止时间（yyyy-MM-dd）", dataType = "String", defaultValue = "当前时间"),
            @ApiImplicitParam(name = "repo_uuids", value = "多个repo_uuid之间用‘，’分割", dataType = "String", defaultValue = "3ecf804e-0ad6-11eb-bb79-5b7ba969027e"),
            @ApiImplicitParam(name = "project_name", value = "项目名称(目前项目和repo_uuids不能一起传)", dataType = "String", defaultValue = ""),
            @ApiImplicitParam(name = "page", value = "页数,仅在查看不规范明细时需传入page", dataType = "int", defaultValue = "1"),
            @ApiImplicitParam(name = "ps", value = "每页显示个数,仅在查看不规范明细时需传入size", dataType = "int", defaultValue = "10"),
            @ApiImplicitParam(name = "asc", value = "排序形式", dataType = "Boolean", defaultValue = "true")
    })

    @GetMapping("/measure/commit-standard")
    @CrossOrigin
    public ResponseBean<Object> getCommitStandard(@RequestParam(value = "developer",required = false)String developer,
                                          @RequestParam(value = "developers",required = false) List<String> developers,
                                          @RequestParam(value = "project_name",required = false) String projectName,
                                          @RequestParam(value = "repo_uuids",required = false)String repoUuid,
                                          @RequestParam(value = "since", required = false)String since,
                                          @RequestParam(value = "until", required = false)String until,
                                          @RequestParam(required = false, defaultValue = "1")int page,
                                          @RequestParam(required = false, defaultValue = "5")int ps,
                                          @RequestParam(required = false, defaultValue = "true") boolean asc,
                                          @RequestParam(required = false, defaultValue = "") String order,
                                          HttpServletRequest request){

        try{
            until = timeProcess(until);
            String token = request.getHeader("token");
            List<String> repoUuidList;
            if(projectName!=null && !"".equals(projectName)) {
                repoUuidList = projectDao.getProjectRepoList(projectName,token);
            }else {
                repoUuidList = projectDao.involvedRepoProcess(repoUuid,token);
            }
            Query query = new Query(token,since,until,developer,repoUuidList);
            List<DeveloperCommitStandard> developerCommitStandardList = measureDeveloperService.getCommitStandard(query,developers);
            if(order!=null && !"".equals(order)) {
                Collections.sort(developerCommitStandardList, (o1, o2) -> {
                    if(asc) {
                        return (int) ((o1.getCommitStandard() - o2.getCommitStandard()) * 100);
                    }else {
                        return (int) ((o2.getCommitStandard() - o1.getCommitStandard()) * 100);
                    }
                });
                int totalPage = developerCommitStandardList.size() % ps == 0 ? developerCommitStandardList.size()/ps : developerCommitStandardList.size()/ps + 1;
                List<DeveloperCommitStandard> selectedDeveloperCommitStandardList = developerCommitStandardList.subList(ps*(page-1),ps*page > developerCommitStandardList.size() ? developerCommitStandardList.size() : ps*page);
                DeveloperCommitStandardFrontend developerCommitStandardFrontend = new DeveloperCommitStandardFrontend(page,totalPage,developerCommitStandardList.size(),selectedDeveloperCommitStandardList);
                return new ResponseBean<>(200,"success",developerCommitStandardFrontend);
            }else if (developer!=null && !"".equals(developer)){
                // 如果传入单个开发者，此时 page 按照不规范明细进行分页
                DeveloperCommitStandard developerCommitStandard = developerCommitStandardList.get(0);
                List<Map<String,String>> developerInvalidCommitList = developerCommitStandard.getDeveloperInvalidCommitInfo().subList((page-1)*ps , page*ps > developerCommitStandard.getDeveloperInvalidCommitCount() ? developerCommitStandard.getDeveloperInvalidCommitCount() : page*ps);
                developerCommitStandard.setDeveloperInvalidCommitInfo(developerInvalidCommitList);
                return new ResponseBean<>(200,"success",Collections.singletonList(developerCommitStandard));
            }else {
                return new ResponseBean<>(200,"success",developerCommitStandardList);
            }
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(401,"failed " + e.getMessage(),null);
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
    public ResponseBean<List<Map<String, Object>>> getDeveloperList(@RequestParam(value = "repo_uuids", required = false)String repoUuid,
                                                                    @RequestParam(value = "project_name" ,required = false) String projectName,
                                                                    @RequestParam(value = "since" , required = false ) String since,
                                                                    @RequestParam(value = "until" , required = false ) String until,
                                                                    @RequestParam(required = false, defaultValue = "1")int page,
                                                                    @RequestParam(required = false, defaultValue = "10")int ps,
                                                                    @RequestParam(required = false, defaultValue = "")String order,
                                                                    @RequestParam(required = false, defaultValue = "true")boolean asc ,
                                                                    HttpServletRequest request){
        try{
            until = timeProcess(until);
            String token = request.getHeader("token");
            return new ResponseBean<>(200,"success",(List<Map<String, Object>>) measureDeveloperService.getDeveloperList(repoUuid,projectName,since,until,token));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(401,"failed "+ e.getMessage(),null);
        }
    }

    /**
     * 查询时间统一处理加一天
     * @param until 查询截止时间
     * @return String until
     */
    private String timeProcess(String until) {
        try {
            if(until!=null && !"".equals(until)) {
                until = dtf.format(LocalDate.parse(until,dtf).plusDays(1));
            }else {
                until = dtf.format(LocalDate.now().plusDays(1));
            }
            return until;
        }catch (Exception e) {
            e.getMessage();
        }
        return null;
    }

    @Autowired
    public void setProjectDao(ProjectDao projectDao) {this.projectDao = projectDao;}

}
