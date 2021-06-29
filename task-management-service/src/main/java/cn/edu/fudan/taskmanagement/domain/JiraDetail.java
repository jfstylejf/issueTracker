package cn.edu.fudan.taskmanagement.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JiraDetail{
    private String commitTime;
    private String jiraUuid;
    private String developer;
    private String commitUuid;
    private String issueType;
}
