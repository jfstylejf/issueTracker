package cn.edu.fudan.cloneservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@Getter
public class CloneDetail implements Comparable<CloneDetail>{
    private String uuid;
    private String projectName;
    private String projectId;
    private String repoUuid;
    private String commitId;
    private String filePath;
    private int groupId;
    private String className;
    private int startLine;
    private int endLine;
    private int lineCount;
    private String detail;
    private int cloneType;

    @Override
    public int compareTo(CloneDetail o) {
        double thisValue;
        double objectValue;
        thisValue = this.groupId;
        objectValue = o.groupId;
        if(thisValue > objectValue){
            return 1;
        }else if(thisValue < objectValue){
            return -1;
        }
        return 0;
    }
}
