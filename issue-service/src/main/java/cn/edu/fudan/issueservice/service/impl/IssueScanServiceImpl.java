package cn.edu.fudan.issueservice.service.impl;

import cn.edu.fudan.issueservice.annotation.GetResource;
import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.config.ScanThreadExecutorConfig;
import cn.edu.fudan.issueservice.core.ScanManagementAsync;
import cn.edu.fudan.issueservice.dao.CommitDao;
import cn.edu.fudan.issueservice.dao.IssueRepoDao;
import cn.edu.fudan.issueservice.dao.IssueScanDao;
import cn.edu.fudan.issueservice.domain.dbo.Commit;
import cn.edu.fudan.issueservice.domain.dbo.IssueRepo;
import cn.edu.fudan.issueservice.domain.dbo.IssueScan;
import cn.edu.fudan.issueservice.domain.dto.RepoResourceDTO;
import cn.edu.fudan.issueservice.domain.dto.ScanCommitInfoDTO;
import cn.edu.fudan.issueservice.domain.enums.RepoNatureEnum;
import cn.edu.fudan.issueservice.service.IssueScanService;
import cn.edu.fudan.issueservice.util.JGitHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/** -【【lsw
 * @author lsw
 */
@Service
@Slf4j
@SuppressWarnings("unchecked")
public class IssueScanServiceImpl implements IssueScanService {

    private BlockingQueue<ScanCommitInfoDTO> scanCommitInfoDTOBlockingQueue;
    private StringRedisTemplate stringRedisTemplate;
    private IssueScanDao issueScanDao;
    private ScanManagementAsync scanManagementAsync;
    private IssueRepoDao issueRepoDao;
    private ApplicationContext applicationContext;
    private RestInterfaceManager restInvoker;
    private CommitDao commitDao;


    @GetResource
    @Override
    public String prepareForScan(RepoResourceDTO repoResourceDTO, String branch, String beginCommit, String toolName) {
        String repoPath;
        try{
            String repoId = repoResourceDTO.getRepoId ();
            repoPath = repoResourceDTO.getRepoPath ();
            if(repoPath == null ){
                throw  new RuntimeException ("can't get repo path!");
            }
            boolean isUpdate = false;

            ScanCommitInfoDTO scanCommitInfoDTO = ScanCommitInfoDTO.builder()
                    .repoId(repoId).branch(branch).toolName(toolName).build();

            if(beginCommit == null){
                isUpdate = true;
            }

            //再次验证项目是否已经扫描过，避免因scan服务发送重复的请求导致数据库数据重复。
            if(beginCommit != null){
                List<IssueScan> issueScans = issueScanDao.getScannedCommitsByRepoIdAndTool (repoId, toolName, null, null);
                if(issueScans != null && !issueScans.isEmpty ()){
                    isUpdate = true;
                }
            }

            // todo 如果更新接口与初次扫描接口分离的话 ，还需验证是否已经扫描过，且是否已经更新至最新
            //第一步，先判断是否在扫描队列中等待扫描 , 实现equals方法，如果repo id 相同，则认为是同一个ScanCommitInfoDTO
            if(scanCommitInfoDTOBlockingQueue.contains (scanCommitInfoDTO)){
                return "scanning";
            }

            JGitHelper jGitInvoker = new JGitHelper (repoPath);

            //第二步判断是否已经在扫描  todo 逻辑比较乱， 后面需要重构
            String redisValue = stringRedisTemplate.opsForValue ().get (repoId + "-" + toolName);
            boolean isCorrectThreadName = false;
            if(redisValue != null ){
                isCorrectThreadName  = redisValue.matches ("^async-issue-scan.*");
            }
            if(isCorrectThreadName){
                ScanThreadExecutorConfig.updateScannedRepoStatus (repoId, toolName);
                return "scanning";
            }else{
                stringRedisTemplate.opsForValue ().getOperations().delete (repoId);
            }

            //第三步，判断是第一次扫描还是更新扫描,并更新scanCommitInfoDTO
            if(isUpdate){
                IssueScan issueScan = issueScanDao.getLatestIssueScanByRepoIdAndTool (repoId, toolName);
                if(issueScan == null ){
                    // todo 需返回提示，该项目未扫描，请提供 begin Commit ，此时应该报错，给scan服务调用失败的提示，这样scan就可以根据此情况，重新发送begin commit
                    return "please provide  begin commit !";
                }
                String latestScannedCommitId = issueScan.getCommitId ();
                List<String> commitIds =  jGitInvoker.getCommitListByBranchAndBeginCommit(branch, latestScannedCommitId);
                //因为必定不为null，所以不做此判断
                if(commitIds.size () <= 1){
                    return "scanned";
                }
                beginCommit = commitIds.get (1);
                scanCommitInfoDTO.setIsUpdate (true);
            }
            scanCommitInfoDTO.setCommitId (beginCommit);

            //第四步，加入扫描池队列中
            scanManagementAsync.addProjectToScanQueue(scanCommitInfoDTO);

            //判断 扫描消费线程池 正在运行的线程是否少于10
            if(ScanThreadExecutorConfig.getConsumerThreadPoolAliveThreadCounts() < 10){
                scanManagementAsync.getProjectFromScanQueue();
            }

            return "start scanning";
        }finally{
            log.info("free repo:{}, path:{}", repoResourceDTO.getRepoId(), repoResourceDTO.getRepoPath());
            restInvoker.freeRepoPath(repoResourceDTO.getRepoId(), repoResourceDTO.getRepoPath());
        }

    }

