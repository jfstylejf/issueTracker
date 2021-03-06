package cn.edu.fudan.scanservice.service.impl;

import cn.edu.fudan.scanservice.component.rest.RestInterfaceManager;
import cn.edu.fudan.scanservice.component.scan.FirstScanCommitFilterStrategy;
import cn.edu.fudan.scanservice.dao.ScanDao;
import cn.edu.fudan.scanservice.domain.dbo.Scan;
import cn.edu.fudan.scanservice.domain.dto.CommitMessage;
import cn.edu.fudan.scanservice.domain.dto.RepoResourceDTO;
import cn.edu.fudan.scanservice.service.InvokeToolService;
import cn.edu.fudan.scanservice.service.MessageListeningService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * description: 自动扫描实现
 *
 * @author fancying
 * create: 2020-03-03 22:20
 **/
@Service
@Slf4j
public class KafkaAutoScanImpl implements MessageListeningService {

    @Value("${defaultScanInterval}")
    private int defaultScanInterval;

    private RestInterfaceManager restInvoker;
    private InvokeToolService invokeToolService;
    private ScanDao scanDao;
    private FirstScanCommitFilterStrategy commitFilter;



    @Override
    @KafkaListener(id = "autoScan", topics = {"repo_downloaded_r1p1","repo_updated_r1p1"}, groupId = "scan")
    public void listing(Object mess) {
        ConsumerRecord<String, String> message = cast(mess);
        String msg = message.value();
        log.info("received message from topic -> {} : {}", message.topic(), msg);
        CommitMessage commitMessage = JSONObject.parseObject(msg, CommitMessage.class);
        String repoId = commitMessage.getRepoId();
        String branch = commitMessage.getBranch();
        boolean isUpdate = true;
        JSONObject project = restInvoker.getProjectsOfRepo(repoId);
        if(project == null || project.isEmpty ()){
            log.error("repo : [{}] info is null", repoId);
            return;
        }
        log.debug("project is :");
        log.debug(project.toJSONString());
        //判断如果不是更新，该项目是否已经扫描过
        Scan preScan = scanDao.getScanByRepoId (repoId);
        if( preScan == null){
            isUpdate = false;
        }
        String startCommit = null;
        if (! isUpdate) {
            int month = 12;
            if(defaultScanInterval != 0){
                month = defaultScanInterval;
            }
            String language = project.getJSONObject("data").getString("language");
            log.debug("language is :" + language);
            if (language.equals("Java")) {
                startCommit = commitFilter.filter (RepoResourceDTO.builder().repoId(repoId).build(), repoId, branch, month);
            } else {
                startCommit = commitFilter.filterWithoutAggregationCommit(RepoResourceDTO.builder().repoId(repoId).build(), repoId, branch, month);
            }
        }
        if (! isUpdate && startCommit == null) {
            log.error("there is none available commit，repo id：{}", repoId);
            // TODO 更新数据库状态用于通知用户 scanDao
            // scan dao
            return;
        }
        log.debug("repoId is :" + repoId);
        log.debug("startCommit is :" + startCommit);
        log.debug("branch is :" + branch);
        log.info("start invoke all tools...");
        // FIXME 调用工具开始扫描
        invokeToolService.invokeTools(repoId, branch, startCommit);
    }


    @SuppressWarnings("unchecked")
    private  <T> T cast(Object obj) {
        return (T) obj;
    }

//    @FreeResource
//    private String getFirstScanCommit(RepoResourceDTO repoResourceDTO, String commitId) {
//        String repoPath = restInvoker.getRepoPath(repoResourceDTO.getRepoId(), commitId);
//        repoResourceDTO.setRepoPath(repoPath);
//        try (JGitHelper jGitHelper = new JGitHelper(repoPath)){
//            // FIXME 聚合点考虑
//            List<RevCommit> aggregationCommit = jGitHelper.getAllAggregationCommit();
//            for (RevCommit commit : aggregationCommit) {
//                jGitHelper.checkout(commit.getName());
//                if (CompileUtil.isCompilable(repoPath)) {
//                    return commit.getName();
//                }
//            }
//        }
//        return commitId;
//    }


    @Autowired
    public void setRestInvoker(RestInterfaceManager restInvoker) {
        this.restInvoker = restInvoker;
    }


    @Autowired
    public void setScanDao(ScanDao scanDao) {
        this.scanDao = scanDao;
    }

    @Autowired
    public void setInvokeToolService(InvokeToolService invokeToolService) {
        this.invokeToolService = invokeToolService;
    }

    @Autowired
    @Qualifier("TAS")
    public void setCommitFilter(FirstScanCommitFilterStrategy commitFilter) {
        this.commitFilter = commitFilter;
    }

}