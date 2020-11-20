package cn.edu.fudan.projectmanager.mapper;

import cn.edu.fudan.projectmanager.domain.AccountRepository;
import org.apache.ibatis.annotations.Delete;
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
    void insertAccountRepositories(List<AccountRepository> accountRepositories);

    /**
     * @param accountUuid 用户uuid
     * @param url repo的URL
     */
    @Select("SELECT count(*) FROM repo_user,sub_repository WHERE account_uuid = #{accountUuid} and url = #{url}")
    Integer getRepoCount(String accountUuid, String url);

    /**
     *
     */
    @Update("UPDATE `repo_user` SET `project_name` = #{newName} WHERE account_uuid = #{accountUuid} and `accountName` = #{oldName};")
    void updateRepoName(String accountUuid, String oldName, String newName);

    @Delete("DELETE FROM `repo_user` WHERE `sub_repository_uuid` = #{subRepoUuid};")
    void deleteRelation(String subRepoUuid);

//    /**
//     * 通过用户id和项目名得到对应的项目
//     * 用于判断同一个用户是否存在同名项目
//     * @param accountId id
//     * @param repoName 项目名
//     * @return
//     */
//    List<AccountRepository> getRepoUserByName(@Param("account_id") String accountId, @Param("accountName") String repoName);
//
//    /**
//     * 通过subRepoUuid和accountId得到项目
//     * 用于判断此用户是否添加过此项目
//     * @param subRepoUuid
//     * @param accountId
//     * @return
//     */
//    List<AccountRepository> getRepoUserBySubRepoUuidAndAccountId(@Param("sub_repository_uuid") String subRepoUuid, @Param("account_id") String accountId);
//
//    /**
//     * 通过accountId来得到repoUser
//     * @param accountId
//     * @return
//     */
//    List<AccountRepository> getRepoUserByAccountId(@Param("account_id") String accountId);
//
//    /**
//     * 非管理员删除项目，通过uuid删除
//     * @param uuid
//     */
//    void remove(@Param("uuid") String uuid);
//
//    /**
//     * 通过subRepoId得到所有的repoUser
//     * 彻底删除项目时使用
//     * @param subRepoUuid
//     * @return
//     */
//    List<AccountRepository> getRepoUserBySubRepoId(@Param("sub_repository_uuid") String subRepoUuid);
}