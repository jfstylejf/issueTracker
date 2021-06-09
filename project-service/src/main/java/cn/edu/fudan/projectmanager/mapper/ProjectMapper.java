package cn.edu.fudan.projectmanager.mapper;

import cn.edu.fudan.projectmanager.domain.Project;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * description:
 *
 * @author Richy
 **/
@Repository
public interface ProjectMapper {

    /**
     * 插入repo信息
     *
     * @param accountUuid 当前登录人
     * @param projectName 项目名
     */
    void insertOneProject(@Param("accountUuid") String accountUuid, @Param("projectName") String projectName);

    /**
     * 更新项目名
     *
     * @param accountUuid    当前登录人
     * @param oldProjectName 旧项目名
     * @param newProjectName 新项目名
     */
    void updateProjectNameP(@Param("accountUuid") String accountUuid, @Param("oldProjectName") String oldProjectName, @Param("newProjectName") String newProjectName);

    /**
     * 获取项目列表
     */
    List<Project> getProjectListP();

    /**
     * 通过生命状态获取项目列表
     *
     * @param lifeStatus 项目生命状态
     */
    List<Project> getProjectListPByLifeStatus(Integer lifeStatus);

    /**
     * 根据项目名获取项目信息
     */
    Project getProjectByNameP(String projectName);

    /**
     * 根据项目名获取项目ID
     *
     * @param projectName 项目名
     * @return projectId
     */
    Integer getProjectIdByNameP(String projectName);

    /**
     * 删除项目
     * @param projectName 项目名
     */
    void deleteProjectByName(String projectName);

    /**
     * 获取项目包含的repo
     * @param projectName 项目名
     */
    List<String> getRepoByProjectName(String projectName);
}
