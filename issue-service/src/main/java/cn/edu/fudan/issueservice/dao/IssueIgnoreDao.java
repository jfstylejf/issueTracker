package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.domain.dbo.IgnoreRecord;
import cn.edu.fudan.issueservice.mapper.IssueIgnoreMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author Beethoven
 */
@Repository
public class IssueIgnoreDao {

    private IssueIgnoreMapper issueIgnoreMapper;

    @Autowired
    public void setIssueIgnoreMapper(IssueIgnoreMapper issueIgnoreMapper) {
        this.issueIgnoreMapper = issueIgnoreMapper;
    }

    public void insertIssueIgnoreRecords(List<IgnoreRecord> ignoreRecords) {
        issueIgnoreMapper.insertIssueIgnoreRecords(ignoreRecords);
    }

    public List<Map<String, Object>> getAllIgnoreRecord(String repoUrl, List<String> preCommits) {
        return issueIgnoreMapper.getAllIgnoreRecord(repoUrl, preCommits);
    }

    public void updateIssueIgnoreRecords(List<String> usedIgnoreRecordsUuid) {
        if (usedIgnoreRecordsUuid.isEmpty()) {
            return;
        }
        issueIgnoreMapper.updateIssueIgnoreRecords(usedIgnoreRecordsUuid);
    }
}
