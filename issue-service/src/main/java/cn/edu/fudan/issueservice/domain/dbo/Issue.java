package cn.edu.fudan.issueservice.domain.dbo;

import cn.edu.fudan.issueservice.domain.enums.IgnoreTypeEnum;
import cn.edu.fudan.issueservice.domain.enums.IssueStatusEnum;
import lombok.Data;
import lombok.Getter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author Beethoven
 */
@Data
@Getter
public class Issue {

    private String uuid;
    private String type;
    private String tool;
    private String startCommit;
    private Date startCommitDate;
    private String endCommit;
    private Date endCommitDate;
    private String repoId;
    private String targetFiles;
    private Date createTime;
    private Date updateTime;
    private IssueType issueType;
    private List<Object> tags;
    private int priority;
    private int displayId;
    private String status;
    private String manualStatus;
    private String resolution;
    private String issueCategory;

    /**
     * 默认并不知道该issue的引入者
     */
    private String producer;
    private String solver;
    private String solveCommit;
    private Date solveCommitDate;

    /**
     * 展示给前端，此处其实应该新建一个 dto 类，对issue展示给前端的字段进行过滤
     */
    private String severity;

    /**
     * 计算缺陷的存活时间，展示前端时更新相应数据
     */
    private String survivalTime;

    public Issue() {
    }

    /**
     * todo 根据rawIssue 产生一个新的Issue
     */
    public static Issue valueOf(RawIssue r) {
        Issue issue = new Issue();
        issue.setUuid(UUID.randomUUID().toString());
        issue.setType(r.getType());
        issue.setTool(r.getTool());
        issue.setStartCommit(r.getCommitId());
        issue.setStartCommitDate(r.getCommitTime());
        issue.setEndCommit(r.getCommitId());
        issue.setEndCommitDate(r.getCommitTime());
        issue.setRepoId(r.getRepoId());
        issue.setTargetFiles(r.getFileName());
        Date date = new Date();
        issue.setCreateTime(date);
        issue.setUpdateTime(date);
        issue.setPriority(r.getPriority());
        issue.setStatus(IssueStatusEnum.OPEN.getName());
        issue.setManualStatus(IgnoreTypeEnum.DEFAULT.getName());
        issue.setResolution(String.valueOf(0));
        issue.setProducer(r.getDeveloperName());
        issue.setSolveCommit(null);
        issue.setSolveCommitDate(null);
        return issue;
    }
}
