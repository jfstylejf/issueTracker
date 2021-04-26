package cn.edu.fudan.dependservice.service;

import cn.edu.fudan.common.component.BaseRepoRestManager;

import cn.edu.fudan.common.jgit.JGitHelper;
import cn.edu.fudan.common.scan.CommonScanProcess;

import cn.edu.fudan.dependservice.config.ShHomeConfig;
import cn.edu.fudan.dependservice.domain.ScanRepo;
import cn.edu.fudan.dependservice.util.DirClone;
import cn.edu.fudan.dependservice.util.TimeUtil;
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

    public void prepareFile(String date,ScanRepo scanRepo) {
        String repoPath= null;
        int timeStamp=getTimeStamp(date);

        try {
            repoPath = baseRepoRestManager.getCodeServiceRepo(scanRepo.getRepoUuid());
        }catch (Exception e){
            log.info("Exception: "+ e.getMessage());
            copyFail(scanRepo);
            return ;
        }
        if (repoPath == null) {
            log.error("{} : can't get repoPath", scanRepo.getRepoUuid());
            copyFail(scanRepo);
            return ;
        }
        scanRepo.setRepoPath(repoPath);
        try {
            JGitHelper jGitHelper=new JGitHelper(repoPath);
            String toScanCommit=jGitHelper.gettoScanCommit(scanRepo.getBranch(),timeStamp);
            if(toScanCommit==null){
                scanRepo.setCopyStatus(false);
                scanRepo.getScanStatus().setStatus("fail");
                scanRepo.setMsg("no commit before the time ");
                return ;
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
                copyOK(scanRepo);
                return ;
            }
            if(copyFile(repoPath,targetDir)){
                copyOK(scanRepo);

            }else {
                copyFail(scanRepo);
                return ;
            }

        } catch (Exception e) {
            log.info("Exception:"+e.getMessage());
            scanRepo.getScanStatus().setStatus("fail");
            e.printStackTrace();
        } finally {
            baseRepoRestManager.freeRepo(scanRepo.getRepoUuid(), repoPath);
        }
        return ;
    }
    private int getTimeStamp(String datetime){
        return TimeUtil.timeStampforJgit(datetime);
    }

    private void copyOK(ScanRepo scanRepo) {
        scanRepo.setCopyStatus(true);
    }
    public void copyFail(ScanRepo scanRepo){
        scanRepo.setCopyStatus(false);
        scanRepo.getScanStatus().setStatus("failed");
        scanRepo.getScanStatus().setMsg("get repo fail");
    }

    public boolean copyFile(String source,String target){
        DirClone clone =new DirClone(source,target);
        return clone.copy();

    }



}
