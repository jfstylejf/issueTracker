package cn.edu.fudan.common.component;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
@Service
public class RestInterfaceManager {
    private static final Logger log = LoggerFactory.getLogger(RestInterfaceManager.class);
    private String codeServiceRepoPath;
    private String codeServiceRepoFreePath;
    private RestTemplate restTemplate;
    private String beginWithRepoParam = "?repo_id=";

    public RestInterfaceManager(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RestInterfaceManager() {
    }

    public String getCodeServiceRepo(String repoId) {
        JSONObject data = ((JSONObject) Optional.ofNullable(this.restTemplate.getForObject(this.codeServiceRepoPath + this.beginWithRepoParam + repoId, JSONObject.class, new Object[0])).orElse(new JSONObject())).getJSONObject("data");
        if (data == null) {
            return null;
        } else {
            String status = data.getString("status");
            String content = data.getString("content");
            if ("Successful".equals(status)) {
                return content;
            } else {
                log.error(content);
                return null;
            }
        }
    }

    public void freeRepo(String repoId, String path) {
        (Optional.ofNullable(this.restTemplate
                .getForObject(this.codeServiceRepoFreePath + this.beginWithRepoParam + repoId + "&path=" + path, JSONObject.class, new Object[0]))
                .orElse(new JSONObject())).getJSONObject("data")
                .getString("status");
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void setCodeServiceRepoPath(final String codeServiceRepoPath) {
        this.codeServiceRepoPath = codeServiceRepoPath;
    }

    public void setCodeServiceRepoFreePath(final String codeServiceRepoFreePath) {
        this.codeServiceRepoFreePath = codeServiceRepoFreePath;
    }

    public void setBeginWithRepoParam(final String beginWithRepoParam) {
        this.beginWithRepoParam = beginWithRepoParam;
    }

    public String getCodeServiceRepoPath() {
        return this.codeServiceRepoPath;
    }

    public String getCodeServiceRepoFreePath() {
        return this.codeServiceRepoFreePath;
    }

    public RestTemplate getRestTemplate() {
        return this.restTemplate;
    }

    public String getBeginWithRepoParam() {
        return this.beginWithRepoParam;
    }
}

