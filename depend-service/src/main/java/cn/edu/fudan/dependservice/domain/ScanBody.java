package cn.edu.fudan.dependservice.domain;

import lombok.Data;

@Data
public class ScanBody {
    private String repoUuid;
    private String beginCommit;
    private String branch;
    private String datetime;
}
