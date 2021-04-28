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
    long ts_start;
    long ts_end;
    @Override
    public String toString(){
        StringBuilder res=new StringBuilder();
        res.append(status);
        res.append(scanTime);
        res.append(startScanTime);
        res.append(endScanTime);
        res.append(msg);
        return res.toString();

    }
}
