package cn.edu.fudan.measureservice.portrait2;

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
    private int totalStatement;
    private int totalCommitCount;
    private String repoName;
    private String repoId;
    private String branch;
    private String developer;
    private Efficiency efficiency;
    private Quality quality;
    private Contribution contribution;

    public DeveloperMetrics(LocalDate firstCommitDate, int totalStatement, int totalCommitCount, String repoName, String repoId, String branch, String developer, Efficiency efficiency, Quality quality, Contribution contribution) {
        this.firstCommitDate = firstCommitDate;
        this.totalStatement = totalStatement;
        this.totalCommitCount = totalCommitCount;
        this.repoName = repoName;
        this.repoId = repoId;
        this.branch = branch;
        this.developer = developer;
        this.efficiency = efficiency;
        this.quality = quality;
        this.contribution = contribution;
    }

    public DeveloperMetrics(int totalStatement, int totalCommitCount, Efficiency efficiency, Quality quality, Contribution contribution) {
        this.totalStatement = totalStatement;
        this.totalCommitCount = totalCommitCount;
        this.efficiency = efficiency;
        this.quality = quality;
        this.contribution = contribution;
    }
}