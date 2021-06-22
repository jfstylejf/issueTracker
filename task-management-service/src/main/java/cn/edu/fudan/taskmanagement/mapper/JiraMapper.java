package cn.edu.fudan.taskmanagement.mapper;


import cn.edu.fudan.taskmanagement.domain.JiraDetail;
import cn.edu.fudan.taskmanagement.domain.JiraMsg;
import cn.edu.fudan.taskmanagement.domain.TeamJiraMsg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


// fix me

@Mapper
public interface JiraMapper {

    void insertCurrentJiraMsg(List<JiraMsg> jiraMsg);

    void insertHistoryJiraMsg(List<JiraMsg> jiraMsg);

    /**
     * 根据指定repo_id、指定人、日期区间从jira_history中获得jiraId的列表
     * 可能有问题
     * @param repoId
     * @param beginDate
     * @param endDate
     * @param developer
     * @return
     */
    List<String> getJiraIdList(@Param("repo_id") String repoId, @Param("begin_date") String beginDate, @Param("end_date") String endDate, @Param("developer") String developer);

    List<String> getRepoUuidByDeveloper(@Param("developer") String developer);
    /**
     * 根据指定repo_id、指定人、日期PWD
     * 区间从commit_view表中读取数据
     * @param repoId
     * @param developerName
     * @param beginDate
     * @param endDate
     * @return
     */
    List<Map<String, String>> getCommitMsgByCondition(@Param("repo_id")String repoId, @Param("developer_name")String developerName,@Param("since")String beginDate,@Param("until")String endDate);

    JiraMsg getJiraMsgFromCurrentDatabase(@Param("jira_id")String jiraId);

    TeamJiraMsg getTeamJiraMsgFromCurrentDatabase(@Param("jira_id")String jiraId);
    
    List<JiraDetail> getJiraDetailFromHistoryDatabase(@Param("jira_id")String jiraId);

    String getFirstCommitDate(@Param("jira_id")String jiraId, @Param("developer")String developer);

    int getDeveloperJiraCommitFromDatabase(@Param("developer") String developer,@Param("jira_id") String jiraId);

    String getUniqueName(@Param("developer") String developer);


}

