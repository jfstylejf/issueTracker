package cn.edu.fudan.dependservice.domain;

import lombok.Data;

@Data
public class ScanBody {
    private String repo_uuid;
    private String begin_commit;
    private String branch;
    private String datetime;
}
