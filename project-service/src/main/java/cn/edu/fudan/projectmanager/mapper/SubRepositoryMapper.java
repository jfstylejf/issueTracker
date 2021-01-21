package cn.edu.fudan.projectmanager.mapper;

import cn.edu.fudan.projectmanager.domain.SubRepository;
import lombok.Data;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author fancying
 */
@Repository
public interface SubRepositoryMapper {


    /**
     * 插入repo信息
     *
     * @param subRepository s
     * @return integer 返回影响行数n（n为0时实际为插入失败）
     */
    Integer insertOneRepo(SubRepository subRepository);

    /**
     * 更新subRepo表
     *
     * @param subRepository s
     */
    void updateSubRepository(SubRepository subRepository);

    /**
     * 通过uuid得到subRepo
     *
     * @param uuid
     * @return subRepository
     */
    SubRepository getSubRepoByUuid(@Param("uuid") String uuid);

    SubRepository getSubRepoByRepoUuid(@Param("repo_uuid") String repoUuid);

    /**
     * 获取repo最新commit时间
     *
     * @param repoUuid uuid
     * @return date
     */
    @Select("select max(commit_time) from commit_view where repo_id = #{repo_id} ;")
    Date getLatestCommitTime(@Param("repo_id") String repoUuid);


    List<SubRepository> getAllSubRepoByAccountId(@Param("account_uuid") String accountUuid);

    List<SubRepository> getLeaderRepoByAccountUuid(@Param("account_uuid") String accountUuid);

    List<SubRepository> getRepoByAccountUuid(@Param("account_uuid") String accountUuid);

    List<SubRepository> getAllSubRepo();


    @Update("UPDATE `sub_repository` SET `recycled` = '1' WHERE `uuid` = #{subRepoUuid};")
    void setRecycled(@Param("subRepoUuid") String subRepoUuid);

    @Delete("DELETE FROM `sub_repository` WHERE `uuid` = #{subRepoUuid};")
    void deleteRepo(@Param("subRepoUuid") String subRepoUuid);

    /**
     * 项目与库的对应关系
     */
    @Select("SELECT s.project_name, r.repo_name, repo_uuid, recycled " +
            "FROM sub_repository as s,account_repository as r " +
            "WHERE s.uuid = r.sub_repository_uuid order by project_name;")
    List<Map<String, Object>> getAllProjectRepoRelation();

    /**
     * 更新项目名
     *
     * @param accountUuid    当前人员uuid
     * @param oldProjectName 旧项目名
     * @param newProjectName 新项目名
     */
    void updateProjectNameSR(@Param("accountUuid") String accountUuid, @Param("oldProjectName") String oldProjectName, @Param("newProjectName") String newProjectName);

    /**
     * 更新库名
     *
     * @param accountUuid 当前人员uuid
     * @param oldRepoName 旧库名
     * @param newRepoName 新库名
     */
    void updateRepoName(@Param("accountUuid") String accountUuid, @Param("oldRepoName") String oldRepoName, @Param("newRepoName") String newRepoName);


    /**
     * 更新repo所属项目
     *
     * @param accountUuid    当前人员uuid
     * @param oldProjectName 旧项目名
     * @param newProjectName 新项目名
     * @param RepoUuid       库uuid
     */
    void updateRepoProjectSR(@Param("accountUuid") String accountUuid, @Param("oldProjectName") String oldProjectName, @Param("newProjectName") String newProjectName, @Param("RepoUuid") String RepoUuid);

    /**
     * 删除库
     *
     * @param accountUuid 当前人员uuid
     * @param RepoUuid    库uuid
     */
    void deleteRepoSR(@Param("accountUuid") String accountUuid, @Param("RepoUuid") String RepoUuid);

    /**
     * 将库放入回收站中
     *
     * @param accountUuid 当前人员uuid
     * @param recycled    回收状态
     * @param repoUuid    库UUID
     */
    void putIntoRecycled(@Param("accountUuid") String accountUuid, @Param("recycled") Integer recycled, @Param("repoUuid") String repoUuid);

    /**
     * 将库从回收站中取出
     *
     * @param accountUuid 当前人员uuid
     * @param recycled    回收状态
     * @param repoUuid    库UUID
     */
    void getFromRecycled(@Param("accountUuid") String accountUuid, @Param("recycled") Integer recycled, @Param("repoUuid") String repoUuid);
}
