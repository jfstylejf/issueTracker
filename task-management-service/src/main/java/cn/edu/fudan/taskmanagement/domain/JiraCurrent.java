package cn.edu.fudan.taskmanagement.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JiraCurrent {

    private String developer;
    private String jira_id;
    private String issueType;
    private String current_status;
    private String summary;
    private String created_time;
    private String unique_name;
    private String due_date;
    private String work_load;
    private String priority;
}
