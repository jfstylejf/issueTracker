package cn.edu.fudan.issueservice.core;

import cn.edu.fudan.issueservice.component.ApplicationContextGetBeanHelper;
import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.config.ScanThreadExecutorConfig;
import cn.edu.fudan.issueservice.core.analyzer.AnalyzerFactory;
import cn.edu.fudan.issueservice.core.analyzer.BaseAnalyzer;
import cn.edu.fudan.issueservice.core.analyzer.SonarQubeBaseAnalyzer;
import cn.edu.fudan.issueservice.core.process.*;
import cn.edu.fudan.issueservice.core.strategy.PositiveSequenceScanStrategy;
import cn.edu.fudan.issueservice.core.strategy.ScanStrategy;
import cn.edu.fudan.issueservice.dao.*;
import cn.edu.fudan.issueservice.domain.dbo.*;
import cn.edu.fudan.issueservice.domain.dto.RepoResourceDTO;
import cn.edu.fudan.issueservice.domain.enums.*;
import cn.edu.fudan.issueservice.util.CompileUtil;
import cn.edu.fudan.issueservice.util.DateTimeUtil;
import cn.edu.fudan.issueservice.util.DirExplorer;
import cn.edu.fudan.issueservice.util.JGitHelper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.tools.ISupportsMessageContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

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

    private IssueScanDao issueScanDao;
    private IssueAnalyzerDao issueAnalyzerDao;
    private RestInterfaceManager restInvoker;
    private ApplicationContext applicationContext;
    private IssueRepoDao issueRepoDao;
    private AnalyzerFactory analyzerFactory;

    /**
     * 工具调用
     */
    @SneakyThrows
    public void invoke(String repoId, String branch, String beginCommit, String toolName, boolean isUpdate) {
        //0.init applicationGetBeanHelper
        ApplicationContextGetBeanHelper applicationContextGetBeanHelper = new ApplicationContextGetBeanHelper();
        applicationContextGetBeanHelper.setApplicationContext(applicationContext);

        //1.get analyzer
        BaseAnalyzer analyzer = analyzerFactory.createAnalyzer(toolName);

        //2.get scan strategy
        ScanStrategy scanStrategy = applicationContext.getBean(PositiveSequenceScanStrategy.class);

        //3.set repo source for scan
        RepoResourceDTO repoResourceDTO = RepoResourceDTO.builder().repoId(repoId).build();

        //4.execute scan
        Assert.notNull(analyzer, "tool " + toolName + " analyzer is null");
        boolean isFirst = true;
        while (isFirst || ScanThreadExecutorConfig.getRepoUpdateStatus(repoId, toolName)) {
            ScanThreadExecutorConfig.delRepoUpdateStatus(repoId, toolName);
            boolean scanResult = executeScan(repoResourceDTO, analyzer, scanStrategy, branch, beginCommit, isUpdate);
            if (!scanResult) {
                ScanThreadExecutorConfig.delRepoUpdateStatus(repoId, toolName);
                break;
            }
            isFirst = false;
            beginCommit = null;
        }
    }

    private boolean executeScan(RepoResourceDTO repoResourceDTO, BaseAnalyzer analyzer, ScanStrategy scanStrategy,
                                String branch, String beginCommit, boolean isUpdate) throws InterruptedException {
        boolean result = true;
        String repoPath = null;
        try {
            repoPath = restInvoker.getRepoPath(repoResourceDTO.getRepoId());
            repoResourceDTO.setRepoPath(repoPath);
            if (StringUtils.isEmpty(repoPath)) {
                log.error("can not get repo path! repo id --> {}", repoResourceDTO.getRepoId());
                return false;
            }
            log.info("get repo path --> {}", repoPath);
            String repoId = repoResourceDTO.getRepoId();
            String toolName = analyzer.getToolName();
            //1.配置jGit资源
            JGitHelper jGitInvoker = new JGitHelper(repoPath);
            String startCommit = issueScanDao.getStartCommitByRepoUuid(repoId);
            List<String> scannedCommits = new ArrayList<>(issueScanDao.getScannedCommitList(repoId, toolName));
            //判断beginCommit 是否为空，如果为空则获取
            if (beginCommit == null) {
                if (scannedCommits.isEmpty()) {
                    log.error(" need begin commit!");
                    return false;
                }
                List<String> commitIds = jGitInvoker.getScanCommitListByBranchAndBeginCommit(branch, startCommit, scannedCommits);
                //因为必定不为null，所以不做此判断
                if (commitIds.isEmpty()) {
                    log.info(" already update! repoId --> {}", repoId);
                    return false;
                }
                beginCommit = commitIds.get(0);
            }
            IssueMatcher issueMatcher = new IssueMatcher();
            IssueStatistics issueStatistics = new IssueStatistics();
            IssuePersistenceManager issueScanTransactionManager = applicationContext.getBean(IssuePersistenceManager.class);
            //2.根据策略获取扫描的commit列表
            ConcurrentLinkedDeque<String> scanCommits = isUpdate ?
                    scanStrategy.getScanCommitLinkedQueue(repoId, jGitInvoker, branch, startCommit, scannedCommits) :
                    scanStrategy.getScanCommitLinkedQueue(repoId, jGitInvoker, branch, beginCommit, scannedCommits);
            if (scanCommits == null || scanCommits.isEmpty()) {
                log.error("get commit List failed or all commits were scanned! repo id --> {}", repoId);
                return false;
            }
            //并且存入全局扫描 commit列表进行管理
            ScanThreadExecutorConfig.setNeedToScanCommitLists(repoId, scanCommits, toolName);
            //3.初始化issue repo信息 并持久化到数据库中
            IssueRepo issueRepo = getIssueRepoByDifferentScenarios(repoId, branch, toolName, beginCommit, scanCommits.size());
            log.info("start scan !  repo id --> {}", repoId);
            //4.遍历扫描
            //设定开关确认是否是stop操作。
            boolean isStop = false;

            for (String commit : scanCommits) {
                long thisCommitStartMilliTime = System.currentTimeMillis();
                boolean threadSwitch = ScanThreadExecutorConfig.getConsumerThreadSwitch(repoId, toolName);
                if (threadSwitch && !Thread.currentThread().isInterrupted()) {
                    log.info("start scan  commit id --> {}", commit);

                    //4.1 初始化 一个IssueScan用于记录scan过程
                    Date commitTime = jGitInvoker.getCommitDateTime(commit);
                    IssueScan issueScan = IssueScan.initIssueScan(repoId, commit, toolName, commitTime);
                    //4.2 checkout到指定的版本
                    jGitInvoker.checkout(commit);
                    //4.4 执行扫描
                    scan(issueScan, repoResourceDTO, analyzer, jGitInvoker, issueMatcher, issueStatistics, issueScanTransactionManager);
                    //4,5 更新issueScan 结束时间
                    issueScan.setEndTime(new Date());
                    //4.5 issueScan 入库
                    boolean scanPersistenceResult = issueScanPersistence(issueScan);
                    if (!scanPersistenceResult) {
                        log.error(" issue scan result  persist failed! commit id --> {}", commit);
                    }
                    log.info(" issue scan result  persist success");
                    //更新 issue repo 信息 ，包括已扫描条数+1 ,  扫描用时 , end commit, end date
                    //更新扫描用时
                    long thisCommitEndMilliTime = System.currentTimeMillis();
                    int consumingTimeSec = (int) (thisCommitEndMilliTime - thisCommitStartMilliTime) / 1000;
                    issueRepo.setScanTime(consumingTimeSec + issueRepo.getScanTime());
                    issueRepo.setEndCommit(commit);
                    issueRepo.setEndScanTime(new Date());
                    issueRepo.incrementScannedCommitCount();
                    issueRepoDao.updateIssueRepo(issueRepo);
                } else {
                    isStop = true;
                    result = false;
                    break;
                }
                analyzer.emptyAnalyzeRawIssues();
            }
            //更新issue repo 的 status
            if (isStop) {
                issueRepo.setStatus(RepoStatusEnum.STOP.getType());
            } else {
                issueRepo.setStatus(RepoStatusEnum.COMPLETE.getType());
            }
            issueRepoDao.updateIssueRepo(issueRepo);
        } finally {
            if (repoPath != null) {
                restInvoker.freeRepoPath(repoResourceDTO.getRepoId(), repoResourceDTO.getRepoPath());
            }
        }
        return result;
    }

    /**
     * FIXME 加入回滚策略
     */
    @Transactional(rollbackFor = Exception.class)
    public void scan(IssueScan issueScan, RepoResourceDTO repoResourceDTO, BaseAnalyzer analyzer, JGitHelper jGitInvoker,
                     IssueMatcher issueMatcher, IssueStatistics issueStatistics, IssuePersistenceManager issuePersistenceManager) throws InterruptedException {
        String repoId = repoResourceDTO.getRepoId();
        String repoPath = repoResourceDTO.getRepoPath();
        String commit = issueScan.getCommitId();

        // 用于匹配的rawIssues 要么从缓存库中获取 要么重新调用工具获取
        List<RawIssue> analyzeRawIssues;

        // 首先判断数据库是否有扫描工具之前的缓存数据
        JSONObject analyzeCache = issueAnalyzerDao.getAnalyzeResultByRepoUuidCommitIdTool(repoId, commit, analyzer.getToolName());
        // 如果没有缓存数据，则进行工具调用
        if (analyzeCache == null) {
            // 初始化IssueAnalyzer，用于记录缓存数据
            IssueAnalyzer issueAnalyzer = IssueAnalyzer.initIssueAnalyzer(repoId, commit, analyzer.getToolName());
            log.info("There is no cache data, need to invoke tool!");
            if (analyzer instanceof SonarQubeBaseAnalyzer) {
                //0. 先清除编译生成的target文件
                long startTime = System.currentTimeMillis();
                DirExplorer.deleteRedundantTarget(repoPath);
                long deleteTargetTime = System.currentTimeMillis();
                log.info("delete target time --> {}", (deleteTargetTime - startTime) / 1000);

                //1. 先判断是否可编译 以及是否编译成功
                if (!CompileUtil.isCompilable(repoPath)) {
                    log.error("compile failed ! ");
                    issueScan.setStatus(ScanStatusEnum.COMPILE_FAILED.getType());
                    // 0表示缓存失败
                    issueAnalyzer.setInvokeResult(0);
                    return;
                }
                long compileTime = System.currentTimeMillis();
                log.info("compile time --> {}, compile success ! ", (compileTime - deleteTargetTime) / 1000);
            }
            long compileTime2 = System.currentTimeMillis();

            //2. 调用工具进行扫描
            boolean invokeToolResult = analyzer.invoke(repoId, repoPath, commit);
            if (!invokeToolResult) {
                long invokeToolTime = System.currentTimeMillis();
                log.info("invoke tool --> {}", (invokeToolTime - compileTime2) / 1000);
                log.error("invoke tool failed ! ");
                issueScan.setStatus(ScanStatusEnum.INVOKE_TOOL_FAILED.getType());
                // 0表示缓存失败
                issueAnalyzer.setInvokeResult(0);
                return;
            }
            long invokeToolTime = System.currentTimeMillis();
            log.info("invoke tool --> {}", (invokeToolTime - compileTime2) / 1000);
            log.info("invoke tool success ! ");

            //3. 调用工具进行解析
            boolean analyzeResult = analyzer.analyze(repoPath, repoId, commit);
            if (!analyzeResult) {
                log.error("analyze failed ! ");
                issueScan.setStatus(ScanStatusEnum.ANALYZE_FAILED.getType());
                // 0表示缓存失败
                issueAnalyzer.setInvokeResult(0);
                return;
            }
            long analyzeToolTime = System.currentTimeMillis();
            log.info("analyze tool --> {}", (analyzeToolTime - invokeToolTime) / 1000);
            log.info("analyze success ! ");
            // 1表示缓存成功
            issueAnalyzer.setInvokeResult(1);
            analyzeRawIssues = analyzer.getResultRawIssues();
            // 将解析好的resultRawIssues 缓存入库
            JSONObject issueAnalyzerResult = new JSONObject();
            issueAnalyzerResult.put("result", analyzeRawIssues);
            issueAnalyzer.setAnalyzeResult(issueAnalyzerResult);
            List<IssueAnalyzer> cacheData = new ArrayList<>();
            cacheData.add(issueAnalyzer);
            issueAnalyzerDao.insertIssueAnalyzer(cacheData);
        } else {
            // 有缓存数据，直接进行匹配
            log.info("The cache data already exists, go ahead to mapping issue!");
            // 第一步：先获取jsonArray数组
            JSONArray resArr = analyzeCache.getJSONArray("result");
            // 第二步：将JSON字符串转换成List集合
            analyzeRawIssues = JSONArray.parseArray(resArr.toJSONString(), RawIssue.class) ;
        }



        //4. 缺陷匹配
        issueMatcher.setAnalyzer(analyzer);
        boolean matchResult = issueMatcher.matchProcess(repoId, commit, jGitInvoker, analyzer.getToolName(), analyzeRawIssues);
        if (!matchResult) {
            log.error("match failed ! ");
            issueScan.setStatus(ScanStatusEnum.MATCH_FAILED.getType());
            return;
        }
        log.info("match success ! ");

        //5. 更新issue信息 ,做相应的缺陷统计
        issueStatistics.setCommitId(commit);
        issueStatistics.setCurrentCommitDate(DateTimeUtil.localToUtc(jGitInvoker.getCommitTime(commit)));
        issueStatistics.setAnalyzer(analyzer);
        issueStatistics.setJGitHelper(jGitInvoker);
        boolean statisticalResult = issueStatistics.doingStatisticalAnalysis(issueMatcher, repoId, analyzer.getToolName());
        if (!statisticalResult) {
            log.error("statistical failed ! ");
            issueScan.setStatus(ScanStatusEnum.STATISTICAL_FAILED.getType());
            return;
        }
        log.info("statistical success ! ");

        //6. 持久化扫描结果
        try {
            issuePersistenceManager.persistScanData(issueStatistics);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("persist failed ! ");
            issueScan.setStatus(ScanStatusEnum.STATISTICAL_FAILED.getType());
            return;
        }
        log.info("persist data success ! ");
        issueScan.setStatus(ScanStatusEnum.DONE.getType());
    }

    private boolean issueScanPersistence(IssueScan issueScan) {
        try {
            issueScanDao.insertOneIssueScan(issueScan);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private IssueRepo getIssueRepoByDifferentScenarios(String repoId, String branch, String toolName, String beginCommit, int commitSize) {
        IssueRepo resultIssueRepo;
        List<IssueRepo> issueRepos = issueRepoDao.getIssueRepoByCondition(repoId, null, toolName);
        if (issueRepos == null || issueRepos.isEmpty()) {
            //第一种情况 未扫描过，数据库无记录
            resultIssueRepo = IssueRepo.initIssueRepo(repoId, branch, beginCommit, toolName, commitSize);
            resultIssueRepo.setNature(RepoNatureEnum.MAIN.getType());
            issueRepoDao.insertOneIssueRepo(resultIssueRepo);

        } else {
            //如果此时存在update记录，则先将原来的update记录合并至主记录先
            IssueRepo preUpdateIssueRepo = null;
            IssueRepo mainIssueRepo = null;
            boolean mappedUpdate = false;
            for (IssueRepo issueRepo : issueRepos) {
                if (RepoNatureEnum.UPDATE.getType().equals(issueRepo.getNature())) {
                    mappedUpdate = true;
                    preUpdateIssueRepo = issueRepo;
                } else {
                    mainIssueRepo = issueRepo;
                }
            }
            if (mappedUpdate) {
                //如果之前存在更新的记录，则判断原先的状态是否是stop
                String repoStatus = preUpdateIssueRepo.getStatus();
                if (RepoStatusEnum.STOP.getType().equals(repoStatus)) {
                    //第二种：如果原来是stop状态，则更新数据后，重新开始扫描
                    int newAllCount = preUpdateIssueRepo.getScannedCommitCount() + commitSize;
                    preUpdateIssueRepo.setStatus(RepoStatusEnum.SCANNING.getType());
                    preUpdateIssueRepo.setTotalCommitCount(newAllCount);
                    resultIssueRepo = preUpdateIssueRepo;
                    issueRepoDao.updateIssueRepo(resultIssueRepo);
                } else {
                    //第三种：如果原来是结束状态，则合并后，重新开始开始扫描
                    mainIssueRepo.setEndCommit(preUpdateIssueRepo.getEndCommit());
                    mainIssueRepo.setTotalCommitCount(preUpdateIssueRepo.getTotalCommitCount() + mainIssueRepo.getScannedCommitCount());
                    mainIssueRepo.setScannedCommitCount(preUpdateIssueRepo.getScannedCommitCount() + mainIssueRepo.getScannedCommitCount());
                    mainIssueRepo.setScanTime(mainIssueRepo.getScanTime() + preUpdateIssueRepo.getScanTime());
                    mainIssueRepo.setEndScanTime(preUpdateIssueRepo.getEndScanTime());
                    issueRepoDao.updateIssueRepo(mainIssueRepo);
                    issueRepoDao.delIssueRepo(repoId, RepoNatureEnum.UPDATE.getType(), toolName);
                    resultIssueRepo = IssueRepo.initIssueRepo(repoId, branch, beginCommit, toolName, commitSize);
                    resultIssueRepo.setNature(RepoNatureEnum.UPDATE.getType());
                    issueRepoDao.insertOneIssueRepo(resultIssueRepo);
                }
            } else {
                String repoStatus = mainIssueRepo.getStatus();
                if (RepoStatusEnum.STOP.getType().equals(repoStatus)) {
                    //第四种：如果原来是stop状态，则更新数据后，重新开始扫描
                    int newAllCount = mainIssueRepo.getScannedCommitCount() + commitSize;
                    mainIssueRepo.setStatus(RepoStatusEnum.SCANNING.getType());
                    mainIssueRepo.setTotalCommitCount(newAllCount);
                    resultIssueRepo = mainIssueRepo;
                    issueRepoDao.updateIssueRepo(resultIssueRepo);
                } else {
                    //第五种：如果原来是结束状态，则新建一个Update repo scan 记录
                    resultIssueRepo = IssueRepo.initIssueRepo(repoId, branch, beginCommit, toolName, commitSize);
                    resultIssueRepo.setNature(RepoNatureEnum.UPDATE.getType());
                    issueRepoDao.insertOneIssueRepo(resultIssueRepo);
                }
            }

        }

        return resultIssueRepo;
    }

    @Autowired
    public void setAnalyzerFactory(AnalyzerFactory analyzerFactory) {
        this.analyzerFactory = analyzerFactory;
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
    public void setIssueScanDao(IssueScanDao issueScanDao) {
        this.issueScanDao = issueScanDao;
    }

    @Autowired
    public void setIssueRepoDao(IssueRepoDao issueRepoDao) {
        this.issueRepoDao = issueRepoDao;
    }

    @Autowired
    public void setIssueAnalyzerDao(IssueAnalyzerDao issueAnalyzerDao) {
        this.issueAnalyzerDao = issueAnalyzerDao;
    }
}
