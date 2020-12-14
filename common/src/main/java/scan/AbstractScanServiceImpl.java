package scan;

import cn.edu.fudan.common.component.RestInterfaceManager;
import cn.edu.fudan.common.domain.ScanInfo;
import cn.edu.fudan.common.jgit.JGitHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public abstract class AbstractScanServiceImpl implements ScanService {
    private static final Logger log = LoggerFactory.getLogger(AbstractScanServiceImpl.class);
    private static ThreadLocal<String> repoPath = new ThreadLocal();
    private ConcurrentHashMap<String, Boolean> scanStatusMap = new ConcurrentHashMap();
    private RestInterfaceManager restInterfaceManager;
    private final Short lock = Short.valueOf((short)1);
    private JGitHelper jGitHelper;

    public AbstractScanServiceImpl() {
    }

    @Override
    @Async("taskExecutor")
    public void scan(String repoId, String branch, String beginCommit) {
        synchronized(this.lock) {
            if (this.scanStatusMap.keySet().contains(repoId)) {
                this.scanStatusMap.put(repoId, true);
                return;
            }

            this.scanStatusMap.putIfAbsent(repoId, false);
        }

        this.prepareForScan(repoId, branch, beginCommit);
    }

    private void prepareForScan(String repoId, String branch, String beginCommit) {
        ScanInfo scanInfo = this.getScanStatus(repoId);
        if (beginCommit != null) {
            if (scanInfo != null) {
                log.warn("{} : already scanned before", repoId);
                this.checkAfterScan(repoId, branch);
                return;
            }

            this.beginScan(repoId, branch, beginCommit, false);
        }

        if (beginCommit == null) {
            if (scanInfo == null || scanInfo.getLatestCommit() == null) {
                log.warn("{} : hasn't scanned before", repoId);
                this.checkAfterScan(repoId, branch);
                return;
            }

            if (ScanInfo.Status.SCANNING.equals(scanInfo.getStatus())) {
                log.warn("{} : already scanning", repoId);
                this.checkAfterScan(repoId, branch);
                return;
            }

            this.beginScan(repoId, branch, scanInfo.getLatestCommit(), true);
        }

        this.checkAfterScan(repoId, branch);
    }

    private void beginScan(String repoId, String branch, String beginCommit, boolean isUpdate) {
        // 获取repo所在路径
        if (this.useCustomPath()) {
            repoPath.set(this.getCustomPath(repoId));
        } else {
            String path = this.restInterfaceManager.getCodeServiceRepo(repoId);
            if (path == null) {
                log.error("{} : can't get repoPath", repoId);
                return;
            }
            repoPath.set(path);
        }

        this.setJGitHelper(repoPath.get());
        List<String> commitList = this.jGitHelper.getScanCommitListByBranchAndBeginCommit(branch, beginCommit);
        log.info("commit size : {}" , commitList.size());
        ScanInfo scanInfo = new ScanInfo(UUID.randomUUID().toString(), ScanInfo.Status.SCANNING, commitList.size(), 0, new Date(), repoId, branch);
        this.insertScanInfo(scanInfo);
        boolean success = this.scanCommitList(repoId, branch, repoPath.get(), this.jGitHelper, commitList, isUpdate, scanInfo);
        scanInfo.setStatus(success ? ScanInfo.Status.COMPLETE : ScanInfo.Status.FAILED);
        this.updateScanInfo(scanInfo);
        this.restInterfaceManager.freeRepo(repoId, repoPath.get());
    }

    private void checkAfterScan(String repoId, String branch) {
        if (!this.scanStatusMap.keySet().contains(repoId)) {
            log.error("{} : not in scan map", repoId);
        } else {
            synchronized(this.lock) {
                boolean newUpdate = (Boolean)this.scanStatusMap.get(repoId);
                if (!newUpdate) {
                    this.scanStatusMap.remove(repoId);
                    return;
                }

                this.scanStatusMap.put(repoId, false);
            }

            this.prepareForScan(repoId, branch, (String)null);
        }
    }

    /**
     * 由子类实现，用于自定义传入repo的路径
     * @param repoUuid repoUuid
     * @return repo路径
     */
    public abstract String getCustomPath(String repoUuid);

    /**
     * 由子类实现，根据路径生成具体的jGitHelper
     * @param repoPath repo路径
     */
    public abstract void setJGitHelper(String repoPath);

    public <T extends JGitHelper> void setJGitHelper(T jGitHelper) {
        this.jGitHelper = jGitHelper;
    }

    /**
     * 由子类实现具体扫描流程
     * @param repoId repoUuid
     * @param branch repo分支
     * @param repoPath repo路径
     * @param jGitHelper jGitHelper
     * @param commitList 扫描列表
     * @param isUpdate 是否为更新扫描，false表示初次扫描，true表示更新扫描
     * @param scanInfo 扫描信息
     * @return 扫描是否完成 true: 完成；false: 未完成
     */
    public abstract boolean scanCommitList(String repoId, String branch, String repoPath, JGitHelper jGitHelper, List<String> commitList, boolean isUpdate, ScanInfo scanInfo);

    /**
     * 由子类实现，用于决定是否使用自定义路径
     * @return true: 是，false: 否
     */
    public abstract boolean useCustomPath();

    public <T extends RestInterfaceManager> void setRestInterfaceManager(T restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    public void setScanStatusMap(final ConcurrentHashMap<String, Boolean> scanStatusMap) {
        this.scanStatusMap = scanStatusMap;
    }

    public ConcurrentHashMap<String, Boolean> getScanStatusMap() {
        return this.scanStatusMap;
    }

    public RestInterfaceManager getRestInterfaceManager() {
        return this.restInterfaceManager;
    }

    public Short getLock() {
        return this.lock;
    }

    public JGitHelper getJGitHelper() {
        return this.jGitHelper;
    }
}
