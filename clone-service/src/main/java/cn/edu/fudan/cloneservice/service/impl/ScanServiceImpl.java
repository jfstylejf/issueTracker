package cn.edu.fudan.cloneservice.service.impl;

import cn.edu.fudan.cloneservice.component.RestInterfaceManager;
import cn.edu.fudan.cloneservice.dao.*;
import cn.edu.fudan.cloneservice.domain.ScanStatus;
import cn.edu.fudan.cloneservice.domain.clone.CloneRepo;
import cn.edu.fudan.cloneservice.service.CloneMeasureService;
import cn.edu.fudan.cloneservice.service.ScanService;
import cn.edu.fudan.cloneservice.task.ScanTask;
import cn.edu.fudan.cloneservice.util.JGitHelper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.reflections.Reflections.log;

/**
 * @author zyh
 * @date 2020/5/25
 */
@Slf4j
@Service
public class ScanServiceImpl implements ScanService {

    private ScanTask scanTask;

    private CloneScanDao cloneScanDao;
    @Autowired
    private CloneInfoDao cloneInfoDao;
    private CloneMeasureDao cloneMeasureDao;
    private RestInterfaceManager rest;
    private CloneLocationDao cloneLocationDao;
    private CloneMeasureService cloneMeasureService;
    @Autowired
    private RestInterfaceManager restInterfaceManager;

    private ConcurrentHashMap<String, Boolean> scanStatus = new ConcurrentHashMap<>();
    private final Short lock = 1;

    private CloneRepoDao cloneRepoDao;

    @Autowired
    public void setCloneRepoDao(CloneRepoDao cloneRepoDao) {
        this.cloneRepoDao = cloneRepoDao;
    }

    @Override
    @Async("taskExecutor")
    public void cloneScan(String repoUuid, String startCommitId, String branch) throws IOException, GitAPIException {
        synchronized (lock) {
            if (scanStatus.keySet().contains(repoUuid)) {
                scanStatus.put(repoUuid, true);
                return;
            }
            scanStatus.putIfAbsent(repoUuid, false);
        }
        log.info("1");
        prepareForScan(repoUuid, branch, startCommitId);
    }

    private void prepareForScan(String repoUuid, String branch, String beginCommit) throws IOException, GitAPIException {
        boolean isUpdate = false;
        // ????????????
        if (StringUtils.isEmpty(beginCommit)) {
            List<String> repoUuids = new ArrayList<>();
            repoUuids.add(repoUuid);
            beginCommit = cloneMeasureDao.getLatestCloneLines(repoUuids).getCommitId();
            if (StringUtils.isEmpty(beginCommit)) {
                log.warn("{} : hasn't scanned before", repoUuid);
                checkAfterScan(repoUuid, branch);
                return;
            }
            isUpdate = true;
        }
        beginScan(repoUuid, branch, beginCommit, isUpdate);
        checkAfterScan(repoUuid, branch);
    }

