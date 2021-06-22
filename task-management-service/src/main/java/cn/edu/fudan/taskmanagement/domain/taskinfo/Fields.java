package cn.edu.fudan.taskmanagement.domain.taskinfo;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zyh
 * @date 2020/7/3
 */
@Setter
@Getter
public class Fields {

    private String summary;
    private String created;
    private IssueType issuetype;
    private Project project;
    private String assignee;
    private Status status;
    private String resolutiondate;
    private String customfield_10304;
    private String customfield_10302;
    private Priority priority;
    public String getSummary() {
        return summary;
    }
    public String getAssignee(){return assignee;}
    public IssueType getIssuetype() {
        return issuetype;
    }
    public Priority getPriority() {return priority;}
    public String getCreated() {
        return created;
    }
    public Status getStatus() {
        return status;
    }
    public String getDueDate(){
        return customfield_10304;
    }
    public String getWorkLoad(){
        return customfield_10302;
    }
}