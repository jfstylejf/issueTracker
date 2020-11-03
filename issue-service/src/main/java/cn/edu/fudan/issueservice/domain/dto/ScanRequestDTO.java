package cn.edu.fudan.issueservice.domain.dto;

import lombok.Data;

/**
 * @author Beethoven
 */
@Data
public class ScanRequestDTO {
    private String repoUuid;
    private String branch;
    private String beginCommit;
}
