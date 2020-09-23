package cn.edu.fudan.projectmanager.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Project {

    private String uuid;
    private String name;
    private String language;
    private String url;
    private String repo_source;
    private String type;
    private String account_id;
    private String account_name;
    private String download_status;
    private String scan_status;
    private Date add_time;
    private Date start_scan_date;
    private Date till_commit_time;
    private Date last_scan_time;
    private String description;
    private String repo_id;
    private String branch;
    private int first_auto_scan;
    private String module;
    private int recycled;
    private Date delete_time;

    public Date getDelete_time() {
        return delete_time;
    }

    public void setDelete_time(Date delete_time) {
        this.delete_time = delete_time;
    }

    public static Project createOneProjectByRepoBasicInfo(RepoBasicInfo r, String type) throws ParseException {
        return new Project(UUID.randomUUID().toString(), r.getName(), r.getLanguage(), r.getUrl(), r.getRepo_source(), type,"superAccount","Downloaded",
                "Not Scan",(new SimpleDateFormat("yyyy-MM-dd")).parse(r.getAdd_time()) , (new SimpleDateFormat("yyyy-MM-dd")).parse(r.getTill_commit_time()), r.getDescription(), r.getRepo_id(), r.getBranch());
    }

    public Project() {
    }

    public Project(String uuid, String name, String language, String url, String repo_source, String type, String account_id, String download_status,
                   String scan_status, Date add_time, Date till_commit_time, String description, String repo_id, String branch) {
        this.uuid = uuid;
        this.name = name;
        this.language = language;
        this.url = url;
        this.repo_source = repo_source;
        this.type = type;
        this.account_id = account_id;
        this.download_status = download_status;
        this.scan_status = scan_status;
        this.add_time = add_time;
        this.till_commit_time = till_commit_time;
        this.description = description;
        this.repo_id = repo_id;
        this.branch = branch;
    }

    public String getAccount_name() {
        return account_name;
    }

    public void setAccount_name(String account_name) {
        this.account_name = account_name;
    }

    public Date getStart_scan_date() {
        return start_scan_date;
    }

    public void setStart_scan_date(Date start_scan_date) {
        this.start_scan_date = start_scan_date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getAdd_time() {
        if(add_time == null){
            return null;
        }
        return (Date)add_time.clone();
    }

    public void setAdd_time(Date add_time) {
        if(add_time == null){
            this.add_time = null;
        }else{
            this.add_time = (Date) add_time.clone();
        }
    }

    public String getRepo_id() {
        return repo_id;
    }

    public void setRepo_id(String repo_id) {
        this.repo_id = repo_id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getAccount_id() {
        return account_id;
    }

    public void setAccount_id(String account_id) {
        this.account_id = account_id;
    }

    public String getDownload_status() {
        return download_status;
    }

    public void setDownload_status(String download_status) {
        this.download_status = download_status;
    }

    public String getScan_status() {
        return scan_status;
    }

    public void setScan_status(String scan_status) {
        this.scan_status = scan_status;
    }

    public Date getTill_commit_time() {
        if(till_commit_time == null){
            return null;
        }
        return (Date) till_commit_time.clone();
    }

    public void setTill_commit_time(Date till_commit_time) {
        if(till_commit_time == null){
            this.till_commit_time = null;
        }else {
            this.till_commit_time = (Date) till_commit_time.clone();
        }
    }

    public Date getLast_scan_time() {
        if(last_scan_time == null){
            return null;
        }
        return (Date) last_scan_time.clone();
    }

    public void setLast_scan_time(Date last_scan_time) {
        if(last_scan_time == null){
            this.last_scan_time = null;
        }else {
            this.last_scan_time = (Date) last_scan_time.clone();
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public int getFirst_auto_scan() {
        return first_auto_scan;
    }

    public void setFirst_auto_scan(int first_auto_scan) {
        this.first_auto_scan = first_auto_scan;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public int getRecycled() {
        return recycled;
    }

    public void setRecycled(int recycled) {
        this.recycled = recycled;
    }
}
