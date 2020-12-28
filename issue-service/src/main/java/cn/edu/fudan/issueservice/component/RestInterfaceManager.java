package cn.edu.fudan.issueservice.component;

import cn.edu.fudan.issueservice.exception.AuthException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author WZY
 * @version 1.0
 **/
@Component
public class RestInterfaceManager {

    private static final Logger logger = LoggerFactory.getLogger(RestInterfaceManager.class);

    @Value("${account.service.path}")
    private String accountServicePath;
    @Value("${project.service.path}")
    private String projectServicePath;
    @Value("${code.service.path}")
    private String codeServicePath;
    @Value("${commit.service.path}")
    private String commitServicePath;
    @Value("${sonar.service.path}")
    private String sonarServicePath;
    @Value("${measure.service.path}")
    private String measureServicePath;
    @Value("${test.repo.path}")
    private String testProjectPath;

    private final RestTemplate restTemplate;

    public RestInterfaceManager(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    //------------------------------------------------account service-----------------------------------------------------------

    public void userAuth(String userToken)throws AuthException{
        JSONObject result = restTemplate.getForObject(accountServicePath + "/user/auth/" + userToken, JSONObject.class);
        if (result == null || result.getIntValue("code") != 200) {
            throw new AuthException("auth failed!");
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getAccountIds() {
        return restTemplate.getForObject(accountServicePath + "/user/accountIds", List.class);
    }

    //-----------------------------------------------project service-------------------------------------------------
    /**
     * 根据account_id查找参与的项目信息
     * @param accountId 用户登录帐号Id
     * @return  参与的项目信息
     */
    public JSONArray getProjectList(String accountId) {
         return restTemplate.getForObject(projectServicePath + "/inner/projects?account_uuid=" + accountId,JSONArray.class);
    }

    /**
     * 根据repo_uuid查找对应的project
     * @param repoUuid 代码库uuid
     * @return Map<String,String> 项目名 代码库名 分支名 代码库uuid
     */
    public Map<String, String> getProjectByRepoId(String repoUuid) {

        JSONObject projectInfo = restTemplate.getForObject(projectServicePath + "/inner/project?repo_uuid=" + repoUuid, JSONObject.class);

        Map<String,String> result = new HashMap<>(8);

        if(projectInfo != null) {
            result.put("projectName", projectInfo.getString("projectName"));
            result.put("repoName", projectInfo.getString("repoName"));
            result.put("branch", projectInfo.getString("branch"));
            result.put("repoUuid", projectInfo.getString("repoUuid"));
        }

        return result;
    }

    /**
     * 获取所有project下所有repo uuid和name
     * @param userToken userToken
     * @return 所有repo uuid和name
     */
    public JSONObject getAllRepo(String userToken){

        HttpHeaders headers = new HttpHeaders();
        headers.add("token", userToken);
        HttpEntity<HttpHeaders> request = new HttpEntity<>(headers);
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(projectServicePath  + "/project/all",HttpMethod.GET,request,JSONObject.class);
        String body = Objects.requireNonNull(responseEntity.getBody()).toString();

        return JSONObject.parseObject(body);
    }

    public Map<String, String> getAllRepoToRepoName(String userToken){
        Map<String, String> repoName = new HashMap<>(64);

        JSONObject allRepo = getAllRepo(userToken);
        for(String projectName : allRepo.keySet()){
            Iterator<Object> iterator = allRepo.getJSONArray(projectName).stream().iterator();
            while (iterator.hasNext()){
                JSONObject next = (JSONObject) iterator.next();
                repoName.put(next.getString("repo_id"), next.getString("name"));
            }
        }

        return repoName;
    }


    /**
     * 根据url返回repoUuid
     * @param url url
     * @param userToken userToken
     * @return repoUuid
     */
    public String getRepoUuidByUrl(String url, String userToken){

        if (StringUtils.isEmpty(url)) {
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("token", userToken);
        HttpEntity<HttpHeaders> request = new HttpEntity<>(headers);
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(projectServicePath  + "/project",HttpMethod.GET,request,JSONObject.class);
        String body = responseEntity.getBody().toString();
        JSONObject result = JSONObject.parseObject(body);
        JSONArray reposDetail = result.getJSONArray("data");

        for(int i = 0;i < reposDetail.size();i++){
            JSONObject repoDetail = reposDetail.getJSONObject(i);
            if(url.equals(repoDetail.get("url").toString())){
                return repoDetail.get("repoUuid").toString();
            }
        }

        return null;
    }

    //---------------------------------------------commit service------------------------------------------------------

    public String getFirstCommitDate(String developerName){
        JSONObject data = restTemplate.getForObject(commitServicePath + "/first-commit?author=" + developerName, JSONObject.class).getJSONObject("data");
        LocalDateTime fistCommitDate = LocalDateTime.parse(data.getJSONObject("repos_summary").getString("first_commit_time_summary"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return fistCommitDate.plusHours(8).toLocalDate().toString();
    }

    //---------------------------------------------code service---------------------------------------------------------

    public JSONObject getRepoPath(String repoId,String commitId){
        JSONObject repoPath = null;
        int tryCount = 0;
        while (tryCount < 5) {

            try{
                JSONObject response = restTemplate.getForObject(codeServicePath + "?repo_id=" + repoId + "&commitId=" + commitId, JSONObject.class);
                if (response != null ) {
                    repoPath = response;
                } else {
                    logger.error("code service response null!");
                }
                break;
            }catch (Exception e){
                e.printStackTrace();
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

    public String getRepoPath(String repoId){
        if (testProjectPath != null && !"false".equals (testProjectPath)) {
            return testProjectPath;
        }

        String repoPath = null;
        int tryCount = 0;
        while (tryCount < 5) {

            try{
                String urlPath = codeServicePath + "?repo_id=" + repoId;
                logger.debug(urlPath);
                JSONObject response = restTemplate.getForObject(urlPath , JSONObject.class);
                if (response != null && response.getJSONObject("data") != null && "Successful".equals(response.getJSONObject ("data").getString ("status"))) {
                    repoPath = response.getJSONObject("data").getString ("content");
                } else {
                    logger.error("code service response null!");
                    logger.error("request url is : {}",urlPath);
                }
                break;
            }catch (Exception e){
                e.printStackTrace();
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

    public void freeRepoPath(String repoId, String repoPath){
        try{
            if (testProjectPath != null && !"false".equals (testProjectPath)) {
                return;
            }
            if(repoPath == null || repoId == null){
                return;
            }
            restTemplate.getForObject(codeServicePath + "/free?repo_id=" + repoId+"&path="+repoPath, JSONObject.class);
        }catch (Exception e){
            logger.error(e.getMessage());
        }
    }

    //--------------------------------------------------------sonar api -----------------------------------------------------

    public JSONObject getSonarIssueResults(String repoName, String type, int pageSize, boolean resolved,int page) {
        Map<String, String> map = new HashMap<>(16);
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(sonarServicePath).append("/api/issues/search?componentKeys={componentKeys}&additionalFields={additionalFields}&s={s}&resolved={resolved}");
        map.put("additionalFields","_all");
        map.put("s","FILE_LINE");
        map.put("componentKeys",repoName);
        map.put("resolved",String.valueOf(resolved));
        if(type != null){
            String[] types = type.split(",");
            StringBuilder stringBuilder = new StringBuilder();
            for (String typeSb : types) {
                if ("CODE_SMELL".equals(typeSb) || "BUG".equals(typeSb) || "VULNERABILITY".equals(typeSb) || "SECURITY_HOTSPOT".equals(typeSb)) {
                    stringBuilder.append(typeSb).append(",");
                }
            }
            if(!stringBuilder.toString().isEmpty()){
                urlBuilder.append("&componentKeys={componentKeys}");
                String requestTypes = stringBuilder.toString().substring(0,stringBuilder.toString().length()-1);
                map.put("types",requestTypes);
            }else{
                logger.error("this request type --> {} is not available in sonar api",type);
                return null;
            }
        }

        if(page>0){
            urlBuilder.append("&p={p}");
            map.put("p",String.valueOf(page));
        }
        if(pageSize>0){
            urlBuilder.append("&ps={ps}");
            map.put("ps",String.valueOf(pageSize));
        }

        String url = urlBuilder.toString();

        try {
            ResponseEntity<JSONObject> entity = restTemplate.getForEntity(url, JSONObject.class,map);
            return JSONObject.parseObject(entity.getBody().toString());
        }catch (RuntimeException e) {
            logger.error("repo name : {}  ----> request sonar api failed", repoName);
            throw e;
        }


    }

    public JSONObject getRuleInfo(String ruleKey,String actives,String organizationKey){
        Map<String, String> map = new HashMap<>(64);

        String baseRequestUrl = sonarServicePath + "/api/rules/show";
        if(ruleKey ==null){
            logger.error("ruleKey is missing");
            return null;
        }else{
            map.put("key",ruleKey);
        }
        if(actives != null){
            map.put("actives",actives);
        }
        if(organizationKey != null){
            map.put("organization",organizationKey);
        }

        try{
            return restTemplate.getForObject(baseRequestUrl + "?key=" + ruleKey, JSONObject.class);
        }catch(RuntimeException e){
            logger.error("ruleKey : {}  ----> request sonar  rule infomation api failed", ruleKey);
            throw e;
        }

    }

    public JSONObject getSonarAnalysisTime(String projectName) {
        JSONObject error = new JSONObject();
        error.put("errors","Component key " + projectName + " not found");

        try {
            String urlPath = sonarServicePath + "/api/components/show?component=" + projectName;
            logger.debug(urlPath);
            return restTemplate.getForObject(urlPath, JSONObject.class);
        }catch (Exception e) {
            logger.error(e.getMessage());
            logger.error("projectName: {} ---> request sonar api failed 获取最新版本时间API 失败", projectName);
        }

        return error;
    }

    // --------------------------------------------------------measure api ---------------------------------------------------------

    public Map<String, Integer> getDeveloperWorkload(Map<String, Object> query){

        HttpEntity<HttpHeaders> request = new HttpEntity<>(new HttpHeaders(){{
            add("token",null);
        }});

        String url = measureServicePath + "/measure/developer/work-load?developer=" +
                (StringUtils.isEmpty(query.get("developer")) ? "" : query.get("developer").toString()) +
                "&repo_uuid=" + (StringUtils.isEmpty(query.get("repoList")) ? "" : query.get("repoList").toString()) +
                "&since=" + (StringUtils.isEmpty(query.get("since")) ? "" : query.get("since").toString()) +
                "&until=" + (StringUtils.isEmpty(query.get("until")) ? "" : query.get("until").toString());

        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url , HttpMethod.GET,request,JSONObject.class);
        JSONObject result = JSONObject.parseObject(responseEntity.getBody().toString(),JSONObject.class);

        if(result.getIntValue("code") != 200){
            logger.error("request /measure/developer/workLoad failed");
            throw  new RuntimeException("get data from /measure/developer/work-load failed!");
        }

        Map<String, Integer> developerWorkLoad = new HashMap<>(16);

        JSONObject data = result.getJSONObject("data");

        data.keySet().forEach(r -> developerWorkLoad.put(r,developerWorkLoad.getOrDefault(r + data.getJSONObject(r).getInteger("delLines") + data.getJSONObject(r).getInteger("addLines"), data.getJSONObject(r).getInteger("delLines") + data.getJSONObject(r).getInteger("addLines"))));

        return developerWorkLoad;
    }

}
