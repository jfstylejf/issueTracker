package cn.edu.fudan.scanservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description:
 *
 * @author fancying
 * create: 2020-03-05 09:36
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommitMessage {

    private String repoId;
    private String branch;
    private boolean isUpdate;

}