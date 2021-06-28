package cn.edu.fudan.taskmanagement.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface JiraCurrentMapper {
    /**
     * 删除jira current， 删除整个项目时用到
     * @param repoId repo id
     */
    void deleteJiraCurrentByRepoId(@Param("repo_id") String repoId);

}
