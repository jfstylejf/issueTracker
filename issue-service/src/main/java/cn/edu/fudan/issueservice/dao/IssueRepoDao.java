package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.common.domain.po.scan.RepoScan;
import cn.edu.fudan.issueservice.mapper.IssueRepoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

/**
 * @author beethoven
 */
@Repository
public class IssueRepoDao {

    private IssueRepoMapper issueRepoMapper;

    public void insertOneIssueRepo(RepoScan issueRepo) {
        issueRepoMapper.insertOneIssueRepo(issueRepo);
    }

//    public void insertOneIssueRepo(RepoScan issueRepo) {
//        issueRepoMapper.insertOneIssueRepo(issueRepo, UUID.randomUUID().toString());
//    }

    public void updateIssueRepo(RepoScan issueRepo) {
        issueRepoMapper.updateIssueRepo(issueRepo);
    }

    public void delIssueRepo(String repoId, String tool) {
        issueRepoMapper.deleteIssueRepoByCondition(repoId, tool);
    }

    public List<RepoScan> getIssueRepoByCondition(String repoId, String tool) {
        return issueRepoMapper.getIssueRepoByCondition(repoId, tool);
    }

    public List<HashMap<String, Integer>> getNotScanCommitsCount(String repoUuid, String tool) {
        return issueRepoMapper.getNotScanCommitsCount(repoUuid, tool);
    }

    public RepoScan getMainIssueRepo(String repoUuid, String tool) {
        return issueRepoMapper.getMainIssueRepo(repoUuid, tool);
    }

    @Autowired
    public void setIssueRepoMapper(IssueRepoMapper issueRepoMapper) {
        this.issueRepoMapper = issueRepoMapper;
    }
}
