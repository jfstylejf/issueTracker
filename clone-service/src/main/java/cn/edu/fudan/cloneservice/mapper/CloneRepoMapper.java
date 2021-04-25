package cn.edu.fudan.cloneservice.mapper;

import cn.edu.fudan.cloneservice.domain.clone.CloneRepo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CloneRepoMapper {

    /**
     * 插入clone repo
     * @param cloneRepo
     */
    void insertCloneRepo(CloneRepo cloneRepo);

    /**
     * 更新clone repo
     * @param cloneRepo 项目的clone扫描状态
     */
    void updateCloneRepo(CloneRepo cloneRepo);

    void deleteRepoByRepoId(@Param("repo_id") String repoId);

    /**
     * 获取最新的clone repo
     * @param repoId repo id
     * @return
     */
    CloneRepo getLatestCloneRepo(@Param("repo_id") String repoId);

    /**
     * 获取repo扫描的次数
     * @param repoId repo id
     * @return
     */
    Integer getScanCount(@Param("repo_id") String repoId);
}
