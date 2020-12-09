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
 * @author fancying
 */
@Repository
public interface AccountRepositoryMapper {

    /**
     * 插入repoUser
     * @param accountRepositories list
     */
    void insertAccountRepositories(@Param("list") List<AccountRepository> accountRepositories);

    /**
     * @param accountUuid 用户uuid
     * @param url repo的URL
     */
    @Select("SELECT count(*) FROM account_repository,sub_repository WHERE account_uuid = #{accountUuid} and url = #{url}")
    Integer getRepoCount(@Param("accountUuid")String accountUuid, @Param("url") String url);

    /**
     * 更新项目名
     * @param accountUuid,oldProjectName,newProjectName
     */
    void updateProjectNameAR(@Param("accountUuid") String accountUuid, @Param("oldProjectName")String oldProjectName, @Param("newProjectName")String newProjectName);

    @Delete("DELETE FROM `account_repository` WHERE `sub_repository_uuid` = #{subRepoUuid};")
    void deleteRelation(@Param("subRepoUuid") String subRepoUuid);

}
