package cn.edu.fudan.issueservice.domain.dbo;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Jeff
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueAnalyzer {
    private String uuid;
    private String repoUuid;
    private String commitId;
    private int invokeResult;
    private JSONObject analyzeResult;
    private String tool;
}
