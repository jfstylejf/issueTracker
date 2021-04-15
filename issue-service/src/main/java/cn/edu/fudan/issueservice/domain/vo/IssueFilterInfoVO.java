package cn.edu.fudan.issueservice.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.Date;

/**
 * @author beethoven
 * @date 2021-03-18 19:06:55
 */
@Builder
@Data
@Getter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class IssueFilterInfoVO {

    private final String uuid;
    private final Integer displayId;
    private final String type;
    private final String issueCategory;
    private final String repoName;
    private final String fileName;
    private final String producer;
    private final Date startCommitDate;
    private final String status;
    private final String priority;
    private final String solver;
    private final Date solveTime;
    private final String solveCommit;

}
