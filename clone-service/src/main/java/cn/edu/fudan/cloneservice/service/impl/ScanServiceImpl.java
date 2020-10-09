package cn.edu.fudan.cloneservice.service.impl;

import cn.edu.fudan.cloneservice.component.RestInterfaceManager;
import cn.edu.fudan.cloneservice.dao.CloneMeasureDao;
import cn.edu.fudan.cloneservice.dao.CloneLocationDao;
import cn.edu.fudan.cloneservice.dao.CloneRepoDao;
import cn.edu.fudan.cloneservice.dao.CloneScanDao;
import cn.edu.fudan.cloneservice.domain.ScanStatus;
import cn.edu.fudan.cloneservice.domain.clone.CloneRepo;
import cn.edu.fudan.cloneservice.service.CloneMeasureService;
import cn.edu.fudan.cloneservice.service.ScanService;
import cn.edu.fudan.cloneservice.task.ScanTask;
import cn.edu.fudan.cloneservice.util.JGitHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zyh
 * @date 2020/5/25
 */
@Slf4j
@Service
public class ScanServiceImpl implements ScanService {
    
    private ScanTask scanTask;

    private CloneScanDao cloneScanDao;
    private CloneMeasureDao cloneMeasureDao;
    private RestInterfaceManager rest;
    private CloneLocationDao cloneLocationDao;
    private CloneMeasureService cloneMeasureService;


    private ConcurrentHashMap<String, Boolean> scanStatus = new ConcurrentHashMap<>();
    private final Short lock = 1;

    private CloneRepoDao cloneRepoDao;

    @Autowired
    public void setCloneRepoDao(CloneRepoDao cloneRepoDao) {
        this.cloneRepoDao = cloneRepoDao;
    }

    @Override
    @Async("taskExecutor")
    public void cloneScan(String repoUuid, String startCommitId, String branch) {
        synchronized (lock) {
            if (scanStatus.keySet().contains(repoUuid)) {
                scanStatus.put(repoUuid, true);
                return;
            }
            scanStatus.putIfAbsent(repoUuid, false);
        }
        prepareForScan(repoUuid, branch, startCommitId);
    }

    private void prepareForScan(String repoUuid, String branch, String beginCommit) {
        boolean isUpdate = false;
        // 更新操作
        if (StringUtils.isEmpty(beginCommit)) {
            beginCommit = cloneMeasureDao.getLatestCloneLines(repoUuid).getCommitId();
            if (StringUtils.isEmpty(beginCommit)) {
                log.warn("{} : hasn't scanned before", repoUuid);
                checkAfterScan(repoUuid,branch);
                return;
            }
            isUpdate = true;
        }
        beginScan(repoUuid, branch, beginCommit, isUpdate);
        checkAfterScan(repoUuid,branch);
    }

    private void beginScan(String repoUuid, String branch, String beginCommit, boolean isUpdate) {
        log.info("{} -> start clone scan", Thread.currentThread().getName());
        String repoPath = rest.getRepoPath(repoUuid);
        if (repoPath == null) {
            log.error("{} : can't get repoPath", repoUuid);
            return;
        }
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        List<String> commitList = jGitHelper.getCommitListByBranchAndBeginCommit(branch, beginCommit, isUpdate);
        int commitSize = commitList.size();
        int lastCommitIndex = commitSize - 1;
        log.info("commit size : " +  commitSize);

        // 先执行粒度为method，仅需执行一次最近的commit
        String uuid = UUID.randomUUID().toString();
        executeLastCommit(uuid, repoUuid, commitList, repoPath);
        CloneRepo cloneRepo = new CloneRepo();
        cloneRepo.setUuid(uuid);

        for (int i = 0; i < commitSize ;i++) {
            long start = System.currentTimeMillis();
            String commitId = commitList.get(i);
            if (i != lastCommitIndex) {
                jGitHelper.checkout(commitId);
                scanTask.runSynchronously(repoUuid, commitId, "snippet", repoPath);
                cloneMeasureService.insertCloneMeasure(repoUuid, commitId, repoPath);
            }
            cloneRepo.setScannedCommitCount(i + 1);
            cloneRepo.setEndScanTime(new Date());
            cloneRepo.setStatus(ScanStatus.COMPLETE);
            long end = System.currentTimeMillis();
            long cost = (end - start)/(1000);
            cloneRepo.setScanTime((int)((end - start)/1000));
            cloneRepoDao.updateScan(cloneRepo);
            log.info("repo:{} -> took {} minutes to complete the clone scan and measure scan", repoUuid, cost);
        }
        rest.freeRepoPath(repoUuid, repoPath);
    }

    private void executeLastCommit(String uuid, String repoUuid, List<String> commitList, String repoPath) {
        String latestCommitId = commitList.get(commitList.size() - 1);
        scanTask.runSynchronously(repoUuid, latestCommitId, "method", repoPath);

        CloneRepo cloneRepo = initCloneRepo(repoUuid);
        cloneRepo.setUuid(uuid);
        cloneRepo.setStartScanTime(new Date());
        cloneRepo.setStartCommit(commitList.get(0));
        cloneRepo.setEndCommit(latestCommitId);
        cloneRepo.setTotalCommitCount(commitList.size());
        cloneRepoDao.insertCloneRepo(cloneRepo);

    }

    private void checkAfterScan(String repoUuid, String branch) {
        if (! scanStatus.keySet().contains(repoUuid)) {
            log.error("{} : not in scan map", repoUuid);
            return;
        }
        //扫完再次判断是否有更新请求
        synchronized (lock) {
            if (! scanStatus.get(repoUuid)) {
                scanStatus.remove(repoUuid);
                return;
            }
            scanStatus.put(repoUuid, false);
        }
        prepareForScan(repoUuid, branch, null);
    }

    private CloneRepo initCloneRepo(String repoId){
        CloneRepo cloneRepo = new CloneRepo();
        cloneRepo.setRepoId(repoId);
        cloneRepo.setStatus(ScanStatus.SCANNING);
        cloneRepo.setScanCount(cloneRepoDao.getScanCount(repoId) + 1);
        return cloneRepo;
    }

    @Override
    public void deleteCloneScan(String repoId) {
        cloneScanDao.deleteCloneScan(repoId);
        cloneLocationDao.deleteCloneLocations(repoId);
    }

    @Override
    public CloneRepo getLatestCloneRepo(String repoId) {
        return cloneRepoDao.getLatestCloneRepo(repoId);
    }


    @Autowired
    public void setRest(RestInterfaceManager rest) {
        this.rest = rest;
    }

    @Autowired
    public void setCloneLocationDao(CloneLocationDao cloneLocationDao) {
        this.cloneLocationDao = cloneLocationDao;
    }
    @Autowired
    public void setCloneScanDao(CloneScanDao cloneScanDao) {
        this.cloneScanDao = cloneScanDao;
    }
    @Autowired
    public void setScanTask(ScanTask scanTask) {
        this.scanTask = scanTask;
    }
    @Autowired
    public void setCloneMeasureDao(CloneMeasureDao cloneMeasureDao) {
        this.cloneMeasureDao = cloneMeasureDao;
    }
    @Autowired
    public void setCloneMeasureService(CloneMeasureService cloneMeasureService) {
        this.cloneMeasureService = cloneMeasureService;
    }
}
