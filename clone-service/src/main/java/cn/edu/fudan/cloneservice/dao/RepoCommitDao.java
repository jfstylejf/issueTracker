package cn.edu.fudan.cloneservice.dao;

import cn.edu.fudan.cloneservice.mapper.CloneScanMapper;
import cn.edu.fudan.cloneservice.mapper.RepoCommitMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;


@Repository
public class RepoCommitDao {
    private RepoCommitMapper repoCommitMapper;
    @Autowired
    private void setRepoCommitMapper(RepoCommitMapper repoCommitMapper){
        this.repoCommitMapper = repoCommitMapper;
    }

    @Cacheable(value = "project_name", key = "#projectId")
    public String getProjectNameByProjectId(String projectId){
        return repoCommitMapper.getProjectNameByProjectId(projectId);
    }

}
