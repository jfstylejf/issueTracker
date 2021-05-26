package cn.edu.fudan.projectmanager.component;

import cn.edu.fudan.projectmanager.domain.ResponseBean;
import cn.edu.fudan.projectmanager.domain.dto.UserInfoDTO;
import cn.edu.fudan.projectmanager.exception.AuthException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class RestInterfaceManager {

    @Value("${account.service.path}")
    private String accountServicePath;
    @Value("${repository.service.path}")
    private String repoServicePath;
    @Value("${commit.service.path}")
    private String commitServicePath;
    @Value("${clone.service.path}")
    private String cloneServicePath;
    @Value("${tag.service.path}")
    private String tagServicePath;
    @Value("${codeTracker.service.path}")
    private String codeTrackerServicePath;
    @Value("${issue.service.path}")
    private String issueServicePath;
    @Value("${measure.service.path}")
    private String measureServicePath;
    @Value("${codeTracker.service.path}")
    private String codetrackerServicePath;
    @Value("${scan.service.path}")
    private String scanServicePath;
    @Value("${dependency.service.path}")
    private String dependencyServicePath;

    private RestTemplate restTemplate;

    public RestInterfaceManager(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    //----------------------------------delete repo----------------------------------------------------
    public boolean deleteCloneRepo(String repoUuid) {
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(cloneServicePath + "/cloneScan/" + repoUuid, HttpMethod.DELETE, null, JSONObject.class);
        JSONObject body = exchange.getBody();
        assert body != null;
        return body.getIntValue("code") == 200;
    }

    public boolean deleteIssueRepo(String repoUuid) {
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(issueServicePath + "/issue/" + repoUuid, HttpMethod.DELETE, null, JSONObject.class);
        JSONObject body = exchange.getBody();
        assert body != null;
        return body.getIntValue("code") == 200;
    }

    public boolean deleteMeasureRepo(String repoUuid) {
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(measureServicePath + "/measure/repo/" + repoUuid, HttpMethod.DELETE, null, JSONObject.class);
        JSONObject body = exchange.getBody();
        assert body != null;
        return body.getIntValue("code") == 200;
    }

    public boolean deleteCodetrackerRepo(String repoUuid) {
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(codetrackerServicePath + "/codetracker/" + repoUuid, HttpMethod.DELETE, null, JSONObject.class);
        JSONObject body = exchange.getBody();
        assert body != null;
        return body.getIntValue("code") == 200;
    }

    public boolean deleteScanRepo(String token, String repoUuid) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token", token);
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(scanServicePath + "/scan/" + repoUuid, HttpMethod.DELETE, httpEntity, JSONObject.class);
        JSONObject body = exchange.getBody();
        assert body != null;
        return body.getIntValue("code") == 200;
    }

    public boolean deleteCommitRepo(String repoUuid) {
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(commitServicePath + "/repository/" + repoUuid, HttpMethod.DELETE, null, JSONObject.class);
        JSONObject body = exchange.getBody();
        assert body != null;
        return body.getIntValue("code") == 200;
    }

    public boolean deleteDependencyRepo(String repoUuid) {
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(dependencyServicePath + "/depend/" + repoUuid, HttpMethod.DELETE, null, JSONObject.class);
        JSONObject body = exchange.getBody();
        assert body != null;
        return body.getIntValue("code") == 200;
    }


    //----------------------------------account service----------------------------------------------------
    public String getAccountId(String userToken) {
        Map<String, String> urlParameters = new HashMap<>();
        urlParameters.put("userToken", userToken);
        return restTemplate.getForObject(accountServicePath + "/user/accountUuid?userToken={userToken}", String.class, urlParameters);
    }

    public String getAccountName(String accountId) {
        return restTemplate.getForObject(accountServicePath + "/user/accountName?accountUuid=" + accountId, String.class);
    }

    public boolean userAuth(String userToken) throws AuthException {
        JSONObject result = restTemplate.getForObject(accountServicePath + "/user/auth/" + userToken, JSONObject.class);
        if (result == null || result.getIntValue("code") != 200) {
            log.error("auth failed!");
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public UserInfoDTO getUserInfoByToken(String token) {
        ResponseBean result = restTemplate.getForObject(accountServicePath + "/user/right/" + token, ResponseBean.class);
        if (result == null) {
            log.error("Response is null");
            return null;
        }

        if (result.getCode() != 200) {
            log.error(result.getMsg());
            return null;
        }
        Map<String, Object> data = (Map<String, Object>) result.getData();
        return new UserInfoDTO(token, (String) data.get("uuid"), (Integer) data.get("right"));
    }


    //-----------------------------------repo service--------------------------------------------------------
    public JSONObject getRepoById(String repoId) {
        return restTemplate.getForObject(repoServicePath + "/" + repoId, JSONObject.class);
    }

    public void deleteIgnoreRecord(String account_id, String repoId) {
        restTemplate.delete(tagServicePath + "/inner/tags/ignore?repo-id=" + repoId + "&account-id=" + account_id);
    }

    public void deleteCodeTrackerOfRepo(String branch, String repoId) {
        restTemplate.delete(codeTrackerServicePath + "/codetracker?repoUuid=" + repoId + "&branch=" + branch, JSONObject.class);
    }
}
