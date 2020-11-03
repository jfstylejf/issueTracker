package cn.edu.fudan.scanservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description:
 *
 * @author fancying
 * create: 2020-03-05 09:39
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScanResultMessage {

    private String repoId;
    private String commitId;
    private String tool;
    private String status;
    private String description;

}