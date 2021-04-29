package cn.edu.fudan.projectmanager.mapper;

import cn.edu.fudan.projectmanager.domain.AccountRepository;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TODO 完善注释
 *
 * @author fancying
 */
@Repository
public interface AccountRepositoryMapper {

    /**
     * 插入repoUser
     *
     * @param accountRepositories list
     */
    void insertAccountRepositories(@Param("list") List<AccountRepository> accountRepositories);

    /**
     * @param branch branch
     * @param url    repo的URL
     */
    @Select("SELECT count(*) FROM sub_repository WHERE  url = #{url}  and branch = #{branch}")
    Integer getRepoCount(@Param("branch") String branch, @Param("url") String url);

    /**
     * 更新项目名
     *
     * @param accountUuid    当前人员uuid
     * @param oldProjectName 旧项目名
     * @param newProjectName 新项目名
     */
    void updateProjectNameAR(@Param("accountUuid") String accountUuid, @Param("oldProjectName") String oldProjectName, @Param("newProjectName") String newProjectName);

    /**
     * 更新repo所属项目
     *
     * @param accountUuid    当前人员uuid
     * @param oldProjectName 旧项目名
     * @param newProjectName 新项目名
     * @param RepoUuid       库uuid
     */
    void updateRepoProjectAR(@Param("accountUuid") String accountUuid, @Param("oldProjectName") String oldProjectName, @Param("newProjectName") String newProjectName, @Param("RepoUuid") String RepoUuid);

    /**
     * 删除库
     *
     * @param accountUuid 当前人员uuid
     * @param repoUuid    库uuid
     */
    void deleteRepoAR(@Param("accountUuid") String accountUuid, @Param("repoUuid") String repoUuid);

    @Delete("DELETE FROM `account_repository` WHERE `sub_repository_uuid` = #{subRepoUuid};")
    void deleteRelation(@Param("subRepoUuid") String subRepoUuid);

}
