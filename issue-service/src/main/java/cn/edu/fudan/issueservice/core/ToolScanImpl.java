package cn.edu.fudan.issueservice.core;

import cn.edu.fudan.common.jgit.JGitHelper;
import cn.edu.fudan.common.scan.ToolScan;
import cn.edu.fudan.issueservice.core.analyzer.AnalyzerFactory;
import cn.edu.fudan.issueservice.core.analyzer.BaseAnalyzer;
import cn.edu.fudan.issueservice.core.process.IssueMatcher;
import cn.edu.fudan.issueservice.core.process.IssuePersistenceManager;
import cn.edu.fudan.issueservice.core.process.IssueStatistics;
import cn.edu.fudan.issueservice.dao.IssueScanDao;
import cn.edu.fudan.issueservice.domain.dbo.IssueScan;
import cn.edu.fudan.issueservice.domain.enums.ScanStatusEnum;
import cn.edu.fudan.issueservice.util.CompileUtil;
import cn.edu.fudan.issueservice.util.DateTimeUtil;
import cn.edu.fudan.issueservice.util.DirExplorer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author beethoven
 * @date 2021-04-25 16:56:35
 */
@Component
@Slf4j
public class ToolScanImpl extends ToolScan {

    private IssueScanDao issueScanDao;

    private AnalyzerFactory analyzerFactory;

    private IssueMatcher issueMatcher;

    private IssueStatistics issueStatistics;

    private IssuePersistenceManager issueScanTransactionManager;


