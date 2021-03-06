package cn.edu.fudan.common.scan;

import cn.edu.fudan.common.component.BaseRepoRestManager;
import cn.edu.fudan.common.domain.ScanInfo;
import cn.edu.fudan.common.domain.po.scan.RepoScan;
import cn.edu.fudan.common.exception.CodePathGetFailedException;
import cn.edu.fudan.common.exception.RepoScanGetException;
import cn.edu.fudan.common.jgit.JGitHelper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author beethoven
 */
@Getter
@NoArgsConstructor
public abstract class CommonScanProcess implements CommonScanService {
    private static final Logger log = LoggerFactory.getLogger(CommonScanProcess.class);
    private static final String KEY_DELIMITER = "-";

    /**
     * key repoUuid
     * value true/false true 代表还需要更新扫描一次
     */
    private final ConcurrentHashMap<String, Boolean> scanStatusMap = new ConcurrentHashMap<>();
    protected BaseRepoRestManager baseRepoRestManager;
    protected ApplicationContext applicationContext;
    private static final Object LOCK = new Object();

    public CommonScanProcess(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        baseRepoRestManager = applicationContext.getBean(BaseRepoRestManager.class);
    }

    @Async("taskExecutor")
    public void scan(String repoUuid, String branch, String beginCommit, String endCommit) throws CodePathGetFailedException, RepoScanGetException {
        // todo 查询repository表 来查看repo 所包含的语言 让后决定采用什么工具来调用
        String[] tools = getToolsByRepo(repoUuid);

        for (String tool : tools) {
            RepoScan scanInfo = getRepoScanStatus(repoUuid, tool);

            boolean isFirstScan = !StringUtils.isEmpty(beginCommit);
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
            beginScan(repoUuid, branch, beginCommit, tool, endCommit);
            checkAfterScan(repoUuid, branch, tool, endCommit);
        }
    }


    /**
     * 设置同步状态 处理接口请求 判断扫描是否需要更新
     **/
    public void scan(String repoUuid, String branch, String beginCommit) throws CodePathGetFailedException, RepoScanGetException {
        scan(repoUuid, branch, beginCommit, null);
    }

    protected String generateKey(String repoUuid, String tool) {
        return repoUuid + KEY_DELIMITER + tool;
    }

    /**
     * 一个 commitList 扫描完成之后再次检查 查看是否有更新扫描的请求
     **/
    private void checkAfterScan(String repoUuid, String branch, String tool, String endCommit) throws CodePathGetFailedException, RepoScanGetException {
        String key = generateKey(repoUuid, tool);
        if (!scanStatusMap.containsKey(key)) {
            log.error("{} : not in cn.edu.fudan.common.scan scanStatusMap", repoUuid);
            return;
        }
        synchronized (LOCK) {
            boolean newUpdate = scanStatusMap.get(key);
            if (!newUpdate) {
                scanStatusMap.remove(key);
                return;
            }
            scanStatusMap.put(key, false);
        }
        beginScan(repoUuid, branch, null, tool, endCommit);
        checkAfterScan(repoUuid, branch, tool, endCommit);
    }

    void beginScan(String repoUuid, String branch, String beginCommit, String tool, String endCommit) throws CodePathGetFailedException, RepoScanGetException {
        Thread curThread = Thread.currentThread();
        String threadName = generateKey(repoUuid, tool);
        curThread.setName(threadName);
        //获取tool scan
        ToolScan specificTool = getToolScan(tool);

        // 获取repo所在路径
        log.info("repoUuid:{} ", repoUuid);
        String repoPath = baseRepoRestManager.getCodeServiceRepo(repoUuid);
        if (repoPath == null) {
            log.error("{} : can't get repoPath", repoUuid);
            throw new CodePathGetFailedException("can't get repo path");
        }

        RepoScan repoScan = prepareRepoScan(specificTool, repoUuid, tool, repoPath, branch, beginCommit, endCommit);
        if (repoScan == null) {
            log.info("scan list size is 0, scan finish");
            return;
        }

        try {
            specificTool.prepareForScan();
            for (String commit : specificTool.getScanData().getToScanCommitList()) {
                log.info("begin scan {}", commit);
                specificTool.prepareForOneScan(commit);
                specificTool.scanOneCommit(commit);
                specificTool.cleanUpForOneScan(commit);
                if (curThread.isInterrupted()) {
                    synchronized (LOCK) {
                        scanStatusMap.remove(threadName);
                    }
                    log.warn("thread:{} stopped", threadName);
                    break;
                }
                repoScan.setEndScanTime(new Date());
                repoScan.setScannedCommitCount(repoScan.getScannedCommitCount() + 1);
                repoScan.setScanTime((repoScan.getEndScanTime().getTime() - repoScan.getStartScanTime().getTime()) / 1000);
                updateRepoScan(repoScan);
            }
            repoScan.setStatus(ScanInfo.Status.COMPLETE.getStatus());
            specificTool.cleanUpForScan();
        } catch (Exception e) {
            e.printStackTrace();
            repoScan.setStatus(ScanInfo.Status.FAILED.getStatus());
            repoScan.setEndScanTime(new Date());
            repoScan.setScanTime((repoScan.getEndScanTime().getTime() - repoScan.getStartScanTime().getTime()) / 1000);
        } finally {
            updateRepoScan(repoScan);
            baseRepoRestManager.freeRepo(repoUuid, repoPath);
        }

    }

