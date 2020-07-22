package cn.edu.fudan.measureservice.portrait;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * description: 开发者指标
 *
 * @author fancying
 * create: 2020-05-18 21:26
 **/
@Data
public class DeveloperMetrics implements Serializable {

    private LocalDate firstCommitDate;
    private int totalLine;
    private int totalCommitCount;
    private String repoName;
    private String developer;
    private Efficiency efficiency;
    private Quality quality;
    private Competence competence;

    public DeveloperMetrics(LocalDate firstCommitDate, int totalLine, int totalCommitCount, String repoName, String developer, Efficiency efficiency, Quality quality, Competence competence) {
        this.firstCommitDate = firstCommitDate;
        this.totalLine = totalLine;
        this.totalCommitCount = totalCommitCount;
        this.repoName = repoName;
        this.developer = developer;
        this.efficiency = efficiency;
        this.quality = quality;
        this.competence = competence;
    }

    public DeveloperMetrics(String developer, Efficiency efficiency, Quality quality, Competence competence) {
        this.developer = developer;
        this.efficiency = efficiency;
        this.quality = quality;
        this.competence = competence;
    }
}