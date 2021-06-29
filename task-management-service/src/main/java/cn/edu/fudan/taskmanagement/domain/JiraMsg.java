package cn.edu.fudan.taskmanagement.domain;

import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JiraMsg {

    private String developer;
    private String assignee;
    private String uniqueName;

    private String jiraUuid;
    private String createdTime;
    private String summary;

    private String issueType;
    //history status
    private String status;
    //current status
    private String currentStatus;

    private String commitUuid;
    private String commitTime;
    private String repoUuid;

    private String dueDate;
    private String workLoad;
    private String priority;

}
