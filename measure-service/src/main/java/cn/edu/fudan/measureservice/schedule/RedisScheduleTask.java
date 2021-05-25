package cn.edu.fudan.measureservice.schedule;

import cn.edu.fudan.measureservice.controller.MeasureDeveloperController;
import cn.edu.fudan.measureservice.dao.ProjectDao;
import cn.edu.fudan.measureservice.domain.dto.ProjectPair;
import cn.edu.fudan.measureservice.domain.dto.Query;
import cn.edu.fudan.measureservice.domain.dto.RepoInfo;
import cn.edu.fudan.measureservice.domain.enums.GranularityEnum;
import cn.edu.fudan.measureservice.domain.vo.ProjectCommitStandardTrendChart;
import cn.edu.fudan.measureservice.mapper.RepoMeasureMapper;
import cn.edu.fudan.measureservice.service.MeasureDeveloperService;
import cn.edu.fudan.measureservice.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * description: 定时刷新redis中的数据
 *
 * @author fancying
 * create: 2020-08-25 22:30
 **/
@Slf4j
@EnableScheduling
@Component
public class RedisScheduleTask {

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private ProjectDao projectDao;

    @Value("${token}")
    private String token;

//    RedisTemplate

    private MeasureDeveloperController measureDeveloperController;
    private MeasureDeveloperService measureDeveloperService;
    private RepoMeasureMapper repoMeasureMapper;

    /**
     * Springboot 默认是用 newSingleThreadScheduledExecutor() 创建，若没有给定TaskScheduler，则无法在同一时间内执行多个任务
     * @return
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(50);
        return taskScheduler;
    }


    /**
     * 缓存的过期时间配置为24小时
     * 每天凌晨两点刷新数据
     * fixme 存在问题之后改
     * todo 添加每日删除developerValidMsg相关信息
     */
    @Scheduled(cron = "0 0 2 * * ?")
    private void configureTasks() throws ParseException {
        String until = dtf.format(LocalDate.now().plusDays(1));
        List<String> leaderIntegratedRepoList = projectDao.getVisibleRepoInfoByToken(token);
        Query query = new Query(token,null,until,null,leaderIntegratedRepoList);
        measureDeveloperService.clearCache();
        log.info("start to get developerList with query : {}",query);
        measureDeveloperService.getDeveloperList(query);
        List<Map<String, Object>> developerList = repoMeasureMapper.getDeveloperListByrepoUuidList(null);
        for (int i = 0; i < developerList.size(); i++){
            Map<String,Object> map = developerList.get(i);
            String developerName = map.get("developer_name").toString();
            measureDeveloperService.getPortraitCompetence(developerName,null,null,null,token);
            measureDeveloperService.getDeveloperRecentNews(null,developerName,null,null);
        }

        // 提交规范性趋势图本周缓存更新
        initProjectCommitStandardTrendChart();

        log.info("Successfully re request developerList, portrait, recentNews API.");
    }


    /**
     *  每天凌晨2点 缓存这一周的提交规范性趋势图
     */
    private void initProjectCommitStandardTrendChart() {
        List<ProjectPair> projectPairList = projectDao.getVisibleProjectPairListByProjectIds(null,token);
        // beginTime 及 endTime 处理为当前周的 周一 和 周日
        LocalDate begin = DateTimeUtil.initBeginTimeByInterval(LocalDate.now(), GranularityEnum.Week.getType());
        LocalDate end = DateTimeUtil.initEndTimeByInterval(LocalDate.now(),GranularityEnum.Week.getType());
        assert begin != null;
        assert end != null;
        for (ProjectPair projectPair : projectPairList) {
            measureDeveloperService.deleteProjectCommitStandardChart(projectPair.getProjectName(),DateTimeUtil.dtf.format(end));
        }
        // 缓存这周的趋势图
        measureDeveloperService.getCommitStandardTrendChartIntegratedByProject(null,DateTimeUtil.dtf.format(begin),DateTimeUtil.dtf.format(end),token,GranularityEnum.Week.getType());
    }

    /**
     * 每月1号凌晨1点 删除提交规范性趋势图
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    private void deleteProjectCommitStandardTrendChart() {
        measureDeveloperService.deleteProjectCommitStandardChart();
    }



    @Autowired
    public void setMeasureDeveloperService(MeasureDeveloperService measureDeveloperService) {
        this.measureDeveloperService = measureDeveloperService;
    }

    @Autowired
    public void setRepoMeasureMapper(RepoMeasureMapper repoMeasureMapper) {
        this.repoMeasureMapper = repoMeasureMapper;
    }

    @Autowired
    public void setProjectDao(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    @Autowired
    public void setMeasureDeveloperController(MeasureDeveloperController measureDeveloperController) {
        this.measureDeveloperController = measureDeveloperController;
    }

}