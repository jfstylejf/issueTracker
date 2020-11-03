package cn.edu.fudan.projectmanager.domain;



/**
 * @description:
 * @author: fancying
 * @create: 2019-03-23 19:56
 **/
public class RepoBasicInfo {

    private String name;
    private String language;
    private String url;
    private String repo_source;
    private String add_time;
    private String till_commit_time;
    private String description;
    private String repo_id;
    private String branch;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRepo_source() {
        return repo_source;
    }

    public void setRepo_source(String repo_source) {
        this.repo_source = repo_source;
    }

    public String getAdd_time() {
        return add_time;
    }

    public void setAdd_time(String add_time) {
        this.add_time = add_time;
    }

    public String getTill_commit_time() {
        return till_commit_time;
    }

    public void setTill_commit_time(String till_commit_time) {
        this.till_commit_time = till_commit_time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRepo_id() {
        return repo_id;
    }

    public void setRepo_id(String repo_id) {
        this.repo_id = repo_id;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}