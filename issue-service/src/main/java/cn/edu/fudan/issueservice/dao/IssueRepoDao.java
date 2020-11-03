package cn.edu.fudan.issueservice.dao;


import cn.edu.fudan.issueservice.domain.dbo.IssueRepo;
import cn.edu.fudan.issueservice.mapper.IssueRepoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IssueRepoDao {

    private IssueRepoMapper issueRepoMapper;

    public void insertOneIssueRepo(IssueRepo issueRepo){
        issueRepoMapper.insertOneIssueRepo (issueRepo);
    }

    public void updateIssueRepo(IssueRepo issueRepo){
        issueRepoMapper.updateIssueRepo (issueRepo);
    }

    public void delIssueRepo(String repoId, String nature, String tool){
        issueRepoMapper.deleteIssueRepoByCondition (repoId, nature, tool);
    }

    public List<IssueRepo> getIssueRepoByCondition(String repoId, String nature, String tool){
        return issueRepoMapper.getIssueRepoByCondition (repoId, nature, tool);
    }

    public List<String> getAllScannedRepoId(){
        return issueRepoMapper.getAllScanedRepoId();
    }


    @Autowired
    public void setIssueRepoMapper(IssueRepoMapper issueRepoMapper) {
        this.issueRepoMapper = issueRepoMapper;
    }
}
