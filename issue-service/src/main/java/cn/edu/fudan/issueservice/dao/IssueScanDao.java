package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.domain.dbo.IssueScan;
import cn.edu.fudan.issueservice.mapper.IssueScanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Repository
public class IssueScanDao {

    private IssueScanMapper issueScanMapper;

    @Autowired
    public void setScanMapper(IssueScanMapper issueScanMapper) {
        this.issueScanMapper = issueScanMapper;
    }

    public void insertOneIssueScan(IssueScan scan) {
        issueScanMapper.insertOneScan(scan);
    }

    public void deleteIssueScanByRepoIdAndTool(String repoId, String tool) {
        issueScanMapper.deleteIssueScanByRepoIdAndTool(repoId, tool);
    }

    public List<IssueScan> getIssueScanByRepoIdAndStatusAndTool(String repoId, List<String> status, String tool) {
        if(status != null  && status.size () == 0){
            return issueScanMapper.getIssueScanByRepoIdAndStatusAndTool(repoId, null, tool);
        }
        return issueScanMapper.getIssueScanByRepoIdAndStatusAndTool(repoId, status, tool);
    }

    public IssueScan getIssueScanByRepoIdAndCommitIdAndTool(String repoId, String commitId, String tool) {
        List<IssueScan> issueScans = issueScanMapper.getIssueScanByRepoIdAndCommitIdAndTool(repoId, commitId, tool, null, null);
        if(issueScans == null || issueScans.isEmpty ()){
            return null;
        }
        return issueScans.get (0);
    }

    public List<IssueScan> getScannedCommitsByRepoIdAndTool(String repoId,  String tool, String since, String until) {
        List<IssueScan> result = new ArrayList<> ();
        List<IssueScan> issueScans = issueScanMapper.getIssueScanByRepoIdAndCommitIdAndTool(repoId, null, tool, null, null);
        if(issueScans != null){
            result = issueScans;
        }
        return result;
    }

    public IssueScan getLatestIssueScanByRepoIdAndTool(String repoId, String tool) {
        return issueScanMapper.getLatestIssueScanByRepoIdAndTool(repoId, tool);
    }

    public HashSet<String> getScannedCommitList(String repoUuid, String tool) {
        return new HashSet<>(issueScanMapper.getScannedCommitList(repoUuid, tool));
    }

    public String getStartCommitByRepoUuid(String repoUuid) {
        return issueScanMapper.getStartCommitByRepoUuid(repoUuid);
    }
}
