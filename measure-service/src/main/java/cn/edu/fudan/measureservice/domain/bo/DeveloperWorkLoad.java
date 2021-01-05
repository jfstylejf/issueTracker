package cn.edu.fudan.measureservice.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author wjzho
 */
@Data
@AllArgsConstructor
public class DeveloperWorkLoad {
    private String developerName;
    private int addLines;
    private int delLines;
    private int totalLoc;
    private int commitCount;
    private int changedFiles;
}
