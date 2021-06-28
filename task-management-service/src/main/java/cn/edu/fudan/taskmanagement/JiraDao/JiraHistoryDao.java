package cn.edu.fudan.taskmanagement.JiraDao;

import cn.edu.fudan.taskmanagement.domain.JiraHistory;
import cn.edu.fudan.taskmanagement.mapper.JiraCurrentMapper;
import cn.edu.fudan.taskmanagement.mapper.JiraHistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class JiraHistoryDao {

    @Autowired
    private JiraHistoryMapper jiraHistoryMapper;

    public void deleteJiraHistory(String repoId){
        jiraHistoryMapper.deleteJiraHistoryByRepoId(repoId);
    }
}
