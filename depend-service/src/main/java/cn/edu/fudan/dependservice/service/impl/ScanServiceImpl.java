package cn.edu.fudan.dependservice.service.impl;

import cn.edu.fudan.common.component.BaseRepoRestManager;
import cn.edu.fudan.common.domain.po.scan.RepoScan;
import cn.edu.fudan.common.jgit.JGitHelper;
import cn.edu.fudan.common.scan.CommonScanProcess;
import cn.edu.fudan.common.scan.ToolScan;
import cn.edu.fudan.dependservice.domain.RepoRestManager;
import cn.edu.fudan.dependservice.mapper.GroupMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * description: 依赖分析流程
 *
 * @author fancying
 * create: 2021-03-02 21:04
 **/
@Slf4j
@Service
public class ScanServiceImpl extends CommonScanProcess {

    RepoScan repoScan;

    @Autowired
    GroupMapper groupMapper;

    @Autowired
    public ScanServiceImpl(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected ToolScan getToolScan(String tool) {
        //todo retur tool by tool name
        return applicationContext.getBean(ToolScanImpl.class);
    }

    @Override
    protected List<String> getScannedCommitList(String repoUuid, String tool) {
        //need find in data base.
        // tool is dependency
        return  groupMapper.getScannedCommitList(repoUuid);
    }

    @Override
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

    // fixme @author：fancying  @shaoxi 这里返回一个空的暂时用于测试
    private String getRepo_path() {
        return null;
    }

    @Override
    protected String[] getToolsByRepo(String repoUuid) {
        return new String[]{"ToolScanImpl"};
//        return new String[0];
    }

    @Override
    protected void insertRepoScan(RepoScan repoScan) {
        this.repoScan=repoScan;

    }

    @Override
    public void updateRepoScan(RepoScan scanInfo) {
        //update if the scan success
        this.repoScan=scanInfo;


    }

    @Override
    public void deleteRepo(String repoUuid) {

    }

    @Override
    public void deleteRepo(String repoUuid, String toolName) {

    }

    @Override
    public RepoScan getRepoScanStatus(String repoUuid, String toolName) {
        //get status by repo and toolname
        return null;
    }

    @Override
    public RepoScan getRepoScanStatus(String repoUuid) {
        return null;
    }
}
