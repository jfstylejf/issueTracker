package cn.edu.fudan.common.scan;

import cn.edu.fudan.common.component.RepoRestManager;
import cn.edu.fudan.common.domain.ScanInfo;
import cn.edu.fudan.common.domain.po.scan.RepoScan;
import cn.edu.fudan.common.jgit.JGitHelper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public abstract class CommonScanProcess implements CommonScanService {
    private static final Logger log = LoggerFactory.getLogger(CommonScanProcess.class);
    private static final String KEY_DELIMITER = "-";

    /**
     * key repoUuid
     * value true/false true 代表还需要更新扫描一次
     **/
    private final ConcurrentHashMap<String, Boolean> scanStatusMap = new ConcurrentHashMap<>();
    private RepoRestManager repoRestManager;
    private final Short lock = 1;

    /**
     *  设置同步状态 处理接口请求 判断扫描是否需要更新
     **/
    public void scan(String repoUuid, String branch, String beginCommit) {

        // todo 查询repository表 来查看repo 所包含的语言 让后决定采用什么工具来调用
        String[] tools = getToolsByRepo(repoUuid);
        for (String tool : tools) {
            RepoScan scanInfo = getRepoScanStatus(repoUuid, tool);

            boolean isFirstScan = beginCommit != null && !"".equals(beginCommit);
            boolean isScanned = scanInfo != null;

            // 扫描过并且是第一次扫描说明该请求已经处理过
            if (isScanned && isFirstScan) {
                log.warn("{} : already scanned before", repoUuid);
                return;
            }

            String key = generateKey(repoUuid, tool);
            synchronized(this.lock) {
                // 正在扫描接收到了请求
                if (scanStatusMap.containsKey(key)) {
                    scanStatusMap.put(key, true);
                    return;
                }
                scanStatusMap.putIfAbsent(key, false);
            }
            beginScan(repoUuid, branch, beginCommit, tool);
            checkAfterScan(repoUuid, branch, tool);
        }

    }

    protected String generateKey(String repoUuid, String tool) {
        return repoUuid + KEY_DELIMITER + tool;
    }

    /**
     * 一个 commitList 扫描完成之后再次检查 查看是否有更新扫描的请求
     **/
    private void checkAfterScan(String repoUuid, String branch, String tool) {
        String key = generateKey(repoUuid, tool);
        if (!scanStatusMap.containsKey(key)) {
            log.error("{} : not in cn.edu.fudan.common.scan scanStatusMap", repoUuid);
            return;
        }
        synchronized(this.lock) {
            boolean newUpdate = scanStatusMap.get(key);
            if (!newUpdate) {
                scanStatusMap.remove(key);
                return;
            }
            scanStatusMap.put(key, false);
        }
        beginScan(repoUuid, branch, null, tool);
        checkAfterScan(repoUuid, branch, tool);
    }

    @Async("taskExecutor")
    void beginScan(String repoUuid, String branch, String beginCommit, String tool) {
        Thread curThread = Thread.currentThread();
        String threadName = generateKey(repoUuid, tool);
        curThread.setName(threadName);

        ToolScan specificTool = getToolScan(tool);
        // 获取repo所在路径
        String repoPath = repoRestManager.getCodeServiceRepo(repoUuid);
        if (repoPath == null) {
            log.error("{} : can't get repoPath", repoUuid);
            return;
        }

        List<String> scannedCommitList = getScannedCommitList(repoUuid, tool);
        boolean initialScan = scannedCommitList.size() == 0;
        int  scannedCommitCount = 0;
        RepoScan repoScan = RepoScan.builder()
                .repoUuid(repoUuid)
                .branch(branch)
                .status(ScanInfo.Status.SCANNING.getStatus())
                .initialScan(initialScan)
                .tool(tool)
                .scannedCommitCount(scannedCommitCount)
                .startScanTime(new Date())
                .endScanTime(new Date())
                .build();
        try {
            if (beginCommit == null || "".equals(beginCommit)) {
                beginCommit = getLastedScannedCommit(repoUuid, tool);
            }
            List<String> toScanCommitList = new JGitHelper(repoPath).getScanCommitListByBranchAndBeginCommit(branch, beginCommit, scannedCommitList);

            if (toScanCommitList.size() == 0) {
                return;
            }
            String firstCommit = toScanCommitList.get(0);
            repoScan.setTotalCommitCount(toScanCommitList.size());
            repoScan.setStart_commit(firstCommit);

            log.info("commit size : {}" , toScanCommitList.size());
            insertRepoScan(repoScan);
            boolean success = false;
            specificTool.loadData(repoUuid, branch, repoPath, initialScan,toScanCommitList);
            specificTool.prepareForScan();

            for (String commit : toScanCommitList) {
                specificTool.prepareForOneScan(commit);
                success = specificTool.scanOneCommit(commit);
                specificTool.cleanUpForOneScan(commit);
                scannedCommitCount++;

                if(curThread.isInterrupted()){
                    synchronized (lock) {
                        scanStatusMap.remove(threadName);
                    }
                    log.warn("thread:{} stopped", threadName);
                    break;
                }
            }
            specificTool.cleanUpForScan();

            repoScan.setStatus(success ? ScanInfo.Status.COMPLETE.getStatus(): ScanInfo.Status.FAILED.getStatus());
            repoScan.setEndScanTime(new Date());
            repoScan.setScannedCommitCount(scannedCommitCount);
            updateRepoScan(repoScan);
        } catch (Exception e) {
            e.printStackTrace();
            repoScan.setStatus(ScanInfo.Status.FAILED.getStatus());
            updateRepoScan(repoScan);
        } finally {
            repoRestManager.freeRepo(repoUuid, repoPath);
        }

    }

    protected abstract ToolScan getToolScan(String tool);

    /**
     * 根据repoUuid 和 代码库的地址决定需要调用的工具列表
     **/
    protected abstract List<String> getScannedCommitList(String repoUuid, String tool);

    /**
     * 根据表中的记录得到最新扫描的commit id
     **/
    protected abstract String getLastedScannedCommit(String repoUuid, String tool);

    /**
     *  根据uuid 和 代码库的地址决定需要调用的工具列表
     **/
    protected abstract String[] getToolsByRepo(String repoUuid);

    /**
     * 插入当前repo的 扫描信息
     **/
    protected abstract void insertRepoScan(RepoScan repoScan);

    public abstract <T extends RepoRestManager> void setRepoRestManager(T restInterfaceManager);


    @Override
    public boolean stopScan(String repoUuid, String toolName) {
        Assert.notNull(repoUuid, "repoUuid is null");
        Assert.notNull(toolName, "toolName is null");

        String threadName = generateKey(repoUuid,toolName);
        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        int activeCount = currentGroup.activeCount();
        Thread[] lstThreads = new Thread[activeCount];
        currentGroup.enumerate(lstThreads);
        for (int i = 0; i < activeCount; i++){
            if(threadName.equals (lstThreads[i].getName())){
                lstThreads[i].interrupt();
                return true;
            }
        }

        return  false;
    }

    @Override
    public boolean stopScan(String repoUuid) {
        Assert.notNull(repoUuid, "repoUuid is null");

        int keyCount;
        Set<String> targetKeys;
        synchronized (lock) {
            Set<String> keys =  scanStatusMap.keySet();
            targetKeys = keys.stream().filter(key -> key.contains(repoUuid)).collect(Collectors.toSet());
            keyCount = targetKeys.size();
        }

        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        int activeCount = currentGroup.activeCount();
        Thread[] lstThreads = new Thread[activeCount];
        currentGroup.enumerate(lstThreads);
        for (int i = 0; i < activeCount; i++){
            if (targetKeys.contains(lstThreads[i].getName())) {
                lstThreads[i].interrupt();
                keyCount--;
            }
        }

        return keyCount == 0;
    }


}
