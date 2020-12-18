package cn.edu.fudan.measureservice.dao;

import cn.edu.fudan.measureservice.component.RestInterfaceManager;
import cn.edu.fudan.measureservice.domain.dto.Query;
import cn.edu.fudan.measureservice.mapper.JiraMapper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wjzho
 */
@Slf4j
@Repository
public class JiraDao {

    /**
     * JiraDao层：jira数据处理层
     */
    private RestInterfaceManager restInterface;

    private JiraMapper jiraMapper;

    /**
     * 开发者的 jiraCommitNum，completedJiraNum，developerJiraBugCount，developerJiraFeatureCount 数据获取处理
     * @param query 查询条件
     * @return key : jiraCommitNum,completedJiraNum,developerJiraBugCount,developerJiraFeatureCount
     */
    public Map<String,Object> getJiraMsgOfOneDeveloper(Query query) {
        JSONArray jiraMsg = restInterface.getJiraMsgOfOneDeveloper(query.getDeveloper(),query.getRepoUuidList().get(0));
        int jiraCommitNum = 0;
        int completedJiraNum = 0;
        int developerJiraBugCount= 0;
        int developerJiraFeatureCount =0;
        if(jiraMsg!=null) {
            jiraCommitNum = jiraMsg.getJSONObject(0).getIntValue("commitNum");
            completedJiraNum = jiraMsg.getJSONObject(0).getIntValue("completedJiraNum");
            developerJiraBugCount = jiraMsg.getJSONObject(4).getIntValue("completedBugNum");
            developerJiraFeatureCount = jiraMsg.getJSONObject(5).getIntValue("completedFeatureNum");
        }
        Map<String,Object> map = new HashMap<>(6);
        map.put("jiraCommitNum",jiraCommitNum);
        map.put("completedJiraNum",completedJiraNum);
        map.put("developerJiraBugCount",developerJiraBugCount);
        map.put("developerJiraFeatureCount",developerJiraFeatureCount);
        return map;
    }

    /**
     * 开发者的 developerAssignedJiraCount，totalAssignedJiraCount 的数据获取处理
     * @param query 查询条件
     * @return key : developerAssignedJiraCount , totalAssignedJiraCount
     */
    public Map<String,Object> getAssignedJiraRate(Query query) {
        JSONObject assignedJiraData = restInterface.getAssignedJiraRate(query.getDeveloper(),query.getRepoUuidList().get(0),query.getSince(),query.getUntil());
        int developerAssignedJiraCount = 0;
        int totalAssignedJiraCount = 0;
        if (assignedJiraData!=null) {
            developerAssignedJiraCount = assignedJiraData.getIntValue("individual_assigned_jira_num");
            totalAssignedJiraCount = assignedJiraData.getIntValue("team_assigned_jira_num");
        }
        Map<String,Object> map = new HashMap<>(4);
        map.put("developerAssignedJiraCount",developerAssignedJiraCount);
        map.put("totalAssignedJiraCount",totalAssignedJiraCount);
        return map;
    }

    /**
     * 开发者的developerSolvedJiraCount，totalSolvedJiraCount 的数据获取处理
     * @param query 查询条件
     * @return key : developerSolvedJiraCount,totalSolvedJiraCount
     */
    public Map<String,Object> getSolvedAssignedJiraRate(Query query) {
        JSONObject solvedAssignedJiraData = restInterface.getSolvedAssignedJiraRate(query.getDeveloper(),query.getRepoUuidList().get(0),query.getSince(),query.getUntil());
        int developerSolvedJiraCount = 0;
        int totalSolvedJiraCount = 0;
        if (solvedAssignedJiraData!=null) {
            developerSolvedJiraCount = solvedAssignedJiraData.getIntValue("individual_solved_assigned_jira_num");
            totalSolvedJiraCount = solvedAssignedJiraData.getIntValue("team_solved_assigned_jira_num");
        }
        Map<String,Object> map = new HashMap<>();
        map.put("developerSolvedJiraCount",developerSolvedJiraCount);
        map.put("totalSolvedJiraCount",totalSolvedJiraCount);
        return map;
    }

    /**
     * 开发者的developerJiraBugCount，totalJiraBugCount的数据获取处理
     * @param query 查询条件
     * @return key: developerJiraBugCount,totalJiraBugCount
     */
    public Map<String,Object> getDefectRate(Query query) {
        JSONObject jiraBugData = restInterface.getDefectRate(query.getDeveloper(),query.getRepoUuidList().get(0),query.getSince(),query.getUntil());
        int developerJiraBugCount = 0;
        int totalJiraBugCount = 0;
        if (jiraBugData!=null){
            developerJiraBugCount = jiraBugData.getIntValue("individual_bugs");
            totalJiraBugCount = jiraBugData.getIntValue("team_bugs");
        }
        Map<String,Object> map = new HashMap<>();
        map.put("developerJiraBugCount",developerJiraBugCount);
        map.put("totalJiraBugCount",totalJiraBugCount);
        return map;
    }


    public int getDeveloperJiraCommitCount(Query query) {
        return jiraMapper.getJiraCountByCondition(query.getRepoUuidList(),query.getSince(),query.getUntil(),query.getDeveloper());
    }

    /**
     * 根据commit message 返回 对应的 jira 单号
     * @param commitMsg commit查询信息
     * @return {jira ID} or "noJiraID"
     */
    public String getJiraIDFromCommitMsg(String commitMsg){
        // 使用Pattern类的compile方法，传入jira单号的正则表达式，得到一个Pattern对象
        Pattern pattern = Pattern.compile("[A-Z][A-Z0-9]*-[0-9]+");
        // 调用pattern对象的matcher方法，传入需要匹配的字符串， 得到一个匹配器对象
        Matcher matcher = pattern.matcher(commitMsg);

        // 从字符串开头，返回匹配到的第一个字符串
        if (matcher.find()) {
            // 输出第一次匹配的内容
            log.info("jira ID is : {}",matcher.group());
            return matcher.group();
        }
        return "noJiraID" ;
    }



    @Autowired
    public void setRestInterface(RestInterfaceManager restInterface){this.restInterface=restInterface;}

    @Autowired
    public void setJiraMapper(JiraMapper jiraMapper) {this.jiraMapper=jiraMapper;}

}