    private RepoScan prepareRepoScan(ToolScan specificTool, String repoUuid, String tool, String repoPath, String branch, String beginCommit, String endCommit) throws RepoScanGetException {

        List<String> scannedCommitList = getScannedCommitList(repoUuid, tool);
        log.info("scannedCommitList.size():{}", scannedCommitList.size());
        boolean initialScan = scannedCommitList.isEmpty();

        if (StringUtils.isEmpty(beginCommit)) {
            beginCommit = getLastedScannedCommit(repoUuid, tool);
            if (beginCommit == null) {
                log.error("get begin commit error");
                throw new RepoScanGetException("get begin commit error");
            }
        }
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        List<String> toScanCommitList = jGitHelper.getScanCommitListByBranchAndBeginCommit(branch, beginCommit, scannedCommitList);
        jGitHelper.close();

        // 筛选出end commit
        if (!StringUtils.isEmpty(endCommit)) {
            int end = toScanCommitList.indexOf(endCommit);
            if (end == -1) {
                log.error("cannot find end commit {}", endCommit);
            } else {
                toScanCommitList = toScanCommitList.subList(0, end + 1);
                log.info("setting end commit {}", endCommit);
            }
        }

        if (toScanCommitList.isEmpty()) {
            return null;
        }

        log.info("commit size : {}", toScanCommitList.size());

        RepoScan repoScan = getRepoScan(repoUuid, tool, branch, toScanCommitList.size());
        if (repoScan == null) {
            repoScan = RepoScan.builder()
                    .repoUuid(repoUuid)
                    .branch(branch)
                    .status(ScanInfo.Status.SCANNING.getStatus())
                    .initialScan(true)
                    .tool(tool)
                    .scannedCommitCount(0)
                    .startScanTime(new Date())
                    .endScanTime(new Date())
                    .totalCommitCount(toScanCommitList.size())
                    .scanTime(0)
                    .startCommit(toScanCommitList.get(0))
                    .build();
            insertRepoScan(repoScan);
        } else {
            if (ScanInfo.Status.FAILED.getStatus().equals(repoScan.getStatus())) {
                log.error("please check the repo last time why failed!");
                throw new RepoScanGetException("the repo scan last time failed");
            }
            updateRepoScan(repoScan);
        }
        // loadData 用于传输扫描的信息
        specificTool.loadData(repoUuid, branch, repoPath, initialScan, toScanCommitList, repoScan, repoScan.getScannedCommitCount());

        return repoScan;
    }

    /**
     * 获取ToolScan
     *
     * @param tool tool
     * @return
     */
    protected abstract ToolScan getToolScan(String tool);

    /**
     * 根据repoUuid tool 代码库的地址决定需要调用的工具列表
     *
     * @param repoUuid repoUuid
     * @param tool     tool
     * @return
     */
    protected abstract List<String> getScannedCommitList(String repoUuid, String tool);

    /**
     * 根据表中的记录得到最新扫描的commit id
     *
     * @param repoUuid repoUuid
     * @param tool     tool
     * @return
     */
    protected abstract String getLastedScannedCommit(String repoUuid, String tool);

    /**
     * 根据uuid和代码库的地址决定需要调用的工具列表
     *
     * @param repoUuid repoUuid
     * @return
     */
    protected abstract String[] getToolsByRepo(String repoUuid);


    /**
     * 插入当前repo的扫描信息
     *
     * @param repoScan repoScan
     */
    protected abstract void insertRepoScan(RepoScan repoScan);

    /**
     * 获取已扫描的的repo信息
     *
     * @param repoUuid
     * @param tool
     * @param branch
     * @param needScanCommit
     * @return
     */
    protected abstract RepoScan getRepoScan(String repoUuid, String tool, String branch, int needScanCommit);

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
