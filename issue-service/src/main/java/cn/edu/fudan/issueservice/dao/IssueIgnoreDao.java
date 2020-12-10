package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.domain.dbo.IgnoreRecord;
import cn.edu.fudan.issueservice.mapper.IssueIgnoreMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Beethoven
 */
@Repository
public class IssueIgnoreDao {

    private IssueIgnoreMapper issueIgnoreMapper;

    public void insertIssueIgnoreRecords(List<IgnoreRecord> ignoreRecords) {
        issueIgnoreMapper.insertIssueIgnoreRecords(ignoreRecords);
    }

    @Autowired
    public void setIssueIgnoreMapper(IssueIgnoreMapper issueIgnoreMapper) {
        this.issueIgnoreMapper = issueIgnoreMapper;
    }
}
