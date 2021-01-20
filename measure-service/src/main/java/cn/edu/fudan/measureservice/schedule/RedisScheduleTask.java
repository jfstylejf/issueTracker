package cn.edu.fudan.measureservice.schedule;

import cn.edu.fudan.measureservice.controller.MeasureDeveloperController;
import cn.edu.fudan.measureservice.domain.dto.Query;
import cn.edu.fudan.measureservice.mapper.RepoMeasureMapper;
import cn.edu.fudan.measureservice.service.MeasureDeveloperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    @Value("${token}")
    private String token;

//    RedisTemplate

    private MeasureDeveloperController measureDeveloperController;
    private MeasureDeveloperService measureDeveloperService;
    private RepoMeasureMapper repoMeasureMapper;

    /**
     * 缓存的过期时间配置为24小时
     * 每天凌晨两点刷新数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    private void configureTasks() throws ParseException {
        String until = dtf.format(LocalDate.now().plusDays(1));
        Query query = new Query(token,null,until,null,new ArrayList<>());
        measureDeveloperService.clearCache();
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

    @Autowired
    public void setMeasureDeveloperService(MeasureDeveloperService measureDeveloperService) {
        this.measureDeveloperService = measureDeveloperService;
    }

    @Autowired
    public void setRepoMeasureMapper(RepoMeasureMapper repoMeasureMapper) {
        this.repoMeasureMapper = repoMeasureMapper;
    }

    @Autowired
    public void setMeasureDeveloperController(MeasureDeveloperController measureDeveloperController) {
        this.measureDeveloperController = measureDeveloperController;
    }

}