package cn.edu.fudan.measureservice.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wjzho
 */
@Data
@AllArgsConstructor
public class DeveloperWorkLoad implements Serializable {
    private String developerName;
    private int addLines;
    private int delLines;
    private int totalLoc;
    private int commitCount;
    private int changedFiles;
}
