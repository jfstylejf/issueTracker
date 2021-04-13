package cn.edu.fudan.dependservice.config;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class ScanConfig {

//    private String filePath;
    @JSONField(name ="architectures")
    private List<String> architectures;
    @JSONField(name = "projects")
    private List<Project> project;
    @JSONField(name = "dynamics")
    private List<Project> dynamics;
    @JSONField(name = "gits")
    private List<Project> gits;
    @JSONField(name = "libs")
    private List<Project> libs;
    @JSONField(name = "clones")
    private List<Project> clones;
    public ScanConfig(){

    }
    @Data
    public  class Project{

        @JSONField(name="path")
        private String path;
        @JSONField(name = "includeDirs")
        private List<String> includeDirs;
        @JSONField(name="microserviceName")
        private String microserviceName;
        @JSONField(name="autoInclude")
        private Boolean autoInclude = true;
        @JSONField(name="serviceGroupName")
        private String serviceGroupName;
        @JSONField(name="project")
        private String project;
        @JSONField(name="language")
        private String language;
        @JSONField(name="isMicroservice")
        private String isMicroservice;
        @JSONField(name = "excludes")
        private List<String> excludes;

    }
    public  class Dynamics{

        @JSONField(name="file_suffixes")
        private String file_suffixes;
        @JSONField(name="logs_path")
        private String logs_path;
        @JSONField(name="features_path")
        private String features_path;

    }



}