    @Override
    public boolean scanOneCommit(String commit) {

        log.info("start scan  commit id --> {}", commit);

        try {
            JGitHelper jGitHelper = new JGitHelper(scanData.getRepoPath());
            BaseAnalyzer analyzer = analyzerFactory.createAnalyzer(scanData.getRepoScan().getTool());

            //1 init IssueScan
            Date commitTime = jGitHelper.getCommitDateTime(commit);
            IssueScan issueScan = IssueScan.initIssueScan(scanData.getRepoUuid(), commit, scanData.getRepoScan().getTool(), commitTime);

            //2 checkout
            jGitHelper.checkout(commit);

            //3 execute scan
            scan(issueScan, scanData.getRepoPath(), analyzer, jGitHelper, issueMatcher, issueStatistics, issueScanTransactionManager);

            //4 update issue scan end time and persistence
            issueScan.setEndTime(new Date());
            boolean scanPersistenceResult = issueScanPersistence(issueScan);
            if (!scanPersistenceResult) {
                log.error(" issue scan result  persist failed! commit id --> {}", commit);
            }
            log.info("issue scan result  persist success! commit id --> {}", commit);

            analyzer.emptyAnalyzeRawIssues();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
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

    public void scan(IssueScan issueScan, String repoPath, BaseAnalyzer analyzer, JGitHelper jGitHelper,
                     IssueMatcher issueMatcher, IssueStatistics issueStatistics, IssuePersistenceManager issuePersistenceManager) throws InterruptedException {

        String repoUuid = issueScan.getRepoUuid();
        String commit = issueScan.getCommitId();

        //1 check this repo need compile
        if (CompileUtil.checkNeedCompile(issueScan.getTool())) {
            //1.1 clean target
            DirExplorer.deleteRedundantTarget(repoPath);
            //1.2 compile
            long startTime = System.currentTimeMillis();
            if (!CompileUtil.isCompilable(repoPath)) {
                log.error("compile failed!repo path is {}, commit is {}", repoPath, commit);
                issueScan.setStatus(ScanStatusEnum.COMPILE_FAILED.getType());
                return;
            }
            long compileTime = System.currentTimeMillis();
            log.info("compile time use {} s, compile success!", (compileTime - startTime) / 1000);
        }

        //2 invoke tool
        long invokeToolStartTime = System.currentTimeMillis();
        boolean invokeToolResult = analyzer.invoke(repoUuid, repoPath, commit);
        if (!invokeToolResult) {
            log.info("invoke tool failed!repo path is {}, commit is {}", repoPath, commit);
            issueScan.setStatus(ScanStatusEnum.INVOKE_TOOL_FAILED.getType());
            return;
        }
        long invokeToolTime = System.currentTimeMillis();
        log.info("invoke tool use {} s,invoke tool success!", (invokeToolTime - invokeToolStartTime) / 1000);

        //3 analyze raw issues
        boolean analyzeResult = analyzer.analyze(repoPath, repoUuid, commit);
        if (!analyzeResult) {
            log.error("analyze raw issues failed!repo path is {}, commit is {}", repoPath, commit);
            issueScan.setStatus(ScanStatusEnum.ANALYZE_FAILED.getType());
            return;
        }
        long analyzeToolTime = System.currentTimeMillis();
        log.info("analyze raw issues use {} s, analyze success!", (analyzeToolTime - invokeToolTime) / 1000);

        //4 issue match
        issueMatcher.setAnalyzer(analyzer);
        boolean matchResult = issueMatcher.matchProcess(repoUuid, commit, jGitHelper, analyzer.getToolName(), analyzer.getResultRawIssues());
        if (!matchResult) {
            log.error("issue match failed!repo path is {}, commit is {}", repoPath, commit);
            issueScan.setStatus(ScanStatusEnum.MATCH_FAILED.getType());
            return;
        }
        long matchTime = System.currentTimeMillis();
        log.info("issue match use {} s,match success!", (matchTime - analyzeToolTime) / 1000);

        //5 issue statistics
        initIssueStatistics(commit, analyzer, jGitHelper);
        boolean statisticalResult = issueStatistics.doingStatisticalAnalysis(issueMatcher, repoUuid, analyzer.getToolName());
        if (!statisticalResult) {
            log.error("statistical failed!repo path is {}, commit is {}", repoPath, commit);
            issueScan.setStatus(ScanStatusEnum.STATISTICAL_FAILED.getType());
            return;
        }
        long issueStatisticsTime = System.currentTimeMillis();
        log.info("issue statistics use {} s,issue statistics success!", (issueStatisticsTime - analyzeToolTime) / 1000);

        //6.issue persistence
        try {
            issuePersistenceManager.persistScanData(issueStatistics);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("persist failed!repo path is {}, commit is {}", repoPath, commit);
            issueScan.setStatus(ScanStatusEnum.STATISTICAL_FAILED.getType());
            return;
        }
        log.info("issue persistence use {} s,issue persistence!", (System.currentTimeMillis() - issueStatisticsTime) / 1000);
        issueScan.setStatus(ScanStatusEnum.DONE.getType());
    }

    private void initIssueStatistics(String commit, BaseAnalyzer analyzer, JGitHelper jGitHelper) {
        issueStatistics.setCommitId(commit);
        issueStatistics.setCurrentCommitDate(DateTimeUtil.localToUtc(jGitHelper.getCommitTime(commit)));
        issueStatistics.setAnalyzer(analyzer);
        issueStatistics.setJGitHelper(jGitHelper);
    }

    @Override
    public void prepareForScan() {

    }

    @Override
    public void prepareForOneScan(String commit) {

    }

    @Override
    public void cleanUpForOneScan(String commit) {

    }

    @Override
    public void cleanUpForScan() {

    }

    @Autowired
    public void setAnalyzerFactory(AnalyzerFactory analyzerFactory) {
        this.analyzerFactory = analyzerFactory;
    }

    @Autowired
    public void setIssueScanDao(IssueScanDao issueScanDao) {
        this.issueScanDao = issueScanDao;
    }

    @Autowired
    public void setIssueMatcher(IssueMatcher issueMatcher) {
        this.issueMatcher = issueMatcher;
    }

    @Autowired
    public void setIssueStatistics(IssueStatistics issueStatistics) {
        this.issueStatistics = issueStatistics;
    }

    @Autowired
    public void setIssueScanTransactionManager(IssuePersistenceManager issueScanTransactionManager) {
        this.issueScanTransactionManager = issueScanTransactionManager;
    }
}
