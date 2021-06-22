package cn.edu.fudan.taskmanagement.service;

import cn.edu.fudan.taskmanagement.domain.Task;
import org.springframework.stereotype.Service;


import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author zyh
 * @date 2020/7/2
 */
@Service
public interface JiraService {

    /**
     * 根据jql查找Jira task
     *
     * @param type    jql的类型，如 project（根据项目来查找task）、key（根据task的key来查找）
     * @param keyword 对应type的值，如 project=10001，project=CODE，key=CODE-1
     * @return task info
     */
    List<Task> getTaskInfoByJql(String type, String keyword);

    Map<String, Object> getDeveloperMsg(String repoUuidPara, String developer, String since, String until) throws ParseException;

    void insertJiraMsg(String repoUuid, String developer, String since, String until);
}