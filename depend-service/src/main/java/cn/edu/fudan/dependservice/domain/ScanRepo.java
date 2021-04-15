package cn.edu.fudan.dependservice.domain;

import lombok.Data;

@Data
public class ScanRepo {
    private String repoUuid;
    private String repoPath;
    private String branch;
    private boolean getResult;
    private boolean recopy;
    private boolean rescan;
    private String copyRepoPath;
    private String scanCommit;
    private String resultFile;
    private String resultAbsolutePath;
    private boolean copyStatus;
    private String msg;
    private ScanStatus scanStatus;
    public void test(){
        scanStatus.getStatus();
    }
}
