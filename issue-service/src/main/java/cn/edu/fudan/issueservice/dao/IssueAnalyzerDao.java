package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.domain.dbo.IssueAnalyzer;
import cn.edu.fudan.issueservice.mapper.IssueAnalyzerMapper;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author beethoven
 */
@Repository
public class IssueAnalyzerDao {

    private IssueAnalyzerMapper issueAnalyzerMapper;

    @Autowired
    public void setIssueAnalyzerMapper(IssueAnalyzerMapper issueAnalyzerMapper) {
        this.issueAnalyzerMapper = issueAnalyzerMapper;
    }

    public void insertIssueAnalyzer(List<IssueAnalyzer> issueAnalyzerList) {
        issueAnalyzerMapper.insertIssueAnalyzerRecords(issueAnalyzerList);
    }

    public JSONObject getAnalyzeResultByRepoUuidCommitIdTool(String repoUuid, String commitId, String tool) {
        IssueAnalyzer issueAnalyzer = issueAnalyzerMapper.getIssueAnalyzeResultByRepoUuidCommitIdTool(repoUuid, commitId, tool);
        if (issueAnalyzer != null && issueAnalyzer.getInvokeResult() == 1) {
            return issueAnalyzer.getAnalyzeResult();
        }
        return null;
    }

}
