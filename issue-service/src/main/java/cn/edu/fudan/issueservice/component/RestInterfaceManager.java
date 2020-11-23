package cn.edu.fudan.issueservice.component;

import cn.edu.fudan.issueservice.exception.AuthException;
import cn.edu.fudan.issueservice.util.RegexUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonAlias;
import io.netty.util.internal.StringUtil;
import org.apache.kafka.common.protocol.types.Field;
import org.aspectj.weaver.patterns.PerObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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

    private static Logger logger = LoggerFactory.getLogger(RestInterfaceManager.class);


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
    public String getAccountId(String userToken){
        Map<String,String> urlParameters=new HashMap<>();
        urlParameters.put("userToken",userToken);
        return restTemplate.getForObject(accountServicePath+"/user/accountId?userToken={userToken}",String.class,urlParameters);
    }

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


    //----------------------------------------------tag service---------------------------------------------------------------
    public void deleteTagsOfIssueInOneRepo(List<String> issueIds){
        if (issueIds != null && !issueIds.isEmpty()) {
            JSONObject response = restTemplate.postForObject(tagServicePath +  "/inner/tags" + "/tagged-delete", issueIds, JSONObject.class);
            if (response == null || response.getIntValue("code") != 200) {
                throw new RuntimeException("tag item delete failed!");
            }
        }
    }

    public JSONArray getTagsOfIssue(String issueId){
        return  restTemplate.getForObject(tagServicePath +  "/inner/tags" + "?item_id=" + issueId, JSONArray.class);
    }

    public JSONArray getSpecificTaggedIssueIds(String ...tagIds){
        return restTemplate.postForObject(tagServicePath +  "/inner/tags" + "/item-ids", tagIds, JSONArray.class);
    }
    public JSONArray getSpecificTaggedIssueIds(JSONArray tagIds){
        return restTemplate.postForObject(tagServicePath +  "/inner/tags" + "/item-ids", tagIds, JSONArray.class);
    }
    public JSONArray getSpecificTaggedIssueIds(List<String> tagIds){
        return restTemplate.postForObject(tagServicePath +  "/inner/tags" + "/item-ids", tagIds, JSONArray.class);
    }

    public JSONArray getSolvedIssueIds(List<String> tag_ids){
        return restTemplate.postForObject(tagServicePath +  "/inner/tags" + "/item-ids", tag_ids, JSONArray.class);
    }


    //----------------------------------------------------end--------------------------------------------------------

    //-----------------------------------------------project service-------------------------------------------------

    public JSONArray getRepoIdsOfAccount(String account_id,String type) {
        return restTemplate.getForObject(projectServicePath + "/inner/project/repo-ids?account_id=" + account_id+"&type="+type, JSONArray.class);
    }

    public String getRepoIdOfProject(String projectId) {
        return restTemplate.getForObject(projectServicePath + "/inner/project/repo-uuid?project_uuid=" + projectId, String.class);
    }

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
        JSONObject result = JSONObject.parseObject(body);

        return result;
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

    @Deprecated
    public JSONObject getOneCommitByCommitId(String commitId){

        JSONObject result = null;
        int tryCount = 0;
        while (tryCount < 5) {

            try{
                result = restTemplate.getForObject(commitServicePath+"/"+commitId,JSONObject.class);
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
        return result;


    }

    public JSONObject checkOut(String repo_id,String commit_id){
        return restTemplate.getForObject(commitServicePath + "/checkout?repo_id=" + repo_id + "&commit_id=" + commit_id, JSONObject.class);
    }

    public JSONObject getCommitTime(String commitId,String repoId){
        return restTemplate.getForObject(commitServicePath+"/commit-time?commit_id="+commitId+"&repo_id="+repoId,JSONObject.class);
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

        JSONObject result = null;
        int tryCount = 0;
        while (tryCount < 5) {

            try{


                result = restTemplate.getForObject(url, JSONObject.class);
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
        return result;
    }

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

    //-----------------------------------recommendation service---------------------------------------------------------
    public void addSolvedIssueInfo(List<JSONObject> solvedInfos){
        try{
            restTemplate.postForObject(recommendationServicePath + "/add-bug-recommendation",solvedInfos,JSONObject.class);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public JSONObject getRepoById(String repoId) {
        return restTemplate.getForObject(repoServicePath + "/" + repoId, JSONObject.class);
    }



    public Map<String, String> getDeveloperByCommits(Set<String> keySet) {
        //restTemplate.postForObject(repoServicePath + "/developerListsByCommits", keySet ,HashMap.class);
/*        Map<String,String> s = new HashMap<>();
        s.put("7e89cfe4d854e5c1b00d6f01b3790ba2d3c9738a", "tatu.saloranta@iki.fi");
        s.put("99d90d43bd14d7b1262e5b32f3fb14355dab220d", "tatu.saloranta@iki.fi");
        s.put("6cfdce3eed883e10b0c67ce4b4e7738cfcb1fc7b", "tatu.saloranta@iki.fi");
        s.put("b712954e0a4d7bf86f470d123f1768e07f14d6c3", "tatu.saloranta@iki.fi");
        s.put("105102ba5bbcdd8cd752f9a9dd820164132688e3", "tatu.saloranta@iki.fi");
        s.put("50905423394bbcf2d6df9d86a1472b81db3b6d62", "tatu.saloranta@iki.fi");
        s.put("c2b69429e5f8791022b3b6c1bbb585592983f880", "tatu.saloranta@iki.fi");
        s.put("feddb66f98874a1022c74a7b0bc5b550dc7236e1", "tatu.saloranta@iki.fi");
        s.put("acda0f9e69043dfcf0a6758ad4dee0af1de3b4ac", "tatu.saloranta@iki.fi");
        s.put("e7551ed0153df46ee7324567fd4dc8ce8afe7aff", "tatu.saloranta@iki.fi");
        s.put("569c9e9ec6e53ba54013d254825cf293257edbf6", "tatu.saloranta@iki.fi");
        s.put("d708338d421df111ab3d4a36bb90b14900594ec0", "tatu.saloranta@iki.fi");
        s.put("3b4b0a174949ddc83bbba5d74283a243c866844b", "tatu.saloranta@iki.fi");
        s.put("fdf1663ef024c89535aedef8f890a34938db8c4c", "tatu.saloranta@iki.fi");
        s.put("d8bed348eb375baa4a2dff933fdac1160ad35f67", "doug.roper@rallyhealth.com");
        s.put("24f65a28db07467ae9d3b8c5e765e7783067cf06", "tatu.saloranta@iki.fi");
        s.put("9b53cf5e214aa55f4eebee9e61cb25af21e35ec1", "tatu.saloranta@iki.fi");
        s.put("465fd8e3ef598abf919feeb01577376b492558a0", "tatu.saloranta@iki.fi");
        s.put("a8eb65dd6d4da0faf8b329de8fcf53ecd4c2fa8a", "tatu.saloranta@iki.fi");*/
        StringBuffer stringBuffer = new StringBuffer();
        for (String s : keySet) {
            stringBuffer.append(s);
            stringBuffer.append(",");
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<>(1);
        requestEntity.add("key_set", stringBuffer.toString());
        JSONObject jsonObject = restTemplate.postForObject(commitServicePath + "/developer-lists-by-commits", requestEntity,JSONObject.class);
        return jsonObject.getObject("data",Map.class);
    }

    public Map getRepoAndLatestScannedCommit(Set repoList) {
        return null;
    }

    public Map getRepoAndCodeLine(Map repoCommit) {
        return null;
    }

/*    public List<String> getScanCommitsIdByDuration(String detail, String start, String end) {
        return null;
    }*/


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
        int sourceListSize = sourcesList.size();
        Map<List<String>,String> regexAndReplaceStr = new HashMap<>();
        regexAndReplaceStr.put(Arrays.asList("<span[^>]*>","</span[^>]*>"),"");
        regexAndReplaceStr.put(Arrays.asList("&lt;"),"<");
        regexAndReplaceStr.put(Arrays.asList("&gt;"),">");
        regexAndReplaceStr.put(Arrays.asList("&amp;"),"&");
        for (int i = 0; i < sourceListSize; i++) {
            String code = (String) ((List<Object>)(sourcesList.get(i))).get(1);
            code = RegexUtil.getNoTagCode(code,regexAndReplaceStr);
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



    public JSONObject getSonarIssueType(String repositories,String status,int page , int ps){

        Map<String, String> map = new HashMap<>();

        String baseRequestUrl = sonarServicePath + "/api/rules/search?repositories={repositories}&status={status}&p={p}&ps={ps}";
        map.put("repositories",repositories);
        map.put("status",status);
        map.put("p",String.valueOf(page));
        map.put("ps",String.valueOf(ps));
        try{
            ResponseEntity entity = restTemplate.getForEntity(baseRequestUrl,JSONObject.class,map);
            return JSONObject.parseObject(entity.getBody().toString());
        }catch (RuntimeException e){
            e.printStackTrace();
            logger.error("componentKey : {}  ----> request sonar  source rule  api failed , repositories --> {} , status --> {}", repositories,status);
            return null;
        }
    }

    //------------------------------------------------------scan api ---------------------------------------------


    public JSONObject getScanByCategoryAndRepoIdAndCommitId(String repoId,String commitId ,String category){
        return restTemplate.getForObject(scanServicePath + "/inner/scan/commit?repo_id=" + repoId+"&commit_id="+commitId+"&category="+category, JSONObject.class);
    }

    public List<String> getPreScannedCommitByCurrentCommit(String repoId,String commitId ,String category){
        JSONArray preCommits = restTemplate.getForObject(scanServicePath + "/inner/scan/pre-scanned-commit?repo_id=" + repoId+"&commit_id="+commitId+"&category="+category, JSONArray.class);
        List<String> parentCommits = new ArrayList<>();
        if(preCommits != null){
            parentCommits = preCommits.toJavaList(String.class);
        }


        return parentCommits;
    }

    public String getLatestScanFailedCommitId(String repoId,String commitId ,String category){
        String failedCommitId = restTemplate.getForObject(scanServicePath + "/inner/scan/pre-failed-commit?repo_id=" + repoId+"&commit_id="+commitId+"&category="+category, String.class);
        if(failedCommitId != null){
            return failedCommitId;
        }
        return null;

    }


    public JSONArray getScanByRepoIdAndStatus(String repoId,String status){
        JSONArray scans = restTemplate.getForObject(scanServicePath + "/inner/scan/get-by-status?repo_id=" + repoId+"&status=" + status, JSONArray.class);
        if(scans != null){
            return scans;
        }
        return null;

    }



    // --------------------------------------------------------measure api ---------------------------------------------------------


    public JSONObject getCodeChangesByDurationAndDeveloperName(String developerName,String since ,String until,String category,String repoId){

        HttpHeaders headers = new HttpHeaders();
        headers.add("token",null);
        HttpEntity request = new HttpEntity(headers);
        StringBuilder urlBuilder = new StringBuilder();
        boolean isFirstPram =true;
        urlBuilder.append(measureServicePath + "/measure/developer/code-change?");
        if(developerName != null){
            if(!isFirstPram){
                urlBuilder.append("&");
            }else{
                isFirstPram=false;
            }
            urlBuilder.append("developer_name=" + developerName);
        }

        if(since != null){
            if(!isFirstPram){
                urlBuilder.append("&");
            }else{
                isFirstPram=false;
            }
            urlBuilder.append("since=" + since);
        }
        if(until != null){
            if(!isFirstPram){
                urlBuilder.append("&");
            }else{
                isFirstPram=false;
            }
            urlBuilder.append("until=" + until);
        }
        if(category != null){
            if(!isFirstPram){
                urlBuilder.append("&");
            }else{
                isFirstPram=false;
            }
            urlBuilder.append("category=" + category);
        }
        if(repoId != null){
            if(!isFirstPram){
                urlBuilder.append("&");
            }else{
                isFirstPram=false;
            }
            urlBuilder.append("repo_id=" + repoId);
        }
        String url = urlBuilder.toString();
        ResponseEntity responseEntity = restTemplate.exchange(url , HttpMethod.GET,request,JSONObject.class);
        String body = responseEntity.getBody().toString();
        JSONObject result = JSONObject.parseObject(body,JSONObject.class);
        return result;
    }


    public JSONObject getDeveloperListByDuration(String developerName,String since ,String until,String repoId){
        HttpHeaders headers = new HttpHeaders();
        headers.add("token",null);
        HttpEntity request = new HttpEntity(headers);
        StringBuilder urlBuilder = new StringBuilder();
        boolean isFirstPram =true;
        urlBuilder.append(measureServicePath + "/measure/repository/duration?");
        urlBuilder.append("repo_id=" + repoId);
        urlBuilder.append("&since=" + since);
        urlBuilder.append("&until=" + until);

        if(developerName != null){
            urlBuilder.append("&developer_name=" + developerName);
        }

        String url = urlBuilder.toString();
        ResponseEntity responseEntity = restTemplate.exchange(url , HttpMethod.GET,request,JSONObject.class);
        String body = responseEntity.getBody().toString();
        JSONObject result = JSONObject.parseObject(body,JSONObject.class);
        if(result == null){
            logger.error("request /measure/repository/duration failed");
        }
        return result;
    }

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
