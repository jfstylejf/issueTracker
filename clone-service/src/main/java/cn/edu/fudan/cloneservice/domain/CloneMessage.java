package cn.edu.fudan.cloneservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zyh
 * @date 2020/5/23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloneMessage implements Comparable<CloneMessage>{

    private String repoId;
    private String developer;
    private Integer increasedCloneLines;
    private Integer selfIncreasedCloneLines;
    private Integer othersIncreasedCloneLines;
    private String increasedCloneLinesRate;
    private Integer eliminateCloneLines;
    private Integer allEliminateCloneLines;
    private Integer addLines;

    @Override
    public int compareTo(CloneMessage o) {
        double thisValue;
        double objectValue;
        if(this.addLines == 0){
            thisValue = 0;
        }else {
            thisValue = this.increasedCloneLines * 1.0 / this.addLines;
        }
        if(o.getAddLines() == 0){
            objectValue = 0;
        }else {
            objectValue = o.getIncreasedCloneLines() * 1.0 / o.getAddLines();
        }
        if(thisValue > objectValue){
            return 1;
        }else if(thisValue < objectValue){
            return -1;
        }
        return 0;    }
}
