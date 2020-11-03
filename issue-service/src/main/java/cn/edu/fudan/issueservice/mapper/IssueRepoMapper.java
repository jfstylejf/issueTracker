package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.dbo.IssueRepo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueRepoMapper {

    void insertOneIssueRepo(IssueRepo issueRepo);

    void updateIssueRepo(IssueRepo issueRepo);

    List<IssueRepo> getIssueRepoByCondition(@Param("repo_id") String repoId, @Param("nature")String nature, @Param("tool") String tool);

    void deleteIssueRepoByCondition(@Param("repo_id") String repoId, @Param("nature")String nature, @Param("tool") String tool);

    @Select("SELECT DISTINCT(repo_id) FROM issue_repo;")
    List<String> getAllScanedRepoId();
}
