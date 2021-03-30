package cn.edu.fudan.dependservice.service.impl;

import cn.edu.fudan.common.component.BaseRepoRestManager;
import cn.edu.fudan.common.domain.po.scan.RepoScan;
import cn.edu.fudan.common.scan.CommonScanProcess;
import cn.edu.fudan.common.scan.CommonScanService;
import cn.edu.fudan.common.scan.ToolScan;
import cn.edu.fudan.dependservice.domain.RepoRestManager;
import cn.edu.fudan.dependservice.mapper.GroupMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TempScanServiceImpl  extends CommonScanProcess {

    RepoScan repoScan;

    @Autowired
    GroupMapper groupMapper;

    @Autowired
    public TempScanServiceImpl(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public void updateRepoScan(RepoScan scanInfo) {

    }
//    public void tempScan(String repoUuid, String branch){
//        ToolScan specificTool = getToolScan("tool");
//        String repoPath = baseRepoRestManager.getCodeServiceRepo(repoUuid);
//
//
//    }



    @Override
    public boolean stopScan(String repoUuid) {
        return false;
    }

    @Override
    protected ToolScan getToolScan(String tool) {
        //todo retur tool by tool name
        return applicationContext.getBean(ToolScanImpl.class);
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
    public boolean stopScan(String repoUuid, String toolName) {
        return false;
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
