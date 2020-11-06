package cn.edu.fudan.cloneservice.component;

import cn.edu.fudan.cloneservice.domain.CommitInfo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author WZY
 * @version 1.0
 **/
@Slf4j
@Component
public class RestInterfaceManager {


    @Value("${code.service.path}")
    private String codeServicePath;
    @Value("${repository.service.path}")
    private String repoServicePath;
    @Value("${commit.service.path}")
    private String commitServicePath;
    @Value("${project.service.path}")
    private String projectServicePath;
    @Value("${issue.service.path}")
    private String issueServicePath;
    @Value("${measure.service.path}")
    private String measureServicePath;

    private RestTemplate restTemplate;

    public RestInterfaceManager(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    //---------------------------------------------code service---------------------------------------------------------
    public String getRepoPath(String repoId){
        String repoPath=null;
        JSONObject response=restTemplate.getForObject(codeServicePath + "?repo_id=" + repoId, JSONObject.class).getJSONObject("data");
        if (response != null ){
            if(response.getString("status").equals("Successful")) {
                repoPath=response.getString("content");
                log.info("repoHome -> {}" ,repoPath);
            }else{
                log.error("get repoHome fail -> {}",response.getString("content"));
                log.error("repoId -> {}",repoId);
            }
        } else {
            log.error("code service response null!");
        }
        return repoPath;
    }

    public JSONObject freeRepoPath(String repoId,String repoPath){
        JSONObject response=restTemplate.getForObject(codeServicePath + "/free?repo_id=" + repoId+"&path="+repoPath, JSONObject.class);
        if(response!=null&&response.getJSONObject("data").getString("status").equals("Successful")){
            log.info("{} -> free success",repoPath);
        }else {
            log.warn("{} -> free failed",repoPath);
        }
        return response;
    }

    public JSONObject getRepoById(String repoId){
        return restTemplate.getForObject(repoServicePath + "/" + repoId, JSONObject.class);
    }

    public String getRepoPath1(String repoId) {
        JSONObject jsonObject = restTemplate.getForObject(codeServicePath + "?repo_id=" + repoId, JSONObject.class);

        return jsonObject.getJSONObject("data").getString("content");
    }

    public int getAddLines(String repoId, String start, String end, String developer){
        int addLines = 0;
        JSONObject response = restTemplate.getForObject(measureServicePath + "/repository/duration?repo_uuid=" + repoId +
                "&since=" + start + "&until=" + end, JSONObject.class);
        List<CommitInfo> list = response.getJSONObject("data").getJSONArray("commitInfoList").toJavaList(CommitInfo.class);
        for(CommitInfo commitInfo : list){
            if(commitInfo.getAuthor().equals(developer)){
                addLines = commitInfo.getAdd();
            }
        }
        return addLines;
    }


}
