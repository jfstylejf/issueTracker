package cn.edu.fudan.common.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
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

    public CommonInfo(String repoUuid, String branch, String commit, Date commitDate, String committer, String commitMessage, String parentCommit) {
        this.repoUuid = repoUuid;
        this.branch = branch;
        this.commit = commit;
        this.commitDate = commitDate;
        this.committer = committer;
        this.commitMessage = commitMessage;
        this.parentCommit = parentCommit;
    }

}
