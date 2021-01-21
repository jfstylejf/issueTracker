package cn.edu.fudan.measureservice.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author wjzho
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeveloperWorkLoad implements Serializable {
    private String developerName;
    private int addLines;
    private int deleteLines;
    private int totalLoc;
    private int commitCount;
    private int changedFiles;
}