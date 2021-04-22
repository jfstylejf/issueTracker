package cn.edu.fudan.dependservice.service;

import cn.edu.fudan.common.component.BaseRepoRestManager;

import cn.edu.fudan.common.jgit.JGitHelper;
import cn.edu.fudan.common.scan.CommonScanProcess;

import cn.edu.fudan.dependservice.config.ShHomeConfig;
import cn.edu.fudan.dependservice.domain.ScanRepo;
import cn.edu.fudan.dependservice.domain.ScanStatus;

import cn.edu.fudan.dependservice.utill.DirClone;
import cn.edu.fudan.dependservice.utill.TimeUtill;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import java.io.File;

@Data
@Service
public class ProcessPrepare{
    private static final Logger log = LoggerFactory.getLogger(CommonScanProcess.class);

    protected BaseRepoRestManager baseRepoRestManager;
    protected ApplicationContext applicationContext;
    private String repoDir;
    private String targetDir;
    private String toScanDate;

    @Autowired
    public ProcessPrepare(ApplicationContext applicationContext){
        this.applicationContext = applicationContext;
        baseRepoRestManager = applicationContext.getBean(BaseRepoRestManager.class);
        this.repoDir = applicationContext.getBean(ShHomeConfig.class).getRepoDir();

    }

    public ScanRepo prepareFile(String repoUuid, String branch, String date) {
        String repoPath= null;
        log.info("to scan bef");
        int timeStamp=getTimeStamp(date);

        ScanRepo scanRepo= new ScanRepo();
        scanRepo.setRepoUuid(repoUuid);
        scanRepo.setBranch(branch);
        ScanStatus scanStatus=new ScanStatus();
        scanRepo.setScanStatus(scanStatus);
        try {
            repoPath = baseRepoRestManager.getCodeServiceRepo(repoUuid);
        }catch (Exception e){
            log.info("Exception: "+ e.getMessage());
            copyFail(scanRepo,scanStatus);
            return scanRepo;
        }
        if (repoPath == null) {
            log.error("{} : can't get repoPath", repoUuid);
            copyFail(scanRepo,scanStatus);
            return scanRepo;
        }
        scanRepo.setRepoPath(repoPath);
        try {
            JGitHelper jGitHelper=new JGitHelper(repoPath);
            String toScanCommit=jGitHelper.gettoScanCommit(branch,timeStamp);
            if(toScanCommit==null){
                scanRepo.setCopyStatus(false);
                scanRepo.getScanStatus().setStatus("fail");
                scanRepo.setMsg(" no commit before the time ");
                return scanRepo;
            }
            jGitHelper.checkout(toScanCommit);
            scanRepo.setScanCommit(toScanCommit);
            String[] repoPaths=repoPath.split(File.separator);
            log.info("repoPath: " +repoPath);

            targetDir=repoDir+repoPaths[repoPaths.length-1]+"_"+toScanCommit;
            log.info("targetDir :"+targetDir);
            scanRepo.setCopyRepoPath(targetDir);
            if(new File(targetDir).exists()) {
                log.info("targetDir exit, do not copy again");
                copyOK(scanRepo,scanStatus);
                return scanRepo;
            }
            if(copyFile(repoPath,targetDir)){
                copyOK(scanRepo,scanStatus);

            }else {
                copyFail(scanRepo,scanStatus);
                return scanRepo;
            }

        } catch (Exception e) {
            log.info("Exception:"+e.getMessage());
            scanRepo.getScanStatus().setStatus("fail");
            e.printStackTrace();
        } finally {
            baseRepoRestManager.freeRepo(repoUuid, repoPath);
        }
        return scanRepo;
    }
    private int getTimeStamp(String datetime){
        return TimeUtill.timeStampforJgit(datetime);
    }

    private void copyOK(ScanRepo scanRepo,ScanStatus scanStatus) {
        scanRepo.setCopyStatus(true);
    }
    public void copyFail(ScanRepo scanRepo,ScanStatus scanStatus){
        scanRepo.setCopyStatus(false);
        scanStatus.setStatus("failed");
        scanStatus.setMsg("get repo fail");
    }

    public boolean copyFile(String source,String target){
        DirClone clone =new DirClone(source,target);
        return clone.copy();

    }



}