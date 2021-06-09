package cn.edu.fudan.cloneservice.mapper;

import lombok.Setter;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * description: commit view 表 查询接口
 * @author fancying
 * create: 2020-08-01 16:01
 **/
@Repository
public interface RepoCommitMapper {

    /**
     *description
     * @param author developer
     * @return list
     */
    @Select("SELECT distinct(repo_uuid) FROM issueTracker.commit_view " +
            "WHERE developer = #{author} AND repo_uuid " +
            "IN (SELECT repo_uuid FROM issueTracker.sub_repository);")
    List<String> getrepoIdList(String author);


    /**
     *description
     * @param
     * @return list
     */
    @Select("SELECT distinct(repo_uuid) FROM issueTracker.commit_view " +
            "WHERE repo_uuid " +
            "IN (SELECT repo_id FROM issueTracker.project);")
    List<String> getProjectRepoIdList();
    /**
     * description
     * @param repoId repoId
     * @param author developer
     * @param start start data
     * @param end end data
     * @return list
     */
    @Select("SELECT distinct(commit_id) FROM issueTracker.commit_view " +
            "WHERE repo_uuid = #{repoId} AND developer = #{author}" +
            "AND commit_time >= #{start} AND commit_time <= #{end} and length(parent_commit) < 50 ORDER BY commit_time;")
    List<String> getAuthorCommitList(String repoId, String author, String start, String end);

    @Select("SELECT distinct(commit_id) FROM issueTracker.commit_view " +
            "WHERE repo_uuid = #{repoId} AND developer_unique_name = #{author}" +
            "AND commit_time >= #{start} AND commit_time <= #{end} and length(parent_commit) < 50 ORDER BY commit_time;")
    List<String> getCommitListByUniqueName(String repoId, String author, String start, String end);

    /**
     * description
     * @param repoId repoId
     * @param start start data
     * @param end end data
     * @return list
     */
    @Select("SELECT distinct(commit_id) FROM issueTracker.commit_view " +
            "WHERE repo_uuid = #{repoId} AND commit_time >= #{start} AND commit_time <= #{end} AND length(parent_commit) < 50 ORDER BY commit_time;")
    List<String> getCommitList(String repoId, String start, String end);


    /**
     * fixme 移动到其他地方
     * @return key accountName、account_gitname
     */
    @Select("SELECT account_name,account_gitname FROM issueTracker.account_author;")
    List<Map<String, String>> getAllTrueName();

    @Select("SELECT account_gitname FROM issueTracker.account_author WHERE `account_name` = #{developer};")
    List<String> getAllGitName(String developer);

    @Select("SELECT language FROM issueTracker.sub_repository where repo_uuid = #{repo_uuid};")
    String getLanguage(String repo_id);

    @Select("select repo_uuid from sub_repository where project_name in(SELECT project_name FROM issueTracker.project where id = #{project_id});")
    List<String> getRepoIdByProjectId(String project_id);

    @Select("SELECT id FROM issueTracker.project")
    List<Integer> getProjectIds();

    @Select("SELECT repo_uuid from issueTracker.commit_view where commit_id = #{commit_id} limit 1;")
    String getRepoIdByCommitId(String commit_id);

    @Select("SELECT commit_time from issueTracker.commit_view where commit_id = #{commit_id} limit 1;")
    String getCommitTimeByCommitId(String commit_id);

    @Select("SELECT project_name FROM issueTracker.project where id = #{project_id};")
    String getProjectNameByProjectId(String projectId);

    @Select("SELECT id FROM issueTracker.project where project_name = #{project_name};")
    String getProjectIdByProjectName(String projectName);

    @Select("SELECT distinct developer_unique_name from issueTracker.commit_view")
    List<String> getDevelopers();
}
