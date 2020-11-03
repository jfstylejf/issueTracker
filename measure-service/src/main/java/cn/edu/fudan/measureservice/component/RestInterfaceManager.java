package cn.edu.fudan.measureservice.component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class RestInterfaceManager {

    @Value("${account.service.path}")
    private String accountServicePath;
    @Value("${code.service.path}")
    private String codeServicePath;
    @Value("${repository.service.path}")
    private String repoServicePath;
    @Value("${commit.service.path}")
    private String commitServicePath;
    @Value("${project.service.path}")
    private String projectServicePath;
    @Value("${issue.service.path}")
    private String issueServicePath;
    @Value("${uniform.service.path}")
    private String uniformServicePath;



    private RestTemplate restTemplate;

    public RestInterfaceManager(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private Logger logger = LoggerFactory.getLogger(RestInterfaceManager.class);


    //----------------------------------account service----------------------------------------------------

    public String getAccountId(String userToken){
        Map<String,String> urlParameters=new HashMap<>();
        urlParameters.put("userToken",userToken);
        return restTemplate.getForObject(accountServicePath+"/user/accountId?userToken={userToken}",String.class,urlParameters);
    }

    //----------------------------------commit service----------------------------------------------------

    public JSONObject getFirstCommitDate(String developerName){
        return restTemplate.getForObject(commitServicePath+"/first-commit?author="+ developerName,JSONObject.class).getJSONObject("data");
    }

    //------------project-service--------------------------------------------------------------------------------------

    @CachePut("projects")
    public JSONObject getProjectByRepoId(String repoUuid,String token){
        HttpHeaders headers = new HttpHeaders();
        headers.add("token",token);
        HttpEntity request = new HttpEntity(headers);
        StringBuilder url = new StringBuilder();
        url.append(projectServicePath).append("/inner/project?repo_uuid=").append(repoUuid);
        ResponseEntity responseEntity =restTemplate.exchange(url.toString(),HttpMethod.GET,request,JSONObject.class);
        String body = responseEntity.getBody().toString();
        JSONObject result = JSONObject.parseObject(body);
        return result;
    }

    //---------------------------------------------code service---------------------------------------------------------
    public String getRepoPath(String repoId,String commit_id){
        String repoPath=null;
        StringBuilder url = new StringBuilder();
        url.append(codeServicePath).append("?repo_id=").append(repoId);
        if (commit_id != null){
            url.append("&commit_id=").append(commit_id);
        }
        try{
            JSONObject response=restTemplate.getForObject(url.toString(), JSONObject.class).getJSONObject("data");
            if (response != null ){
                if(response.getString("status").equals("Successful")) {
                    repoPath = response.getString("content");
                logger.info("repoHome -> {} , repoId -->{} , commit_id -->{}" ,repoPath,repoId,commit_id);
                }else{
                    logger.error("get repoHome fail -> {}",response.getString("content"));
                    logger.error("repoId -> {} commitId -> {}",repoId,commit_id);
                }
            } else {
                logger.error("code service response null!");
            }
        } catch (RestClientException e) {
            logger.error("Get exception when getting repoPath");
            e.printStackTrace();
        }
        return repoPath;
    }

    public JSONObject freeRepoPath(String repoId,String repoPath){
        JSONObject response=restTemplate.getForObject(codeServicePath + "/free?repo_id=" + repoId+"&path="+repoPath, JSONObject.class);
        if(response!=null&&response.getJSONObject("data").getString("status").equals("Successful")){
            logger.info("{} -> free success",repoPath);
        }else {
            logger.warn("{} -> free failed",repoPath);
        }
        return response;
    }


    //---------------------------------------------repo service---------------------------------------------------------
    public JSONObject getRepoById(String repoId){
        return restTemplate.getForObject(repoServicePath + "/" + repoId, JSONObject.class);
    }

    public int getRepoAge(String repoId, String endDate){
        JSONObject response = restTemplate.getForObject(repoServicePath + "/repository_year" + "?repo_id=" + repoId + "&end_date=" + endDate, JSONObject.class);
        if (response.containsKey("data")){
            return response.getJSONObject("data").getIntValue("commit_time");
        }
        return -1;
    }




    //---------------------------------------------issue service---------------------------------------------------------

    public int getNumberOfNewIssueByCommit(String repoId,String commit,String category,String spaceType,String token){
        HttpHeaders headers = new HttpHeaders();
        headers.add("token",token);
        HttpEntity request = new HttpEntity(headers);

        ResponseEntity responseEntity = restTemplate.exchange(issueServicePath  + "/measurement/newIssue/"+ repoId + "/" + commit + "?spaceType=" + spaceType + "&category=" + category ,HttpMethod.GET,request,Object.class);
        int result = Integer.valueOf(responseEntity.getBody().toString());
        return result;
    }

    public int getNumberOfEliminateIssueByCommit(String repoId,String commit,String category,String spaceType,String token){
        HttpHeaders headers = new HttpHeaders();
        headers.add("token",token);
        HttpEntity request = new HttpEntity(headers);

        ResponseEntity responseEntity = restTemplate.exchange(issueServicePath  + "/measurement/eliminateIssue/"+ repoId + "/" + commit + "?spaceType=" + spaceType + "&category=" + category ,HttpMethod.GET,request,Object.class);
        int result = Integer.valueOf(responseEntity.getBody().toString());
        return result;
    }

    public int getNumberOfRemainingIssue(String repoId,String commit,String spaceType ,String detail,String token){
        HttpHeaders headers = new HttpHeaders();
        headers.add("token",token);
        HttpEntity request = new HttpEntity(headers);

        ResponseEntity responseEntity = restTemplate.exchange(issueServicePath  + "/measurement/remainingIssue/"+ repoId + "/" + commit + "?spaceType=" + spaceType ,HttpMethod.GET,request,Object.class);
        int result = Integer.valueOf(responseEntity.getBody().toString());
        return result;
    }

    public int getIssueCountByConditions(String developer, String repoId, String since, String until, String tool, String general_category, String token){
        HttpHeaders headers = new HttpHeaders();
        headers.add("token",token);
        HttpEntity request = new HttpEntity(headers);
        StringBuilder url = new StringBuilder();
        url.append(issueServicePath).append("/measurement/issueCount").append("?repoId=").append(repoId).append("&since=").append(since).append("&until=").append(until).append("&tool=").append(tool);
        if (developer != null && !developer.equals("")){
            url.append("&developer=").append(developer);
        }
        if (general_category != null && !general_category.equals("")){
            url.append("&general_category=").append(general_category);
        }
        ResponseEntity responseEntity = restTemplate.exchange(url.toString(), HttpMethod.GET, request, JSONObject.class);
        String body = responseEntity.getBody().toString();
        JSONObject result = JSONObject.parseObject(body);
        if(result.getIntValue("code") == 200){
            return result.getIntValue("data");
        }
        return 0;
    }


    public JSONArray getNewElmIssueCount(String repoId, String since, String until, String tool, String token){
        HttpHeaders headers = new HttpHeaders();
        headers.add("token",token);
        HttpEntity request = new HttpEntity(headers);
        StringBuilder url = new StringBuilder();
        url.append(issueServicePath).append("/measurement/issue/developer").append("?repo_id=").append(repoId).append("&since=").append(since).append("&until=").append(until).append("&category=").append(tool);
        ResponseEntity responseEntity = restTemplate.exchange(url.toString(), HttpMethod.GET, request, JSONObject.class);
        String body = responseEntity.getBody().toString();
        JSONObject result = JSONObject.parseObject(body);
        return result.getJSONArray("data");
    }

    public JSONObject getDayAvgSolvedIssue(String developer, String repoId, String since, String until, String tool, String token){

        HttpHeaders headers = new HttpHeaders();
        headers.add("token",token);
        HttpEntity request = new HttpEntity(headers);
        StringBuilder url = new StringBuilder();
        url.append(uniformServicePath).append("/measurement/developer/dayAvgSolvedIssue?developer=").append(developer);
        if (repoId != null && repoId.length()>0){
            url.append("&repoId=").append(repoId);
        }
        if (since != null && since.length()>0){
            url.append("&since=").append(since);
        }
        if (until != null && until.length()>0){
            url.append("&until=").append(until);
        }
        if (tool != null && tool.length()>0){
            url.append("&tool=").append(tool);
        }
        ResponseEntity responseEntity = restTemplate.exchange(url.toString(), HttpMethod.GET, request, JSONObject.class);
        String body = responseEntity.getBody().toString();
        JSONObject result = JSONObject.parseObject(body);

        if(result.getIntValue("code") == 200){
            return result.getJSONObject("data");
        }
        return null;
    }

    //--------------------------------code-tracker service------------------------------------------------

    public JSONObject getStatements(String repoUuid, String beginDate, String endDate, String developer){
        StringBuilder url = new StringBuilder();
        url.append(uniformServicePath).append("/codewisdom/code/line-count?repo_uuids=").append(repoUuid);
        if(beginDate!=null) {
            url.append("&since=").append(beginDate);
        }
        if(endDate!=null) {
            url.append("&until=").append(endDate);
        }
        if(developer!=null && !"".equals(developer)) {
            url.append("&developer=").append(developer);
        }
        JSONObject response = restTemplate.getForObject(url.toString(), JSONObject.class);
        if (response == null || response.getIntValue("code") != 200){
            return null;
        }
        return response.getJSONObject("data");
    }

    public JSONObject getCodeLifeCycle(String repoUuid,String developer,String since,String until) {
        StringBuilder url = new StringBuilder();
        url.append(uniformServicePath).append("/codewisdom/code/lifecycle?developer=").append(developer);
        if(repoUuid!=null && !"".equals(repoUuid)) {
            url.append("&repo_uuids=").append(repoUuid);
        }
        if(since!=null && !"".equals(since)) {
            url.append("&since=").append(since);
        }
        if (until!=null && !"".equals(until)) {
            url.append("&until").append(until);
        }
        JSONObject response = restTemplate.getForObject(url.toString(),JSONObject.class);
        if(response!=null && response.getIntValue("code")==200) {
            return response.getJSONObject("data");
        }else {
            return null;
        }
    }

    public JSONObject getFocusFilesCount(String repoUuid, String developer,String since, String until){
        StringBuilder url = new StringBuilder();
        url.append(uniformServicePath).append("/statistics/focus/file/num?developer=").append(developer);
        if(repoUuid!=null && !"".equals(repoUuid)) {
            url.append("&repo_uuid").append(repoUuid);
        }
        if (since!=null && !"".equals(since)) {
            url.append("&since=").append(since);
        }
        if (until!=null && !"".equals(until)) {
            url.append("&until").append(until);
        }
        JSONObject response = restTemplate.getForObject(url.toString(),JSONObject.class);
        if(response!=null && response.get("data")!=null) {
            return response.getJSONObject("data");
        }else {
            return  null;
        }
    }


    //-------------------------------------------clone service---------------------------------------
    public JSONObject getCloneMeasure(String repo_id, String developer, String start, String end){
        return restTemplate.getForObject(uniformServicePath+"/cloneMeasure/getMeasureClone"  + "?repo_id=" + repo_id + "&developer=" + developer + "&start=" + start + "&end=" + end, JSONObject.class).getJSONObject("data");
    }

    //-------------------------------------------jira API-------------------------------------------
    @Cacheable(cacheNames = {"jiraInfoByJiraID"})
    public JSONArray getJiraInfoByKey(String type, String keyword){
        JSONObject response = restTemplate.getForObject(uniformServicePath+"/jira/jql"  + "?keyword=" + keyword +  "&type=" + type, JSONObject.class);
        if(response.getIntValue("code") == 200){
            return response.getJSONArray("data");
        }
        return null;
    }

    public JSONArray getJiraMsgOfOneDeveloper(String developer, String repoId){
        StringBuilder url = new StringBuilder();
        url.append(uniformServicePath).append("/jira/developer_msg?developer=").append(developer);
        if (repoId != null && repoId.length()>0){
            url.append("&repo-id=").append(repoId);
        }
        JSONObject response = restTemplate.getForObject(url.toString(), JSONObject.class);
        if(response.getIntValue("code") == 200){
            return response.getJSONArray("data");
        }
        return null;
    }

    public JSONObject getSolvedAssignedJiraRate(String developer, String repoId, String since, String until){
        StringBuilder url = new StringBuilder();
        url.append(uniformServicePath).append("/jira/getSolvedAssignedJiraRate?developer=").append(developer);
        if (repoId != null && repoId.length()>0){
            url.append("&repo_id=").append(repoId);
        }
        if (since != null && since.length()>0){
            url.append("&begin_date=").append(since);
        }
        if (until != null && until.length()>0){
            url.append("&end_date=").append(until);
        }

        JSONObject response = restTemplate.getForObject(url.toString(), JSONObject.class);
        if(response.getIntValue("code") == 200){
            return response.getJSONObject("data");
        }
        return null;
    }

    public JSONObject getAssignedJiraRate(String developer, String repoId, String since, String until){
        StringBuilder url = new StringBuilder();
        url.append(uniformServicePath).append("/jira/getAssignedJiraRate?developer=").append(developer);
        if (repoId != null && repoId.length()>0){
            url.append("&repo_id=").append(repoId);
        }
        if (since != null && since.length()>0){
            url.append("&begin_date=").append(since);
        }
        if (until != null && until.length()>0){
            url.append("&end_date=").append(until);
        }

        JSONObject response = restTemplate.getForObject(url.toString(), JSONObject.class);
        if(response.getIntValue("code") == 200){
            return response.getJSONObject("data");
        }
        return null;
    }

    public JSONObject getDefectRate(String developer, String repoId, String since, String until){
        StringBuilder url = new StringBuilder();
        url.append(uniformServicePath).append("/jira/getDefectRate?developer=").append(developer);
        if (repoId != null && repoId.length()>0){
            url.append("&repo_id=").append(repoId);
        }
        if (since != null && since.length()>0){
            url.append("&begin_date=").append(since);
        }
        if (until != null && until.length()>0){
            url.append("&end_date=").append(until);
        }

        JSONObject response = restTemplate.getForObject(url.toString(), JSONObject.class);
        if(response.getIntValue("code") == 200){
            return response.getJSONObject("data");
        }
        return null;
    }







}

