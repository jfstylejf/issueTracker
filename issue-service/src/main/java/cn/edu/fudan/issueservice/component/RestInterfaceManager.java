package cn.edu.fudan.issueservice.component;

import cn.edu.fudan.issueservice.exception.AuthException;
import cn.edu.fudan.issueservice.util.RegexUtil;
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
    @Value("${tag.service.path}")
    private String tagServicePath;
    @Value("${recommendation.path}")
    String recommendationServicePath;
    @Value("${repository.service.path}")
    private String repoServicePath;
    @Value("${scan.service.path}")
    private String scanServicePath;
    @Value("${sonar.service.path}")
    private String sonarServicePath;
    @Value("${measure.service.path}")
    private String measureServicePath;
    @Value("${test.repo.path}")
    private String testProjectPath;

    private RestTemplate restTemplate;

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

    //------------------------------------------------end----------------------------------------------------------------------

    //-----------------------------------------------project service-------------------------------------------------
    /**
     * 根据account_id查找参与的项目信息
     * @param account_id 用户登录帐号Id
     * @return  参与的项目信息
     */
    public JSONArray getProjectList(String account_id) {
        JSONArray projectInfo = restTemplate.getForObject(projectServicePath + "/inner/projects?account_uuid=" + account_id, JSONArray.class);

        return restTemplate.getForObject(projectServicePath + "/inner/projects?account_uuid=" + account_id,JSONArray.class);
    }


    /**
     * 根据repo_uuid查找对应的project
     * @param repo_id 代码库uuid
     * @return Map<String,String> 项目名 代码库名 分支名 代码库uuid
     */
    public Map<String, String> getProjectByRepoId(String repo_id) {

        JSONObject projectInfo = restTemplate.getForObject(projectServicePath + "/inner/project?repo_uuid=" + repo_id, JSONObject.class);

        Map<String,String> result = new HashMap<>();

        result.put("projectName", projectInfo.getString("projectName"));
        result.put("repoName", projectInfo.getString("repoName"));
        result.put("branch", projectInfo.getString("branch"));
        result.put("repoUuid", projectInfo.getString("repoUuid"));

        return result;
    }

    /**
     * 获取所有project下所有repo uuid和name
     * @param userToken
     * @return
     */
    public JSONObject getAllRepo(String userToken){

        HttpHeaders headers = new HttpHeaders();
        headers.add("token", userToken);
        HttpEntity request = new HttpEntity(headers);
        ResponseEntity responseEntity = restTemplate.exchange(projectServicePath  + "/project/all",HttpMethod.GET,request,JSONObject.class);
        String body = responseEntity.getBody().toString();

        return JSONObject.parseObject(body);
    }

    /**
     * 根据url返回repoUuid
     * @param url
     * @param userToken
     * @return repoUuid
     */
    public String getRepoUuidByUrl(String url, String userToken){

        if (StringUtils.isEmpty(url)) {
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("token", userToken);
        HttpEntity request = new HttpEntity(headers);
        ResponseEntity responseEntity = restTemplate.exchange(projectServicePath  + "/project",HttpMethod.GET,request,JSONObject.class);
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

    //-------------------------------------------------end-------------------------------------------------------------

    //---------------------------------------------commit service------------------------------------------------------
    public String getFirstCommitDate(String developerName){
        JSONObject data = restTemplate.getForObject(commitServicePath + "/first-commit?author=" + developerName, JSONObject.class).getJSONObject("data");
        LocalDateTime fistCommitDate = LocalDateTime.parse(data.getJSONObject("repos_summary").getString("first_commit_time_summary"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return fistCommitDate.plusHours(8).toLocalDate().toString();
    }

    //----------------------------------------------end-----------------------------------------------------------------

    //---------------------------------------------code service---------------------------------------------------------
    public JSONObject getRepoPath(String repoId,String commit_id){
        JSONObject repoPath = null;
        int tryCount = 0;
        while (tryCount < 5) {

            try{
                JSONObject response = restTemplate.getForObject(codeServicePath + "?repo_id=" + repoId + "&commit_id=" + commit_id, JSONObject.class);
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
        if (testProjectPath != null && !testProjectPath.equals ("false")) {
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

    public JSONObject freeRepoPath(String repoId,String repoPath){
        try{
            if (testProjectPath != null && !testProjectPath.equals ("false")) {
                return null;
            }
            if(repoPath == null || repoId == null){
                return null;
            }
            restTemplate.getForObject(codeServicePath + "/free?repo_id=" + repoId+"&path="+repoPath, JSONObject.class);
        }catch (Exception e){
            logger.error(e.getMessage());
        }
        return null;
    }

    //--------------------------------------------------------sonar api -----------------------------------------------------
    public JSONObject getSonarIssueResults(String repoName, String type, int pageSize, boolean resolved,int page) {
        Map<String, String> map = new HashMap<>();
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(sonarServicePath + "/api/issues/search?componentKeys={componentKeys}&additionalFields={additionalFields}&s={s}&resolved={resolved}");
        map.put("additionalFields","_all");
        map.put("s","FILE_LINE");
        map.put("componentKeys",repoName);
        map.put("resolved",String.valueOf(resolved));
        if(type != null){
            String[] types = type.split(",");
            StringBuilder stringBuilder = new StringBuilder();
            for(int i=0;i<types.length;i++){
                String typeSb = types[i];
                if("CODE_SMELL".equals(typeSb) || "BUG".equals(typeSb) || "VULNERABILITY".equals(typeSb) || "SECURITY_HOTSPOT".equals(typeSb)){
                    stringBuilder.append(typeSb+",");
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
            ResponseEntity entity = restTemplate.getForEntity(url, JSONObject.class,map);
            JSONObject result  = JSONObject.parseObject(entity.getBody().toString());
            return result;

        }catch (RuntimeException e) {
            logger.error("repo name : {}  ----> request sonar api failed", repoName);
            throw e;
        }


    }

    public JSONObject getSonarIssueResultsBySonarIssueKey(String issues,int pageSize) {
        //由于pathurl不能过长，所以请求时设置为5个issueKey为一组
        String baseRequestUrl = sonarServicePath + "/api/issues/search?issues={issues}";
        Map<String, String> map = new HashMap<>();
        map.put("issues",issues);
        if(pageSize>0){
            map.put("ps",pageSize+"");
            baseRequestUrl= baseRequestUrl+"&ps={ps}";
        }
        try {
            ResponseEntity entity = restTemplate.getForEntity(baseRequestUrl,JSONObject.class,map);
            JSONObject result  = JSONObject.parseObject(entity.getBody().toString());
            return result;
        } catch (RuntimeException e) {
            logger.error("issues : {}  ----> request sonar api failed", issues);
            throw e;
        }
    }


    public JSONObject getRuleInfo(String ruleKey,String actives,String organizationKey){
        Map<String, String> map = new HashMap<>();

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

    public JSONObject getSonarSourceLines(String componentKey,int from,int to){
        if(to<from){
            logger.error("lines {} can not greater {} ",from,to);
        }
        if(from <= 0){
            from = 1;
        }
        Map<String, String> map = new HashMap<>();

        String baseRequestUrl = sonarServicePath + "/api/sources/lines?key={key}&from={from}&to={to}";
        map.put("key",componentKey);
        map.put("from",String.valueOf(from));
        map.put("to",String.valueOf(to));
        try{
            ResponseEntity entity = restTemplate.getForEntity(baseRequestUrl,JSONObject.class,map);
            return JSONObject.parseObject(entity.getBody().toString());
        }catch (RuntimeException e){
            e.printStackTrace();
            logger.error("componentKey : {}  ----> request sonar  source Lines  api failed , from --> {} , to --> {}", componentKey,from,to);
            return null;
        }



    }

    /**该函数用来解析/api/sources/show返回来的每行代码（由于返回的数据出现各种标签，现需去掉标签）
     * @param componentKey
     * @param from
     * @param to
     * @return
     */
    public List<String> getSonarSourcesLinesShow(String componentKey, int from, int to){
        if (to<from){
            logger.error("lines {} can not greater than {} ",from,to);
        }

        ArrayList<String> linesList = new ArrayList<>();
        Map<String,String> paraMap = new HashMap<>(3);
        String baseRequestUrl = sonarServicePath + "api/sources/show?key={key}&from={from}&to={to}";
        paraMap.put("key",componentKey);
        paraMap.put("from",String.valueOf(from));
        paraMap.put("to",String.valueOf(to));
        JSONObject linesJO = restTemplate.getForObject(baseRequestUrl, JSONObject.class,paraMap);
        List<java.lang.Object> sourcesList = (List<Object>) linesJO.get("sources");
        Map<List<String>,String> regexAndReplaceStr = new HashMap<>();
        regexAndReplaceStr.put(Arrays.asList("<span[^>]*>","</span[^>]*>"),"");
        regexAndReplaceStr.put(Arrays.asList("&lt;"),"<");
        regexAndReplaceStr.put(Arrays.asList("&gt;"),">");
        regexAndReplaceStr.put(Arrays.asList("&amp;"),"&");
        for (Object o : sourcesList) {
            String code = (String) ((List<Object>) o).get(1);
            code = RegexUtil.getNoTagCode(code, regexAndReplaceStr);
            linesList.add(code);
        }
        return linesList;
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

    public void deleteSonarProject(String projectName){
        try{
            String url = sonarServicePath + "/api/authentication/login?login=admin&password=admin";
            ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.POST, null, JSONObject.class);
            List<String> sonarDeleteHeaders = responseEntity.getHeaders().get("Set-Cookie");
            if(sonarDeleteHeaders == null || sonarDeleteHeaders.size() != 2){
                logger.error("get sonar delete headers failed !");
                logger.error("projectName: {} ---> delete sonar project failed !", projectName);
                return;
            }
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-XSRF-TOKEN", sonarDeleteHeaders.get(0));
            headers.add("Cookie", sonarDeleteHeaders.get(1));
            String urlPath = sonarServicePath + "/api/projects/bulk_delete?projects=" + projectName;
            restTemplate.exchange(urlPath, HttpMethod.POST, new HttpEntity(headers), JSONObject.class);
            logger.info("projectName: {} ---> delete sonar project success !", projectName);
        }catch (Exception e){
            logger.error(e.getMessage());
            logger.error("projectName: {} ---> delete sonar project failed !", projectName);
        }
    }

    //------------------------------------------------------scan api ---------------------------------------------

    public JSONObject getScanByCategoryAndRepoIdAndCommitId(String repoId,String commitId ,String category){
        return restTemplate.getForObject(scanServicePath + "/inner/scan/commit?repo_id=" + repoId+"&commit_id="+commitId+"&category="+category, JSONObject.class);
    }

    // --------------------------------------------------------measure api ---------------------------------------------------------

    public Map<String, Integer> getDeveloperWorkload(Map<String, Object> query){

        HttpEntity request = new HttpEntity(new HttpHeaders(){{
            add("token",null);
        }});

        String url = new StringBuilder().append(measureServicePath).append("/measure/developer/work-load?developer=")
                .append(StringUtils.isEmpty(query.get("developer")) ? "" : query.get("developer").toString())
                .append("&repo_uuid=").append(StringUtils.isEmpty(query.get("repoList")) ? "" : query.get("repoList").toString())
                .append("&since=").append(StringUtils.isEmpty(query.get("since")) ? "" : query.get("since").toString())
                .append("&until=").append(StringUtils.isEmpty(query.get("until")) ? "" : query.get("until").toString())
                .toString();

        ResponseEntity responseEntity = restTemplate.exchange(url , HttpMethod.GET,request,JSONObject.class);
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
