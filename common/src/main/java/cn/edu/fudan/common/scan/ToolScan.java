package cn.edu.fudan.common.scan;

import cn.edu.fudan.common.domain.po.scan.RepoScan;
import cn.edu.fudan.common.domain.po.scan.ScanData;

import java.util.List;


/**
 * 不同工具扫描流程
 *
 * @author beethoven
 */
public abstract class ToolScan {

    protected ScanData scanData = new ScanData();

    /**
     * 由子类实现具体扫描流程
     *
     * @param commit commit
     * @return
     */
    public abstract boolean scanOneCommit(String commit);

    void loadData(String repoUuid, String branch, String repoPath, boolean initialScan, List<String> toScanCommitList, RepoScan repoScan, Integer scannedCommitCount) {
        scanData.setBranch(branch);
        scanData.setRepoPath(repoPath);
        scanData.setRepoUuid(repoUuid);
        scanData.setToScanCommitList(toScanCommitList);
        scanData.setInitialScan(initialScan);
        scanData.setRepoScan(repoScan);
        scanData.setScannedCommitCount(scannedCommitCount);
    }

    public ScanData getScanData() {
        return scanData;
    }

    /**
     * 开始扫描commit列表之前的准备工作,可以为空方法
     **/
    public abstract void prepareForScan();

    /**
     * 开始扫描一个commit之前的准备工作,可以为空方法
     *
     * @param commit commit
     */
    public abstract void prepareForOneScan(String commit);

    /**
     * 完成扫描一个commit之后的清理工作,可以为空方法
     *
     * @param commit commit
     */
    public abstract void cleanUpForOneScan(String commit);

    /**
     * 完成扫描commit列表之后的清理工作,可以为空方法
     **/
    public abstract void cleanUpForScan();
}
