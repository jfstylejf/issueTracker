package cn.edu.fudan.projectmanager.dao;

import cn.edu.fudan.projectmanager.domain.Project;
import cn.edu.fudan.projectmanager.mapper.ProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * description:
 *
 * @author Richy
 * create: 2020-11-24 14:38
 **/
@Repository
public class ProjectDao {
    private ProjectMapper projectMapper;

    public void insertOneProject(String accountUuid, String projectName) {
        projectMapper.insertOneProject(accountUuid, projectName);
    }

    public void updateProjectNameP(String accountUuid, String oldProjectName, String newProjectName) {
        projectMapper.updateProjectNameP(accountUuid, oldProjectName, newProjectName);
    }

    public List<Project> getProjectList() {
        return projectMapper.getProjectListP();
    }

    public List<Project> getProjectListByLifeStatus(Integer lifeStatus) {
        return projectMapper.getProjectListPByLifeStatus(lifeStatus);
    }

    public Project getProjectByName(String projectName) {
        return projectMapper.getProjectByNameP(projectName);
    }

    public Integer getProjectIdByName(String projectName) {
        return projectMapper.getProjectIdByNameP(projectName);
    }

    public void deleteProject(String projectName){ projectMapper.deleteProjectByName(projectName); };

    public List<String> getProjectRepo(String projectName){return projectMapper.getRepoByProjectName(projectName); };

    @Autowired
    public void setProjectMapper(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }
}