package cn.edu.fudan.cloneservice.task;

import cn.edu.fudan.cloneservice.component.RestInterfaceManager;
import cn.edu.fudan.cloneservice.dao.CloneScanDao;
import cn.edu.fudan.cloneservice.domain.clone.CloneScan;
import cn.edu.fudan.cloneservice.domain.clone.CloneScanInitialInfo;
import cn.edu.fudan.cloneservice.domain.clone.CloneScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

/**
 * @author zyh
 * @date 2020/5/25
 */
@Component
public class ScanOperationAdapter implements ScanOperation {
    private final static Logger logger = LoggerFactory.getLogger(ScanOperationAdapter.class);

    RestInterfaceManager restInterfaceManager;

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    private CloneScanDao cloneScanDao;

    @Autowired
    public void setCloneScanDao(CloneScanDao cloneScanDao){
        this.cloneScanDao = cloneScanDao;
    }

    @Override
    public boolean isScanned(String repoId,String commitId,String type) {
        return cloneScanDao.isScanned(repoId,commitId,type);
    }

    @Override
    public CloneScanInitialInfo initialScan(String repoId, String commitId, String type, String repoPath) throws RuntimeException{
        Date startTime = new Date();
        //新建一个Scan对象
        CloneScan cloneScan = new CloneScan();
        String uuid = UUID.randomUUID().toString();
        cloneScan.setType(type);
        cloneScan.setStartTime(startTime);
        cloneScan.setStatus("doing...");
        cloneScan.setRepoId(repoId);
        cloneScan.setCommitId(commitId);
        cloneScan.setUuid(uuid);
        cloneScanDao.insertCloneScan(cloneScan);
        return new CloneScanInitialInfo(cloneScan, repoId, repoPath, true);
    }

    @Override
    public CloneScanResult doScan(CloneScanInitialInfo cloneScanInitialInfo) {
        //等待子类的具体实现
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean updateScan(CloneScanInitialInfo cloneScanInitialInfo) {
        CloneScan cloneScan = cloneScanInitialInfo.getCloneScan();
        //更新当前Scan的状态
        cloneScan.setEndTime(new Date());
        cloneScanDao.updateCloneScan(cloneScan);
        return true;
    }
}
