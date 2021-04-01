package cn.edu.fudan.common.component;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
public abstract class BaseRepoRestManager {
    protected RestTemplate restTemplate;
    protected String codeServiceRepoPath;

    private static final String FREE_REPO_PATH = "/free";
    private static final String beginWithRepoParam = "?repo_id=";

    public String getCodeServiceRepo(String repoId) {
        JSONObject data = (Optional.ofNullable(this.restTemplate.getForObject(this.codeServiceRepoPath + beginWithRepoParam + repoId, JSONObject.class)).orElse(new JSONObject())).getJSONObject("data");
        log.info(data.toJSONString());
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
                .getForObject(this.codeServiceRepoPath + FREE_REPO_PATH + beginWithRepoParam + repoId + "&path=" + path, JSONObject.class))
                .orElse(new JSONObject())).getJSONObject("data")
                .getString("status");
    }

    public BaseRepoRestManager(final RestTemplate restTemplate, final String codeServiceRepoPath) {
        this.restTemplate = restTemplate;
        this.codeServiceRepoPath = codeServiceRepoPath;
    }

}

