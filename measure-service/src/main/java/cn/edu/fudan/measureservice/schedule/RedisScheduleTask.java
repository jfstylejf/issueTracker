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
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
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
@Configuration
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

    @Scheduled(cron = "0 0 2 1 * ?")
    private void initProjectCommitStandardTrendChart() throws ParseException{
        List<ProjectPair> projectPairList = projectDao.getVisibleProjectPairListByProjectIds(null,token);
        LocalDate begin = LocalDate.now();
        LocalDate end = begin.with(TemporalAdjusters.lastDayOfMonth());
        // 缓存这个月的趋势图
        measureDeveloperService.getCommitStandardTrendChartIntegratedByProject(null,DateTimeUtil.dtf.format(begin),DateTimeUtil.dtf.format(end),token,GranularityEnum.Week.getType());
        //删除上个月的趋势图
        LocalDate delBegin = begin.minusMonths(1);
        LocalDate delEnd = end.minusMonths(1);
        // 根据 interval 对 beginTime 及 endTime 处理为当前周的 周一 和 周日
        delBegin = DateTimeUtil.initBeginTimeByInterval(delBegin, GranularityEnum.Week.getType());
        delEnd = DateTimeUtil.initEndTimeByInterval(delEnd,GranularityEnum.Week.getType());
        assert delBegin != null;
        assert delEnd != null;
        while (delBegin.isBefore(delEnd)) {
            LocalDate tempTime = DateTimeUtil.selectTimeIncrementByInterval(delBegin,GranularityEnum.Week.getType());
            if(tempTime == null) {
                break;
            }
            if(tempTime.isAfter(delEnd)) {
                tempTime = delEnd;
            }
            for (ProjectPair projectPair : projectPairList) {
                Query query = new Query(token,delBegin.format(dtf),tempTime.format(dtf),null,null);
                measureDeveloperService.deleteSingleProjectCommitStandardChart(query,projectPair);
            }
            delBegin = tempTime;
        }
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