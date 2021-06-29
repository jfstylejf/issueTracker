package cn.edu.fudan.taskmanagement.JiraDao;

import cn.edu.fudan.taskmanagement.mapper.JiraCurrentMapper;
import cn.edu.fudan.taskmanagement.mapper.RepoCommitMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class JiraCurrentDao {
    @Autowired
    private JiraCurrentMapper jiraCurrentMapper;

    public void deleteJiraCurrent(String repoId){
        jiraCurrentMapper.deleteJiraCurrentByRepoId(repoId);
    }

}
