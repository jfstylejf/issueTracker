package cn.edu.fudan.dependservice.domain;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class GroupView {
    private String projectName;
    private String repoUuid;
    private String groupId;
    private String repoName;
    private List<RelationView> relationViews;
//    private Set<String> files;
    private Set<FileNode> files;
    private Set<String> fileStrings;
    private String commit_id;
    private int id;
    public GroupView(List<RelationView> relationViews,String groupId){
        this.groupId= groupId;
        this.relationViews=relationViews;


    }
    public GroupView(){


    }
}
