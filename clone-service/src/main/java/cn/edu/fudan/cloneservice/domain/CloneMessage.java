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
public class CloneMessage {

    private String repoId;
    private String increasedCloneLines;
    private String selfIncreasedCloneLines;
    private String increasedCloneLinesRate;
    private String eliminateCloneLines;
    private String allEliminateCloneLines;

}
