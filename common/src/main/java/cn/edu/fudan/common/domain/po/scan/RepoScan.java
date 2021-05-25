package cn.edu.fudan.common.domain.po.scan;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * description: 项目的扫描信息
 *
 * @author fancying
 * create: 2021-02-24 15:30
 **/
@Data
@Builder
public class RepoScan {

    String repoUuid;
    String branch;
    String tool;
    String status;
    int totalCommitCount;
    int scannedCommitCount;
    Date startScanTime;
    Date endScanTime;
    boolean initialScan;
    String startCommit;
    long scanTime;

}
