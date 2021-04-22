package cn.edu.fudan.measureservice.dao;

import cn.edu.fudan.measureservice.component.RestInterfaceManager;
import cn.edu.fudan.measureservice.domain.dto.Query;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
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


    /**
     * 获取jira相关信息
     * @param query 查询条件
     * @return Map<String,Object>
     * key : developerCompletedJiraNum, developerJiraCommitNum, developerJiraBugCount, developerCompletedJiraBugCount, developerCompletedJiraFeatureCount,
     *       totalJiraBugCount, developerAssignedJiraCount, totalAssignedJiraCount, developerSolvedJiraCount, totalSolvedJiraCount
     */
    public Map<String,Object> getJiraMsgInfo(Query query) {
        Map<String,Object> map = new HashMap<>();
        JSONObject jiraResponse = restInterface.getJiraMsgOfOneDeveloper(query.getDeveloper(),query.getRepoUuidList().get(0),query.getSince(),query.getUntil());
        int developerCompletedJiraNum = 0;
        int developerJiraCommitNum = 0;
        int developerJiraBugCount = 0;
        int developerCompletedJiraBugCount = 0;
        int developerJiraFeatureCount = 0;
        int developerCompletedJiraFeatureCount = 0;
        int totalJiraBugCount = 0;
        int developerAssignedJiraCount = 0;
        int totalAssignedJiraCount = 0;
        int developerSolvedJiraCount = 0;
        int totalSolvedJiraCount = 0;
        if (jiraResponse != null){
            JSONObject commitPerJira = jiraResponse.getJSONObject("commitPerJira");
            developerCompletedJiraNum = commitPerJira.getIntValue("finishedJiraSum");
            developerJiraCommitNum = commitPerJira.getIntValue("commitSum");

            JSONObject differentTypeSum = jiraResponse.getJSONObject("differentTypeSum");
            developerCompletedJiraBugCount = differentTypeSum.getIntValue("completedBugSum");
            developerCompletedJiraFeatureCount = differentTypeSum.getIntValue("completedTaskSum");
            developerJiraFeatureCount = differentTypeSum.getIntValue("totalTaskSum");

            JSONObject defectRate = jiraResponse.getJSONObject("defectRate");
            developerJiraBugCount = defectRate.getIntValue("individualBugSum");
            totalJiraBugCount = defectRate.getIntValue("teamBugSum");

            JSONObject assignedJiraRate = jiraResponse.getJSONObject("assignedJiraRate");
            developerAssignedJiraCount = assignedJiraRate.getIntValue("individualJiraSum");
            totalAssignedJiraCount = assignedJiraRate.getIntValue("teamJiraSum");
            developerSolvedJiraCount = assignedJiraRate.getIntValue("solvedIndividualJiraSum");
            totalSolvedJiraCount = assignedJiraRate.getIntValue("solvedTeamJiraSum");
        }
        map.put("developerCompletedJiraNum",developerCompletedJiraNum);
        map.put("developerJiraCommitNum",developerJiraCommitNum);
        map.put("developerCompletedJiraBugCount",developerCompletedJiraBugCount);
        map.put("developerJiraFeatureCount",developerJiraFeatureCount);
        map.put("developerCompletedJiraFeatureCount",developerCompletedJiraFeatureCount);
        map.put("developerJiraBugCount",developerJiraBugCount);
        map.put("totalJiraBugCount",totalJiraBugCount);
        map.put("developerAssignedJiraCount",developerAssignedJiraCount);
        map.put("totalAssignedJiraCount",totalAssignedJiraCount);
        map.put("developerSolvedJiraCount",developerSolvedJiraCount);
        map.put("totalSolvedJiraCount",totalSolvedJiraCount);
        return map;
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
            return matcher.group();
        }
        return "noJiraID" ;
    }



    @Autowired
    public void setRestInterface(RestInterfaceManager restInterface){this.restInterface=restInterface;}

}
