package cn.edu.fudan.issueservice.domain.vo;

import cn.edu.fudan.issueservice.domain.dbo.Commit;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author beethoven
 * @date 2021-06-23 15:18:53
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommitVO extends Commit {

    private String scanStatus;

    public CommitVO(Commit commit, String scanStatus) {
        this.scanStatus = scanStatus;
        this.commitId = commit.getCommitId();
        this.commitTime = commit.getCommitTime();
        this.developer = commit.getDeveloper();
        this.developerEmail = commit.getDeveloperEmail();
        this.message = commit.getMessage();
        this.repoId = commit.getRepoId();
        this.uuid = commit.getUuid();
        this.scanned = commit.getScanned();
    }
}
