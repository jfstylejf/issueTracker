package cn.edu.fudan.dependservice.service;

import cn.edu.fudan.common.component.BaseRepoRestManager;
import cn.edu.fudan.common.domain.ScanInfo;
import cn.edu.fudan.common.domain.po.scan.RepoScan;
import cn.edu.fudan.common.jgit.JGitHelper;
import cn.edu.fudan.common.scan.CommonScanProcess;
import cn.edu.fudan.common.scan.CommonScanService;
import cn.edu.fudan.common.scan.ToolScan;
import cn.edu.fudan.dependservice.domain.ScanRepo;
import cn.edu.fudan.dependservice.mapper.GroupMapper;
import cn.edu.fudan.dependservice.service.impl.ToolScanImplPara;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Data
@Service
public class ScanProcess implements CommonScanService {
    // todo use toolscan one time
    private static final Logger log = LoggerFactory.getLogger(CommonScanProcess.class);
    private static final String KEY_DELIMITER = "-";
    RepoScan repoScan;

    ApplicationContext applicationContext;


    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    /**
     * key repoUuid
     * value true/false true 代表还需要更新扫描一次
     **/
    private final Short lock = 1;

    protected ToolScanImplPara getToolScan(String tool) {
        //todo retur tool by tool name
        return applicationContext.getBean(ToolScanImplPara.class);
    }

    protected String[] getToolsByRepo(String repoUuid) {
        return new String[]{"ToolScanImpl"};
    }


    public void beginScan(List<ScanRepo> scanRepos, String tool) {
        ToolScanImplPara specificTool = getToolScan(tool);
        try {

            specificTool.setScanRepos(scanRepos);
            specificTool.prepareForScan();
            specificTool.prepareForOneScan(null);
            boolean success = specificTool.scanOneCommit(null);
            specificTool.cleanUpForOneScan(null);
            specificTool.cleanUpForScan();
            if(!success){
                // todo write to date base that all fail
            }
        } catch (Exception e) {
            e.printStackTrace();
            //todo set san fail
        } finally {
            specificTool.cleanUpForScan();

        }

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

    @Override
    public void updateRepoScan(RepoScan scanInfo) {


    }

    @Override
    public boolean stopScan(String repoUuid) {
        return false;
    }



}
