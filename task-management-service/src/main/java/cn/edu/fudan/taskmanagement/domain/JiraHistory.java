package cn.edu.fudan.taskmanagement.domain;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JiraHistory {

    private String developer;
    private String jira_id;
    private String commit_id;
    private String status;
    private String commit_time;
    private String summary;
    private String issueType;
    private String repo_id;
    private String unique_name;
    private String due_date;
    private String work_load;
    private String priority;
}
