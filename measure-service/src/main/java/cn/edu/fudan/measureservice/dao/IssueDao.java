package cn.edu.fudan.measureservice.dao;

import cn.edu.fudan.measureservice.component.RestInterfaceManager;
import cn.edu.fudan.measureservice.domain.dto.Query;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wjzho
 */
@Slf4j
@Repository
public class IssueDao {

    private static final String STANDARD = "Code Smell";
    private static final String SECURITY = "Bug";
    private static final String TOOL = "sonarqube";

    private RestInterfaceManager restInterface;

    /**
     * 获取开发者解决的issue数
     * @param query 查询条件
     * @return int solvedSonarIssue
     */
    public int getSolvedSonarIssue (Query query) {
        JSONObject sonarResponse = restInterface.getDayAvgSolvedIssue(query.getDeveloper(),query.getRepoUuidList().get(0),query.getSince(),query.getUntil(),TOOL,query.getToken());
        int solvedSonarIssue = 0;
        if (sonarResponse != null){
            solvedSonarIssue = sonarResponse.getIntValue("solvedIssuesCount");
        }
        return solvedSonarIssue;
    }

    /**
     * 获取开发者各类问题数
     * @Param query 查询条件
     * @return Map<String,Object> key : developerStandardIssueCount, totalStandardIssueCount, developerIssueCount, totalIssueCount, developerSecurityIssueCount
     */
    public Map<String,Object> getIssueCountByConditions(Query query) {
        Map<String,Object> map = new HashMap<>();
        int developerStandardIssueCount = 0;
        int totalStandardIssueCount = 0;
        int developerIssueCount = 0;
        int totalIssueCount = 0;
        int developerSecurityIssueCount = 0;
        try {
            developerStandardIssueCount = restInterface.getIssueCountByConditions(query.getDeveloper(),query.getRepoUuidList().get(0),query.getSince(),query.getUntil(),TOOL,STANDARD,query.getToken());
            totalStandardIssueCount = restInterface.getIssueCountByConditions(null, query.getRepoUuidList().get(0),query.getSince(),query.getUntil(),TOOL,STANDARD,query.getToken());
            developerIssueCount = restInterface.getIssueCountByConditions(query.getDeveloper(),query.getRepoUuidList().get(0),query.getSince(),query.getUntil(),TOOL,null,query.getToken());
            totalIssueCount = restInterface.getIssueCountByConditions(null, query.getRepoUuidList().get(0),query.getSince(),query.getUntil(),TOOL,null,query.getToken());
            developerSecurityIssueCount = restInterface.getIssueCountByConditions(query.getDeveloper(),query.getRepoUuidList().get(0),query.getSince(),query.getUntil(),TOOL,SECURITY,query.getToken());
        }catch (Exception e) {
            log.error("Rest Issue Service failed");
            e.printStackTrace();
        }
        map.put("developerStandardIssueCount",developerStandardIssueCount);
        map.put("totalStandardIssueCount",totalStandardIssueCount);
        map.put("developerIssueCount",developerIssueCount);
        map.put("totalIssueCount",totalIssueCount);
        map.put("developerSecurityIssueCount",developerSecurityIssueCount);
        return map;
    }


    @Autowired
    private void setRestInterface(RestInterfaceManager restInterface) {
        this.restInterface = restInterface;
    }

}
