package cn.edu.fudan.common.scan;

import cn.edu.fudan.common.component.BaseRepoRestManager;
import cn.edu.fudan.common.domain.ScanInfo;
import cn.edu.fudan.common.domain.po.scan.RepoScan;
import cn.edu.fudan.common.jgit.JGitHelper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private String repo_path;

    /**
     * key repoUuid
     * value true/false true 代表还需要更新扫描一次
     **/
    private final ConcurrentHashMap<String, Boolean> scanStatusMap = new ConcurrentHashMap<>();
    protected BaseRepoRestManager baseRepoRestManager;
    private static final Short LOCK = 1;

    /**
     * 设置同步状态 处理接口请求 判断扫描是否需要更新
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
            synchronized (LOCK) {
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
        synchronized (this.LOCK) {
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
        log.info("repoUuid: " + repoUuid);
        String repoPath = Boolean.TRUE.equals(this.useLocalRepoPath()) ? this.getLocalRepoPath() : baseRepoRestManager.getCodeServiceRepo(repoUuid);
        if (repoPath == null) {
            log.error("{} : can't get repoPath", repoUuid);
            return;
        }
        List<String> scannedCommitList = getScannedCommitList(repoUuid, tool);

        log.info("scannedCommitList.size():" + scannedCommitList.size());
        boolean initialScan = scannedCommitList.isEmpty();
        int scannedCommitCount = 0;
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
        //  todo repo_path is added by shaoxi ,delete it later
        this.repo_path=repoPath;
        try {
            if (beginCommit == null || "".equals(beginCommit)) {
                beginCommit = getLastedScannedCommit(repoUuid, tool);
            }
            List<String> toScanCommitList = new JGitHelper(repoPath).getScanCommitListByBranchAndBeginCommit(branch, beginCommit, scannedCommitList);

            if (toScanCommitList.isEmpty()) {
                return;
            }
            String firstCommit = toScanCommitList.get(0);
            repoScan.setTotalCommitCount(toScanCommitList.size());
            repoScan.setStartCommit(firstCommit);

            log.info("commit size : {}", toScanCommitList.size());

            insertRepoScan(repoScan);
            boolean success = false;
            specificTool.loadData(repoUuid, branch, repoPath, initialScan, toScanCommitList);
            specificTool.prepareForScan();

            for (String commit : toScanCommitList) {
                specificTool.prepareForOneScan(commit);
                success = specificTool.scanOneCommit(commit);
                specificTool.cleanUpForOneScan(commit);
                repoScan.setScannedCommitCount(++scannedCommitCount);
                recordScannedCommit(commit, repoScan);
                updateRepoScan(repoScan);
                if (curThread.isInterrupted()) {
                    synchronized (this.LOCK) {
                        scanStatusMap.remove(threadName);
                    }
                    log.warn("thread:{} stopped", threadName);
                    break;
                }
            }
            specificTool.cleanUpForScan();


            repoScan.setStatus(success ? ScanInfo.Status.COMPLETE.getStatus() : ScanInfo.Status.FAILED.getStatus());
            repoScan.setEndScanTime(new Date());
            updateRepoScan(repoScan);
        } catch (Exception e) {
            e.printStackTrace();
            repoScan.setStatus(ScanInfo.Status.FAILED.getStatus());
            updateRepoScan(repoScan);
        } finally {
            baseRepoRestManager.freeRepo(repoUuid, repoPath);
        }

    }

    protected abstract ToolScan getToolScan(String tool);

    /**
     * 根据repoUuid 和 tool 代码库的地址决定需要调用的工具列表
     **/
    protected abstract List<String> getScannedCommitList(String repoUuid, String tool);

    /**
     * 记录扫描过的commit信息
     **/
    protected abstract void recordScannedCommit(String commit, RepoScan repoScan);

    /**
     * 根据表中的记录得到最新扫描的commit id
     **/
    protected abstract String getLastedScannedCommit(String repoUuid, String tool);

    /**
     * 根据uuid 和 代码库的地址决定需要调用的工具列表
     **/
    protected abstract String[] getToolsByRepo(String repoUuid);

    /**
     * 插入当前repo的 扫描信息
     **/
    protected abstract void insertRepoScan(RepoScan repoScan);

    /**
     * 是否使用本地repoPath
     **/
    protected abstract Boolean useLocalRepoPath();

    /**
     * 获取本地repoPath
     **/
    protected abstract String getLocalRepoPath();

    @Autowired
    public abstract <T extends BaseRepoRestManager> void setBaseRepoRestManager(T restInterfaceManager);

    @Override
    public boolean stopScan(String repoUuid, String toolName) {
        Assert.notNull(repoUuid, "repoUuid is null");
        Assert.notNull(toolName, "toolName is null");

        String threadName = generateKey(repoUuid, toolName);
        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        int activeCount = currentGroup.activeCount();
        Thread[] lstThreads = new Thread[activeCount];
        currentGroup.enumerate(lstThreads);
        for (int i = 0; i < activeCount; i++) {
            if (threadName.equals(lstThreads[i].getName())) {
                lstThreads[i].interrupt();
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean stopScan(String repoUuid) {
        Assert.notNull(repoUuid, "repoUuid is null");

        int keyCount;
        Set<String> targetKeys;
        synchronized (LOCK) {
            Set<String> keys = scanStatusMap.keySet();
            targetKeys = keys.stream().filter(key -> key.contains(repoUuid)).collect(Collectors.toSet());
            keyCount = targetKeys.size();
        }

        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        int activeCount = currentGroup.activeCount();
        Thread[] lstThreads = new Thread[activeCount];
        currentGroup.enumerate(lstThreads);
        for (int i = 0; i < activeCount; i++) {
            if (targetKeys.contains(lstThreads[i].getName())) {
                lstThreads[i].interrupt();
                keyCount--;
            }
        }

        return keyCount == 0;
    }


}
