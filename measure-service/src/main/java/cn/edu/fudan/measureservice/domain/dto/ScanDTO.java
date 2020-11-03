package cn.edu.fudan.measureservice.domain.dto;

import lombok.Data;

/**
 * @author wjzho
 */
@Data
public class ScanDTO {
    String repoUuid;
    String branch;
    String beginCommit;
}
