package cn.edu.fudan.dependservice.domain;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class RelationView {
    private String projectName;
    private String repoUuid;
    private String groupId;
    private String repoName;
    private String sourceFile;
    private String targetFile;
    private String relationType;
    private String commit_id;
    private int id;
    private int times;
    private Map<String, Integer> dependsOnTypes;

    //    private String latestFilePath;
    public void setDependsOnTypes() {
        this.dependsOnTypes=new HashMap<>();

        for(String s:relationType.split(";")){
            String key =s.substring(0,s.indexOf('('));
            Integer value =Integer.parseInt(s.substring(s.indexOf('(')+1,s.indexOf(')')));
            dependsOnTypes.put(key,value);
        }
        this.times=0;
        for(Integer i:dependsOnTypes.values()){
            times+=i;
        }

    }
}
