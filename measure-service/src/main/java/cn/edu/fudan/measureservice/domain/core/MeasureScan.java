package cn.edu.fudan.measureservice.domain.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeasureScan {
    String uuid;
    String repoUuid;
    String tool;
    String startCommit;
    String endCommit;
    int totalCommitCount;
    int scannedCommitCount;
    int scanTime;
    String status;
    Date startScanTime;
    Date endScanTime;
}
