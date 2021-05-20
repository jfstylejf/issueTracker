package cn.edu.fudan.measureservice.controller;

import cn.edu.fudan.measureservice.domain.ResponseBean;
import cn.edu.fudan.measureservice.service.MeasureDevHistoryService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * description:
 *
 * @author fancying
 * create: 2020-06-11 10:41
 **/
@Slf4j
@CrossOrigin
@RestController
public class MeasureDevHistoryController {


    private MeasureDevHistoryService measureDevHistoryService;

    /**
     *   @ApiOperation注解 value添加该api的说明，用note表示该api的data返回类型，httpMethod表示请求方式
     *   @ApiImplicitParams 参数列表集合注解
     *   @ApiImplicitParam 参数说明接口 对应api中@RequestParam作解释说明 可显示在swagger2-ui页面上
     *   name表示参数名 value表示对参数的中文解释 dataType表示该参数类型 required表示该参数是否必须 defaultValue提供测试样例
     *   具体@ApiImplicitParam其他属性ctrl+click 点进源码
     */

    @ApiOperation(value = "开发历史动画 commit相关数据", notes = "@return List<Map<String, Object>> key: developer_name, commit_time, commit_message, commit_id, first_parent_commit_id", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuid", value = "repo_uuid", dataType = "String", required = true, defaultValue = "3ecf804e-0ad6-11eb-bb79-5b7ba969027e" ),
            @ApiImplicitParam(name = "since", value = "起始时间（yyyy-MM-dd）", dataType = "String", defaultValue = "2019-02-20"),
            @ApiImplicitParam(name = "until", value = "截止时间（yyyy-MM-dd）", dataType = "String", defaultValue = "当前时间"),
    })

    @SuppressWarnings("unchecked")
    @GetMapping("/measure/development-history/commit")
    public ResponseBean<List<Map<String, Object>>> getDevHistoryCommitInfo(@RequestParam("repo_uuid")String repoUuid,
                                                                           @RequestParam(value = "since",required = false)String since,
                                                                           @RequestParam(value = "until",required = false)String until){

        try{
            return new ResponseBean<>(HttpStatus.OK.value(),"success",(List<Map<String, Object>>) measureDevHistoryService.getDevHistoryCommitInfo(repoUuid,since,until));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<>(HttpStatus.BAD_REQUEST.value(),"failed "+ e.getMessage(),null);
        }
    }

    @ApiOperation(value = "开发历史动画 文件相关数据", notes = "@return List<Map<String, Object>> key: file_path,add_lines,diff_ccn,del_lines,lastCcn,commit_id,ccn", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "commit_id", value = "commit_id", dataType = "String", required = true, defaultValue = "7697c69d749dad14f37e1a6072b0090cb869caf2" ),
    })
    @SuppressWarnings("unchecked")
    @GetMapping("/measure/development-history/file")
    public ResponseBean<List<Map<String, Object>>> getDevHistoryFileInfo(@RequestParam("commit_id")String commitId){

        try{
            return new ResponseBean<> (HttpStatus.OK.value(),"success",(List<Map<String, Object>>) measureDevHistoryService.getDevHistoryFileInfo(commitId));
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean<> (HttpStatus.BAD_REQUEST.value(),"failed "+ e.getMessage(),null);
        }
    }


    @Autowired
    public void setMeasureScanService(MeasureDevHistoryService measureDevHistoryService) {
        this.measureDevHistoryService = measureDevHistoryService;
    }


}