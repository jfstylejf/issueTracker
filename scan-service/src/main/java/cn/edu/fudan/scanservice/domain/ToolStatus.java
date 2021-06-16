package cn.edu.fudan.scanservice.domain;

import cn.edu.fudan.scanservice.domain.dto.ScanStatus;
import cn.edu.fudan.scanservice.domain.enums.ScanStatusEnum;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * description: 工具的扫描状态
 *
 * @author fancying
 * create: 2020-10-09 17:26
 **/
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ToolStatus {

    String uuid;
    String status;
    String totalCommitCount;
    String scannedCommitCount;
    int scanTime;

    String repoUuid;
    String startScanTime;
    String endScanTime;

    public ToolStatus(JSONObject toolStatus) {
        status = toolStatus.getString ("status");
        if (StringUtils.isEmpty(status)) {
            status = ScanStatusEnum.WAITING_FOR_SCAN.getType();
        }

        if(status.contains("failed")){
            status = ScanStatusEnum.ANALYZE_FAILED.getType();
        }

        if (status.contains("scanning")) {
            status = ScanStatusEnum.SCANNING.getType();
        }

        if (status.contains("waiting for scan")) {
            status = ScanStatusEnum.WAITING_FOR_SCAN.getType();
        }

        if (status.contains("complete")) {
            status = ScanStatusEnum.COMPLETE.getType();
        }


        startScanTime = toolStatus.getString ("startScanTime");
        endScanTime = toolStatus.getString ("endScanTime");

        Integer time = toolStatus.getInteger ("scanTime");
        scanTime = 0;
        if (time != null) {
            scanTime = time;
        }
    }
}