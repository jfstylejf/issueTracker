package cn.edu.fudan.issueservice.service.impl;

import cn.edu.fudan.issueservice.component.IssueEventManager;
import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.component.TagMapHelper;
import cn.edu.fudan.issueservice.dao.IssueDao;
import cn.edu.fudan.issueservice.dao.RawIssueDao;
import cn.edu.fudan.issueservice.dao.ScanResultDao;
import cn.edu.fudan.issueservice.domain.EventType;
import cn.edu.fudan.issueservice.domain.Issue;
import cn.edu.fudan.issueservice.domain.RawIssue;
import cn.edu.fudan.issueservice.domain.RawIssueDetail;
import cn.edu.fudan.issueservice.service.MappingService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author WZY
 * @version 1.0
 **/
@Slf4j
@Service
public class BaseMappingServiceImpl implements MappingService {
    private Logger logger = LoggerFactory.getLogger(BaseMappingServiceImpl.class);

    @Value("${solved.tag_id}")
    private String solvedTagId;
    @Value("${ignore.tag_id}")
    String ignoreTagId;

    int currentDisplayId = 1;
    volatile boolean  isDefaultDisplayId = true;
    IssueEventManager issueEventManager;
    IssueDao issueDao;
    RawIssueDao rawIssueDao;
    ScanResultDao scanResultDao;
    RestInterfaceManager restInterfaceManager;

    private StringRedisTemplate stringRedisTemplate;
    private TagMapHelper tagMapHelper;
    private KafkaTemplate kafkaTemplate;

