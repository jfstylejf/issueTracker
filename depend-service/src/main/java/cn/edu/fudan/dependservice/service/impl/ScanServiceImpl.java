package cn.edu.fudan.dependservice.service.impl;

import cn.edu.fudan.dependservice.component.ScanProcessor;
import cn.edu.fudan.dependservice.dao.ScanDao;
import cn.edu.fudan.dependservice.dao.StatisticsDao;
import cn.edu.fudan.dependservice.domain.RepoInfo;
import cn.edu.fudan.dependservice.domain.ScanRepo;
import cn.edu.fudan.dependservice.domain.ScanStatus;
import cn.edu.fudan.dependservice.service.ScanService;
import cn.edu.fudan.dependservice.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ScanServiceImpl implements ScanService {
    @Autowired
    StatisticsDao statisticsDao;

    @Autowired
    ScanDao scanDao;

    @Autowired
    ScanProcessor scanProcessor;
    //
    @Override
    public List<ScanRepo> scanAllRepoNearToOneDate(String toScanDate) {
        // todo change here
        List<RepoInfo> repoInfos = statisticsDao.getallRepoUuid();
        List<RepoInfo> repoInfosThatNeedScan = new ArrayList<>();
        for (RepoInfo repoInfo : repoInfos) {
            if (repoInfo.getLanguage() != null && (repoInfo.getLanguage().equals("Java") || repoInfo.getLanguage().equals("C++"))) {
                repoInfosThatNeedScan.add(repoInfo);
            }
        }
        List<ScanRepo> scanRepos = new ArrayList<>();
        for (RepoInfo re : repoInfosThatNeedScan) {
            // todo set scanstatus
            ScanRepo scanRepo = new ScanRepo();
            scanRepo.setRepoUuid(re.getRepoUuid());
            scanRepo.setBranch(re.getBranch());
            ScanStatus scanStatus = new ScanStatus();
            scanStatus.setStartScanTime(TimeUtil.getCurrentDateTime());
            scanStatus.setTs_start(System.currentTimeMillis());
            scanStatus.setStatus("scanning");
            scanRepo.setScanStatus(scanStatus);
            scanRepo.setToScanDate(toScanDate);
            scanRepos.add(scanRepo);
        }
        // scan
        scanProcessor.scan(scanRepos);
        return scanRepos;
    }

    @Override
    public void canNotScan(ScanRepo scanRepo) {
        scanDao.updateScan(scanRepo);
    }

    @Override
    @Async("taskExecutor")
    public void scanOneRepo(ScanRepo scanRepo, List<ScanRepo> toScanList) {
        try {
            toScanList.add(scanRepo);
            int size =toScanList.size();
            //many thread have a same wait time
            Thread.sleep(10*1000);
            if(toScanList.size()==size){
                List<ScanRepo> scanRepoList=new ArrayList<>(toScanList);
                toScanList.clear();
                scanProcessor.scan(scanRepoList);
            }

        }catch (InterruptedException e){
            e.printStackTrace();
        }


    }
}
