package cn.edu.fudan.cloneservice.domain.clone;

/**
 * @author zyh
 * @date 2020/5/26
 */
public class CloneScanInitialInfo {

    private CloneScan cloneScan;
    private String repoId;
    private String repoPath;
    private boolean isSuccess;
    private String language;
    public CloneScanInitialInfo(){}

    public CloneScanInitialInfo(CloneScan cloneScan, String repoId, String repoPath, boolean isSuccess, String language) {
        this.cloneScan = cloneScan;
        this.repoId = repoId;
        this.repoPath = repoPath;
        this.isSuccess = isSuccess;
        this.language = language;
    }

    public CloneScan getCloneScan() {
        return cloneScan;
    }

    public void setCloneScan(CloneScan cloneScan) {
        this.cloneScan = cloneScan;
    }

    public String getRepoId() {
        return repoId;
    }

    public void setRepoId(String repoId) {
        this.repoId = repoId;
    }

    public String getRepoPath() {
        return repoPath;
    }

    public String getLanguage(){ return language;}

    public void setRepoPath(String repoPath) {
        this.repoPath = repoPath;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }
}
