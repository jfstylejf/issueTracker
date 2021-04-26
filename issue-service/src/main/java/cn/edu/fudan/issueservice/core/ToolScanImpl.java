package cn.edu.fudan.issueservice.core;

import cn.edu.fudan.common.jgit.JGitHelper;
import cn.edu.fudan.common.scan.ToolScan;
import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.core.analyzer.AnalyzerFactory;
import cn.edu.fudan.issueservice.core.analyzer.BaseAnalyzer;
import cn.edu.fudan.issueservice.core.process.IssueMatcher;
import cn.edu.fudan.issueservice.core.process.IssuePersistenceManager;
import cn.edu.fudan.issueservice.core.process.IssueStatistics;
import cn.edu.fudan.issueservice.dao.IssueRepoDao;
import cn.edu.fudan.issueservice.dao.IssueScanDao;
import cn.edu.fudan.issueservice.domain.dbo.IssueRepo;
import cn.edu.fudan.issueservice.domain.dbo.IssueScan;
import cn.edu.fudan.issueservice.domain.dto.RepoResourceDTO;
import cn.edu.fudan.issueservice.util.CompileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author beethoven
 * @date 2021-04-25 16:56:35
 */
@Component
@Slf4j
public class ToolScanImpl extends ToolScan {

    private IssueScanDao issueScanDao;

    private RestInterfaceManager restInterfaceManager;

    private ApplicationContext applicationContext;

    private IssueRepoDao issueRepoDao;

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
            scan(issueScan, scanData.getRepoUuid(), scanData.getRepoPath(), analyzer, jGitHelper, issueMatcher, issueStatistics, issueScanTransactionManager);

            //4 update issue scan end time and persistence
            issueScan.setEndTime(new Date());
            boolean scanPersistenceResult = issueScanPersistence(issueScan);
            if (!scanPersistenceResult) {
                log.error(" issue scan result  persist failed! commit id --> {}", commit);
            }
            log.info("issue scan result  persist success! commit id --> {}", commit);

            analyzer.emptyAnalyzeRawIssues();
        }catch (Exception e){
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

    @Transactional(rollbackFor = Exception.class)
    void scan(IssueScan issueScan, String repoUuid, String repoPath, BaseAnalyzer analyzer, JGitHelper jGitHelper, IssueMatcher issueMatcher, IssueStatistics issueStatistics, IssuePersistenceManager issueScanTransactionManager) {
        //1 check this repo need compile
        if (CompileUtil.checkNeedCompile(issueScan.getTool())){

        }

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
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
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
