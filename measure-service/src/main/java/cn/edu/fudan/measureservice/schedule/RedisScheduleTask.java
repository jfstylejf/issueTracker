package cn.edu.fudan.measureservice.schedule;

import cn.edu.fudan.measureservice.controller.MeasureDeveloperController;
import cn.edu.fudan.measureservice.dao.MeasureDao;
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
    private MeasureDao measureDao;

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


        log.info("Successfully re request developerList, portrait, recentNews API.");
    }

    /**
     *  每天凌晨2点 缓存开发者列表
     */
    @Scheduled(cron = "0 0 2 * * ?")
    private void initDeveloperInfo() {
        List<String> visibleRepoList = projectDao.getVisibleRepoListByProjectNameAndRepo(null,null,token);
        // 缓存开发者列表
        projectDao.getDeveloperList(visibleRepoList);
    }

    /**
     *  每天凌晨2点 缓存最新的项目相关信息
     */
    @Scheduled(cron = "0 0 2 * * ?")
    private void initProjectInfo() {
        List<String> visibleRepoList = projectDao.getVisibleRepoListByProjectNameAndRepo(null,null,token);
        // 缓存项目id和项目名的关系
        projectDao.getProjectNameListById(null);
        // 缓存 repoUuid 和项目名，库名的对应关系
        for (String repoUuid : visibleRepoList) {
            projectDao.getProjectName(repoUuid);
            projectDao.getRepoName(repoUuid);
            measureDao.getCurrentBigFileInfo(repoUuid,null);
        }
        List<String> projectNameList = projectDao.getVisibleProjectByToken(token);
        // 缓存项目包含库的关系
        for (String projectName : projectNameList) {
            projectDao.getProjectRepoList(projectName);
        }
    }

    /**
     *  每天凌晨2点 缓存这一周的提交规范性趋势图,超大文件数趋势图
     */
    @Scheduled(cron = "0 0 2 * * ?")
    private void initProjectTreadChart() {
        // beginTime 及 endTime 处理为当前周的 周一 和 周日
        LocalDate begin = DateTimeUtil.initBeginTimeByInterval(LocalDate.now().minusMonths(2), GranularityEnum.Week.getType());
        LocalDate end = DateTimeUtil.initEndTimeByInterval(LocalDate.now(),GranularityEnum.Week.getType());
        assert begin != null;
        assert end != null;
        // 缓存这周的趋势图
        measureDeveloperService.getCommitStandardTrendChartIntegratedByProject(null,DateTimeUtil.dtf.format(begin),DateTimeUtil.dtf.format(end),token,GranularityEnum.Week.getType());
        measureDeveloperService.getHugeLocRemainedFile(null,DateTimeUtil.dtf.format(begin),DateTimeUtil.dtf.format(end),token,GranularityEnum.Week.getType());
    }

    /**
     *  每天凌晨2点 缓存今天为止的超大文件数明细
     */
    @Scheduled(cron = "0 0 2 * * ?")
    private void initCurrentBigFileInfo() {
        measureDeveloperService.getHugeLocRemainedDetail(null,null,token);
    }


    /**
     * 每月1号凌晨1点 删除提交规范性趋势图
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    private void deleteProjectCommitStandardTrendChart() {
        measureDeveloperService.deleteProjectCommitStandardChart();
    }

    /**
     * 每月1号凌晨1点 删除提交规范性趋势图
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    private void deleteProjectBigFileTrendChart() {
        measureDao.deleteRepoBigFileTrendChart();
        measureDeveloperService.deleteProjectBigFileTrendChart();
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
    public void setMeasureDao(MeasureDao measureDao) {
        this.measureDao = measureDao;
    }

    @Autowired
    public void setMeasureDeveloperController(MeasureDeveloperController measureDeveloperController) {
        this.measureDeveloperController = measureDeveloperController;
    }

}