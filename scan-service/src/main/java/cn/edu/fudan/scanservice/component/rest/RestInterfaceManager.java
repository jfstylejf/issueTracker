package cn.edu.fudan.scanservice.component.rest;

import cn.edu.fudan.scanservice.exception.AuthException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;


/**
 * @author WZY
 * @version 1.0
 **/
@Slf4j
@Component
public class RestInterfaceManager {


    @Value("${account.service.path}")
    private String accountServicePath;
    @Value("${project.service.path}")
    private String projectServicePath;
    @Value("${commit.service.path}")
    private String commitServicePath;
    @Value("${clone.service.path}")
    private String cloneServicePath;
    @Value("${measure.service.path}")
    private String measureServicePath;
    @Value("${code.service.path}")
    private String codeServicePath;
    @Value("${code-tracker.service.path}")
    private String codeTrackerServicePath;
    @Value("${test.repo.path}")
    private String testProjectPath;

    @Value("${issue.service.path}")
    private String issueServicePath;

    private RestTemplate restTemplate;

    public RestInterfaceManager(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private Logger logger = LoggerFactory.getLogger(RestInterfaceManager.class);

    //----------------------------------account service----------------------------------------------------
    public void userAuth(String userToken) throws AuthException {
        JSONObject result = restTemplate.getForObject(accountServicePath + "/user/auth/" + userToken, JSONObject.class);
        if (result == null || result.getIntValue("code") != 200) {
            throw new AuthException("auth failed!");
        }
    }

    //-----------------------------------commit service-------------------------------------------------------
    public JSONObject checkOut(String repo_id, String commit_id) {
        return restTemplate.getForObject(commitServicePath + "/checkout?repo_id=" + repo_id + "&commit_id=" + commit_id, JSONObject.class);
    }

    public JSONObject getCommitTime(String commitId) {
        return restTemplate.getForObject(commitServicePath + "/commit-time?commit_id=" + commitId, JSONObject.class);
    }

    public JSONObject getCommitByCommitId(String commitId) {
        return restTemplate.getForObject(commitServicePath + "/commit/" + commitId, JSONObject.class);
    }

    public JSONObject getCommitsOfRepo(String repoId, Integer page, Integer size) {
        String url = commitServicePath + "?repo_id=" + repoId;
        if(page != null ){
            if(size != null){
                if(size<=0 || page<=0){
                    logger.error("page size or page is not correct . page size --> {},page --> {}",size,page);
                    return null;
                }
                url += "&per_page="+size;
            }
            url += "&page="+page;
        }

        return restTemplate.getForObject(commitServicePath + "?repo_id=" + repoId + "&page=" + page + "&per_page=" + size + "&is_whole=true", JSONObject.class);
    }

    public JSONObject getCommitsOfRepoByConditions(String repoId, Integer page, Integer pageSize,Boolean isWhole) {

        String url = commitServicePath + "?repo_id=" + repoId;
        if(page != null ){
            if(pageSize != null){
                if(pageSize<=0 || page<=0){
                    logger.error("page size or page is not correct . page size --> {},page --> {}",pageSize,page);
                    return null;
                }
                url += "&per_page=" + pageSize;
            }
            url += "&page=" + page;
        }

        if(isWhole != null){
            url += "&is_whole=" + isWhole ;
        }
        return restTemplate.getForObject(url, JSONObject.class);

    }

    //---------------------------------------------project service---------------------------------------------------------
    public JSONObject getProjectsOfRepo(String repoId) {
        String path =  projectServicePath + "/inner/project?repo_uuid=" + repoId;
        log.debug("get request path is {}", path);

        try {
            // 最多等待180秒
            for (int i = 1; i <= 180; i++) {
                TimeUnit.SECONDS.sleep(5);
                JSONObject res = restTemplate.getForObject(path , JSONObject.class);
                if (res == null || res.isEmpty()){
                    log.warn("repo : [{}] info is null, continue waiting", repoId);
                    continue;
                }
                return res;
            }
        }catch (InterruptedException e) {
            log.error(e.getMessage());
        }

        return restTemplate.getForObject(path , JSONObject.class);
    }

    //---------------------------------------------code service---------------------------------------------------------
    public String getRepoPath(String repoId, String commit_id) {
        String repoPath = null;
        JSONObject response = restTemplate.getForObject(codeServicePath + "?repo_id=" + repoId + "&commit_id=" + commit_id, JSONObject.class).getJSONObject("data");
        if (response != null) {
            if (response.getString("status").equals("Successful")) {
                repoPath = response.getString("content");
                log.info("repoHome -> {}", repoPath);
            } else {
                log.error("get repoHome fail -> {}", response.getString("content"));
            }
        } else {
            log.error("code service response null!");
        }
        return repoPath;
    }

    public JSONObject freeRepoPath(String repoId, String repoPath) {
        if (testProjectPath != null && !testProjectPath.equals ("false")) {
            return null;
        }
        if (repoPath != null) {
            return restTemplate.getForObject(codeServicePath + "/free?repo_id=" + repoId + "&path=" + repoPath, JSONObject.class);
        }
        return null;
    }

    public String getRepoPath(String repoId){
        if (testProjectPath != null && !testProjectPath.equals ("false")) {
            return testProjectPath;
        }

        String repoPath = null;
        int tryCount = 0;
        while (tryCount < 5) {

            try{
                JSONObject response = restTemplate.getForObject(codeServicePath + "?repo_id=" + repoId , JSONObject.class);
                if (response != null && response.getJSONObject("data") != null && "Successful".equals(response.getJSONObject ("data").getString ("status"))) {
                    repoPath = response.getJSONObject("data").getString ("content");
                } else {
                    logger.error("code service response null!");
                }
                break;
            }catch (Exception e){
                log.error("getRepoPath Exception！ {}", e.getMessage());
                try{
                    TimeUnit.SECONDS.sleep(20);
                }catch(Exception sleepException){
                    e.printStackTrace();
                }

                tryCount++;
            }
        }
        return repoPath;

    }

//    -------------------------------------------------- code tracker ------------------------------

    public boolean startCodeTracker(String repoId, String branch, String beginCommit) {
        boolean result = false;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("repoId",repoId);
        jsonObject.put("branch",branch);
        jsonObject.put("beginCommit",beginCommit);
        JSONObject requestResult = restTemplate.postForObject(codeTrackerServicePath+"/project/auto", jsonObject, JSONObject.class);
        if(requestResult != null){
            int code = requestResult.getInteger("code");
            if(code == 200){
                result = true;
            }
        }
        return result;
    }

    public boolean updateCodeTracker(String repoId, String branch) {
        boolean result = false;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("repoId",repoId);
        jsonObject.put("branch",branch);
        JSONObject requestResult = restTemplate.postForObject(codeTrackerServicePath+"/project/auto/update", jsonObject, JSONObject.class);
        if(requestResult != null){
            int code = requestResult.getInteger("code");
            if(code == 200){
                result = true;
            }
        }
        return result;
    }

    public String getCodeTrackerStatus(String repoId, String branch) {
        String result = "";
        JSONObject requestResult = restTemplate.getForObject(codeTrackerServicePath + "/project/scan/status?repoId=" + repoId + "&branch=" + branch, JSONObject.class);
        if(requestResult != null){
            int code = requestResult.getInteger("code");
            if(code == 200){
                result = requestResult.getString("data");
            }
        }
        return result;
    }

    public boolean invokeTools(String toolType, String toolName, String repoId, String branch, String beginCommit) {
        boolean result = false;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("repoUuid",repoId);
        jsonObject.put("branch",branch);
        jsonObject.put("beginCommit",beginCommit);
        // toolName 和 toolType 都来自于 Tool表
        String servicePath = getServicePathByToolType(toolType) + "/" + toolType + "/" + toolName;
        try {
            JSONObject requestResult = restTemplate.postForObject(servicePath, jsonObject, JSONObject.class);
            if(requestResult != null){
                int code = requestResult.getInteger("code");
                if(code == HttpStatus.OK.value()){
                    result = true;
                }
            }
        }catch (Exception e) {
            log.error("invoke tool failure, service path is {}", servicePath);
            log.error(e.getMessage());
        }
        return result;
    }

    public JSONObject getToolsScanStatus(String toolType, String toolName, String repoId) {
        JSONObject result = null;
        try {
            // toolName 和 toolType 都来自于 Tool表
            String servicePath = getServicePathByToolType(toolType);
            String requestPath = servicePath + "/" + toolType + "/"
                    + toolName + "/scan-status?repo_uuid=" + repoId;
            log.debug("request path is {}", requestPath);
            JSONObject requestResult = restTemplate.getForObject (requestPath, JSONObject.class);
            if(requestResult != null){
                int code = requestResult.getInteger("code");
                if(code == HttpStatus.OK.value()){
                    result = requestResult.getJSONObject ("data");
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return result;
    }

    private String getServicePathByToolType(String toolName) {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getName().contains(toolName)) {
                try {
                    return   (String) field.get(this);
                } catch (IllegalAccessException e) {
                    log.error("get tool {}  service path failed", toolName);
                }
            }
        }
        return null;
    }

}

