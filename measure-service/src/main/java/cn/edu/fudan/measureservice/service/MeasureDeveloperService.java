package cn.edu.fudan.measureservice.service;

import java.text.ParseException;

public interface MeasureDeveloperService {

    /**
     * 获取一个项目在指定一段时间内提交次数
     * @param repo_id repo的唯一标识
     * @param since 起始时间
     * @param until 终止时间
     * @return repo的一段时间代码提交次数
     */
    int getCommitCountsByDuration(String repo_id, String since, String until);

    /**
     * 获取一个项目在过去三个月的活跃度判定
     * @param repo_id repo的唯一标识
     * @return repo的活跃值
     */
    String getActivityByRepoId(String repo_id);

    /**
     * 获取在issueTracker用户下，所有项目中某个开发者在一段时间内的代码提交行数变化
     * @param developer_name 开发者的提交名字
     * @param since 提交的起始时间
     * @param until 提交的终止时间
     * @param token 用户token
     * @param category 工具类别
     * @param repoId repo唯一ID
     * @return 开发者在一段时间内代码变化信息
     */
    Object getCodeChangesByDurationAndDeveloperName(String developer_name, String since, String until, String token, String category, String repoId);

    /**
     * 获取在issueTracker用户下，所有项目中某个开发者在一段时间内的代码提交次数
     * @param developer_name 开发者的提交名字
     * @param since 提交的起始时间
     * @param until 提交的终止时间
     * @param token 用户token
     * @param category 工具类别
     * @param repoId repo唯一ID
     * @return 提交次数
     */
    int getCommitCountByDurationAndDeveloperName(String developer_name, String since, String until, String token, String category, String repoId);

    /**
     * 获取在issueTracker用户下，该用户参与过的项目列表，以及各个项目的活跃度
     * @param developer_name 开发者的提交名字
     * @param token 用户token
     * @param category 工具类别
     * @return 项目以及项目的活跃度
     */
    Object getRepoListByDeveloperName(String developer_name, String token, String category);


    /**
     * 获取在issueTracker用户下，该用户最近一定数量的commit的代码质量度量信息
     * @param developer_name 开发者的提交名字
     * @param token 用户token
     * @param category 工具类别
     * @param counts commit数量
     * @param project_name 项目名
     * @return 项目以及项目的活跃度
     */
    Object getQualityChangesByDeveloperName(String developer_name, String token, String category, int counts, String project_name);

    /**
     * @return 根据时间粒度获取某段时间内，该项目、该开发者的addLines delLines
     */
    Object getDeveloperActivenessByGranularity(String repo_id, String granularity, String developer_name);

    /**
     *
     * @return 根据时间段获取该项目、该开发者的addLines delLines
     */
    Object getDeveloperActivenessByDuration(String repo_id, String since, String until, String developer_name);


    /**
     *
     * @return 返回开发者雷达图数据
     */
    Object getPortrait(String repoId, String developer, String beginDate, String endDate, String token, String tool) throws ParseException;

    /**
     *
     * @return 根据条件返回开发者LOC数据
     */
    Object getLOCByCondition(String repoId, String developer, String beginDate, String endDate, String type);

    /**
     *
     * @return 返回开发者用户画像评星等级
     */
    Object getPortraitLevel(String developer, String token) throws ParseException;


    /**
     *
     * @return 根据条件返回开发者Commit Message数据
     */
    Object getCommitMsgByCondition(String repoId, String developer, String beginDate, String endDate);

    /**
     *
     * @return 返回开发者jira相关的度量数据
     */
    Object getJiraMeasureInfo(String repoId, String developer, String beginDate, String endDate) throws ParseException;

    /**
     *
     * @return 返回最近动态
     */
    Object getDeveloperRecentNews(String repoId, String developer, String beginDate, String endDate);



}
