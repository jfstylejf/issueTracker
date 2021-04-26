package cn.edu.fudan.dependservice.service.impl;

import cn.edu.fudan.dependservice.component.ScanProcessor;
import cn.edu.fudan.dependservice.config.ShHomeConfig;
import cn.edu.fudan.dependservice.dao.StatisticsDao;
import cn.edu.fudan.dependservice.domain.RepoUuidsInfo;
import cn.edu.fudan.dependservice.domain.ScanRepo;
import cn.edu.fudan.dependservice.domain.ScanStatus;
import cn.edu.fudan.dependservice.service.ProcessPrepare;
import cn.edu.fudan.dependservice.service.ScanProcess;
import cn.edu.fudan.dependservice.service.TestService;
import cn.edu.fudan.dependservice.util.TimeUtil;
import cn.edu.fudan.dependservice.util.WriteUtil2;
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
    ScanProcessor scanProcessor;

    @Autowired
    ProcessPrepare processPrepare;

    @Autowired
    ScanProcess scanProcess;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    // this is scan latest  // get to scan repo
    //
    @Override
    public List<ScanRepo> scanAllRepoNew() {
        List<RepoUuidsInfo> repoUuidsInfos= statisticsDao.getallRepoUuid();
        List<RepoUuidsInfo> repoUuidsInfosThatNeedScan=new ArrayList<>();
        // todo change to lambda
        for(RepoUuidsInfo repoUuidsInfo:repoUuidsInfos){
            if(repoUuidsInfo.getLanguage()!=null&&(repoUuidsInfo.getLanguage().equals("Java")||repoUuidsInfo.getLanguage().equals("C++"))){
                repoUuidsInfosThatNeedScan.add(repoUuidsInfo);
            }
        }
        List<ScanRepo> scanRepos =new ArrayList<>();
        for(RepoUuidsInfo re:repoUuidsInfosThatNeedScan){
            // todo set scanstatus
            ScanRepo scanRepo=new ScanRepo();
            scanRepo.setRepoUuid(re.getRepoUuid());
            scanRepo.setBranch(re.getBranch());
            ScanStatus scanStatus =new ScanStatus();
            scanStatus.setStartScanTime(TimeUtil.getCurrentDateTime());
            scanStatus.setTs_start(System.currentTimeMillis());
            scanStatus.setStatus("scanning");
            scanRepos.add(scanRepo);
        }
        // scan
        scanProcessor.scan(scanRepos);
        return scanRepos;
    }
    @Deprecated
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
            ScanRepo scanRepo=new ScanRepo();
            scanRepo.setRepoUuid(re.getRepoUuid());
            scanRepo.setBranch(re.getBranch());
                    processPrepare.prepareFile(null,scanRepo);
            if(scanRepo.isCopyStatus()){
                repoDirs.add(scanRepo.getCopyRepoPath());
            }
            scanRepos.add(scanRepo);
        }
        // scan
        scanProcessor.scan(scanRepos);
        String configFile = applicationContext.getBean(ShHomeConfig.class).getResultFileDir()+ "source-project-conf.json";
        //todo not all project is java
        WriteUtil2.writeProjecConf(configFile,repoDirs);
        scanProcess.beginScan(scanRepos,null);
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
            ScanRepo scanRepo=new ScanRepo();
            scanRepo.setBranch(re.getBranch());
            scanRepo.setRepoUuid(re.getRepoUuid());
                    processPrepare.prepareFile(toScanDate,scanRepo);
            if(scanRepo.isCopyStatus()){
                repoDirs.add(scanRepo.getCopyRepoPath());
            }
            scanRepos.add(scanRepo);
        }
        String configFile = applicationContext.getBean(ShHomeConfig.class).getResultFileDir()+ "source-project-conf.json";
        //todo not all project is java
        WriteUtil2.writeProjecConf(configFile,repoDirs);
        scanProcess.beginScan(scanRepos,null);
        return scanRepos;
    }
}
