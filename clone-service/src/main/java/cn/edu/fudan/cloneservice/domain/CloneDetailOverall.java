package cn.edu.fudan.cloneservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@Getter
public class CloneDetailOverall implements Comparable<CloneDetailOverall>{
    private String uuid;
    private String projectName;
    private String projectId;
    private String repoUuid;
    private String commitId;
    private int groupId;
    private int cloneType;
    private int caseSum;
    private int fileSum;
    private int codeLengthAverage;

    @Override
    public int compareTo(CloneDetailOverall o) {
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
