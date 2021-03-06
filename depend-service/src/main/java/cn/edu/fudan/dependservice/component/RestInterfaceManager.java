package cn.edu.fudan.dependservice.component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
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

import javax.security.auth.message.AuthException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author WZY
 * @version 1.0
 **/
@Component
@Slf4j
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
    @Value("${code.tracker.path}")
    private String codeTrackerServicePath;
    @Value("${test.repo.path}")
    private String testProjectPath;
    private final RestTemplate restTemplate;

    private static final String PROJECT_NAME = "projectName";
    private static final String REPO_NAME = "repoName";
    private static final String REPO_UUID = "repoUuid";
    private static final String TOKEN = "token";
    private static final String REPO_LIST = "repoList";
    private static final String DATA = "data";
    private static final String REPO_ID = "repo_id";

    public RestInterfaceManager(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    //------------------------------------------------account service-----------------------------------------------------------

    public void userAuth(String userToken) throws AuthException {
        JSONObject result = restTemplate.getForObject(accountServicePath + "/user/auth/" + userToken, JSONObject.class);
        log.info("result: ");
        log.info(result.toJSONString());
        if (result == null || result.getIntValue("code") != 200) {
            new AuthException("auth failed!").printStackTrace();
        }
    }

    public List<String> getDeveloperInRepo(String repoUuids, String since, String until) {
        List<String> developers = new ArrayList<>();
        String url = since == null ? accountServicePath + "/user/developers?repo_uuids=" + repoUuids + "&is_whole=true" :
                accountServicePath + "/user/developers?repo_uuids=" + repoUuids + "&is_whole=true&since=" + since + "&until=" + until;
        JSONObject result = restTemplate.getForObject(url, JSONObject.class);
        assert result != null;
        JSONArray rows = result.getJSONArray(DATA);
        for (Object row : rows) {
            JSONObject developer = (JSONObject) row;
            developers.add(developer.getString("developerName"));
        }
        return developers;
    }

    //-----------------------------------------------project service-------------------------------------------------

    /**
     * ??????repo_uuid???????????????project
     *
     * @param repoUuid ?????????uuid
     * @return Map<String, String> ????????? ???????????? ????????? ?????????uuid
     */
    public Map<String, String> getProjectByRepoId(String repoUuid) {

        JSONObject projectInfo = Objects.requireNonNull(restTemplate.getForObject(projectServicePath + "/inner/project?repo_uuid=" + repoUuid, JSONObject.class)).getJSONObject(DATA);

        Map<String, String> result = new HashMap<>(8);

        if (projectInfo != null) {
            result.put(PROJECT_NAME, projectInfo.getString(PROJECT_NAME));
            result.put(REPO_NAME, projectInfo.getString(REPO_NAME));
            result.put("branch", projectInfo.getString("branch"));
            result.put(REPO_UUID, projectInfo.getString(REPO_UUID));
        }

        return result;
    }

    /**
     * ????????????project?????????repo uuid???name
     *
     * @param userToken userToken
     * @return ??????repo uuid???name
     */
    public JSONObject getAllRepo(String userToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.add(TOKEN, userToken);
        HttpEntity<HttpHeaders> request = new HttpEntity<>(headers);
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(projectServicePath + "/project/all", HttpMethod.GET, request, JSONObject.class);

        return Objects.requireNonNull(responseEntity.getBody()).getJSONObject(DATA);
    }

    public Map<String, String> getAllRepoToRepoName(String userToken) {
        Map<String, String> repoName = new HashMap<>(64);

        JSONObject allRepo = getAllRepo(userToken);
        for (String projectName : allRepo.keySet()) {
            Iterator<Object> iterator = allRepo.getJSONArray(projectName).stream().iterator();
            while (iterator.hasNext()) {
                JSONObject next = (JSONObject) iterator.next();
                repoName.put(next.getString(REPO_ID), next.getString("name"));
            }
        }

        return repoName;
    }

    public Map<String, Map<String, String>> getAllRepoToProjectName(String userToken) {
        Map<String, Map<String, String>> projectName = new HashMap<>(64);
        JSONObject allRepo = getAllRepo(userToken);
        for (String repo : allRepo.keySet()) {
            Iterator<Object> iterator = allRepo.getJSONArray(repo).stream().iterator();
            while (iterator.hasNext()) {
                JSONObject next = (JSONObject) iterator.next();
                projectName.put(next.getString(REPO_ID), new HashMap<String, String>(4) {{
                    put(REPO_NAME, next.getString("name"));
                    put(PROJECT_NAME, repo);
                }});
            }
        }

        return projectName;
    }

    /**
     * ??????url??????repoUuid
     *
     * @param url       url
     * @param userToken userToken
     * @return repoUuid
     */
    public String getRepoUuidByUrl(String url, String userToken) {

        if (StringUtils.isEmpty(url)) {
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(TOKEN, userToken);
        HttpEntity<HttpHeaders> request = new HttpEntity<>(headers);
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(projectServicePath + "/project", HttpMethod.GET, request, JSONObject.class);
        JSONArray reposDetail = Objects.requireNonNull(responseEntity.getBody()).getJSONArray(DATA);

        for (int i = 0; i < reposDetail.size(); i++) {
            JSONObject repoDetail = reposDetail.getJSONObject(i);
            if (url.equals(repoDetail.get("url").toString())) {
                return repoDetail.get(REPO_UUID).toString();
            }
        }

        return null;
    }

    public List<String> getAllRepoUuidsByProjectName(String userToken, String projectIds) {
        List<String> repoUuids = new ArrayList<>();
        HttpHeaders headers = new HttpHeaders();
        headers.add(TOKEN, userToken);
        HttpEntity<HttpHeaders> request = new HttpEntity<>(headers);
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(projectServicePath + "/project/all?project_names=" + projectIds, HttpMethod.GET, request, JSONObject.class);
        JSONObject repoDetails = Objects.requireNonNull(responseEntity.getBody()).getJSONObject(DATA);
        for (Object value : repoDetails.values()) {
            JSONArray repoDetail = (JSONArray) value;
            for (int i = 0; i < repoDetail.size(); i++) {
                repoUuids.add(repoDetail.getJSONObject(i).getString(REPO_ID));
            }
        }
        return repoUuids;
    }
    public boolean deleteRecall(String repoId, String token) {
        String path =  projectServicePath + "/repo?service_name=DEPENDENCY&repo_uuid=" + repoId;
        HttpHeaders headers = new HttpHeaders();
        String defaulttoken = "ec15d79e36e14dd258cfff3d48b73d35";
        headers.add("token", defaulttoken);
        HttpEntity<HttpHeaders> request = new HttpEntity<>(headers);
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(path, HttpMethod.PUT, request, JSONObject.class);
        log.info(responseEntity.toString());
        return Objects.requireNonNull(responseEntity.getBody()).getIntValue("code") == 200;
    }

    //---------------------------------------------commit service------------------------------------------------------

    public String getFirstCommitDate(String developerName) {
        JSONObject data = Objects.requireNonNull(restTemplate.getForObject(commitServicePath + "/first-commit?author=" + developerName, JSONObject.class)).getJSONObject(DATA);
        LocalDateTime fistCommitDate = LocalDateTime.parse(data.getJSONObject("repos_summary").getString("first_commit_time_summary"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return fistCommitDate.plusHours(8).toLocalDate().toString();
    }

    //---------------------------------------------code service---------------------------------------------------------

    public JSONObject getRepoPath(String repoId, String commitId) {
        JSONObject repoPath = null;
        int tryCount = 0;
        while (tryCount < 5) {

            try {
                JSONObject response = restTemplate.getForObject(codeServicePath + "?repo_id=" + repoId + "&commitId=" + commitId, JSONObject.class);
                if (response != null) {
                    repoPath = response;
                } else {
                    logger.error("code service response null!");
                }
                break;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    TimeUnit.SECONDS.sleep(20);
                } catch (Exception sleepException) {
                    e.printStackTrace();
                }

                tryCount++;
            }
        }
        return repoPath;

    }

    public String getRepoPath(String repoId) {
        if (testProjectPath != null && !"false".equals(testProjectPath)) {
            return testProjectPath;
        }

        String repoPath = null;
        int tryCount = 0;
        while (tryCount < 5) {

            try {
                String urlPath = codeServicePath + "?repo_id=" + repoId;
                logger.debug(urlPath);
                JSONObject response = restTemplate.getForObject(urlPath, JSONObject.class);
                if (response != null && response.getJSONObject(DATA) != null && "Successful".equals(response.getJSONObject(DATA).getString("status"))) {
                    repoPath = response.getJSONObject(DATA).getString("content");
                } else {
                    logger.error("code service response null!");
                    logger.error("request url is : {}", urlPath);
                }
                break;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    TimeUnit.SECONDS.sleep(20);
                } catch (Exception sleepException) {
                    e.printStackTrace();
                }

                tryCount++;
            }
        }
        return repoPath;
    }

    public void freeRepoPath(String repoId, String repoPath) {
        try {
            if (testProjectPath != null && !"false".equals(testProjectPath)) {
                return;
            }
            if (repoPath == null || repoId == null) {
                return;
            }
            restTemplate.getForObject(codeServicePath + "/free?repo_id=" + repoId + "&path=" + repoPath, JSONObject.class);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    //--------------------------------------------------------sonar api -----------------------------------------------------

    public JSONObject getSonarIssueResults(String repoName, String type, int pageSize, boolean resolved, int page) {
        Map<String, String> map = new HashMap<>(16);
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(sonarServicePath).append("/api/issues/search?componentKeys={componentKeys}&additionalFields={additionalFields}&s={s}&resolved={resolved}");
        map.put("additionalFields", "_all");
        map.put("s", "FILE_LINE");
        map.put("componentKeys", repoName);
        map.put("resolved", String.valueOf(resolved));
        if (type != null) {
            String[] types = type.split(",");
            StringBuilder stringBuilder = new StringBuilder();
            for (String typeSb : types) {
                if ("CODE_SMELL".equals(typeSb) || "BUG".equals(typeSb) || "VULNERABILITY".equals(typeSb) || "SECURITY_HOTSPOT".equals(typeSb)) {
                    stringBuilder.append(typeSb).append(",");
                }
            }
            if (!stringBuilder.toString().isEmpty()) {
                urlBuilder.append("&componentKeys={componentKeys}");
                String requestTypes = stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1);
                map.put("types", requestTypes);
            } else {
                logger.error("this request type --> {} is not available in sonar api", type);
                return null;
            }
        }

        if (page > 0) {
            urlBuilder.append("&p={p}");
            map.put("p", String.valueOf(page));
        }
        if (pageSize > 0) {
            urlBuilder.append("&ps={ps}");
            map.put("ps", String.valueOf(pageSize));
        }

        String url = urlBuilder.toString();

        try {
            ResponseEntity<JSONObject> entity = restTemplate.getForEntity(url, JSONObject.class, map);
            return JSONObject.parseObject(Objects.requireNonNull(entity.getBody()).toString());
        } catch (RuntimeException e) {
            logger.error("repo name : {}  ----> request sonar api failed", repoName);
            throw e;
        }


    }

    public JSONObject getRuleInfo(String ruleKey, String actives, String organizationKey) {
        Map<String, String> map = new HashMap<>(64);

        String baseRequestUrl = sonarServicePath + "/api/rules/show";
        if (ruleKey == null) {
            logger.error("ruleKey is missing");
            return null;
        } else {
            map.put("key", ruleKey);
        }
        if (actives != null) {
            map.put("actives", actives);
        }
        if (organizationKey != null) {
            map.put("organization", organizationKey);
        }

        try {
            return restTemplate.getForObject(baseRequestUrl + "?key=" + ruleKey, JSONObject.class);
        } catch (RuntimeException e) {
            logger.error("ruleKey : {}  ----> request sonar  rule infomation api failed", ruleKey);
            throw e;
        }

    }

    public JSONObject getSonarAnalysisTime(String projectName) {
        JSONObject error = new JSONObject();
        error.put("errors", "Component key " + projectName + " not found");

        try {
            String urlPath = sonarServicePath + "/api/components/show?component=" + projectName;
            logger.debug(urlPath);
            return restTemplate.getForObject(urlPath, JSONObject.class);
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error("projectName: {} ---> request sonar api failed ????????????????????????API ??????", projectName);
        }

        return error;
    }

    // --------------------------------------------------------measure api ---------------------------------------------------------



    // --------------------------------------------------------codeTracker api ---------------------------------------------------------

    public JSONObject getMethodTraceHistory(String meteUuid, String token) {
        HttpEntity<HttpHeaders> request = new HttpEntity<>(new HttpHeaders() {{
            add(TOKEN, token);
        }});
        return Objects.requireNonNull(restTemplate.exchange(codeTrackerServicePath + "/history/issue/method/meta?meta_uuid=" + meteUuid + "&level=METHOD", HttpMethod.GET, request, JSONObject.class).getBody()).getJSONObject(DATA);
    }
}
