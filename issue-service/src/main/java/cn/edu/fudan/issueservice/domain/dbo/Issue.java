package cn.edu.fudan.issueservice.domain.dbo;

import cn.edu.fudan.issueservice.domain.dbo.IssueType;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Issue {

    private String uuid;
    private String type;
    private String tool;
    private String start_commit;
    private Date start_commit_date;
    private String end_commit;
    private Date end_commit_date;
    private String repo_id;
    private String target_files;
    private Date create_time;
    private Date update_time;
    private IssueType issueType;
    private List<Object> tags;
    private int priority;
    private int displayId ;
    private String status;
    private String manual_status;
    private String resolution;
    private String issueCategory;

    /**
     * 默认并不知道该issue的引入者
     */
    private String producer = "DefaultNull";


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

    public Issue(String uuid, String type, String tool, String start_commit, Date start_commit_date, String end_commit, Date end_commit_date, String repo_id, String target_files, Date create_time, Date update_time ,int displayId) {
        this.uuid = uuid;
        this.type = type;
        this.tool = tool;
        this.start_commit = start_commit;
        if(start_commit_date == null){
            this.start_commit_date = null;
        }else {
            this.start_commit_date = (Date) start_commit_date.clone();
        }
        this.end_commit = end_commit;
        if(end_commit_date == null){
            this.end_commit_date = null;
        }else {
            this.end_commit_date = (Date) end_commit_date.clone();
        }
        this.repo_id = repo_id;
        this.target_files = target_files;
        if(create_time == null){
            this.create_time = null;
        }else {
            this.create_time = (Date) create_time.clone();
        }
        if(update_time == null){
            this.update_time = null;
        }else {
            this.update_time = (Date) update_time.clone();
        }
        this.displayId = displayId;
    }


    public Date getCreate_time() {
        if(create_time == null){
            return null;
        }
        return (Date)create_time.clone();
    }

    public void setCreate_time(Date create_time) {
        if(create_time == null){
            this.create_time = null;
        }else {
            this.create_time = (Date) create_time.clone();
        }
    }

    public Date getUpdate_time() {
        if(update_time == null){
            return null;
        }
        return (Date)update_time.clone();
    }

    public void setUpdate_time(Date update_time) {
        if(update_time == null){
            this.update_time = null;
        }else {
            this.update_time = (Date) update_time.clone();
        }
    }


    public List<Object> getTags() {
        return tags;
    }

    public void setTags(List<Object> tags) {
        this.tags = tags;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public String getStart_commit() {
        return start_commit;
    }

    public void setStart_commit(String start_commit) {
        this.start_commit = start_commit;
    }

    public String getEnd_commit() {
        return end_commit;
    }

    public void setEnd_commit(String end_commit) {
        this.end_commit = end_commit;
    }



    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getRepo_id() {
        return repo_id;
    }

    public void setRepo_id(String repo_id) {
        this.repo_id = repo_id;
    }

    public String getTarget_files() {
        return target_files;
    }

    public void setTarget_files(String target_files) {
        this.target_files = target_files;
    }

    public int getDisplayId() {
        return displayId;
    }

    public void setDisplayId(int displayId) {
        this.displayId = displayId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getManual_status() {
        return manual_status;
    }

    public void setManual_status(String manual_status) {
        this.manual_status = manual_status;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }


    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

}
