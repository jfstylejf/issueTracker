package cn.edu.fudan.projectmanager.dao;

import cn.edu.fudan.projectmanager.domain.SubRepository;
import cn.edu.fudan.projectmanager.mapper.AccountRepositoryMapper;
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

    public Integer insertOneProject(Map<String,Integer> newProject){
        return projectMapper.insertOneProject(newProject);
    }

    @Autowired
    public void setProjectMapper(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }
}