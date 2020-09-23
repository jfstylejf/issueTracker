package cn.edu.fudan.projectmanager.service;

import cn.edu.fudan.projectmanager.component.RestInterfaceManager;
import cn.edu.fudan.projectmanager.dao.ProjectDao;
import cn.edu.fudan.projectmanager.domain.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class KafkaConsumerService {


    private ProjectService projectService;

    private ProjectDao projectDao;

    @Autowired
    private RestInterfaceManager restInterfaceManager;

    @Autowired
    public void setProjectDao(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    @Autowired
    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    private Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    @KafkaListener(id = "projectUpdate", topics = {"RepoManager"}, groupId = "project")
    public void update(ConsumerRecord<String, String> consumerRecord) {
        String msg = consumerRecord.value();
        logger.info("receive message from topic -> " + consumerRecord.topic() + " : " + msg);
        CompleteDownLoad completeDownLoad = JSONObject.parseObject(msg, CompleteDownLoad.class);
        Project currentProject = projectService.getProjectByID(completeDownLoad.getProjectId());
        currentProject.setLanguage(completeDownLoad.getLanguage());
        currentProject.setDownload_status(completeDownLoad.getStatus());
        currentProject.setDescription(completeDownLoad.getDescription());
        currentProject.setRepo_id(completeDownLoad.getRepo_id());
        completeDownLoad.setTill_commiit_time(projectDao.getLatestCommitTime(completeDownLoad.getRepo_id()));
        currentProject.setTill_commit_time(completeDownLoad.getTill_commiit_time());
        projectService.updateProjectStatus(currentProject);
    }

    @KafkaListener(id = "repoIn", topics = {"repoIn"}, groupId = "repoIn")
    public void repoIn(ConsumerRecord<String, String> consumerRecord) throws ParseException {
        String msg = consumerRecord.value();
        logger.info("Auto-Scan  -> topic :" + consumerRecord.topic() + ";  msg :" + msg);
        RepoBasicInfo repoBasicInfo = JSONObject.parseObject(msg, RepoBasicInfo.class);
        projectDao.addOneProject(Project.createOneProjectByRepoBasicInfo(repoBasicInfo, "bug"));
        projectDao.addOneProject(Project.createOneProjectByRepoBasicInfo(repoBasicInfo, "clone"));
    }

    @KafkaListener(id = "updateCommit", topics = {"UpdateCommitTime"}, groupId = "updateCommit1")
    public void updateCommitTime(ConsumerRecord<String, String> consumerRecord) {
        String msg = consumerRecord.value();
        List<ScanMessageWithTime> commits=JSONArray.parseArray(msg,ScanMessageWithTime.class);
        commits = commits.stream().distinct().collect(Collectors.toList());
        int size=commits.size();
        logger.info("received message from topic -> " + consumerRecord.topic());
        if(!commits.isEmpty()){
            List<String> repoIds = new ArrayList<>();
            for(ScanMessageWithTime scanMessageWithTime : commits){
                repoIds.add(scanMessageWithTime.getRepoId());
            }
            repoIds = repoIds.stream().distinct().collect(Collectors.toList());
            for(String repoId:repoIds){
                updateLatestCommitTime(repoId);
            }
        }
    }

    @KafkaListener(id = "projectScan", topics = {"Scan"}, groupId = "scan1")
    public void updateTime(ConsumerRecord<String, String> consumerRecord) {
        String msg = consumerRecord.value();
        logger.info("received message from topic -> " + consumerRecord.topic() + " : " + msg);
        ScanMessage scanMessage = JSONObject.parseObject(msg, ScanMessage.class);
        String repoId = scanMessage.getRepoId();
        updateLatestCommitTime(repoId);

    }

    private void updateLatestCommitTime(String repoId){
//        JSONObject jsonObject = restInterfaceManager.getLatestCommitTime(repoId);
//        Date date = jsonObject.getJSONObject("data").getDate("commit_time");
        Date date = projectDao.getLatestCommitTime(repoId);
        try {
            List<Project> projects = projectDao.getProjectByRepoId(repoId);
            if (projects != null && !projects.isEmpty()) {
                for (int i = 0; i < projects.size(); i++) {
                    Project project=projects.get(i);
                    project.setTill_commit_time(date);
                    projectDao.updateProjectStatus(project);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("project update failed!");
        }
    }

}
