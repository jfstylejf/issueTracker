package cn.edu.fudan.cloneservice.task;

import cn.edu.fudan.cloneservice.domain.clone.CloneScanInitialInfo;
import cn.edu.fudan.cloneservice.domain.clone.CloneScanResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author zyh
 * @date 2020/5/25
 */
@Slf4j
@Component
public class ScanTask {

    @Resource(name = "CPUClone")
    private ScanOperation scanOperation;


    private void scan(ScanOperation scanOperation, String repoId, String commitId, String type, String repoPath) throws RuntimeException, IOException {
        //没有共享资源，不需要锁
        //判断当前repoId, commitId, type是否扫描过
        if (scanOperation.isScanned(repoId,commitId,type)) {
            log.warn("{} -> this commit has been scanned", Thread.currentThread().getName());
            return;
        }
        log.info("{} -> this commit ---> {} has not been scanned,start the scan initialization......", Thread.currentThread().getName(), commitId);

        CloneScanInitialInfo cloneScanInitialInfo = scanOperation.initialScan(repoId, commitId, type, repoPath);
        if(!cloneScanInitialInfo.isSuccess()){
            log.error("{} -> Initial Failed!", Thread.currentThread().getName());
            return;
        }
        CloneScanResult cloneScanResult = scanOperation.doScan(cloneScanInitialInfo);

        if ("failed".equals(cloneScanResult.getStatus())) {
            scanOperation.updateScan(cloneScanInitialInfo);
            log.error(cloneScanResult.getDescription());
            return;
        }

        log.info("{} -> scan complete ->" + cloneScanResult.getDescription(), Thread.currentThread().getName());
        log.info("{} -> start to update scan status", Thread.currentThread().getName());
        cloneScanInitialInfo.getCloneScan().setStatus("done");
        if (!scanOperation.updateScan(cloneScanInitialInfo)) {
            log.error("{} -> Scan Update Failed!", Thread.currentThread().getName());
            return;
        }
        log.info("{} -> scan update complete", Thread.currentThread().getName());
    }

    public void runSynchronously(String repoId, String commitId, String category, String repoPath) throws RuntimeException, IOException {
        scan(scanOperation, repoId, commitId, category, repoPath);
    }

}
