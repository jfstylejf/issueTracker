package cn.edu.fudan.common.domain.po.scan;

/**
 * description: 扫描的三种状态  正在扫描、完成、失败
 *
 * @author fancying
 * create: 2021-02-24 15:35
 **/
public enum ScanStatus {

    SCANNING("scanning"),
    COMPLETE("complete"),
    FAILED("failed");

    private final String status;

    ScanStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

}
