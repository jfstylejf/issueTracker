package cn.edu.fudan.projectmanager.component;

import cn.edu.fudan.projectmanager.exception.AuthException;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RestInterfaceManager {

    @Value("${account.service.path}")
    private String accountServicePath;
    @Value("${issue.service.path}")
    private String issueServicePath;
    @Value("${scan.service.path}")
    private String scanServicePath;
    @Value("${event.service.path}")
    private String eventServicePath;
    @Value("${repository.service.path}")
    private String repoServicePath;
    @Value("${tag.service.path}")
    private String tagServicePath;
    @Value("${measure.service.path}")
    private String measureServicePath;
    @Value("${bug.recommendation.service.path}")
    private String bugRecommendationServicePath;
    @Value("${codeTracker.service.path}")
    private String codeTrackerServicePath;
    @Value("${commit.service.path}")
    private String commitServicePath;
    @Value("${clone.service.path}")
    private String cloneServicePath;

    private RestTemplate restTemplate;

    public RestInterfaceManager(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    //----------------------------------account service----------------------------------------------------
    public void deleteCloneMeasure(String repoId){
        restTemplate.delete(cloneServicePath + "/cloneMeasure/" + repoId);
    }

    //----------------------------------account service----------------------------------------------------
    public String getAccountId(String userToken){
        Map<String,String> urlParameters=new HashMap<>();
        urlParameters.put("userToken",userToken);
        return restTemplate.getForObject(accountServicePath+"/user/accountId?userToken={userToken}",String.class,urlParameters);
    }

    public String getAccountName(String accountId){
        return restTemplate.getForObject(accountServicePath+"/user/accountName?accountId="+accountId,String.class);
    }

    public boolean userAuth(String userToken)throws AuthException {
        JSONObject result = restTemplate.getForObject(accountServicePath + "/user/auth/" + userToken, JSONObject.class);
        if (result == null || result.getIntValue("code") != 200) {
            log.error("auth failed!");
            return false;
        }
        return true;
    }

    //--------------------------------issue service----------------------------------------------------------

    public void deleteIssuesOfRepo(String repoId,String category){
        restTemplate.delete(issueServicePath + "/inner/issue/" +category+"/"+ repoId);
    }

    public void deleteRawIssueOfRepo(String repoId,String category){
        restTemplate.delete(issueServicePath + "/inner/raw-issue/" +category+"/"+ repoId);
    }

    public void deleteScanResultOfRepo(String repoId,String category){
        restTemplate.delete(issueServicePath + "/inner/issue/scan-results/" +category+"/"+ repoId);
    }

    //------------------------------------bug-recommendation service-------------------------------------------

    //-------------------------------scan service--------------------------------------------------------------
    public void deleteScanOfRepo(String repoId,String category){
        restTemplate.delete(scanServicePath+"/inner/scan/" +category+"/"+ repoId);
    }

    //-----------------------------event service---------------------------------------------------------------
    public void deleteEventOfRepo(String repoId,String category){
        restTemplate.delete(eventServicePath+"/inner/event/" +category+"/"+ repoId);
    }

    public void deleteRepoMeasure(String repoId){
        restTemplate.delete(measureServicePath+"/measure/repo-information?repo_id=" + repoId);
    }

    //-----------------------------------repo service--------------------------------------------------------
    public JSONObject getRepoById(String repoId){
        return restTemplate.getForObject(repoServicePath + "/" + repoId, JSONObject.class);
    }

    public void deleteIgnoreRecord(String account_id, String repoId) {
        restTemplate.delete(tagServicePath + "/inner/tags/ignore?repo-id=" + repoId + "&account-id=" + account_id);
    }

    public void deleteCodeTrackerOfRepo(String branch, String repoId) {
        restTemplate.delete(codeTrackerServicePath + "/codetracker?repoUuid=" + repoId + "&branch=" + branch,JSONObject.class);
    }

    public JSONObject getLatestCommitTime(String repoId) {
        return restTemplate.getForObject(commitServicePath + "/latest–commit–time?repo_id=" + repoId,JSONObject.class);
    }
}
