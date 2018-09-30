package cn.edu.fudan.projectmanager.service.impl;


import cn.edu.fudan.projectmanager.dao.*;
import cn.edu.fudan.projectmanager.domain.NeedDownload;
import cn.edu.fudan.projectmanager.domain.Project;
import cn.edu.fudan.projectmanager.service.ProjectService;
import cn.edu.fudan.projectmanager.util.DateTimeUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Value("${github.api.path}")
    private String githubAPIPath;
    @Value("${inner.service.path}")
    private String innerServicePath;

    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private HttpHeaders httpHeaders;

    @Autowired
    public void setHttpHeaders(HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    private KafkaTemplate kafkaTemplate;

    @Autowired
    public void setKafkaTemplate(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private RestTemplate restTemplate;

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    private ProjectDao projectDao;

    @Autowired
    public void setProjectDao(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    @SuppressWarnings("unchecked")
    private void send(String projectId, String url) {
        NeedDownload needDownload = new NeedDownload(projectId, url);
        kafkaTemplate.send("ProjectManager", JSONObject.toJSONString(needDownload));
        logger.info("send message to topic ProjectManage ---> " + JSONObject.toJSONString(needDownload));
    }

    private String getAccountId(String userToken) {
        HttpEntity<String> requestEntity = new HttpEntity<>(null, httpHeaders);
        return restTemplate.exchange(innerServicePath + "/user/accountId?userToken=" + userToken, HttpMethod.GET, requestEntity, String.class).getBody();
    }

    private void checkProjectURL(String url) {
        Pattern pattern = Pattern.compile("https://github.com(/[\\w-]+/[\\w-]+)");
        Matcher matcher = pattern.matcher(url);
        if (!matcher.matches()) {
            throw new RuntimeException("invalid url!");
        }
        //前面做过一次全匹配，需要把matcher重置一下才能接着从头匹配
        matcher.reset();
        boolean isMavenProject = false;
        while (matcher.find()) {
            String author_projectName = matcher.group(1);
            JSONArray fileList = restTemplate.getForEntity(githubAPIPath + author_projectName + "/contents", JSONArray.class).getBody();
            if (fileList != null) {
                for (int i = 0; i < fileList.size(); i++) {
                    JSONObject file = fileList.getJSONObject(i);
                    if (file.getString("name").equals("pom.xml")) {
                        isMavenProject = true;
                        break;
                    }
                }
                if (!isMavenProject)
                    throw new RuntimeException("failed,this project is not maven project!");
            } else {
                throw new RuntimeException("invalid url!");
            }
        }
    }

    @Override
    public void addOneProject(String userToken, Project project) {
        String url = project.getUrl();
        if (url == null) {
            throw new RuntimeException("please input the project url!");
        }
        url = url.trim();
        checkProjectURL(url);
        if (project.getType() == null)
            project.setType("bug");
        String account_id = getAccountId(userToken);
        if (projectDao.hasBeenAdded(account_id, url, project.getType())) {
            throw new RuntimeException("The project has been added!");
        }
        String projectId = UUID.randomUUID().toString();
        project.setUuid(projectId);
        project.setUrl(url);
        project.setAccount_id(account_id);
        project.setDownload_status("Downloading");
        project.setScan_status("Not Scanned");
        project.setAdd_time(DateTimeUtil.formatedDate(new Date()));
        projectDao.addOneProject(project);
        //向RepoManager这个Topic发送消息，请求开始下载
        send(projectId, url);
    }

    @Override
    public Object getProjectList(String userToken,String type) {
        String account_id = getAccountId(userToken);
        return projectDao.getProjectList(account_id,type);
    }

    @Override
    public Object getProjectByRepoId(String repo_id) {
        return projectDao.getProjectByRepoId(repo_id);
    }

    @Override
    public Object getProjectIdList(String account_id) {
        return projectDao.getProjectByAccountId(account_id).stream().map(Project::getUuid).collect(Collectors.toList());
    }

    @Override
    public Object getProjectListByKeyWord(String userToken, String keyWord) {
        String account_id = getAccountId(userToken);
        return projectDao.getProjectByKeyWordAndAccountId(account_id, keyWord.trim());
    }

    @Override
    public Project getProjectByID(String projectId) {
        return projectDao.getProjectByID(projectId);
    }

    @Override
    public void updateProjectStatus(Project project) {
        projectDao.updateProjectStatus(project);
    }


    @Override
    public void remove(String projectId, String userToken) {
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        String repoId = restTemplate.exchange(innerServicePath + "/inner/project/repo-id?project-id=" + projectId, HttpMethod.GET, requestEntity, String.class).getBody();
        String account_id = getAccountId(userToken);
        //如果当前repoId只有这一个projectId与其对应，那么删除project的同时会删除repo的相关内容
        //否则还有其他project与当前repoId对应，该repo的相关内容就不删
        if (!projectDao.existOtherProjectWithThisRepoId(repoId)) {
            JSONObject response = null;
            response = restTemplate.exchange(innerServicePath + "/inner/issue/" + repoId, HttpMethod.DELETE, requestEntity, JSONObject.class).getBody();
            if (response != null)
                logger.info(response.toJSONString());
            response = restTemplate.exchange(innerServicePath + "/inner/raw-issue/" + repoId, HttpMethod.DELETE, requestEntity, JSONObject.class).getBody();
            if (response != null)
                logger.info(response.toJSONString());
            response = restTemplate.exchange(innerServicePath + "/inner/scan/" + repoId, HttpMethod.DELETE, requestEntity, JSONObject.class).getBody();
            if (response != null)
                logger.info(response.toJSONString());
            //delete info in redis
            stringRedisTemplate.multi();
            stringRedisTemplate.delete("dashboard:day:" + repoId);
            stringRedisTemplate.delete("dashboard:week:" + repoId);
            stringRedisTemplate.delete("dashboard:month:" + repoId);
            stringRedisTemplate.delete("trend:day:new:" + account_id + ":" + repoId);
            stringRedisTemplate.delete("trend:day:remaining:" + account_id + ":" + repoId);
            stringRedisTemplate.delete("trend:day:eliminated:" + account_id + ":" + repoId);
            stringRedisTemplate.delete("trend:week:new:" + account_id + ":" + repoId);
            stringRedisTemplate.delete("trend:week:remaining:" + account_id + ":" + repoId);
            stringRedisTemplate.delete("trend:week:eliminated:" + account_id + ":" + repoId);
            stringRedisTemplate.exec();
        }
        projectDao.remove(projectId);
    }

    @Override
    public String getRepoId(String projectId) {
        return projectDao.getRepoId(projectId);
    }
}