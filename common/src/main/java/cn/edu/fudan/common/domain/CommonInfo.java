package cn.edu.fudan.common.domain;

import java.util.Date;
import java.util.List;

public class CommonInfo {
    private String startCommit;
    private Date startCommitDate;
    private String endCommit;
    private Date endCommitDate;
    private String repoUuid;
    private String branch;
    private Date commitDate;
    private String commit;
    private String committer;
    private String commitMessage;
    private String parentCommit;
    private List<String> deleteFile;
    private List<String> addFile;
    private List<String> filterFile;

    public CommonInfo() {
    }

    public CommonInfo(String repoUuid, String branch, String commit, Date commitDate, String committer, String commitMessage, String parentCommit) {
        this.repoUuid = repoUuid;
        this.branch = branch;
        this.commit = commit;
        this.commitDate = commitDate;
        this.committer = committer;
        this.commitMessage = commitMessage;
        this.parentCommit = parentCommit;
    }

    public String getStartCommit() {
        return this.startCommit;
    }

    public Date getStartCommitDate() {
        return this.startCommitDate;
    }

    public String getEndCommit() {
        return this.endCommit;
    }

    public Date getEndCommitDate() {
        return this.endCommitDate;
    }

    public String getRepoUuid() {
        return this.repoUuid;
    }

    public String getBranch() {
        return this.branch;
    }

    public Date getCommitDate() {
        return this.commitDate;
    }

    public String getCommit() {
        return this.commit;
    }

    public String getCommitter() {
        return this.committer;
    }

    public String getCommitMessage() {
        return this.commitMessage;
    }

    public String getParentCommit() {
        return this.parentCommit;
    }

    public List<String> getDeleteFile() {
        return this.deleteFile;
    }

    public List<String> getAddFile() {
        return this.addFile;
    }

    public List<String> getFilterFile() {
        return this.filterFile;
    }

    public void setStartCommit(final String startCommit) {
        this.startCommit = startCommit;
    }

    public void setStartCommitDate(final Date startCommitDate) {
        this.startCommitDate = startCommitDate;
    }

    public void setEndCommit(final String endCommit) {
        this.endCommit = endCommit;
    }

    public void setEndCommitDate(final Date endCommitDate) {
        this.endCommitDate = endCommitDate;
    }

    public void setRepoUuid(final String repoUuid) {
        this.repoUuid = repoUuid;
    }

    public void setBranch(final String branch) {
        this.branch = branch;
    }

    public void setCommitDate(final Date commitDate) {
        this.commitDate = commitDate;
    }

    public void setCommit(final String commit) {
        this.commit = commit;
    }

    public void setCommitter(final String committer) {
        this.committer = committer;
    }

    public void setCommitMessage(final String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public void setParentCommit(final String parentCommit) {
        this.parentCommit = parentCommit;
    }

    public void setDeleteFile(final List<String> deleteFile) {
        this.deleteFile = deleteFile;
    }

    public void setAddFile(final List<String> addFile) {
        this.addFile = addFile;
    }

    public void setFilterFile(final List<String> filterFile) {
        this.filterFile = filterFile;
    }
}
