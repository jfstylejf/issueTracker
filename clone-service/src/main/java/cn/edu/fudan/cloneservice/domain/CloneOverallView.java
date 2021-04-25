package cn.edu.fudan.cloneservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@Getter
public class CloneOverallView implements Comparable<CloneOverallView>{
    private String projectName;
    private String projectId;
    private String date;
    private String repoUuid;
    private int caseSum;
    private int fileSum;
    private int codeLengthAverage;
    private int cloneType;
    private String commitId;

    @Override
    public int compareTo(CloneOverallView o) {
        double thisValue;
        double objectValue;
        thisValue = Integer.parseInt(this.projectId);
        objectValue = Integer.parseInt(o.projectId);
        if(thisValue > objectValue){
            return 1;
        }else if(thisValue < objectValue){
            return -1;
        }
        return 0;
    }
}
