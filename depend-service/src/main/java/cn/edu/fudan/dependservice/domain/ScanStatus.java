package cn.edu.fudan.dependservice.domain;

import lombok.Data;

@Data
public class ScanStatus{
    // SCANNING("scanning"),
    //    COMPLETE("complete"),
    //    FAILED("failed");
    String status;
    String scanTime;
    String startScanTime;
    String endScanTime;
    String msg;
}
