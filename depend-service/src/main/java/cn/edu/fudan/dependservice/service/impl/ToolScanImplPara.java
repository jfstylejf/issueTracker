package cn.edu.fudan.dependservice.service.impl;

import cn.edu.fudan.common.scan.ToolScan;
import cn.edu.fudan.dependservice.config.ShHomeConfig;
import cn.edu.fudan.dependservice.dao.ScanDao;
import cn.edu.fudan.dependservice.domain.Group;
import cn.edu.fudan.dependservice.domain.RelationShip;
import cn.edu.fudan.dependservice.domain.ScanRepo;
import cn.edu.fudan.dependservice.domain.ScanStatus;
import cn.edu.fudan.dependservice.mapper.GroupMapper;
import cn.edu.fudan.dependservice.mapper.RelationshipMapper;
import cn.edu.fudan.dependservice.utill.WriteUtill;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * description:
 * dependency scan tool that can scan many repo one time
 *
 * @author shao xi
 * create: 2021-04-06 21:06
 **/

/**
 * c
 */
@Slf4j
@Service
@Data
public class ToolScanImplPara extends ToolScan {
    // where sh run
    String dependenceHome;
    String shName;
    //unit second.it is 40 minutes
    final long batchWaitTime = 2400;
    //unit second, it is 3 minutes
    final long detectInterval =3*60;
    String resultFileDir;
    List<ScanRepo> scanRepos;
    ApplicationContext applicationContext;

    @Autowired
    RelationshipMapper relationshipMapper;

    @Autowired
    ScanDao scanDao;

    @Autowired
    GroupMapper groupMapper;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean scanOneCommit(String commit) {
        ShThread shRunner = new ShThread();
        shRunner.setShName(shName);
        shRunner.setDependenceHome(dependenceHome);
        shRunner.setRepoPath(scanData.getRepoPath());

        Thread shThread = new Thread(shRunner);
        shThread.start();
        log.info("sh1 start time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        long startTime = System.currentTimeMillis();
        boolean continuedetect = true;
        log.info("wait for sh result");

        while (continuedetect) {
            if ((System.currentTimeMillis() - startTime) / 1000 > batchWaitTime) {
                log.error("wait for sh result time too long");
                break;
            }
            if (resultFileDetect()) {
                break;
            }
            try {
                Thread.sleep(detectInterval*1000);
            } catch (Exception e) {
                log.error("exception ms:" + e.getMessage());
                return false;
            }

        }
        long endTime = System.currentTimeMillis();
        log.info("The total cost of waiting for the sh results -> {}  second", (endTime - startTime) / 1000);
        for (ScanRepo scanRepo : scanRepos) {
            if(scanRepo.isGetResult()){
                scanRepo.getScanStatus().setStatus("success");
                Map<String, List> fileRes = null;
                String[] ss=scanRepo.getCopyRepoPath().split("\\/");
                String duplicateDirectoryName=ss[ss.length-1];
                ReadUtill readUtill = ReadUtill.builder().commitId(scanRepo.getScanCommit()).repo_uuid(scanRepo.getRepoUuid()).duplicateDirectoryName(duplicateDirectoryName).build();
                try {
                    fileRes = readUtill.getFileResult(resultFileDir+scanRepo.getResultFile());
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("deal result file error");
                    return false;
                }
                if (fileRes.size() > 0) {
                    put2DataBase(fileRes);
                }

            }else {
                scanRepo.getScanStatus().setStatus("fail");
            }
            setScanResult(scanRepo);
        }
        return true;
    }

    private void setScanResult(ScanRepo scanRepo) {
        scanDao.updateScan(scanRepo);

    }

    public boolean resultFileDetect() {
        log.info(" one result detect  every ->{} seconds " ,detectInterval);
        log.info("-----------------------------------------------");
        log.info("---------------------detect--------------------");
        log.info("-----------------------------------------------");
        boolean res = true;
        File dir = new File(resultFileDir);
        if (!dir.isDirectory()) {
            log.error("resultfileDir Wrong");
            return false;
        }
        String[] files = dir.list();// 读取目录下的所有目录文件信息
        int needScan=0;
        int haveResult=0;
        int haveNotResult=0;
        for (ScanRepo scanRepo : scanRepos) {
            if(scanRepo.isCopyStatus()) needScan++;
        }
        for (ScanRepo scanRepo : scanRepos) {
            if (scanRepo.isCopyStatus()&&!scanRepo.isGetResult()) {
                String fileName= scanRepo.getCopyRepoPath().substring(scanRepo.getCopyRepoPath().lastIndexOf("/")+1);
                for (String s : files) {
                    if (s.indexOf(fileName) != -1) {
                        scanRepo.setGetResult(true);
                        scanRepo.setResultFile(s);
                    }
                }
            }

            if (scanRepo.isCopyStatus() && !scanRepo.isGetResult()) {
                haveNotResult++;
                res = false;

            }
            if (scanRepo.isCopyStatus() && scanRepo.isGetResult()) {
                haveResult++;
            }
        }
        log.info("detectResult:");
        log.info("needScan: "+needScan);
        log.info("haveResult: "+haveResult);
        log.info("haveNotResult: "+haveNotResult);
        return res;
    }

    @Override
    public void prepareForScan() {

        //  this.dependenceHome = applicationContext.getBean(ShHomeConfig.class).getDependenceHome();
        this.setDependenceHome(applicationContext.getBean(ShHomeConfig.class).getDependenceHome());
        this.setShName(applicationContext.getBean(ShHomeConfig.class).getShName());
        this.setResultFileDir(applicationContext.getBean(ShHomeConfig.class).getResultFileDir());
        //make config file
        String configFile = this.resultFileDir + "source-project-conf.json";
        log.info("configFile :" + configFile);
//        WriteUtill.writeProjecConf(configFile, this.getScanData().getRepoPath());
    }

    @Override
    public void prepareForOneScan(String commit) {
        // check out to commit

    }

    @Override
    public void cleanUpForOneScan(String commit) {

        try {
            ShThread2 shRunner = new ShThread2();
            shRunner.setShName("tdepend2.sh");
            shRunner.setDependenceHome(dependenceHome);
            shRunner.setRepoPath(scanData.getRepoPath());
            Thread shThread = new Thread(shRunner);
            shThread.start();
            shThread.join();
            log.info("sh2 end ");

        } catch (Exception e) {
            log.error("Exception:" + e.getMessage());
        }


    }


    @Override
    public void cleanUpForScan() {
        // todo close neo4j
    }

    public void put2DataBase(Map<String, List> fileRes) {

        if (fileRes.containsKey("group")) {
            for (Object g : fileRes.get("group")) {
                scanDao.addGroup((Group) g);

            }

        }
        if (fileRes.containsKey("relation")) {
            for (Object g : fileRes.get("relation")) {
                scanDao.addRelation((RelationShip) g);
            }

        }


    }
}
