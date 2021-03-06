package cn.edu.fudan.common.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class ScanInfo implements Serializable {
    private String uuid;
    private int totalCommitCount;
    private int scannedCommitCount;
    private Date startScanTime;
    private Date endScanTime;
    private String status;
    /**
     * 总耗时 单位s
     */
    private long scanTime;
    private String latestCommit;
    private String branch;
    private String repoId;

    public ScanInfo () {

    }

    public ScanInfo (String uuid, String status, int totalCommitCount, int scannedCommitCount, Date startScanTime, String repoId, String branch) {
        this.uuid = uuid;
        this.status = status;
        this.totalCommitCount = totalCommitCount;
        this.scannedCommitCount = scannedCommitCount;
        this.startScanTime = startScanTime;
        this.repoId = repoId;
        this.branch = branch;
    }


    /**
     * 描述节点的状态
     */
    public enum Status {

        /**
         * scanning
         */
        SCANNING("scanning"),

        COMPLETE("complete"),
        FAILED("failed");

        private final String status;

        Status(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }

}
