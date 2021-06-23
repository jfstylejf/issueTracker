package cn.edu.fudan.taskmanagement.component;

import cn.edu.fudan.taskmanagement.JiraDao.UserInfoDTO;
import cn.edu.fudan.taskmanagement.domain.ResponseBean;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson.JSONObject;
import javax.validation.constraints.Null;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

/**
 * todo 后续的JSONArray JSONObject 一律换成定义的对象 用于与具体的任务管理系统解耦
 *
 * @author zyh
 * @date 2020/7/2
 */

@Slf4j
@Component
public class JiraAPI {

    @Value("${server.ip}")
    private  String serverIp;

    @Value("${jira.base.url}")
    private String jiraBaseUrl;

    @Value("${jira.username}")
    private String username;

    @Value("${jira.password}")
    private String password;

    @Value("${server.port}")
    private String serverPort;

    @Value("${server.host}")
    private String serverHost;

    @Value("${account.service.path}")
    private String accountServicePath;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 根据jira api，请求头中需要使用base64加密
     * @return
     */
    private HttpHeaders getHeaders(){
        byte[] bytes = (username + ":" + password).getBytes();
        String encoded = Base64.getEncoder().encodeToString(bytes);
        encoded = "Basic " + encoded;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization",encoded);
        return headers;
    }

    public JSONObject getTaskByJql(String jql){
        HttpEntity httpEntity = new HttpEntity(getHeaders());
        String url = jiraBaseUrl + "/rest/api/2/search?jql=" + jql;
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity, JSONObject.class);
        return exchange.getBody();
    }

    public  JSONArray getRepoIdByDeveloper(String developer){
        //此处可能有问题
        StringBuilder url = new StringBuilder(serverHost+":8000").append("/measure/developer/involvedRepoList");
        if(developer != null){
            url.append("?developer=").append(developer);

        }
        JSONObject response = restTemplate.getForObject(url.toString(), JSONObject.class);
        if(response.getIntValue("code") == 200){
            return response.getJSONArray("data");
        }
        return new JSONArray();
    }

//    -------------------------------------------jira API-------------------------------------------
//RUSSIAN SPECIALITY, OPTIMIZATION REQUIRED
//    public  JSONArray getJiraInfoByKey(String type, String keyword){
//        //此处可能有问题
//        StringBuilder url = new StringBuilder(serverHost+":8000").append("/jira/jql");
//        if(keyword != null){
//            url.append("?keyword=").append(keyword);
//        }
//        if(type != null){
//            url.append("&type=").append(type);
//        }
//        JSONObject response = restTemplate.getForObject(url.toString(), JSONObject.class);
//        if(response.getIntValue("code") == 200){
//            return response.getJSONArray("data");
//        }
//        return null;
//    }

    public JSONObject getFirstCommitDate( String developer) {
        JSONObject response = restTemplate.getForObject(serverHost+":8102/commit/first-commit" + "?author=" + developer, JSONObject.class);
        return response.getJSONObject("data");

    }


    public  JSONArray getRepoUuidByDeveloper(String developer){
        //此处可能有问题
        StringBuilder url = new StringBuilder(serverHost+":8000").append("/measure/developer/involvedRepoList");
        if(developer != null){
            url.append("?developer=").append(developer);

        }
        JSONObject response = restTemplate.getForObject(url.toString(), JSONObject.class);
        if(response.getIntValue("code") == 200){
            return response.getJSONArray("data");
        }
        return new JSONArray();
    }

    public UserInfoDTO getUserInfoByToken(String token) {
        Objects.requireNonNull(token);
        ResponseBean result = restTemplate.getForObject(accountServicePath + "/user/right/" + token, ResponseBean.class);
        if (result == null) {
            log.error("Response is null");
            return null;
        }

        if (result.getCode() != 200) {
            log.error(result.getMsg());
            return null;
        }
        Map<String,Object> data =  (Map<String, Object>) result.getData();
        return new UserInfoDTO(token, (String) data.get("uuid"), (Integer) data.get("right"));
    }
}

