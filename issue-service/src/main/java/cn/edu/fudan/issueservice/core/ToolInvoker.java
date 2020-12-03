package cn.edu.fudan.issueservice.core;


import cn.edu.fudan.issueservice.component.ApplicationContextGetBeanHelper;
import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.config.ScanThreadExecutorConfig;
import cn.edu.fudan.issueservice.core.process.*;
import cn.edu.fudan.issueservice.core.strategy.MatchStrategy;
import cn.edu.fudan.issueservice.core.strategy.ScanStrategy;
import cn.edu.fudan.issueservice.dao.*;
import cn.edu.fudan.issueservice.domain.dbo.*;
import cn.edu.fudan.issueservice.domain.dto.RepoResourceDTO;
import cn.edu.fudan.issueservice.domain.enums.*;
import cn.edu.fudan.issueservice.util.CompileUtil;
import cn.edu.fudan.issueservice.util.DirExplorer;
import cn.edu.fudan.issueservice.util.JGitHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * description:
 *
 * @author fancying
 * create: 2020-05-19 21:36
 **/
@Slf4j
@Service
public class ToolInvoker {

    @Autowired
    private CommitDao commitDao;
    private RawIssueDao rawIssueDao;
    private IssueScanDao issueScanDao;
    private IssueDao issueDao;
    private IssueTypeDao issueTypeDao;
    private RestInterfaceManager restInvoker;
    private ApplicationContext applicationContext;
    private IssueRepoDao issueRepoDao;
    @Value("${binHome}")
    protected String binHome;

    @Autowired
    @Qualifier("TMatchStrategy")
    MatchStrategy matchStrategy;

    @Value("${resultFileHome}")
    private String findbugsResultFileHome;
    /**
     * 工具调用
     */
    @SneakyThrows
    public void invoke(String repoId, String branch, String beginCommit, String toolName) {
        ApplicationContextGetBeanHelper applicationContextGetBeanHelper = new ApplicationContextGetBeanHelper ();
        applicationContextGetBeanHelper.setApplicationContext (applicationContext);
        //1.获取相应的扫描工具

        BaseAnalyzer analyzer;

        // todo 处理多线程的多例情况 目前因为存在扫描问题，故先直接new一个对象 ,后面解决了prototype问题后，重新优化结构
        if(ToolEnum.SONAR.getType ().equals (toolName)){
            SonarQubeBaseAnalyzer sonarQubeBaseAnalyzer = new SonarQubeBaseAnalyzer ();
            sonarQubeBaseAnalyzer.setRestInvoker (restInvoker);
            sonarQubeBaseAnalyzer.setCommitDao(commitDao);
            analyzer = sonarQubeBaseAnalyzer;
        }else if (ToolEnum.FINDBUGS.getType ().equals (toolName)) {
            FindbugsBaseAnalyzer findbugsBaseAnalyzer = new FindbugsBaseAnalyzer ();
            findbugsBaseAnalyzer.setResultFileHome (findbugsResultFileHome);
            analyzer = findbugsBaseAnalyzer;
        }else {
            log.error ("toolName is error , do not have {}-->", toolName);
            return ;
        }
        analyzer.setBinHome (binHome);

        //2. TODO 获取相应扫描策略
        String strategyName = "PSSS";
        ScanStrategy scanStrategy = (ScanStrategy) applicationContext.getBean(strategyName);

        //3.设置所需的资源
        RepoResourceDTO repoResourceDTO = new RepoResourceDTO();
        repoResourceDTO.setRepoId(repoId);

        //4. 开始执行扫描
        Assert.notNull(analyzer,"tool " + toolName + " analyzer is null");
        boolean isFirst = true;
        while(isFirst || ScanThreadExecutorConfig.getRepoUpdateStatus(repoId, toolName)){
            ScanThreadExecutorConfig.delRepoUpdateStatus (repoId, toolName);
            boolean scanResult = executeScan(repoResourceDTO, analyzer, scanStrategy, branch, beginCommit);
            if(!scanResult){
                ScanThreadExecutorConfig.delRepoUpdateStatus (repoId, toolName);
                break;
            }
            isFirst = false;
            beginCommit = null;
        }

    }


