package cn.edu.fudan.cloneservice.mapper;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * description: commit view 表 查询接口
 *
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
    @Select("SELECT distinct(repo_id) FROM issueTracker.commit_view " +
            "WHERE developer = #{author} AND repo_id " +
            "IN (SELECT repo_id FROM issueTracker.project);")
    List<String> getrepoIdList(String author);


    /**
     *description
     * @param
     * @return list
     */
    @Select("SELECT distinct(repo_id) FROM issueTracker.commit_view " +
            "WHERE repo_id " +
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
    @Select("SELECT commit_id FROM issueTracker.commit_view " +
            "WHERE repo_id = #{repoId} AND developer = #{author}" +
            "AND commit_time >= #{start} AND commit_time <= #{end} ORDER BY commit_time;")
    List<String> getAuthorCommitList(String repoId, String author, String start, String end);


    /**
     * description
     * @param repoId repoId
     * @param start start data
     * @param end end data
     * @return list
     */
    @Select("SELECT commit_id FROM issueTracker.commit_view " +
            "WHERE repo_id = #{repoId} AND commit_time >= #{start} AND commit_time <= #{end} ORDER BY commit_time;")
    List<String> getCommitList(String repoId, String start, String end);

}
