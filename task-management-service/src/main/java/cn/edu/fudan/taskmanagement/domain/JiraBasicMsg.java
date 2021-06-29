package cn.edu.fudan.taskmanagement.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JiraBasicMsg {
    private String jiraUuid;
    private String commitUuid;
    private String repoUuid;
    private String developer;
    private String commitTime;
}