    private boolean executeScan(RepoResourceDTO repoResourceDTO, BaseAnalyzer analyzer,
                            ScanStrategy scanStrategy,
                            String branch,
                            String beginCommit) throws InterruptedException{
        boolean result = true;
        String repoPath = null;
        try{
            repoPath = restInvoker.getRepoPath (repoResourceDTO.getRepoId());
            repoResourceDTO.setRepoPath (repoPath);
            if(repoPath == null || "".equals (repoPath)){
                log.error ("can not get repo path! repo id --> {}", repoResourceDTO.getRepoId ());
                return false ;
            }
            log.info ("get repo path --> {}" , repoPath);
            String repoId = repoResourceDTO.getRepoId();
            String toolName = analyzer.getToolName();

            //1.配置jGit资源
            JGitHelper jGitInvoker = new JGitHelper (repoPath);
            //判断beginCommit 是否为空，如果为空则获取
            if(beginCommit == null){
                IssueScan latestIssueScan = issueScanDao.getLatestIssueScanByRepoIdAndTool (repoId, toolName);
                if(latestIssueScan == null ){
                    log.error (" need begin commit!");
                    return false;
                }
                String latestScannedCommitId = latestIssueScan.getCommitId ();
                List<String> commitIds =  jGitInvoker.getScanCommitListByBranchAndBeginCommit(branch, latestScannedCommitId);
                //因为必定不为null，所以不做此判断
                if(commitIds.size () <= 1){
                    log.info (" already update! repoId --> {}", repoId);
                    return false;
                }
                beginCommit = commitIds.get (1);
            }

            IssueMatcher issueMatcher = new IssueMatcher (issueDao, rawIssueDao, issueScanDao, matchStrategy);
            IssueStatisticalTool issueStatisticalTool = new IssueStatisticalTool (issueDao, issueScanDao, issueTypeDao);
            IssueScanTransactionManager issueScanTransactionManager = (IssueScanTransactionManager)applicationContext.getBean("DataPersistManager");

            //2.根据策略获取扫描的commit列表
            ConcurrentLinkedDeque<String>  scanCommits = scanStrategy.getScanCommitLinkedQueue(repoId, jGitInvoker, branch, beginCommit);

            if(scanCommits == null || scanCommits.isEmpty ()){
                log.error ("get commit List failed or all commits were scanned! repo id --> {}", repoId);
                return false;
            }
            //并且存入全局扫描 commit列表进行管理
            ScanThreadExecutorConfig.setNeedToScanCommitLists (repoId, scanCommits, toolName);

            //3.初始化issue repo信息 并持久化到数据库中
            IssueRepo issueRepo = getIssueRepoByDifferentScenarios(repoId, branch, toolName, beginCommit, scanCommits.size ());

            log.info ("start scan !  repo id --> {}", repoId);
            //4.遍历扫描
            //设定开关确认是否是stop操作。
            boolean isStop = false;

            for (String commit : scanCommits) {
                long thisCommitStartMilliTime = System.currentTimeMillis ();
                boolean threadSwitch = ScanThreadExecutorConfig.getConsumerThreadSwitch(repoId,toolName);
                if( threadSwitch && !Thread.currentThread ().isInterrupted()){
                    log.info ("start scan  commit id --> {}", commit);

                    //4.1 初始化 一个IssueScan用于记录scan过程
                    Date commitTime = jGitInvoker.getCommitDateTime(commit);
                    IssueScan issueScan = IssueScan.initIssueScan (repoId, commit, toolName, commitTime);

                    //4.2 checkout到指定的版本
                    jGitInvoker.checkout(commit);

                    //4.3 清理上一次工具扫描时工具存储的结果
                    analyzer.emptyAnalyzeRawIssues ();
                    issueMatcher.emptyMatchResult ();
                    issueStatisticalTool.emptyStatisticalResult();

                    //4.4 执行扫描
                    scan(issueScan, repoResourceDTO, analyzer, jGitInvoker, issueMatcher, issueStatisticalTool, issueScanTransactionManager);

                    //4,5 更新issueScan 结束时间
                    issueScan.setEndTime (new Date ());

                    //4.5 issueScan 入库
                    boolean scanPersistenceResult =  issueScanPersistence(issueScan);
                    if(!scanPersistenceResult){
                        log.error (" issue scan result  persist failed! commit id --> {}" , commit);
                    }

                    log.info (" issue scan result  persist success");

                    //更新 issue repo 信息 ，包括已扫描条数+1 ,  扫描用时 , end commit, end date
                    //更新扫描用时
                    long thisCommitEndMilliTime = System.currentTimeMillis ();
                    int consumingTimeSec = (int)(thisCommitEndMilliTime - thisCommitStartMilliTime) / 1000 ;
                    issueRepo.setScanTime (consumingTimeSec + issueRepo.getScanTime ());

                    issueRepo.setEndCommit (commit);
                    issueRepo.setEndScanTime (new Date ());

                    issueRepo.incrementScannedCommitCount();
                    issueRepoDao.updateIssueRepo (issueRepo);
                }else{
                    isStop = true;
                    result = false;
                    break;
                }

            }

            //更新issue repo 的 status
            if(isStop){
                issueRepo.setStatus (RepoStatusEnum.STOP.getType ());

            }else{
                issueRepo.setStatus (RepoStatusEnum.COMPLETE.getType ());
            }
            issueRepoDao.updateIssueRepo (issueRepo);
        }finally{
            if(repoPath != null){
                restInvoker.freeRepoPath(repoResourceDTO.getRepoId(), repoResourceDTO.getRepoPath());
            }
        }
        return result;

    }

