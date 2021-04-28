package cn.edu.fudan.dependservice.service.impl;

import cn.edu.fudan.dependservice.component.ScanProcessor;
import cn.edu.fudan.dependservice.dao.StatisticsDao;
import cn.edu.fudan.dependservice.domain.RepoUuidsInfo;
import cn.edu.fudan.dependservice.domain.ScanRepo;
import cn.edu.fudan.dependservice.domain.ScanStatus;
import cn.edu.fudan.dependservice.service.ProcessPrepare;
import cn.edu.fudan.dependservice.service.ScanProcess;
import cn.edu.fudan.dependservice.service.ScanService;
import cn.edu.fudan.dependservice.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ScanServiceImpl implements ScanService {
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

    //
    @Override
    public List<ScanRepo> scanAllRepoNearToOneDate(String toScanDate) {
        List<RepoUuidsInfo> repoUuidsInfos = statisticsDao.getallRepoUuid();
        List<RepoUuidsInfo> repoUuidsInfosThatNeedScan = new ArrayList<>();
        for (RepoUuidsInfo repoUuidsInfo : repoUuidsInfos) {
            if (repoUuidsInfo.getLanguage() != null && (repoUuidsInfo.getLanguage().equals("Java") || repoUuidsInfo.getLanguage().equals("C++"))) {
                repoUuidsInfosThatNeedScan.add(repoUuidsInfo);
            }
        }
        List<ScanRepo> scanRepos = new ArrayList<>();
        for (RepoUuidsInfo re : repoUuidsInfosThatNeedScan) {
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
}
