package cn.edu.fudan.taskmanagement.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface JiraHistoryMapper {
    /**
     * 删除jira history， 删除整个项目时用到
     * @param repoId repo id
     */
    void deleteJiraHistoryByRepoId(@Param("repo_id") String repoId);

}
