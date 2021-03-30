package cn.edu.fudan.dependservice.service;

import cn.edu.fudan.common.component.BaseRepoRestManager;
import cn.edu.fudan.common.domain.ScanInfo;
import cn.edu.fudan.common.domain.po.scan.RepoScan;
import cn.edu.fudan.common.jgit.JGitHelper;
import cn.edu.fudan.common.scan.CommonScanProcess;
import cn.edu.fudan.common.scan.CommonScanService;
import cn.edu.fudan.common.scan.ToolScan;
import cn.edu.fudan.dependservice.domain.RepoRestManager;
import cn.edu.fudan.dependservice.mapper.GroupMapper;
import cn.edu.fudan.dependservice.service.impl.ToolScanImpl;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
@Data
@Service
public class TempProcess implements CommonScanService {
    private static final Logger log = LoggerFactory.getLogger(CommonScanProcess.class);
    private static final String KEY_DELIMITER = "-";
    private String repo_path;
    RepoScan repoScan;

    ApplicationContext applicationContext;

    @Autowired
    GroupMapper groupMapper;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    /**
     * key repoUuid
     * value true/false true 代表还需要更新扫描一次
     **/
    private final ConcurrentHashMap<String, Boolean> scanStatusMap = new ConcurrentHashMap<>();
    protected BaseRepoRestManager baseRepoRestManager;
    private final Short lock = 1;
    protected ToolScan getToolScan(String tool) {
        //todo retur tool by tool name
        return applicationContext.getBean(ToolScanImpl.class);
    }
    protected String[] getToolsByRepo(String repoUuid) {
        return new String[]{"ToolScanImpl"};
//        return new String[0];
    }

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
    protected List<String> getScannedCommitList(String repoUuid, String tool) {
        //need find in data base.
        // tool is dependency
        return  groupMapper.getScannedCommitList(repoUuid);
    }
    protected String getLastedScannedCommit(String repoUuid, String tool) {
        List<String> scannedCommitList =getScannedCommitList(repoUuid,tool);
        if(scannedCommitList.size()==0) return null;
        String res=scannedCommitList.get(0);
        JGitHelper jg =new JGitHelper(getRepo_path());
        Date  resDate=jg.getCommitDateTime(res);
        int num=0;
        for(String commit:scannedCommitList){
            Date thisDate=jg.getCommitDateTime(commit);
            if(thisDate!=null) num++;
            if(thisDate!=null&&thisDate.compareTo(resDate)>0){
                res= commit;
                resDate=thisDate;
            }
        }
        log.info("num of data not null :"+num);
        return res;
    }
    protected String getLastedCommit(List<String> toScanCommitList, String repo_path) {
//        List<String> scannedCommitList =getScannedCommitList(repoUuid,tool);
        if(toScanCommitList.size()==0) return null;
        String res=toScanCommitList.get(0);
        JGitHelper jg =new JGitHelper(getRepo_path());
        Date  resDate=jg.getCommitDateTime(res);
        int num=0;
        for(String commit:toScanCommitList){
            Date thisDate=jg.getCommitDateTime(commit);
            if(thisDate!=null) num++;
            if(thisDate!=null&&thisDate.compareTo(resDate)>0){
                res= commit;
                resDate=thisDate;
            }
        }
        log.info("num of data not null :"+num);
        return res;
    }


    protected void insertRepoScan(RepoScan repoScan) {
        this.repoScan=repoScan;

    }

//    @Async("taskExecutor")
    void beginScan(String repoUuid, String branch, String beginCommit, String tool) {
        Thread curThread = Thread.currentThread();
        String threadName = generateKey(repoUuid, tool);
        curThread.setName(threadName);

        ToolScan specificTool = getToolScan(tool);
        String repoPath= null;
        try {
            repoPath = baseRepoRestManager.getCodeServiceRepo(repoUuid);
        }catch (Exception e){
            log.info("Exception: "+ e.getMessage());
        }
        if (repoPath == null) {
            log.error("{} : can't get repoPath", repoUuid);
            return;
        }
        List<String> scannedCommitList = getScannedCommitList(repoUuid, tool);
        if(scannedCommitList.size()>=1) return;
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
        this.repo_path=repoPath;
        try {
            String toScanCommit = new JGitHelper(repoPath).getLatestCommitByBranch(branch);
            log.info("toScanCommit:"+toScanCommit);
            insertRepoScan(repoScan);
            boolean success = false;
            specificTool.loadData(repoUuid, branch, repoPath, initialScan,null, repoScan, scannedCommitCount);
            specificTool.prepareForScan();
            specificTool.prepareForOneScan(toScanCommit);
            success = specificTool.scanOneCommit(toScanCommit);
            specificTool.cleanUpForOneScan(toScanCommit);
            scannedCommitCount++;
            if(curThread.isInterrupted()){
                synchronized (lock) {
                    scanStatusMap.remove(threadName);
                }
                log.warn("thread:{} stopped", threadName);
            }

//            for (String commit : toScanCommitList) {
//                //
//
//                specificTool.prepareForOneScan(commit);
//                success = specificTool.scanOneCommit(commit);
//                specificTool.cleanUpForOneScan(commit);
//                scannedCommitCount++;
//                if(curThread.isInterrupted()){
//                    synchronized (lock) {
//                        scanStatusMap.remove(threadName);
//                    }
//                    log.warn("thread:{} stopped", threadName);
//                    break;
//                }
//                break;
//            }
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
            baseRepoRestManager.freeRepo(repoUuid, repoPath);
        }

    }



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
    public void deleteRepo(String repoUuid) {

    }

    @Override
    public void deleteRepo(String repoUuid, String toolName) {

    }

    @Override
    public RepoScan getRepoScanStatus(String repoUuid, String toolName) {
        return null;
    }

    @Override
    public RepoScan getRepoScanStatus(String repoUuid) {
        return null;
    }

    @Override
    public void updateRepoScan(RepoScan scanInfo) {


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
