package cn.edu.fudan.common.scan;

import cn.edu.fudan.common.domain.ScanInfo;

public interface ScanService {

    /**
     * 扫描
     * @param repoId repoUuid
     * @param branch repo所在分支
     * @param beginCommit 首个扫描版本id，为null说明是更新扫描
     */
    void scan(String repoId, String branch, String beginCommit);

    /**
     * 获取扫描信息
     * @param repoId repo所在分支
     * @return
     */
    ScanInfo getScanStatus(String repoId);

    /**
     * 更新扫描信息
     * @param scanInfo
     */
    void updateScanInfo(ScanInfo scanInfo);

    /**
     * 停止扫描
     * @param repoId
     * @return
     */
    boolean stopScan(String repoId);

    boolean continueScan(String repoId);

    /**
     * 删除代码库所有数据
     * @param repoId
     */
    void deleteRepo(String repoId);

    /**
     * 插入扫描信息
     * @param scanInfo
     */
    void insertScanInfo(ScanInfo scanInfo);
}
