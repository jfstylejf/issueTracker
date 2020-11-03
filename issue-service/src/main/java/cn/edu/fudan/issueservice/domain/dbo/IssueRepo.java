package cn.edu.fudan.issueservice.domain.dbo;

import cn.edu.fudan.issueservice.domain.enums.RepoStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
public class IssueRepo {

    private String uuid;
    private String repoId;
    private String branch;
    private String tool;
    private String startCommit;
    private String endCommit;
    private int totalCommitCount;
    private int scannedCommitCount;
    private long scanTime;
    private String status;
    private String nature;
    private Date startScanTime;
    private Date endScanTime;

    public void incrementScannedCommitCount(){
        scannedCommitCount++;
    }

    public static IssueRepo initIssueRepo(String repoId, String branch, String commitId, String toolName,int totalCommitCount){
        IssueRepo issueRepo = new IssueRepo ();
        issueRepo.setUuid (UUID.randomUUID().toString());
        issueRepo.setRepoId (repoId);
        issueRepo.setBranch (branch);
        issueRepo.setTool (toolName);
        issueRepo.setStartCommit (commitId);
        issueRepo.setTotalCommitCount (totalCommitCount);
        issueRepo.setStatus (RepoStatusEnum.SCANNING.getType ());
        issueRepo.setScanTime (0);
        issueRepo.setStartScanTime (new Date ());

        return issueRepo;
    }


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getRepoId() {
        return repoId;
    }

    public void setRepoId(String repoId) {
        this.repoId = repoId;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public String getStartCommit() {
        return startCommit;
    }

    public void setStartCommit(String startCommit) {
        this.startCommit = startCommit;
    }

    public String getEndCommit() {
        return endCommit;
    }

    public void setEndCommit(String endCommit) {
        this.endCommit = endCommit;
    }

    public int getTotalCommitCount() {
        return totalCommitCount;
    }

    public void setTotalCommitCount(int totalCommitCount) {
        this.totalCommitCount = totalCommitCount;
    }

    public int getScannedCommitCount() {
        return scannedCommitCount;
    }

    public void setScannedCommitCount(int scannedCommitCount) {
        this.scannedCommitCount = scannedCommitCount;
    }

    public long getScanTime() {
        return scanTime;
    }

    public void setScanTime(long scanTime) {
        this.scanTime = scanTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        this.nature = nature;
    }

    public Date getStartScanTime() {
        return startScanTime;
    }

    public void setStartScanTime(Date startScanTime) {
        this.startScanTime = startScanTime;
    }

    public Date getEndScanTime() {
        return endScanTime;
    }

    public void setEndScanTime(Date endScanTime) {
        this.endScanTime = endScanTime;
    }
}
