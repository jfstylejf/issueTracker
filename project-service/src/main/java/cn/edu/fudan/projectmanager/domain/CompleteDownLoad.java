package cn.edu.fudan.projectmanager.domain;

import java.util.Date;

public class CompleteDownLoad {

    private String projectId;
    private String language;
    private String status;
    private String description;
    private Date till_commiit_time;


    public String getRepo_id() {
        return repo_id;
    }

    public void setRepo_id(String repo_id) {
        this.repo_id = repo_id;
    }

    private String repo_id;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getTill_commiit_time() {
        return till_commiit_time;
    }

    public void setTill_commiit_time(Date till_commiit_time) {
        this.till_commiit_time = till_commiit_time;
    }
}