    /**
     * FIXME 加入回滚策略
     */
    @Transactional(rollbackFor = Exception.class)
    public void scan(IssueScan issueScan,
                      RepoResourceDTO repoResourceDTO,
                      BaseAnalyzer analyzer,
                      JGitHelper jGitInvoker,
                      IssueMatcher issueMatcher,
                      IssueStatisticalTool issueStatisticalTool,
                      IssueScanTransactionManager issueScanTransactionManager) throws InterruptedException {
        String repoId = repoResourceDTO.getRepoId ();
        String repoPath = repoResourceDTO.getRepoPath ();
        String commit = issueScan.getCommitId ();

        long startTime = System.currentTimeMillis();
        //0. 先清除编译生成的target文件
        DirExplorer.deleteRedundantTarget(repoPath);
        long deleteTargetTime = System.currentTimeMillis();
        log.info("delete target time --> {}", (deleteTargetTime-startTime)/1000 );

        //1. 先判断是否可编译 以及是否编译成功
        if(!CompileUtil.isCompilable(repoPath)){
            log.error ("compile failed ! ");
            issueScan.setStatus (ScanStatusEnum.COMPILE_FAILED.getType ());
            return ;
        }
        long compileTime = System.currentTimeMillis();
        log.info("compile time --> {}\n compile success ! ", (compileTime-deleteTargetTime)/1000 );

        //2. 调用工具进行扫描
        boolean invokeToolResult = analyzer.invoke(repoId, repoPath, commit);
        if(!invokeToolResult){
            long invokeToolTime = System.currentTimeMillis();
            log.info("invoke tool --> {}", (invokeToolTime-compileTime)/1000 );
            log.error ("invoke tool failed ! " );
            issueScan.setStatus (ScanStatusEnum.INVOKE_TOOL_FAILED.getType ());
            return ;
        }
        long invokeToolTime = System.currentTimeMillis();
        log.info("invoke tool --> {}", (invokeToolTime-compileTime)/1000 );
        log.info ("invoke tool success ! " );

        //3. 调用工具进行解析
        boolean analyzeResult = analyzer.analyze(repoPath, repoId, commit);
        if(!analyzeResult){
            log.error ("analyze failed ! " );
            issueScan.setStatus (ScanStatusEnum.ANALYZE_FAILED.getType ());
            return ;
        }
        long analyzeToolTime = System.currentTimeMillis();
        log.info("analyze tool --> {}", (analyzeToolTime-invokeToolTime)/1000 );
        log.info ("analyze success ! " );

        List<RawIssue> analyzeRawIssues = analyzer.getResultRawIssues ();
        //4. 缺陷匹配
        boolean matchResult = issueMatcher.matchProcess (repoId, commit, jGitInvoker, analyzer.getToolName (), analyzeRawIssues);
        if(!matchResult){
            log.error ("match failed ! " );
            issueScan.setStatus (ScanStatusEnum.MATCH_FAILED.getType ());
            return ;
        }
        log.info ("match success ! " );

        //5. 更新issue信息 ,做相应的缺陷统计
        List<RawIssue> currentRawIssuesResult = issueMatcher.getCurrentRawIssuesResult ();
        Map<String, List<RawIssue>> parentRawIssuesResult = issueMatcher.getParentRawIssuesResult ();
        boolean statisticalResult = issueStatisticalTool.doingStatisticalAnalysis (repoId, commit, jGitInvoker,
                currentRawIssuesResult, parentRawIssuesResult, analyzer);
        if(!statisticalResult){
            log.error ("statistical failed ! " );
            issueScan.setStatus (ScanStatusEnum.STATISTICAL_FAILED.getType ());
            return ;
        }

        log.info ("statistical success ! " );

        //6. 持久化扫描结果
        try{
            issueScanTransactionManager.persistScanData (currentRawIssuesResult, parentRawIssuesResult, issueScan , issueStatisticalTool);
        }catch (Exception e){
            e.printStackTrace ();
            log.error ("persist failed ! " );
            issueScan.setStatus (ScanStatusEnum.STATISTICAL_FAILED.getType ());
            return ;
        }

        log.info ("persist data success ! " );

        issueScan.setStatus (ScanStatusEnum.DONE.getType ());
    }

    private boolean issueScanPersistence(IssueScan issueScan){
        try{
            issueScanDao.insertOneIssueScan (issueScan);

        }catch(Exception e){
            e.printStackTrace ();
            return false;
        }
        return true;
    }

