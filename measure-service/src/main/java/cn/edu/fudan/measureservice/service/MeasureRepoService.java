package cn.edu.fudan.measureservice.service;
import cn.edu.fudan.measureservice.domain.*;

import java.util.List;

public interface MeasureRepoService {


    /**
     * 获取一个项目在某个时间段特定时间单位的项目级别的所有度量信息
     * @param repoUuid repo的唯一标识
     * @param since 起始时间
     * @param until 终止时间
     * @param granularity 时间段的单位day,week,month
     * @return 每个时间点上的项目级度量信息
     */
    List<RepoMeasure> getRepoMeasureByRepoUuid(String repoUuid, String since, String until, Granularity granularity);

    /**
     * 获取一个项目在某个commit的所有度量信息
     * @param repoUuid repo的唯一标识
     * @param commitId commit的唯一标志
     * @return 每个时间点上的项目级度量信息
     */
    RepoMeasure getRepoMeasureByrepoUuidAndCommitId(String repoUuid, String commitId);


    /**
     * 获取一个项目在某个特定commit快照下代码行数的变化
     * @param repo_id repo的唯一标识
     * @param commit_id commit的唯一标识
     * @return repo在某个commit的代码变化信息以及提交者的信息
     */
    CommitBase getCommitBaseInformation(String repo_id, String commit_id);

    /**
     * 获取一个项目在一段时间代码变化信息以及提交者的信息
     * @param repo_id repo的唯一标识
     * @param since 起始时间
     * @param until 终止时间
     * @return repo的一段时间代码变化信息以及提交者的信息
     */
    CommitBaseInfoDuration getCommitBaseInformationByDuration(String repo_id, String since, String until, String developer_name);

    /**
     * 获取一个项目在指定一段时间内提交次数
     * @param repo_id repo的唯一标识
     * @param since 起始时间
     * @param until 终止时间
     * @return repo的一段时间代码提交次数
     */
    int getCommitCountsByDuration(String repo_id, String since, String until);


    /**
     *
     * @return 某段时间内，该项目中提交次数最多的前三名开发者的姓名以及对应的commit次数
     */
    Object getDeveloperRankByCommitCount(String repo_id, String since, String until);

    /**
     * @return 某段时间内，该项目中提交代码行数（LOC）最多的前三名开发者的姓名以及对应的LOC
     */
    Object getDeveloperRankByLoc(String repo_id, String since, String until);


    /**
     * @return 某段时间内，该项目每天的所有开发者产生的LOC以及commit次数
     */
    Object getCommitCountLOCDaily(String repo_id, String since, String until);

}
