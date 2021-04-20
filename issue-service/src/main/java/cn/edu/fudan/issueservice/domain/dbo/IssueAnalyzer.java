package cn.edu.fudan.issueservice.domain.dbo;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

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

    public static IssueAnalyzer initIssueAnalyzer(String repoId, String commitId, String toolName) {
        IssueAnalyzer issueAnalyzer = new IssueAnalyzer();
        issueAnalyzer.setUuid(UUID.randomUUID().toString());
        issueAnalyzer.setTool(toolName);
        issueAnalyzer.setRepoUuid(repoId);
        issueAnalyzer.setCommitId(commitId);
        return issueAnalyzer;
    }

}