    @Autowired
    public void setKafkaTemplate(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Autowired
    public void setTagMapHelper(TagMapHelper tagMapHelper) {
        this.tagMapHelper = tagMapHelper;
    }

    @Autowired
    public void setScanResultDao(ScanResultDao scanResultDao) {
        this.scanResultDao = scanResultDao;
    }

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    @Autowired
    public void setIssueEventManager(IssueEventManager issueEventManager) {
        this.issueEventManager = issueEventManager;
    }

    @Autowired
    public void setIssueDao(IssueDao issueDao) {
        this.issueDao = issueDao;
    }

    @Autowired
    public void setRawIssueDao(RawIssueDao rawIssueDao) {
        this.rawIssueDao = rawIssueDao;
    }

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    void newIssueInfoUpdate(List<Issue> issueList,String category,String repo_id){
        String todayNewIssueKey="dashboard:"+category+":day:new:" + repo_id;
        String weekNewIssueKey="dashboard:"+category+":week:new:" + repo_id;
        String monthNewIssueKey="dashboard:"+category+":month:new:"+repo_id;
        stringRedisTemplate.setEnableTransactionSupport(true);
        stringRedisTemplate.multi();
        for(Issue issue:issueList){
            stringRedisTemplate.opsForList().rightPush(todayNewIssueKey,issue.getUuid());
            stringRedisTemplate.opsForList().rightPush(weekNewIssueKey,issue.getUuid());
            stringRedisTemplate.opsForList().rightPush(monthNewIssueKey,issue.getUuid());
        }
        stringRedisTemplate.exec();
    }

   private void eliminatedInfoUpdate(List<Issue> issueList,String category,String repo_id){
        String todayEliminatedIssueKey="dashboard:"+category+":day:eliminated:" + repo_id;
        String weekEliminatedIssueKey="dashboard:"+category+":week:eliminated:" + repo_id;
        String monthEliminatedIssueKey="dashboard:"+category+":month:eliminated:"+repo_id;
        stringRedisTemplate.setEnableTransactionSupport(true);
        stringRedisTemplate.multi();
        for(Issue issue:issueList){
            stringRedisTemplate.opsForList().rightPush(todayEliminatedIssueKey,issue.getUuid());
            stringRedisTemplate.opsForList().rightPush(weekEliminatedIssueKey,issue.getUuid());
            stringRedisTemplate.opsForList().rightPush(monthEliminatedIssueKey,issue.getUuid());
        }
        stringRedisTemplate.exec();
    }


    void dashboardUpdate(String repo_id, int newIssueCount, int remainingIssueCount, int eliminatedIssueCount, String category) {
        //注意只有remaining是覆盖的，其余是累增的
        String todayKey = "dashboard:"+category+":day:" + repo_id;
        String weekKey = "dashboard:"+category+":week:" + repo_id;
        String monthKey = "dashboard:"+category+":month:" + repo_id;
        stringRedisTemplate.setEnableTransactionSupport(true);
        stringRedisTemplate.multi();
        stringRedisTemplate.opsForHash().increment(todayKey, "new", newIssueCount);
        stringRedisTemplate.opsForHash().put(todayKey, "remaining", String.valueOf(remainingIssueCount));
        stringRedisTemplate.opsForHash().increment(todayKey, "eliminated", eliminatedIssueCount);
        stringRedisTemplate.opsForHash().increment(weekKey, "new", newIssueCount);
        stringRedisTemplate.opsForHash().put(weekKey, "remaining", String.valueOf(remainingIssueCount));
        stringRedisTemplate.opsForHash().increment(weekKey, "eliminated", eliminatedIssueCount);
        stringRedisTemplate.opsForHash().increment(monthKey, "new", newIssueCount);
        stringRedisTemplate.opsForHash().put(monthKey, "remaining", String.valueOf(remainingIssueCount));
        stringRedisTemplate.opsForHash().increment(monthKey, "eliminated", eliminatedIssueCount);
        stringRedisTemplate.exec();
    }

    Date getCommitDate(String commitId){
        JSONObject response=restInterfaceManager.getCommitTime(commitId);
        if(response!=null){
            return response.getJSONObject("data").getDate("commit_time");
        }
        return null;
    }

    String getDeveloper(String commitId){
        JSONObject response=restInterfaceManager.getOneCommitByCommitId(commitId);
        if(response!=null){
            return response.getJSONObject("data").getString("developer");
        }
        return null;
    }

    @Override
    public void mapping(String repo_id, String pre_commit_id, String current_commit_id, String category,String committer) {
        throw new UnsupportedOperationException();
    }

    int addTag(List<JSONObject> tags, JSONArray ignoreTypes, RawIssue rawIssue, Issue issue){
        int result=0;
        String tagID;
        if(ignoreTypes!=null&&!ignoreTypes.isEmpty()&&ignoreTypes.contains(rawIssue.getType())){
            //如果新增的issue的类型包含在ignore的类型之中，打ignore的tag
            tagID=ignoreTagId;
            issue.setPriority(5);
            result=1;
        }else{
            RawIssueDetail rawIssueDetail= JSONObject.parseObject(rawIssue.getDetail(),RawIssueDetail.class);
            tagID=tagMapHelper.getTagIdByRank(Integer.parseInt(rawIssueDetail.getRank()));
        }
        if(tagID!=null){
            JSONObject tagged = new JSONObject();
            tagged.put("item_id", issue.getUuid());
            tagged.put("tag_id", tagID);
            tags.add(tagged);
        }
        return result;
    }

    void modifyToSolvedTag(String repo_id,String category, String pre_commit_id,EventType eventType,String committer,Date currentCommitTime) {
        //mapping完后end commit还是上一个commit说明被solved
        List<Issue> issues=issueDao.getIssuesByEndCommit(repo_id,category,pre_commit_id);
        if(issues != null) {
            issueEventManager.sendIssueEvent(eventType, issues, committer, repo_id,currentCommitTime);
            if (!issues.isEmpty()) {
                eliminatedInfoUpdate(issues, category, repo_id);
                List<JSONObject> taggeds = new ArrayList<>();
                for (Issue issue : issues) {
                    JSONObject tagged = new JSONObject();
                    tagged.put("item_id", issue.getUuid());
                    tagged.put("tag_id", solvedTagId);
                    taggeds.add(tagged);
                    issueDao.updateOneIssuePriority(issue.getUuid(),6);
                }
                restInterfaceManager.modifyTags(taggeds);
            }
        }
    }

    @SuppressWarnings("unchecked")
    void saveSolvedInfo(List<RawIssue> rawIssues,String repo_id,String pre_commit_id,String current_commit_id){
        List<JSONObject> solvedInfos=new ArrayList<>();
        for(RawIssue rawIssue:rawIssues){
            JSONObject solvedInfo=new JSONObject();
            solvedInfo.put("type",rawIssue.getType());
            solvedInfo.put("location",rawIssue.firstLocation().getFile_path());
            solvedInfo.put("bug_lines",rawIssue.firstLocation().getBug_lines());
            solvedInfo.put("class_name",rawIssue.firstLocation().getClass_name());
            solvedInfo.put("method_name",rawIssue.firstLocation().getMethod_name());
            solvedInfo.put("start_line",rawIssue.firstLocation().getStart_line());
            solvedInfo.put("end_line",rawIssue.firstLocation().getEnd_line());
            solvedInfo.put("curr_commitid",pre_commit_id);
            solvedInfo.put("next_commitid",current_commit_id);
            solvedInfo.put("repoid",repo_id);
            solvedInfos.add(solvedInfo);
        }
        logger.info("solvedBug -> {}",JSONArray.toJSONString(solvedInfos));
        if(!solvedInfos.isEmpty()){
            kafkaTemplate.send("solvedBug",JSONArray.toJSONString(solvedInfos));
        }
    }

}
