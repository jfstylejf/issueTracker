package cn.edu.fudan.taskmanagement.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ToDoJiraMsg {

    private String jiraUuid;
    private String createdTime;
    private String summary;
    private String issueType;
    private List<JiraDetail> jiraDetails;
}
