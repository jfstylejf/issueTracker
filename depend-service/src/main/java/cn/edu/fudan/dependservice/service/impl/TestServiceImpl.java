package cn.edu.fudan.dependservice.service.impl;

import cn.edu.fudan.common.domain.po.scan.RepoScan;
import cn.edu.fudan.common.scan.CommonScanProcess;
import cn.edu.fudan.common.scan.CommonScanService;
import cn.edu.fudan.dependservice.dao.StatisticsDao;
import cn.edu.fudan.dependservice.domain.ProjectIdsInfo;
import cn.edu.fudan.dependservice.domain.RepoUuidsInfo;
import cn.edu.fudan.dependservice.mapper.GroupMapper;
import cn.edu.fudan.dependservice.mapper.RepoMapper;
import cn.edu.fudan.dependservice.service.TempProcess;
import cn.edu.fudan.dependservice.service.TestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TestServiceImpl implements TestService {
    @Autowired
    GroupMapper groupMapper;
    @Autowired
    RepoMapper repoMapper;

    @Autowired
    StatisticsDao statisticsDao;

//    @Autowired
//    TempScanServiceImpl scanService;


//    @Autowired
//    ScanServiceImpl scanService;

//    @Autowired
//    TempProcess tempProcess;

    @Autowired
    CommonScanProcess scanService;


    @Override
    public List<String> getAllRepoUuidThatNeedScan() {
//        List<ProjectIdsInfo> res=repoMapper.getAllProjectIds();
        List<RepoUuidsInfo> repoUuidsInfos= statisticsDao.getallRepoUuid();
        List<RepoUuidsInfo> repoUuidsInfosThatNeedScan=new ArrayList<>();
        for(RepoUuidsInfo repoUuidsInfo:repoUuidsInfos){
            if(repoUuidsInfo.getLanguage()!=null&&(repoUuidsInfo.getLanguage().equals("Java")||repoUuidsInfo.getLanguage().equals("C++"))){
                repoUuidsInfosThatNeedScan.add(repoUuidsInfo);
            }
        }
        log.info("need scan size:"+repoUuidsInfosThatNeedScan.size());

        for(RepoUuidsInfo re:repoUuidsInfosThatNeedScan){
//            scanService.scan(re.getRepoUuid(),re.getBranch(),null);
//            tempProcess.scan(re.getRepoUuid(),re.getBranch(), null);
            scanService.scan(re.getRepoUuid(),re.getBranch(),null);

            //to do scan this repo
        }

        return null;
    }
    public RepoScan getScanStatus(String repouuid){
        //
        return null;

    }
    public RepoScan StartOneScan(String repouuid,String branch,String startCommitId){
        scanService.scan(repouuid,branch,startCommitId);
        return null;

    }

    public void scanAllRepo() {


    }
}
