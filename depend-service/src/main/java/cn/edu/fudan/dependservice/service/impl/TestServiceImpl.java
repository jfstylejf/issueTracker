package cn.edu.fudan.dependservice.service.impl;

import cn.edu.fudan.common.domain.po.scan.RepoScan;
import cn.edu.fudan.common.scan.CommonScanProcess;
import cn.edu.fudan.common.scan.CommonScanService;
import cn.edu.fudan.dependservice.config.ShHomeConfig;
import cn.edu.fudan.dependservice.dao.StatisticsDao;
import cn.edu.fudan.dependservice.domain.ProjectIdsInfo;
import cn.edu.fudan.dependservice.domain.RepoUuidsInfo;
import cn.edu.fudan.dependservice.domain.ScanRepo;
import cn.edu.fudan.dependservice.mapper.GroupMapper;
import cn.edu.fudan.dependservice.mapper.RepoMapper;
import cn.edu.fudan.dependservice.service.ProcessPrepare;
import cn.edu.fudan.dependservice.service.TempProcess;
import cn.edu.fudan.dependservice.service.TempTempProcess;
import cn.edu.fudan.dependservice.service.TestService;
import cn.edu.fudan.dependservice.utill.WriteUtill;
import cn.edu.fudan.dependservice.utill.WriteUtill2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TestServiceImpl implements TestService {
    @Autowired
    StatisticsDao statisticsDao;

    ApplicationContext applicationContext;

    @Autowired
    ProcessPrepare processPrepare;

    @Autowired
    TempTempProcess tempTempProcess;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    // this is scan latest
    public List<ScanRepo> scanAllRepo() {
        List<RepoUuidsInfo> repoUuidsInfos= statisticsDao.getallRepoUuid();
        List<RepoUuidsInfo> repoUuidsInfosThatNeedScan=new ArrayList<>();
        for(RepoUuidsInfo repoUuidsInfo:repoUuidsInfos){
            if(repoUuidsInfo.getLanguage()!=null&&(repoUuidsInfo.getLanguage().equals("Java")||repoUuidsInfo.getLanguage().equals("C++"))){
                repoUuidsInfosThatNeedScan.add(repoUuidsInfo);
            }
        }
        List<String> repoDirs=new ArrayList<>();
        List<ScanRepo> scanRepos =new ArrayList<>();
        for(RepoUuidsInfo re:repoUuidsInfosThatNeedScan){
            ScanRepo scanRepo=processPrepare.prepareFile(re.getRepoUuid(),re.getBranch(),null);
            if(scanRepo.isCopyStatus()){
                repoDirs.add(scanRepo.getCopyRepoPath());
            }
            scanRepos.add(scanRepo);
        }
        String configFile = applicationContext.getBean(ShHomeConfig.class).getResultFileDir()+ "source-project-conf.json";
        //todo not all project is java
        WriteUtill2.writeProjecConf(configFile,repoDirs);
        tempTempProcess.beginScan(scanRepos,null);
        return scanRepos;


    }
    //
    public List<ScanRepo> scanAllRepoNearToOneDate(String toScanDate) {
        List<RepoUuidsInfo> repoUuidsInfos= statisticsDao.getallRepoUuid();
        List<RepoUuidsInfo> repoUuidsInfosThatNeedScan=new ArrayList<>();
        for(RepoUuidsInfo repoUuidsInfo:repoUuidsInfos){
            if(repoUuidsInfo.getLanguage()!=null&&(repoUuidsInfo.getLanguage().equals("Java")||repoUuidsInfo.getLanguage().equals("C++"))){
                repoUuidsInfosThatNeedScan.add(repoUuidsInfo);
            }
        }
        List<String> repoDirs=new ArrayList<>();
        List<ScanRepo> scanRepos =new ArrayList<>();
        for(RepoUuidsInfo re:repoUuidsInfosThatNeedScan){
            ScanRepo scanRepo=processPrepare.prepareFile(re.getRepoUuid(),re.getBranch(),toScanDate);
            if(scanRepo.isCopyStatus()){
                repoDirs.add(scanRepo.getCopyRepoPath());
            }
            scanRepos.add(scanRepo);
        }
        String configFile = applicationContext.getBean(ShHomeConfig.class).getResultFileDir()+ "source-project-conf.json";
        //todo not all project is java
        WriteUtill2.writeProjecConf(configFile,repoDirs);
        tempTempProcess.beginScan(scanRepos,null);
        return scanRepos;


    }
}
