package cn.edu.fudan.measureservice.portrait;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * description: 开发者指标
 *
 * @author fancying
 * create: 2020-05-18 21:26
 **/
@Data
public class DeveloperMetrics implements Serializable {

    private LocalDate firstCommitDate;
    private int totalStatement;//需要改成逻辑行statement
    private int totalCommitCount;
    private String repoName;
    private String repoUuid;
    private String developer;
    private Efficiency efficiency;
    private Quality quality;
    private Competence competence;

    public DeveloperMetrics(LocalDate firstCommitDate, int totalStatement, int totalCommitCount, String repoName, String repoUuid, String developer, Efficiency efficiency, Quality quality, Competence competence) {
        this.firstCommitDate = firstCommitDate;
        this.totalStatement = totalStatement;
        this.totalCommitCount = totalCommitCount;
        this.repoName = repoName;
        this.repoUuid = repoUuid;
        this.developer = developer;
        this.efficiency = efficiency;
        this.quality = quality;
        this.competence = competence;
    }
}