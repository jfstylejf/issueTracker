package cn.edu.fudan.measureservice.domain.dto;

import cn.edu.fudan.measureservice.domain.enums.ScanStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @author wjzho
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScanCommitInfoDto {
    private String repoPath;
    private String repoUuid;
    private String commitId;
    private String branch;
    private String commitTime;
    private String developerName;
    private String toolName;
    private ScanStatusEnum status;
    private String mailAddress;
    private String firstParentCommitId;
    private String secondParentCommitId;

    @Override
    public String toString() {
        return "ScanCommitInfoDto{" +
                "repoPath='" + repoPath + '\'' +
                ", repoUuid='" + repoUuid + '\'' +
                ", commitId='" + commitId + '\'' +
                ", branch='" + branch + '\'' +
                ", toolName='" + toolName + '\'' +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScanCommitInfoDto that = (ScanCommitInfoDto) o;
        return Objects.equals(repoPath, that.repoPath) &&
                Objects.equals(repoUuid, that.repoUuid) &&
                Objects.equals(commitId, that.commitId) &&
                Objects.equals(branch, that.branch) &&
                Objects.equals(toolName, that.toolName) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repoPath, repoUuid, commitId, branch, toolName, status);
    }

}
