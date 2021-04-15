package cn.edu.fudan.issueservice.domain.dbo;

import cn.edu.fudan.issueservice.domain.enums.RepoStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

/**
 * @author beethoven
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
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

    public void incrementScannedCommitCount() {
        scannedCommitCount++;
    }

    public static IssueRepo initIssueRepo(String repoId, String branch, String commitId, String toolName, int totalCommitCount) {
        IssueRepo issueRepo = new IssueRepo();
        issueRepo.setUuid(UUID.randomUUID().toString());
        issueRepo.setRepoId(repoId);
        issueRepo.setBranch(branch);
        issueRepo.setTool(toolName);
        issueRepo.setStartCommit(commitId);
        issueRepo.setTotalCommitCount(totalCommitCount);
        issueRepo.setStatus(RepoStatusEnum.SCANNING.getType());
        issueRepo.setScanTime(0);
        issueRepo.setStartScanTime(new Date());

        return issueRepo;
    }
}
