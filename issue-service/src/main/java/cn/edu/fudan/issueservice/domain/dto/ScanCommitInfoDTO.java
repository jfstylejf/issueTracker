package cn.edu.fudan.issueservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanCommitInfoDTO {

    String repoId;
    String commitId;
    String branch;
    String toolName;
    Boolean isUpdate;

    @Override
    public String toString() {
        return "commit{" +
                "repoId='" + repoId +
                ", commitId=" + commitId +
                ", branch=" + branch +
                ", toolName=" + toolName +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(repoId, toolName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScanCommitInfoDTO scanCommitInfoDTO = (ScanCommitInfoDTO) o;
        return repoId.equals(scanCommitInfoDTO.getRepoId()) &&
                toolName.equals(scanCommitInfoDTO.getToolName());
    }
}
