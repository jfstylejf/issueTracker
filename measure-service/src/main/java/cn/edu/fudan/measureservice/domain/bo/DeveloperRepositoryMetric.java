package cn.edu.fudan.measureservice.domain.bo;

import cn.edu.fudan.measureservice.portrait.Competence;
import cn.edu.fudan.measureservice.portrait.Efficiency;
import cn.edu.fudan.measureservice.portrait.Quality;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeveloperRepositoryMetric implements Serializable {

    private String firstCommitDate;
    private int totalStatement;
    private int totalCommitCount;
    private String repoName;
    private String repoUuid;
    private String developer;
    private Efficiency efficiency;
    private Quality quality;
    private Competence competence;

}