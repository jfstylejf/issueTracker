package cn.edu.fudan.projectmanager.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author fancying
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanMessage {

    private String repoId;
    private String commitId;
    private String category;

}
