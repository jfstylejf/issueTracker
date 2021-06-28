package cn.edu.fudan.taskmanagement.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * description: commit view 表 查询接口
 * @author fancying
 * create: 2020-08-01 16:01
 **/
@Mapper
public interface RepoCommitMapper {

    @Select("select repo_uuid from sub_repository where project_name in(SELECT project_name FROM issueTracker.project where id = #{project_id});")
    List<String> getRepoIdByProjectId(String project_id);

    @Select("SELECT id FROM issueTracker.project")
    List<Integer> getProjectIds();

    @Select("SELECT id FROM issueTracker.project where project_name = #{project_name};")
    String getProjectIdByProjectName(String projectName);

    @Select("SELECT distinct developer_unique_name from issueTracker.commit_view")
    List<String> getDevelopers();

    @Select("SELECT count(distinct(jira_id)) FROM issueTracker.jira_history" +
            " where unique_name = #{developer} " +
            "and status = #{status} and repo_id = #{repoId} " +
            "AND commit_time >= #{start} " +
            "AND commit_time <= #{end};")
    int getJiraCountByDeveloperAndRepoId(String developer, String status, String repoId, String start, String end);
}