    private void beginScan(String repoUuid, String branch, String beginCommit, boolean isUpdate) throws IOException {
        log.info("{} -> start clone scan", Thread.currentThread().getName());
//        String repoPath = "C:\\Users\\86189\\Desktop\\cl\\fortestjs\\";
//        String repoPath = "C:\\Users\\86189\\Desktop\\cl\\fortestjs-davidtest_duplicate_fdse-6\\";

        String repoPath = rest.getRepoPath(repoUuid);
        log.info(repoUuid);
        if (repoPath == null) {
            log.error("{} : can't get repoPath", repoUuid);
            return;
        }
        JGitHelper jGitHelper = new JGitHelper(repoPath);

        //fixme beginScan if update,begin 1.
        List<String> commitList = jGitHelper.getCommitListByBranchAndBeginCommit(branch, beginCommit, isUpdate);
        List<String> commitListWeekly = jGitHelper.getCommitListByBranchAndBeginCommitWeekly(branch, beginCommit, isUpdate);
        log.debug("commitListWeekly contains: "+ commitListWeekly.toString());
        int commitSize = commitList.size();
        int lastCommitIndex = commitSize - 1;
        log.debug("commit size : " + commitSize);

        // ??????????????????method??????????????????????????????commit
        String uuid = UUID.randomUUID().toString();


        executeLastCommit(uuid, repoUuid, commitList, repoPath);
        CloneRepo cloneRepo = new CloneRepo();
        cloneRepo.setUuid(uuid);

        for (int i = 0; i < commitSize; i++) {
            long start = System.currentTimeMillis();
            String commitId = commitList.get(i);
            jGitHelper.checkout(commitId);
            scanTask.runSynchronously(repoUuid, commitId, "snippet", repoPath);
            cloneMeasureService.insertCloneMeasure(repoUuid, commitId, repoPath);
            if(!commitListWeekly.contains(commitId)){
                cloneLocationDao.deleteCloneLocationByCommitId(commitId);
                log.debug("delete commit " + commitId + " success");
            }else {
                log.debug("insert "+commitId+" success");
            }
            cloneRepo.setScannedCommitCount(i + 1);
            cloneRepo.setEndScanTime(new Date());
            cloneRepo.setStatus(ScanStatus.COMPLETE);
            long end = System.currentTimeMillis();
            long cost = (end - start) / (1000);
            cloneRepo.setScanTime((int) ((end - start) / 1000));
            cloneRepoDao.updateScan(cloneRepo);
            log.info("repo:{} -> took {} minutes to complete the clone scan and measure scan", repoUuid, cost);
        }
        rest.freeRepoPath(repoUuid, repoPath);
        jGitHelper.close();
    }

    private void executeLastCommit(String uuid, String repoUuid, List<String> commitList, String repoPath) throws IOException {
        String latestCommitId = commitList.get(commitList.size() - 1);
//        scanTask.runSynchronously(repoUuid, latestCommitId, "snippet", repoPath);
        CloneRepo cloneRepo = initCloneRepo(repoUuid);
        cloneRepo.setUuid(uuid);
        cloneRepo.setStartScanTime(new Date());
        cloneRepo.setStartCommit(commitList.get(0));
        cloneRepo.setEndCommit(latestCommitId);
        cloneRepo.setTotalCommitCount(commitList.size());
        cloneRepoDao.insertCloneRepo(cloneRepo);

    }

    private void checkAfterScan(String repoUuid, String branch) throws IOException, GitAPIException {
        if (!scanStatus.keySet().contains(repoUuid)) {
            log.error("{} : not in scan map", repoUuid);
            return;
        }
        //???????????????????????????????????????
        synchronized (lock) {
            if (!scanStatus.get(repoUuid)) {
                scanStatus.remove(repoUuid);
                return;
            }
            scanStatus.put(repoUuid, false);
        }
        prepareForScan(repoUuid, branch, null);
    }

    private CloneRepo initCloneRepo(String repoId) {
        CloneRepo cloneRepo = new CloneRepo();
        cloneRepo.setRepoId(repoId);
        cloneRepo.setStatus(ScanStatus.SCANNING);
        cloneRepo.setScanCount(cloneRepoDao.getScanCount(repoId) + 1);
        return cloneRepo;
    }

    /**
     * @param repoId
     */
    @Override
    @Async("taskExecutor")
    public void deleteCloneScan(String repoId) {
        cloneScanDao.deleteCloneScan(repoId);
        cloneLocationDao.deleteCloneLocations(repoId);
        cloneMeasureDao.deleteCloneMeasureByRepoId(repoId);
        cloneInfoDao.deleteCloneInfo(repoId);
        cloneRepoDao.deleteCloneRepo(repoId);
        boolean recallRes = restInterfaceManager.deleteRecall(repoId);
        if(recallRes){
            log.info(" recall ok");
        }else {
            log.info(" recall false");
        }
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
