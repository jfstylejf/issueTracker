package cn.edu.fudan.measureservice.component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
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
    public JSONArray getCommitList(String repo_id){
        JSONObject response = restTemplate.getForObject(commitServicePath  + "?repo_id=" + repo_id + "&is_whole=true",JSONObject.class);
        JSONArray data = response.getJSONArray("data");
        return data;
    }

    public JSONArray getCommitsOfRepo(String repoId){
        JSONObject response=restTemplate.getForObject(commitServicePath + "?repo_id=" + repoId + "&is_whole=true", JSONObject.class);
        if(response==null||response.getJSONArray("data")==null) {
            return null;
        }
        return response.getJSONArray("data");
    }

    public JSONObject getCommitByCommitId(String commitId){
        return restTemplate.getForObject(commitServicePath+"/"+ commitId,JSONObject.class);
    }
    public JSONObject getFirstCommitDate(String developerName){
        return restTemplate.getForObject(commitServicePath+"/first-commit?author="+ developerName,JSONObject.class).getJSONObject("data");
    }

    //-----------------------------------------------project service-------------------------------------------------
    public String getRepoIdOfProject(String projectId) {
        return restTemplate.getForObject(projectServicePath + "/inner/project/repo-id?project-id=" + projectId, String.class);
    }

    public JSONObject getProjectListByCondition(String token,String category,String name,String module){

        HttpHeaders headers = new HttpHeaders();
        headers.add("token",token);
        HttpEntity request = new HttpEntity(headers);
        ResponseEntity responseEntity = restTemplate.exchange(projectServicePath  + "/project/search?category=" + category+"&name=" +name+"&module="+module,HttpMethod.GET,request,JSONObject.class);
        String body = responseEntity.getBody().toString();
        JSONObject result = JSONObject.parseObject(body);
        return result;
    }


    public JSONArray getProjectList(String account_id) {
        return restTemplate.getForObject(projectServicePath + "/inner/projects?account_id=" + account_id,JSONArray.class);
    }

    @CachePut("projects")
    public JSONArray getProjectsOfRepo(String repoId){
        JSONArray result = restTemplate.getForObject(projectServicePath + "/inner/project?repo_id=" + repoId,JSONArray.class);
        return result;
    }

    public List<JSONObject> getProjectListByCategory(String token,String category){
        HttpHeaders headers = new HttpHeaders();
        headers.add("token",token);
        HttpEntity request = new HttpEntity(headers);

        ResponseEntity responseEntity = restTemplate.exchange(projectServicePath  + "/project" + "?type=" + category ,HttpMethod.GET,request,JSONArray.class);
        String body = responseEntity.getBody().toString();
        List<JSONObject> result = JSONArray.parseArray(body,JSONObject.class);
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
        return restTemplate.getForObject(repoServicePath + "/repository_year" + "?repo_id=" + repoId + "&end_date=" + endDate, JSONObject.class).getJSONObject("data").getIntValue("commit_time");
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

    //--------------------------------code-tracker service------------------------------------------------
    public JSONObject getStatements(String repoUuid, String beginDate, String endDate, String branch){
        return restTemplate.getForObject(uniformServicePath+"/statistics/statements"  + "?repoUuid=" + repoUuid + "&beginDate=" + beginDate + "&endDate=" + endDate + "&branch=" + branch, JSONObject.class).getJSONObject("data");
    }

    public JSONObject getValidLine(String repoUuid, String beginDate, String endDate, String branch){
        return restTemplate.getForObject(uniformServicePath+"/statistics/committer/line/valid"  + "?repoUuid=" + repoUuid + "&beginDate=" + beginDate + "&endDate=" + endDate + "&branch=" + branch, JSONObject.class).getJSONObject("data");
    }

    public JSONObject getFocusFilesCount(String repoUuid, String beginDate, String endDate){
        return restTemplate.getForObject(uniformServicePath+"/statistics/focus/file/num"  + "?repoUuid=" + repoUuid + "&beginDate=" + beginDate + "&endDate=" + endDate, JSONObject.class).getJSONObject("data");
    }

    public JSONObject getChangedCodeAge(String repoUuid, String beginDate, String endDate, String developer){
        return restTemplate.getForObject(uniformServicePath+"/statistics/change/info"  + "?repoUuid=" + repoUuid + "&beginDate=" + beginDate + "&endDate=" + endDate + "&developer=" + developer, JSONObject.class).getJSONObject("data");
    }

    public JSONObject getDeletedCodeAge(String repoUuid, String beginDate, String endDate, String developer){
        return restTemplate.getForObject(uniformServicePath+"/statistics/delete/info"  + "?repoUuid=" + repoUuid + "&beginDate=" + beginDate + "&endDate=" + endDate + "&developer=" + developer, JSONObject.class).getJSONObject("data");
    }


    //-------------------------------------------clone service---------------------------------------
    public JSONObject getCloneMeasure(String repo_id, String developer, String start, String end){
        return restTemplate.getForObject(uniformServicePath+"/cloneMeasure/getMeasureClone"  + "?repo_id=" + repo_id + "&developer=" + developer + "&start=" + start + "&end=" + end, JSONObject.class).getJSONObject("data");
    }

    //-------------------------------------------jira API-------------------------------------------
    public JSONArray getJiraInfoByKey(String type, String keyword){
        JSONObject response = restTemplate.getForObject("http://127.0.0.1:8887/jira/jql"  + "?keyword=" + keyword +  "&type=" + type, JSONObject.class);
        if(response.getIntValue("code") == 200){
            return response.getJSONArray("data");
        }
        return null;
    }






}

