package cn.edu.fudan.issueservice.domain.dbo;

import com.alibaba.fastjson.JSONObject;
import lombok.*;

import java.util.List;
import java.util.UUID;


/**
 * @author Jeff
 */
@Data
@Getter
@Builder
public class IssueAnalyzer {
    private String uuid;
    private String repoUuid;
    private String commitId;
    private int invokeResult;
    private JSONObject analyzeResult;
    private String tool;

    @Getter
    public enum InvokeResult {
        /**
         * issue analyzer 状态
         */
        SUCCESS(1),
        FAILED(0);

        private final int status;

        InvokeResult(int status) {
            this.status = status;
        }
    }

    public void updateIssueAnalyzeStatus(List<RawIssue> resultRawIssues) {
        JSONObject result = new JSONObject();
        result.put("result", resultRawIssues);
        this.setInvokeResult(InvokeResult.SUCCESS.getStatus());
        this.setAnalyzeResult(result);
    }

    public static IssueAnalyzer initIssueAnalyze(String repoUuid, String commitId, String tool) {
        return IssueAnalyzer.builder()
                .uuid(UUID.randomUUID().toString())
                .repoUuid(repoUuid)
                .commitId(commitId)
                .invokeResult(InvokeResult.FAILED.getStatus())
                .analyzeResult(new JSONObject())
                .tool(tool)
                .build();
    }
}
