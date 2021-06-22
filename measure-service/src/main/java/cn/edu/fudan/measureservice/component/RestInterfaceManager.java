package cn.edu.fudan.measureservice.component;

import cn.edu.fudan.measureservice.annotation.MethodMeasureAnnotation;
import cn.edu.fudan.measureservice.domain.ResponseBean;
import cn.edu.fudan.measureservice.domain.dto.UserInfoDTO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @Deprecated
    public JSONObject getFirstCommitDate(String developerName){
        return restTemplate.getForObject(commitServicePath+"/first-commit?author="+ developerName,JSONObject.class).getJSONObject("data");
    }

    //------------project-service--------------------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    @CachePut("projects")
    public Map<String,Object> getProjectByrepoUuid(String repoUuid,String token){
        HttpHeaders headers = new HttpHeaders();
        headers.add("token",token);
        HttpEntity request = new HttpEntity(headers);
        StringBuilder url = new StringBuilder();
        url.append(projectServicePath).append("/inner/project?repo_uuid=").append(repoUuid);
        ResponseEntity<ResponseBean> responseEntity =restTemplate.exchange(url.toString(),HttpMethod.GET,request,ResponseBean.class);
        ResponseBean<Map<String,Object>> result = responseEntity.getBody();
        if (result!=null && result.getCode()==HttpStatus.OK.value()) {
            return result.getData();
        }else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String,List<Map<String,String>>> getProjectInfo(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("token",token);
        HttpEntity request= new HttpEntity(headers);
        StringBuilder url = new StringBuilder();
        url.append(projectServicePath).append("/project/all");
        ResponseEntity<ResponseBean> responseEntity = restTemplate.exchange(url.toString(),HttpMethod.GET,request,ResponseBean.class);
        ResponseBean result = responseEntity.getBody();
        if(result!=null) {
            return ( Map<String,List<Map<String,String>>>) result.getData();
        }
        return null;
    }


    @SuppressWarnings("unchecked")
    public UserInfoDTO getUserInfoByToken(String token) {
        Objects.requireNonNull(token);
        ResponseBean result = restTemplate.getForObject(accountServicePath + "/user/right/" + token, ResponseBean.class);
        if (result == null) {
            log.error("Response is null");
            return null;
        }

        if (result.getCode() != HttpStatus.OK.value()) {
            log.error(result.getMsg());
            return null;
        }
        Map<String,Object> data =  (Map<String, Object>) result.getData();
        return new UserInfoDTO(token, (String) data.get("uuid"), (Integer) data.get("right"));
    }

    public boolean deleteRecall(String repoUuid, String token) {
        String path =  projectServicePath + "/repo?service_name=MEASURE&repo_uuid=" + repoUuid;
        HttpHeaders headers = new HttpHeaders();
        headers.add("token", token);
        HttpEntity<HttpHeaders> request = new HttpEntity<>(headers);
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(path, HttpMethod.PUT, request, JSONObject.class);
        log.info(responseEntity.toString());
        return Objects.requireNonNull(responseEntity.getBody()).getIntValue("code") == HttpStatus.OK.value();
    }

    //---------------------------------------------code service---------------------------------------------------------
    public String getRepoPath(String repoUuid,String commit_id){
        String repoPath=null;
        StringBuilder url = new StringBuilder();
        url.append(codeServicePath).append("?repo_id=").append(repoUuid);
        if (commit_id != null){
            url.append("&commit_id=").append(commit_id);
        }
        try{
            JSONObject response=restTemplate.getForObject(url.toString(), JSONObject.class).getJSONObject("data");
            if (response != null ){
                if(response.getString("status").equals("Successful")) {
                    repoPath = response.getString("content");
                logger.info("repoHome -> {} , repoUuid -->{} , commit_id -->{}" ,repoPath,repoUuid,commit_id);
                }else{
                    logger.error("get repoHome fail -> {}",response.getString("content"));
                    logger.error("repoUuid -> {} commitId -> {}",repoUuid,commit_id);
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

    public JSONObject freeRepoPath(String repoUuid,String repoPath){
        JSONObject response=restTemplate.getForObject(codeServicePath + "/free?repo_id=" + repoUuid+"&path="+repoPath, JSONObject.class);
        if(response!=null&&response.getJSONObject("data").getString("status").equals("Successful")){
            logger.info("{} -> free success",repoPath);
        }else {
            logger.warn("{} -> free failed",repoPath);
        }
        return response;
    }


    //---------------------------------------------repo service---------------------------------------------------------
    public JSONObject getRepoById(String repoUuid){
        return restTemplate.getForObject(repoServicePath + "/" + repoUuid, JSONObject.class);
    }

    public int getRepoAge(String repoUuid, String until){
        JSONObject response = restTemplate.getForObject(repoServicePath + "/repository_year" + "?repo_id=" + repoUuid + "&end_date=" + until, JSONObject.class);
        if (response.containsKey("data")){
            return response.getJSONObject("data").getIntValue("commit_time");
        }
        return -1;
    }




    //---------------------------------------------issue service---------------------------------------------------------

    public int getIssueCountByConditions(String developer, String repoUuid, String since, String until, String tool, String category, String token){
        HttpHeaders headers = new HttpHeaders();
        headers.add("token",token);
        HttpEntity request = new HttpEntity(headers);
        StringBuilder url = new StringBuilder();
        url.append(issueServicePath).append("/issue/filter").append("?repo_uuids=").append(repoUuid).append("&ps=").append(0);
        if(since !=null && !"".equals(since)) {
            url.append("&since=").append(since);
        }
        if(until !=null && !"".equals(until)) {
            url.append("&until=").append(until);
        }
        if(tool != null && !"".equals(tool)) {
            url.append("&tool=").append(tool);
        }
        if (developer != null && !"".equals(developer)){
            url.append("&introducer=").append(developer);
        }
        if (category != null && !"".equals(category)){
            url.append("&category=").append(category);
        }
        ResponseEntity responseEntity = restTemplate.exchange(url.toString(), HttpMethod.GET, request, JSONObject.class);
        String body = responseEntity.getBody().toString();
        JSONObject result = JSONObject.parseObject(body);
        if(result.getIntValue("code") == HttpStatus.OK.value()){
            return result.getJSONObject("data").getIntValue("total");
        }
        return 0;
    }


    public int getAddIssueCount(String repoUuid, String developer,String since, String until, String tool, String token){
        HttpHeaders headers = new HttpHeaders();
        headers.add("token",token);
        HttpEntity request = new HttpEntity(headers);
        StringBuilder url = new StringBuilder();
        url.append(issueServicePath).append("/issue/filter").append("?repo_uuid=").append(repoUuid).append("&ps=").append(0);
        if(since !=null && !"".equals(since)) {
            url.append("&since=").append(since);
        }
        if(until !=null && !"".equals(until)) {
            url.append("&until=").append(until);
        }
        if(tool != null && !"".equals(tool)) {
            url.append("&tool=").append(tool);
        }
        if (developer != null && !"".equals(developer)){
            url.append("&introducer=").append(developer);
        }
        ResponseEntity responseEntity = restTemplate.exchange(url.toString(), HttpMethod.GET, request, JSONObject.class);
        String body = responseEntity.getBody().toString();
        JSONObject result = JSONObject.parseObject(body);
        if(result.getIntValue("code") == HttpStatus.OK.value()){
            return result.getJSONObject("data").getIntValue("total");
        }
        return 0;
    }

    public JSONObject getDayAvgSolvedIssue(String developer, String repoUuid, String since, String until, String tool, String token){

        HttpHeaders headers = new HttpHeaders();
        headers.add("token",token);
        HttpEntity request = new HttpEntity(headers);
        StringBuilder url = new StringBuilder();
        url.append(uniformServicePath).append("/measurement/developer/day-avg-solved-issue?developer=").append(developer);
        if (repoUuid != null && repoUuid.length()>0){
            url.append("&repo_uuid=").append(repoUuid);
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

        if(result.getIntValue("code") == HttpStatus.OK.value()){
            return result.getJSONObject("data");
        }
        return null;
    }

    //--------------------------------code-tracker service------------------------------------------------

    public List<Map<String,Object>> getStatements(String repoUuid, String since, String until, String developer,String type){
        StringBuilder url = new StringBuilder();
        url.append(uniformServicePath).append("/codewisdom/code/line-count?level=").append(type);
        if(since!=null) {
            url.append("&since=").append(since);
        }
        if(until!=null) {
            url.append("&until=").append(until);
        }
        if(repoUuid!=null && !"".equals(repoUuid)) {
            url.append("&repo_uuids=").append(repoUuid);
        }
        if(developer!=null && !"".equals(developer)) {
            url.append("&developers=").append(developer);
        }
        ResponseBean<List<Map<String,Object>>> response = restTemplate.getForObject(url.toString(), ResponseBean.class);
        if (response == null || response.getCode() != HttpStatus.OK.value()){
            return null;
        }
        return response.getData();
    }

    public List<Map<String,Object>> getCodeLifeCycle(String repoUuid,String developer,String since,String until,String level,String type) {
        StringBuilder url = new StringBuilder();
        url.append(uniformServicePath).append("/codewisdom/code/lifecycle?level=").append(level).append("&type=").append(type);
        if (developer!=null && !"".equals(developer)) {
            url.append("&developers=").append(developer);
        }
        if(repoUuid!=null && !"".equals(repoUuid)) {
            url.append("&repo_uuids=").append(repoUuid);
        }
        if(since!=null && !"".equals(since)) {
            url.append("&since=").append(since);
        }
        if (until!=null && !"".equals(until)) {
            url.append("&until").append(until);
        }
        ResponseBean<List<Map<String,Object>>> response = restTemplate.getForObject(url.toString(),ResponseBean.class);
        if(response!=null && response.getCode()==HttpStatus.OK.value()) {
            return response.getData();
        }else {
            return null;
        }
    }

    public List<Map<String,Object>> getFocusFilesCount(String repoUuid, String developer,String since, String until){
        StringBuilder url = new StringBuilder();
        url.append(uniformServicePath).append("/statistics/focus/file/num?developers=").append(developer);
        if(repoUuid!=null && !"".equals(repoUuid)) {
            url.append("&repo_uuids").append(repoUuid);
        }
        if (since!=null && !"".equals(since)) {
            url.append("&since=").append(since);
        }
        if (until!=null && !"".equals(until)) {
            url.append("&until").append(until);
        }
        ResponseBean<List<Map<String,Object>>> response = restTemplate.getForObject(url.toString(),ResponseBean.class);
        if(response!=null && response.getCode()==HttpStatus.OK.value()) {
            return response.getData();
        }else {
            return  null;
        }
    }


    //-------------------------------------------clone service---------------------------------------
    public List<Map<String,Object>> getCloneMeasure(String repoUuid, String developer, String since, String until){
        StringBuilder url = new StringBuilder();
        url.append(uniformServicePath).append("/cloneMeasure"  + "?repo_uuids=").append(repoUuid).append("&developers=").append(developer);
        if (since!=null && !"".equals(since)) {
            url.append("&since=").append(since);
        }
        if (until!=null && !"".equals(until)) {
            url.append("&until").append(until);
        }
        try {
            ResponseBean<List<Map<String,Object>>> result = restTemplate.getForObject(url.toString(), ResponseBean.class);
            if (result!=null && result.getCode()==HttpStatus.OK.value()) {
                return result.getData();
            }else {
                return null;
            }
        }catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    //-------------------------------------------jira API-------------------------------------------
    @Cacheable(cacheNames = {"jiraInfoByJiraID"})
    public JSONArray getJiraInfoByKey(String type, String keyword){
        try {
            JSONObject response = restTemplate.getForObject(uniformServicePath+"/jira/jql"  + "?keyword=" + keyword +  "&type=" + type, JSONObject.class);
            if(response.getIntValue("code") == HttpStatus.OK.value()){
                return response.getJSONArray("data");
            }
        }catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public JSONObject getJiraMsgOfOneDeveloper(String developer, String repoUuid,String since,String until){
        try {
            StringBuilder url = new StringBuilder();
            url.append(uniformServicePath).append("/jira/developer-msg?developer=").append(developer);
            if (repoUuid != null && !"".equals(repoUuid)){
                url.append("&repo_uuid=").append(repoUuid);
            }
            if(since !=null && !"".equals(since)) {
                url.append("&since=").append(since);
            }
            if(until!=null && !"".equals(until)) {
                url.append("&until").append(until);
            }
            JSONObject response = restTemplate.getForObject(url.toString(), JSONObject.class);
            if(response.getIntValue("code") == HttpStatus.OK.value()){
                return response.getJSONObject("data");
            }
        }catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

}

