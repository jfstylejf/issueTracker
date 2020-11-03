package cn.edu.fudan.issueservice.scheduler;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.service.IssueMeasureInfoService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuartzScheduler {


    private RestInterfaceManager restInterfaceManager;

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Autowired
    private IssueMeasureInfoService issueMeasureInfoService;

    /**
     * 每天凌晨两点清楚缓存数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void refreshCache() {
        issueMeasureInfoService.clearCache();
    }

    //@Scheduled(cron = "0 50 23 * * *")
    public void perDay() {
       durationUpdate("day");
    }

    //@Scheduled(cron = "0 55 23 * * SUN")
    public void perWeek() {
        durationUpdate("week");
    }

    //@Scheduled(cron = "0 10 0 1 * *")
    public void perMonth() {
        JSONArray projects = restInterfaceManager.getProjectList(null);//所有用户的所有project
        if (projects == null||projects.isEmpty()) {
            return;
        }
        for (int i=0;i<projects.size();i++) {
            JSONObject project=projects.getJSONObject(i);
            if(project!=null){
                String repo_id=project.getString("uuid");
                String category=project.getString("type");
                String dashboardKey = "dashboard:"+category+":month:"+ repo_id;
                stringRedisTemplate.delete(dashboardKey);
            }
        }
    }

    private void initialNewAndEliminated(String key){
        stringRedisTemplate.setEnableTransactionSupport(true);
        stringRedisTemplate.multi();
        stringRedisTemplate.opsForHash().put(key,"new","0");
        stringRedisTemplate.opsForHash().put(key,"eliminated","0");
        stringRedisTemplate.exec();
    }

    private void durationUpdate(String duration){
        List<String> accountIds=restInterfaceManager.getAccountIds();
        for (String account_id : accountIds) {
            JSONArray projects = restInterfaceManager.getProjectList(account_id);
            if (projects == null||projects.isEmpty()) {
                continue;
            }
            for (int i=0;i<projects.size();i++) {
                JSONObject project=projects.getJSONObject(i);
                if(project!=null){
                    String repo_id=project.getString("repo_id");
                    String category=project.getString("type");
                    String dashboardKey = "dashboard:"+category+":"+duration+":"+ repo_id;
                    String newIssueInfoKey="dashboard:"+category+":"+duration+":new:"+ repo_id;
                    String eliminatedInfoKey="dashboard:"+category+":"+duration+":eliminated:"+ repo_id;
                    stringRedisTemplate.delete(newIssueInfoKey);
                    stringRedisTemplate.delete(eliminatedInfoKey);
                    Boolean hasKey=stringRedisTemplate.hasKey(dashboardKey);
                    if(hasKey!=null&&hasKey){
                        //new和eliminated清零
                        initialNewAndEliminated(dashboardKey);
                    }
                }
            }
        }
    }

    public void updateIssueCount(String time) {
        if (time.equals("day")) {
            perDay();
        }else if (time.equals("week")){
            perWeek();
        }else {
            perMonth();
        }
    }




}