    @Override
    public void stopScan(String  repoId, String toolName){
        ScanCommitInfoDTO scanCommitInfoDTO = new ScanCommitInfoDTO();
        scanCommitInfoDTO.setRepoId (repoId);
        scanCommitInfoDTO.setToolName (toolName);
        boolean result = false;
        //todo 1. 直接调用BlockingQueue的 remove方法，如果删除成功返回true ，如果不存在则返回false
        result = scanCommitInfoDTOBlockingQueue.remove (scanCommitInfoDTO);
        //todo 此处采用抛出异常的方式还是返回布尔值有待权衡
        if(result){
            return;
        }

        //2. 判断该项目是否已经在扫描
        // 此处因为在判断的时候，没有上锁，可能会存在判断的时候正在扫描，但是清除的时候项目已经扫描完成了，此时调用interrupt会影响到其他项目的扫描。
        // 所以加了开关跟interrupt双验证
        String threadName = stringRedisTemplate.opsForValue ().get (repoId + "-" + toolName);
        if(threadName != null){
            Thread scanThread = getThreadByThreadName(threadName);
            if(scanThread != null){
                scanThread.interrupt ();
                ScanThreadExecutorConfig.setConsumerThreadSwitch (repoId, false, toolName);
            }
        }
    }

    @Override
    public IssueRepo getScanStatus(String  repoId, String toolName) throws Exception {
        List<IssueRepo> issueRepos = issueRepoDao.getIssueRepoByCondition (repoId, null, toolName);
        //三种情况
        if(issueRepos == null || issueRepos.isEmpty ()){
            //第一种还未扫描
            return null;
        }else if(issueRepos.size () == 1){
            return issueRepos.get (0);
        }else{
            IssueRepo updateIssueRepo = null;
            for(IssueRepo issueRepo : issueRepos){
                if(RepoNatureEnum.UPDATE.getType ().equals (issueRepo.getNature ())){
                    updateIssueRepo = issueRepo;
                }
            }
            return updateIssueRepo;
        }
    }

