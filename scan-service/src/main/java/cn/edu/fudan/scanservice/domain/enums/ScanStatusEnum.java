package cn.edu.fudan.scanservice.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author fancying
 */
@Getter
@AllArgsConstructor
public enum ScanStatusEnum {
    /**
     *
     */
    NOT_SCANNED("not scanned", 0),
    INVOKE_FAILED("invoke tool failed", 1),
    WAITING_FOR_SCAN("waiting for cn.edu.fudan.common.scan", 1),
    SCANNING ("scanning", 3),
    ANALYZE_FAILED("analyze failed", 2),
    COMPLETE("complete", 4);

    private String type;
    private int priority;

    public static ScanStatusEnum getScanStatusEnum(String status){
        for(ScanStatusEnum scanStatus : ScanStatusEnum.values()){
            if(scanStatus.getType ().equals(status)){
                return scanStatus;
            }
        }
        return ScanStatusEnum.NOT_SCANNED;
    }
}
