package cn.edu.fudan.measureservice.mapper;

import cn.edu.fudan.measureservice.domain.bo.DeveloperLevel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author wjzho
 */
@Repository
public interface ProjectMapper {

    /**
     * 返回具体情况下的 developerGitNameList
     * @param repoUuidList 查询库列表
     * @param since 查询起止时间
     * @param until 查询结束时间
     * @return List<String> 开发者列表信息
     */
    List<String> getCommitGitNameList(@Param("repoUuidList") List<String> repoUuidList, @Param("since")String since, @Param("until")String until);

    /**
     * 返回开发者在参与库中的信息
     * @param repoUuid 查询库
     * @param since 查询起止时间
     * @param until 查询结束时间
     * @return key : developerGitName
     */
    List<Map<String,String>> getDeveloperRepoInfoList(@Param("repoUuid") String repoUuid,@Param("since")String since, @Param("until")String until);

    /**
     * fixme 改为从 gitName 查
     * 返回单个开发者在参与库/项目中的第一次提交时间
     * @param repoUuid 查询库
     * @param since 查询起止时间
     * @param until 查询结束时间
     * @param developer 查询开发者
     * @return
     */
    Map<String,String> getDeveloperFirstCommitDate(@Param("repoUuid") String repoUuid, @Param("since") String since, @Param("until") String until, @Param("developer") String developer);


    /**
     * 开发者在职状态
     * @return List<Map<String,String>> key : account_name,account_status
     */
    List<Map<String,String>> getDeveloperDutyTypeList();



    /**
     * fixme
     * 根据开发者的名字得到其参加过的项目信息
     * @param developer 开发者名字
     * @return Map key repo_id module
     */
    List<Map<String, String>> getProjectInfo(@Param("developer") String developer) ;


    /**
     * 获取开发者参与库列表
     * @param developer 开发者姓名
     * @param since 查询起始时间
     * @param until 查询结束时间
     * @return List<String> developerRepoList
     */
    List<String> getDeveloperRepoList(@Param("developer")String developer,@Param("since")String since,@Param("until")String until);


    /**
     * 获取开发者参与库的合法提交信息（不含Merge）
     * @param repoUuidList 查询库列表
     * @param since 查询起始时间
     * @param until 查询结束时间
     * @param accountGitNameList 开发者姓名
     * @return  List<Map<String,Object>> key : repo_id, developer, commit_time , commit_id , message
     */
    List<Map<String,String>> getDeveloperValidCommitMsg(@Param("repoUuidList")List<String> repoUuidList,@Param("since")String since,@Param("until")String until,@Param("accountGitNameList")List<String> accountGitNameList);

    /**
     * 获取项目下包含库的合法提交信息 （不含Merge）
     * @param repoUuidList 查询库列表
     * @param since 查询起始时间
     * @param until 查询结束时间
     * @return List<Map<String,Object>> key : repo_id , developer, commit_time , commit_id , message
     */
    List<Map<String,String>> getProjectValidCommitMsg(@Param("repoUuidList")List<String> repoUuidList,@Param("since")String since,@Param("until")String until);

    /**
     * 获取查询库列表中前3位提交次数最多的开发者
     * @param repoUuidList 查询库列表
     * @param since 查询起始时间
     * @param until 查询结束时间
     * @return key : developerName , countNum
     */
    List<Map<String, Object>> getDeveloperRankByCommitCount(@Param("repoUuidList") List<String> repoUuidList, @Param("since")String since, @Param("until")String until);

    /**
     * 获取开发者该库下的总提交次数
     * @param repoUuidList 查询库
     * @param since 查询起始时间
     * @param until 查询结束时间
     * @param developer 开发者姓名
     * @return int developerCommitCount
     */
    int getDeveloperCommitCountsByDuration(@Param("repoUuidList")List<String> repoUuidList, @Param("since")String since, @Param("until")String until, @Param("developer")String developer);

    /**
     * 查询sub_repository表获取repoUuid对应repo_name
     * @param repoUuid 查询库
     * @return String repoName
     */
    String getRepoName(@Param("repoUuid") String repoUuid);

    /**
     * 查询sub_repository表获取repoUuid对应project_name
     * @param repoUuid 查询库
     * @return String projectName
     */
    String getProjectName(@Param("repoUuid") String repoUuid);

    /**
     * 用户鉴权，返回Leader管理库列表
     * @param userUuid 查询用户 account_uuid
     * @return List<String> projectList
     */
    List<String> getProjectByAccountId(@Param("account_uuid") String userUuid);

    /**
     * 开发者星级数据入库
     * @param developerLevel
     */
    void insertDeveloperLevel(DeveloperLevel developerLevel);

    /**
     * 从库中获取开发者星级数据
     * @param developerList 开发者人员列表
     * @return
     */
    List<DeveloperLevel> getDeveloperLevelList(@Param("developerList") List<String> developerList);

    /**
     * 获取该库的主要语言
     * @param repoUuid 查询库
     * @return String language
     */
    String getRepoLanguage(@Param("repoUuid")String repoUuid);

    /**
     * 根据 projectId 获取项目名称
     * @param projectIdList 查询 projectId 列表
     * @return
     */
    List<Map<String,Object>> getProjectNameById(@Param("projectIdList") List<String> projectIdList);

    /**
     * 根据 projectName 获取 projectId
     * @param projectName 查询项目名
     * @return
     */
    Integer getProjectIdByName(@Param("projectName") String projectName);

}
