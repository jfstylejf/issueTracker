package cn.edu.fudan.measureservice.domain.bo;

import cn.edu.fudan.measureservice.domain.enums.LevelEnum;
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
    /**
     * 开发者综合等级
     */
    private LevelEnum level;
}
