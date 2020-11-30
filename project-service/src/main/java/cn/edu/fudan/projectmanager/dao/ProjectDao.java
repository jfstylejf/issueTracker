package cn.edu.fudan.projectmanager.dao;

import cn.edu.fudan.projectmanager.domain.SubRepository;
import cn.edu.fudan.projectmanager.mapper.AccountRepositoryMapper;
import cn.edu.fudan.projectmanager.mapper.ProjectMapper;
import org.apache.ibatis.annotations.Param;
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

    public void insertOneProject(String accountUuid,String projectName){
        projectMapper.insertOneProject(accountUuid,projectName);
    }

    public void updateProjectNameP(String accountUuid,String oldProjectName, String newProjectName) {
        projectMapper.updateProjectNameP(accountUuid, oldProjectName, newProjectName);
    }

    public List<Map<String, Object>> getProjectAll(){
        return projectMapper.getProjectP();
    }

    @Autowired
    public void setProjectMapper(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }
}