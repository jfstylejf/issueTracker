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
     * @param subRepository s
     * @return integer 返回影响行数n（n为0时实际为插入失败）
     */
    Integer insertOneRepo (SubRepository subRepository);

    /**
     * 更新subRepo表
     * @param subRepository s
     */
    void updateSubRepository(SubRepository subRepository);

    /**
     * 通过uuid得到subRepo
     * @param uuid
     * @return subRepository
     */
    SubRepository getSubRepoByUuid(@Param("uuid")String uuid);

    SubRepository getSubRepoByRepoUuid(@Param("repo_uuid")String repoUuid);

    /**
     * 获取repo最新commit时间
     * @param repoUuid uuid
     * @return date
     */
    @Select("select max(commit_time) from commit_view where repo_id = #{repo_id} ;")
    Date getLatestCommitTime(@Param("repo_id") String repoUuid);


    List<SubRepository> getAllSubRepoByAccountId(@Param("account_uuid") String accountUuid);


    @Update("UPDATE `sub_repository` SET `recycled` = '1' WHERE `uuid` = #{subRepoUuid};")
    void setRecycled(String subRepoUuid);

    @Delete("DELETE FROM `sub_repository` WHERE `uuid` = #{subRepoUuid};")
    void deleteRepo(String subRepoUuid);

    /**
     * 项目与库的对应关系
     */
    @Select("SELECT s.project_name, name, repo_uuid, recycled " +
            "FROM sub_repository as s,repo_user as r " +
            "WHERE s.uuid = r.sub_repository_uuid order by project_name;")
    List<Map<String, Object>> getAllProjectRepoRelation();


}