    @Override
    public Map<String, Object> getCommitsCount(String repoUuid, String tool) {
        AtomicInteger notScanCommitCount = new AtomicInteger();

        List<HashMap<String, Integer>> notScanCommitsInfos = issueRepoDao.getNotScanCommitsCount(repoUuid, tool);

        notScanCommitsInfos.forEach(notScanCommitsInfo -> notScanCommitCount.addAndGet(notScanCommitsInfo.get("total_commit_count") - notScanCommitsInfo.get("scanned_commit_count")));

        return new HashMap<String, Object>(8){{put("total", notScanCommitCount);}};
    }

    @Override
    public Map<String, Object> getCommits(String repoUuid, Integer page, Integer size, Boolean isWhole, String tool) {

        HashSet<String> scannedCommitList = issueScanDao.getScannedCommitList(repoUuid, tool);

        LinkedList<Commit> wholeCommits = commitDao.getCommits(repoUuid,null);

        if(isWhole){
            wholeCommits.forEach(commit -> commit.setScanned(scannedCommitList.contains(commit.getCommitId())));
            return new HashMap<String, Object>(8){{
                put("total", wholeCommits.size());
                put("commitList", wholeCommits.subList((page - 1) * size, Math.min(page * size, wholeCommits.size())));
                put("pageCount", wholeCommits.size() % size != 0 ? wholeCommits.size() / size + 1 : wholeCommits.size()/size);
            }};
        }

        String commitTime = commitDao.getCommitByCommitId(repoUuid, issueRepoDao.getMainIssueRepo(repoUuid, tool).getStartCommit()).getCommit_time();

        LinkedList<Commit> commits = commitDao.getCommits(repoUuid, commitTime.substring(0, 19));

        commits.removeIf(commit -> scannedCommitList.contains(commit.getCommitId()));

        return new HashMap<String, Object>(8){{
            put("total", commits.size());
            put("commitList", commits.subList((page - 1) * size, Math.min(page * size, commits.size())));
            put("pageCount", commits.size() % size != 0 ? commits.size() / size + 1 : commits.size()/size);
        }};
    }

    private Thread getThreadByThreadName(String threadName){
        Thread result = null;
        if(threadName == null ){
            return null;
        }
        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        int noThreads = currentGroup.activeCount();
        Thread[] lstThreads = new Thread[noThreads];
        currentGroup.enumerate(lstThreads);
        for (int i = 0; i < noThreads; i++){
            if(threadName.equals (lstThreads[i].getName ())){
                result =  lstThreads[i];
            }
        }
        return result;
    }

    /**
     * 获取正在扫描的repo commit列表需要增加更新的commit集合
     * @param repoId
     * @param jGitInvoker
     * @param branch
     * @return
     */
    private List<String> getCommitList(String repoId, JGitHelper jGitInvoker, String branch, String toolName){
        List<String> commitList = null;
        String lastCommit = ScanThreadExecutorConfig.getLastCommitIdFromCommitList (repoId, toolName);
        commitList = jGitInvoker.getCommitListByBranchAndBeginCommit (branch,lastCommit);
        if(commitList.size () <= 1){
            return null;
        }
        commitList = commitList.subList (1, commitList.size ()-1);
        return commitList;
    }



    @Autowired
    public void setScanCommitInfoDTOBlockingQueue(BlockingQueue<ScanCommitInfoDTO> scanCommitInfoDTOBlockingQueue) {
        this.scanCommitInfoDTOBlockingQueue = scanCommitInfoDTOBlockingQueue;
    }

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Autowired
    public void setIssueScanDao(IssueScanDao issueScanDao) {
        this.issueScanDao = issueScanDao;
    }

    @Autowired
    public void setScanManagementAsync(ScanManagementAsync scanManagementAsync) {
        this.scanManagementAsync = scanManagementAsync;
    }

    @Autowired
    public void setIssueRepoDao(IssueRepoDao issueRepoDao) {
        this.issueRepoDao = issueRepoDao;
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setRestInvoker(RestInterfaceManager restInvoker) {
        this.restInvoker = restInvoker;
    }

    @Autowired
    public void setCommitDao(CommitDao commitDao) {
        this.commitDao = commitDao;
    }
}