    private IssueRepo getIssueRepoByDifferentScenarios(String repoId, String branch,
                                                       String toolName,
                                                     String beginCommit,
                                                     int commitSize){
        IssueRepo resultIssueRepo ;
        List<IssueRepo> issueRepos = issueRepoDao.getIssueRepoByCondition (repoId, null ,toolName);
        if(issueRepos == null || issueRepos.isEmpty ()){
            //第一种情况 未扫描过，数据库无记录
            resultIssueRepo = IssueRepo.initIssueRepo (repoId, branch, beginCommit, toolName, commitSize);
            resultIssueRepo.setNature (RepoNatureEnum.MAIN.getType ());
            issueRepoDao.insertOneIssueRepo (resultIssueRepo);

        }else{
            //如果此时存在update记录，则先将原来的update记录合并至主记录先
            IssueRepo preUpdateIssueRepo = null;
            IssueRepo mainIssueRepo = null;
            boolean mappedUpdate = false;
            for(IssueRepo issueRepo :issueRepos){
                if(RepoNatureEnum.UPDATE.getType ().equals (issueRepo.getNature())){
                    mappedUpdate = true;
                    preUpdateIssueRepo = issueRepo;
                }else{
                    mainIssueRepo = issueRepo;
                }
            }

            if(mappedUpdate){
                //如果之前存在更新的记录，则判断原先的状态是否是stop

                String repoStatus = preUpdateIssueRepo.getStatus ();
                if(RepoStatusEnum.STOP.getType ().equals (repoStatus)){
                    //第二种：如果原来是stop状态，则更新数据后，重新开始扫描
                    int newAllCount = preUpdateIssueRepo.getScannedCommitCount () + commitSize;
                    preUpdateIssueRepo.setStatus (RepoStatusEnum.SCANNING.getType ());
                    preUpdateIssueRepo.setTotalCommitCount (newAllCount);
                    resultIssueRepo = preUpdateIssueRepo;
                    issueRepoDao.updateIssueRepo (resultIssueRepo);

                }else{
                    //第三种：如果原来是结束状态，则合并后，重新开始开始扫描
                    mainIssueRepo.setEndCommit (preUpdateIssueRepo.getEndCommit ());
                    mainIssueRepo.setTotalCommitCount (preUpdateIssueRepo.getTotalCommitCount () + mainIssueRepo.getScannedCommitCount ());
                    mainIssueRepo.setScannedCommitCount (preUpdateIssueRepo.getScannedCommitCount () + mainIssueRepo.getScannedCommitCount ());
                    mainIssueRepo.setScanTime (mainIssueRepo.getScanTime () + preUpdateIssueRepo.getScanTime ());
                    mainIssueRepo.setEndScanTime (preUpdateIssueRepo.getEndScanTime ());

                    issueRepoDao.updateIssueRepo (mainIssueRepo);
                    issueRepoDao.delIssueRepo(repoId, RepoNatureEnum.UPDATE.getType (), toolName);
                    resultIssueRepo = IssueRepo.initIssueRepo (repoId, branch, beginCommit, toolName, commitSize);
                    resultIssueRepo.setNature (RepoNatureEnum.UPDATE.getType ());
                    issueRepoDao.insertOneIssueRepo (resultIssueRepo);

                }

            }else{
                String repoStatus = mainIssueRepo.getStatus ();
                if(RepoStatusEnum.STOP.getType ().equals (repoStatus)){
                    //第四种：如果原来是stop状态，则更新数据后，重新开始扫描
                    int newAllCount = mainIssueRepo.getScannedCommitCount () + commitSize;
                    mainIssueRepo.setStatus (RepoStatusEnum.SCANNING.getType ());
                    mainIssueRepo.setTotalCommitCount (newAllCount);
                    resultIssueRepo = mainIssueRepo;
                    issueRepoDao.updateIssueRepo (resultIssueRepo);
                }else{
                    //第五种：如果原来是结束状态，则新建一个Update repo scan 记录
                    resultIssueRepo = IssueRepo.initIssueRepo (repoId, branch, beginCommit, toolName, commitSize);
                    resultIssueRepo.setNature (RepoNatureEnum.UPDATE.getType ());
                    issueRepoDao.insertOneIssueRepo (resultIssueRepo);
                }
            }

        }

        return resultIssueRepo;
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
    public void setRawIssueDao(RawIssueDao rawIssueDao) {
        this.rawIssueDao = rawIssueDao;
    }

    @Autowired
    public void setIssueDao(IssueDao issueDao) {
        this.issueDao = issueDao;
    }

    @Autowired
    public void setIssueScanDao(IssueScanDao issueScanDao) {
        this.issueScanDao = issueScanDao;
    }

    @Autowired
    public void setIssueRepoDao(IssueRepoDao issueRepoDao) {
        this.issueRepoDao = issueRepoDao;
    }

    @Autowired
    public void setIssueTypeDao(IssueTypeDao issueTypeDao) {
        this.issueTypeDao = issueTypeDao;
    }

}
