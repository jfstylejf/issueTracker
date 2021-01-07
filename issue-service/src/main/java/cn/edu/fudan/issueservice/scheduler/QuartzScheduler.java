package cn.edu.fudan.issueservice.scheduler;

import cn.edu.fudan.issueservice.service.IssueMeasureInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Beethoven
 */
@Component
public class QuartzScheduler {

    @Autowired
    private IssueMeasureInfoService issueMeasureInfoService;

    /**
     * 每天凌晨两点清楚缓存数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void refreshCache() {
        issueMeasureInfoService.clearCache();
    }
}
