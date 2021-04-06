package cn.edu.fudan.issueservice.domain.dbo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * description: commit view 表对应关系
 *
 * @author fancying
 * create: 2020-08-18 17:17
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commit implements Serializable {

    String commitId;
    String commitTime;
    String developer;
    String developerEmail;
    String message;
    String repoId;
    String uuid;
    boolean scanned = true;

}