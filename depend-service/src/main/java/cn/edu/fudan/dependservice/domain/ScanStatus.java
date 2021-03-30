package cn.edu.fudan.dependservice.domain;

import lombok.Data;

@Data
public class ScanStatus {
    String status;
    String scanTime;
    String startScanTime;
    String endScanTime;
}
