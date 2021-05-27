package cn.edu.fudan.cloneservice.domain.clone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * @author zyh
 * @date 2020/6/22
 */
@Data
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CloneRepo {

    private String uuid;
    private String repoId;
    private String startCommit;
    private String endCommit;
    private Integer totalCommitCount;
    private Integer scannedCommitCount;
    private Integer scanTime;
    private String status;
    private Date startScanTime;
    private Date endScanTime;
    /**
     * 记录扫描次数
     */
    private int scanCount;
}
