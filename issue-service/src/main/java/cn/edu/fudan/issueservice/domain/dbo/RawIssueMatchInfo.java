package cn.edu.fudan.issueservice.domain.dbo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * description: 用于记录raw issue的匹配信息
 *
 * @author fancying
 * create: 2021-01-06 21:53
 **/
@Data
@Builder
@AllArgsConstructor
public class RawIssueMatchInfo {

    public static final String EMPTY = "empty";

    /**
     * 这个rawIssue是与哪一个版本的rawIssue进行的比较
     */
    private String uuid;
    private String curRawIssueUuid;
    private String curCommitId;
    private String preRawIssueUuid;
    private String preCommitId;
    private String issueUuid;
    private String status;

    /**
     * 当同一个rawIssue 在与不同的commit匹配 遇到不同的 Issue 时用到
     */
    double matchDegree;
}
