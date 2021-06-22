package cn.edu.fudan.taskmanagement.service.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter

public class TaskJiraResponse {
    //所有的Jira
    private final int total;
    //commit和Jira有对应的jira个数
    private final int linked;
    //commit的次数
    private final int linkedNumber;
    private List<JiraInfoType> task;

    public TaskJiraResponse(int total, int linked, int linkedNumber, List<JiraInfoType> task) {
        this.total = total;
        this.linked = linked;
        this.linkedNumber = linkedNumber;
        this.task = task;
    }
}


