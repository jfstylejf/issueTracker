package cn.edu.fudan.measureservice.component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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

    private RestTemplate restTemplate;

    public RestInterfaceManager(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    //-----------------------------------------------project service-------------------------------------------------
    public String getRepoIdOfProject(String projectId) {
        return restTemplate.getForObject(projectServicePath + "/inner/project/repo-id?project-id=" + projectId, String.class);
    }

    public JSONArray getCommitsOfRepo(String repoId){
        JSONObject response=restTemplate.getForObject(commitServicePath + "?repo_id=" + repoId + "&is_whole=true", JSONObject.class);
        if(response==null||response.getJSONArray("data")==null)
        return null;
        return response.getJSONArray("data");
    }

    //---------------------------------------------code service---------------------------------------------------------
    public String getRepoPath(String repoId,String commit_id){
        String repoPath=null;
        JSONObject response=restTemplate.getForObject(codeServicePath + "?repo_id=" + repoId+"&commit_id="+commit_id, JSONObject.class).getJSONObject("data");
        if (response != null ){
            if(response.getString("status").equals("Successful")) {
                repoPath=response.getString("content");
                log.info("repoHome -> {}" ,repoPath);
            }else{
                log.error("get repoHome fail -> {}",response.getString("content"));
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
        }else
            log.warn("{} -> free failed",repoPath);
        return response;
    }
}
