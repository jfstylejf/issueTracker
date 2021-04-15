package cn.edu.fudan.issueservice.domain.dbo;

import cn.edu.fudan.issueservice.domain.enums.ScanStatusEnum;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

/**
 * description: 记录缺陷扫描工具对repo每个commit扫描情况。
 *
 * @author lsw
 * @blame lsw
 * @since 20/05/21 09:28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueScan {

    private String uuid;
    private String tool;
    private Date startTime;
    private Date endTime;
    private String status;
    private String repoId;
    private String commitId;
    private Date commitTime;
    private String resultSummary;

    public static IssueScan initIssueScan(String repoId, String commitId, String toolName, Date commitTime) {
        IssueScan issueScan = new IssueScan();
        issueScan.setUuid(UUID.randomUUID().toString());
        issueScan.setTool(toolName);
        issueScan.setStartTime(new Date());
        issueScan.setStatus(ScanStatusEnum.DOING.getType());
        issueScan.setRepoId(repoId);
        issueScan.setCommitId(commitId);
        issueScan.setCommitTime(commitTime);
        return issueScan;
    }


}
