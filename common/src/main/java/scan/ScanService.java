package scan;

import cn.edu.fudan.common.domain.ScanInfo;

public interface ScanService {

    void scan(String repoId, String branch, String beginCommit);

    ScanInfo getScanStatus(String repoId);

    void updateScanInfo(ScanInfo scanInfo);

    boolean stopScan(String repoId);

    boolean continueScan(String repoId);

    void deleteRepo(String repoId);

    void insertScanInfo(ScanInfo scanInfo);
}
