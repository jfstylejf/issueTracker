package cn.edu.fudan.issueservice.core;

import cn.edu.fudan.common.domain.po.scan.RepoScan;
import cn.edu.fudan.common.scan.CommonScanProcess;
import cn.edu.fudan.common.scan.ToolScan;
import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.dao.IssueRepoDao;
import cn.edu.fudan.issueservice.dao.IssueScanDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author beethoven
 * @date 2021-04-25 13:51:11
 */
@Component
public class IssueScanProcess extends CommonScanProcess {

    private IssueScanDao issueScanDao;

    private IssueRepoDao issueRepoDao;

    private RestInterfaceManager restInterfaceManager;

    public IssueScanProcess(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    protected ToolScan getToolScan(String tool) {
        return applicationContext.getBean(ToolScanImpl.class);
    }

    @Override
    protected List<String> getScannedCommitList(String repoUuid, String tool) {
        return new ArrayList<>(issueScanDao.getScannedCommitList(repoUuid, tool));
    }

    @Override
    protected String getLastedScannedCommit(String repoUuid, String tool) {
        return null;
    }

    @Override
    protected String[] getToolsByRepo(String repoUuid) {
        String tool = restInterfaceManager.getToolByRepoUuid(repoUuid);
        return new String[]{tool};
    }

    @Override
    protected void insertRepoScan(RepoScan repoScan) {
        issueRepoDao.insertOneIssueRepo(repoScan);
    }

    @Override
    public void updateRepoScan(RepoScan scanInfo) {
        issueRepoDao.updateIssueRepo(scanInfo);
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

    @Autowired
    public void setIssueScanDao(IssueScanDao issueScanDao) {
        this.issueScanDao = issueScanDao;
    }

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    @Autowired
    public void setIssueRepoDao(IssueRepoDao issueRepoDao) {
        this.issueRepoDao = issueRepoDao;
    }
}
