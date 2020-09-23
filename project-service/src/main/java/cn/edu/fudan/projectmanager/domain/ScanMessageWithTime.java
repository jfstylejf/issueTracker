package cn.edu.fudan.projectmanager.domain;


/**
 * @author WZY
 * @version 1.0
 **/
public class ScanMessageWithTime {

    private String repoId;
    private String commitId;
    private String commitTime;

    public ScanMessageWithTime() {
    }

    public ScanMessageWithTime(String repoId, String commitId) {
        this.repoId = repoId;
        this.commitId = commitId;
    }

    public String getRepoId() {
        return repoId;
    }

    public void setRepoId(String repoId) {
        this.repoId = repoId;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(String commitTime) {
        this.commitTime = commitTime;
    }


    @Override
    public String toString() {
        return "ScanMessageWithTime{" +
                "repoId='" + repoId + '\'' +
                ", commitId='" + commitId + '\'' +
                ", commitTime=" + commitTime +
                '}';
    }

    @Override
    public int hashCode() {
        int result = repoId != null ? repoId.hashCode() : 0;
        result = 31 * result + (commitId != null ? commitId.hashCode() : 0);
        result = 31 * result + (commitTime != null ? commitTime.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj) {
            return true;
        }
        if(obj == null){
            return false;
        }
        if(getClass() != obj.getClass()){
            return false;
        }
        ScanMessageWithTime other = (ScanMessageWithTime)obj;
        return repoId == other.getRepoId() && commitId == other.getCommitId() && commitTime == other.getCommitTime();
    }
}
