package cn.edu.fudan.common.component;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class RepoRestManager {
    protected RestTemplate restTemplate;
    protected String codeServiceRepoPath;

    private static final String FREE_REPO_PATH = "/free";
    private static final String beginWithRepoParam = "?repo_id=";

    public String getCodeServiceRepo(String repoId) {
        JSONObject data = (Optional.ofNullable(this.restTemplate.getForObject(this.codeServiceRepoPath + beginWithRepoParam + repoId, JSONObject.class, new Object[0])).orElse(new JSONObject())).getJSONObject("data");
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
                .getForObject(this.codeServiceRepoPath + FREE_REPO_PATH + beginWithRepoParam + repoId + "&path=" + path, JSONObject.class, new Object[0]))
                .orElse(new JSONObject())).getJSONObject("data")
                .getString("status");
    }

}

