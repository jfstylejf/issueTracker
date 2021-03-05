package cn.edu.fudan.common.scan;

import cn.edu.fudan.common.domain.ScanData.ScanData;

import java.util.List;


/**
 * 不同工具的具体扫描流程
 **/
public interface ToolScan {

    ScanData scanData =  new ScanData();

    /**
     * 由子类实现具体扫描流程
     * @param commit 单个commit
     */
    boolean scanOneCommit(String commit);


    default void loadData(String repoUuid, String branch, String repoPath, boolean initialScan, List<String> toScanCommitList){
        scanData.setBranch(branch);
        scanData.setRepoPath(repoPath);
        scanData.setRepoUuid(repoUuid);
        scanData.setToScanCommitList(toScanCommitList);
        scanData.setInitialScan(initialScan);
    }

    default ScanData getScanData() {
        return scanData;
    }

    /**
     * 开始扫描commit列表之前的准备工作 可以为空方法
     **/
    void prepareForScan();

    /**
     * 开始扫描一个 commit 之前的准备工作 可以为空方法
     **/
    void prepareForOneScan(String commit);

    /**
     * 完成扫描一个 commit 之后的清理工作 可以为空方法
     **/
    void cleanUpForOneScan(String commit);
    /**
     * 完成扫描 commit 列表 之后的清理工作 可以为空方法
     **/
    void cleanUpForScan();
}
