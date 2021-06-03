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

    /**
     * 开发者该库最早提交时间
     */
    private String firstCommitDate;
    /**
     * 开发者在该库提交总逻辑行数
     */
    private int totalStatement;
    /**
     * 开发者总提交次数
     */
    private int totalCommitCount;
    /**
     * 库名称
     */
    private String repoName;
    /**
     * 库 id
     */
    private String repoUuid;
    /**
     * 开发者
     */
    private String developer;
    private Efficiency efficiency;
    private Quality quality;
    private Competence competence;

}