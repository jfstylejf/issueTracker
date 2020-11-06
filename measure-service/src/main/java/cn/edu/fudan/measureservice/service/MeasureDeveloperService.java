package cn.edu.fudan.measureservice.service;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface MeasureDeveloperService {

    /**
     * 根据条件获取开发者对应的工作量：新增物理行、删除物理行、修改文件次数、提交次数
     * @param developer
     * @param since
     * @param until
     * @param repoUuidList
     * @return
     */
    Object getDeveloperWorkLoad(String developer, String since, String until, String repoUuidList);


    /**
     *
     * @param repoUuidList
     * @param developer
     * @param since
     * @param until
     * @param token
     * @param tool
     * @return
     * @throws ParseException
     */
    Object getPortrait(String repoUuidList, String developer, String since, String until, String token, String tool) throws ParseException;


    /**
     *
     * @return 根据条件返回开发者statement逻辑行数据
     */
    Object getStatementByCondition(String repoUuidList, String developer, String since, String until) throws ParseException;



    /**
     *
     * @return 返回开发者用户画像评星等级
     */
    Object getPortraitLevel(String developer,String since,String until, String token) throws ParseException;

    /**
     *
     * @return 返回开发者用户画像开发者能力数据以及评星等级数据
     */
    Object getPortraitCompetence(String developer,String repoUuidList,String since,String until, String token) throws ParseException;


    /**
     *
     * @return 返回最近动态
     */
    Object getDeveloperRecentNews(String repoUuid, String developer, String since, String until);

    /**
     * 返回开发人员列表
     * @param repoUuidList
     * @param token
     * @return List<Map<String, Object>>
     */
    Object getDeveloperList(String repoUuidList, String token) throws ParseException;


    /**
     * 获取提交规范性或不规范明细
     * @param developer
     * @param repoUuidList
     * @param since
     * @param until
     * @param token
     * @param condition
     * @return condition=1 返回提交规范性；condition=2，返回不规范提交明细
     */
    List<Map<String,Object>> getCommitStandard(String developer, String repoUuidList, String since, String until, String token, String condition);

    /**
     * 清空缓存
     */
    void clearCache();

}
