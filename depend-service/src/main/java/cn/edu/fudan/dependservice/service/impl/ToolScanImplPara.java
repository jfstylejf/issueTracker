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
import cn.edu.fudan.dependservice.util.ReadUtill;
import cn.edu.fudan.dependservice.util.TimeUtil;
import cn.edu.fudan.dependservice.util.WriteUtil2;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

@Slf4j
@Service
@Data
public class ToolScanImplPara extends ToolScan {
    // where sh run
    String dependenceHome;
    String shName;
     long batchWaitTime;
    final static long oneWaitTime=6 * 60;
    //unit second, it is 3 minutes
    // todo set a   appropriate detectInterval
    final long detectInterval =2*60;
    String resultFileDir;
//    String configFile;
    boolean beforedetectRes;
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
    @Autowired
    public void setbatchWaitTime(@Value("${batchSize}") Integer batchSize) {
        this.batchWaitTime=(long) batchSize *oneWaitTime;
        log.info("batchWaitTime"+batchWaitTime);
    }

    @Override
    public boolean scanOneCommit(String commit) {
        //may do not need start scan.sh
        if(beforedetectRes){
            log.info("all have result , do not need star scan.sh ");
            putBatchData();
            return true;
        }
        ShThread shRunner = new ShThread();
        shRunner.setShName(shName);
        shRunner.setDependenceHome(dependenceHome);
        shRunner.setRepoPath(scanData.getRepoPath());

        Thread shThread = new Thread(shRunner);
        shThread.start();
        log.info("scan.sh start time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        long startTime = System.currentTimeMillis();
        boolean continuedetect = true;
        log.info("batchWaitTime: "+batchWaitTime);

        while (continuedetect) {
            if (resultFileDetect()) {
                break;
            }
            if ((System.currentTimeMillis() - startTime) / 1000 > batchWaitTime) {
                log.error("wait for sh result time too long");
                break;
            }
            try {
                Thread.sleep(detectInterval*1000);
            } catch (Exception e) {
                log.error("exception msg in sleep:" + e.getMessage());
                return false;
            }

        }
        long endTime = System.currentTimeMillis();
        log.info("The total cost of waiting for the sh results -> {}  second", (endTime - startTime) / 1000);
        putBatchData();

        return true;
    }
    public void putBatchData(){
        for (ScanRepo scanRepo : scanRepos) {
            if(scanRepo.isGetResult()){
                scanRepo.getScanStatus().setStatus("complete");
                Map<String, List> fileRes = null;
                String[] ss=scanRepo.getCopyRepoPath().split("\\/");
                String duplicateDirectoryName=ss[ss.length-1];
                ReadUtill readUtill = ReadUtill.builder().commitId(scanRepo.getScanCommit()).repo_uuid(scanRepo.getRepoUuid()).duplicateDirectoryName(duplicateDirectoryName).build();
                try {
                    fileRes = readUtill.getFileResult(resultFileDir+scanRepo.getResultFile());
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("deal result file error");
                }
                if (fileRes.size() > 0) {
                    put2DataBase(fileRes);
                }

            }else {
                scanRepo.getScanStatus().setStatus("fail");
                if(scanRepo.isCopyStatus()&&!scanRepo.isGetResult()){
                    scanRepo.getScanStatus().setMsg("tool fail,too long time");
                }
                if(!scanRepo.isCopyStatus()){
                    scanRepo.getScanStatus().setMsg("copy fail");
                }
            }
            setScanResult(scanRepo);
        }

    }

    private void setScanResult(ScanRepo scanRepo) {
        ScanStatus scanStatus =scanRepo.getScanStatus();
        scanStatus.setEndScanTime(TimeUtil.getCurrentDateTime());
        long now =System.currentTimeMillis();
        scanStatus.setTs_end(now);
        scanStatus.setScanTime(String.valueOf((now-scanStatus.getTs_start())/1000));
        scanDao.updateScan(scanRepo);
    }

    public boolean resultFileDetect() {
        log.info(" one result detect  every ->{} seconds " ,detectInterval);
        log.info("-----------------------------------------------");
        log.info("---------------------detect--------------------");
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
        this.setDependenceHome(applicationContext.getBean(ShHomeConfig.class).getDependenceHome());
        this.setShName(applicationContext.getBean(ShHomeConfig.class).getShName());
        this.setResultFileDir(applicationContext.getBean(ShHomeConfig.class).getResultFileDir());
    }

    @Override
    public void prepareForOneScan(String commit) {
        // check out to commit
        // write config
        log.info("before detect:");
        beforedetectRes=resultFileDetect();
        if(!beforedetectRes){
            // write config
            List<String> repoDirs =new ArrayList<>();
            for(ScanRepo scanRepo:scanRepos){
                if(scanRepo.toString()==null) scanRepo.setToScanDate(TimeUtil.getCurrentDateTime());
                    if(scanRepo.isCopyStatus()&&!scanRepo.isGetResult()){
                        repoDirs.add(scanRepo.getCopyRepoPath());
                    }
            }
            // scan
            //todo not all project is java
//            String configFile =null;
            String configFile = this.resultFileDir + "source-project-conf.json";

            WriteUtil2.writeProjecConf(configFile,repoDirs);

        }


    }

    @Override
    public void cleanUpForOneScan(String commit) {

        try {
            ShThread2 shRunner = new ShThread2();
            shRunner.setShName("stopScan.sh");
            shRunner.setDependenceHome(dependenceHome);
            shRunner.setRepoPath(scanData.getRepoPath());
            Thread shThread = new Thread(shRunner);
            shThread.start();
            shThread.join();
            log.info("stopScan.sh end ");
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
