package cn.edu.fudan.dependservice.service.impl;

import cn.edu.fudan.common.component.RepoRestManager;
import cn.edu.fudan.common.domain.po.scan.RepoScan;
import cn.edu.fudan.common.scan.CommonScanProcess;
import cn.edu.fudan.common.scan.ToolScan;

import java.util.List;

/**
 * description: 依赖分析流程
 *
 * @author fancying
 * create: 2021-03-02 21:04
 **/
public class ScanServiceImpl extends CommonScanProcess {
    @Override
    protected ToolScan getToolScan(String tool) {
        return null;
    }

    @Override
    protected List<String> getScannedCommitList(String repoUuid, String tool) {
        return null;
    }

    @Override
    protected String getLastedScannedCommit(String repoUuid, String tool) {
        return null;
    }

    @Override
    protected String[] getToolsByRepo(String repoUuid) {
        return new String[0];
    }

    @Override
    protected void insertRepoScan(RepoScan repoScan) {

    }

    @Override
    public <T extends RepoRestManager> void setRepoRestManager(T restInterfaceManager) {

    }

    @Override
    public void updateRepoScan(RepoScan scanInfo) {

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
}
