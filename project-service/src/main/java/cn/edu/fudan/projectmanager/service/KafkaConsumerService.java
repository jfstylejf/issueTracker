package cn.edu.fudan.projectmanager.service;

import cn.edu.fudan.projectmanager.dao.SubRepositoryDao;
import cn.edu.fudan.projectmanager.domain.*;
import cn.edu.fudan.projectmanager.domain.message.RepoUpdateInfo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fancying
 */
@Slf4j
@Component
public class KafkaConsumerService {



    @Autowired
    private SubRepositoryDao subRepositoryDao;

    /**
     *  test case
     *  kafka-console-producer.sh --broker-list localhost:9092 --topic RepoManager
     * {"projectId":"170cc963-e452-4600-8ba9-4f699e29a102","language":"java","status":"Downloaded","description":"true","repoId":"d7bda1e6-685b-11ea-8448-6b6e4cffaf55"}
     */
    @KafkaListener(id = "projectUpdate", topics = {"RepoManager"}, groupId = "projectName")
    public void update(ConsumerRecord<String, String> consumerRecord) {
        String msg = consumerRecord.value();
        log.info("receive message from topic -> " + consumerRecord.topic() + " : " + msg);
        CompleteDownLoad completeDownLoad = JSONObject.parseObject(msg, CompleteDownLoad.class);
        SubRepository subRepository = subRepositoryDao.getSubRepoByUuid(completeDownLoad.getSubRepositoryUuid());
        subRepository.setLanguage(completeDownLoad.getLanguage());
        subRepository.setDownloadStatus(completeDownLoad.getStatus());
        subRepository.setRepoUuid(completeDownLoad.getRepo_id());
        completeDownLoad.setLatestCommitTime(subRepositoryDao.getLatestCommitTime(completeDownLoad.getRepo_id()));
        subRepository.setLatestCommitTime(completeDownLoad.getLatestCommitTime());
        subRepositoryDao.updateRepository(subRepository);
        log.info("update sub repo[{}] success", subRepository.getUuid());
    }

//    @KafkaListener(id = "repoIn", topics = {"repoIn"}, groupId = "repoIn")
//    public void repoIn(ConsumerRecord<String, String> consumerRecord) throws ParseException {
//        String msg = consumerRecord.value();
//        log.info("Auto-Scan  -> topic :" + consumerRecord.topic() + ";  msg :" + msg);
//        RepoBasicInfo repoBasicInfo = JSONObject.parseObject(msg, RepoBasicInfo.class);
//        projectDao.addOneProject(Project.createOneProjectByRepoBasicInfo(repoBasicInfo, "bug"));
//        projectDao.addOneProject(Project.createOneProjectByRepoBasicInfo(repoBasicInfo, "clone"));
//    }

    /**
     * repo_updated_r1p1
     */
    @KafkaListener(id = "updateCommit", topics = {"repo_updated_r1p1"}, groupId = "subRepoUpdateTime")
    public void updateCommitTime(ConsumerRecord<String, String> consumerRecord) {
        String msg = consumerRecord.value();
        log.debug("message is {}", msg);
        RepoUpdateInfo  repoUpdateInfo = JSONObject.parseObject(msg, RepoUpdateInfo.class);
        log.info("received message from topic -> {}", consumerRecord.topic());
        updateLatestCommitTime(repoUpdateInfo.getRepoId());
    }


    private void updateLatestCommitTime(String repoId){
        Date date = subRepositoryDao.getLatestCommitTime(repoId);
        try {
            SubRepository subRepository = subRepositoryDao.getSubRepoByRepoUuid(repoId);
            if (subRepository != null) {
                subRepository.setLatestCommitTime(date);
                subRepositoryDao.updateRepository(subRepository);
            }
        } catch (Exception e) {
            log.error("projectName update failed!");
        }
    }

}
