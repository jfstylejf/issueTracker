package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.mapper.RawIssueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author WZY
 * @version 1.0
 **/
@Repository
public class RawIssueDao {

    private RawIssueMapper rawIssueMapper;

    @Autowired
    public void setRawIssueMapper(RawIssueMapper rawIssueMapper) {
        this.rawIssueMapper = rawIssueMapper;
    }

    public void insertRawIssueList(List<RawIssue> list) {
        rawIssueMapper.insertRawIssueList(list);
    }

    public void deleteRawIssueByIds(List<String> rawIssueIds) {
        if(rawIssueIds == null || rawIssueIds.isEmpty ()){
            return;
        }
        rawIssueMapper.deleteRawIssueByIds(rawIssueIds);
    }

    public List<RawIssue> getRawIssueByCommitIDAndTool(String repoUuid,String tool,String commitId) {
        return rawIssueMapper.getRawIssueByCommitIDAndTool(repoUuid,tool, commitId);
    }

    public List<RawIssue> getRawIssueByRepoList(List<String> repoUuids,String tool,String commitId) {
        return rawIssueMapper.getRawIssueByRepoList(repoUuids,tool, commitId);
    }

    public List<Map<String, Object>> getRawIssueByIssueId(String issueId) {
        return rawIssueMapper.getRawIssueByIssueId(issueId);
    }

    public List<RawIssue> getRawIssueListByIssueId(Map<String, Object> map) {
        return rawIssueMapper.getRawIssueListByIssueId(map);
    }

    public int getNumberOfRawIssuesByIssueIdAndStatus(String issueId, List<String> status) {
        return rawIssueMapper.getNumberOfRawIssuesByIssueIdAndStatus(issueId,status);
    }

    public List<RawIssue> getRawIssueByRepoIdAndTool(String repoId,String tool) {
        return rawIssueMapper.getRawIssueByRepoIdAndTool(repoId,tool);
    }

    public String getAdderOfOneIssue(String issueId){
        return rawIssueMapper.getAdderOfOneIssue(issueId);
    }

    public String getLastSolverOfOneIssue(String issueId){
        return rawIssueMapper.getLastSolverOfOneIssue(issueId);
    }

    public Map<String, Object> getLastSolvedInfoOfOneIssue(String issueId){
        return rawIssueMapper.getLastSolvedInfoOfOneIssue(issueId);
    }

    public List<String> getIssueIntroducers(List<String> repoUuids) {
        return rawIssueMapper.getIssueIntroducers(repoUuids);
    }

    public String getRawIssueUuidByIssueUuidAndCommit(String issueUuid, String commit){
        return rawIssueMapper.getRawIssueUuidByIssueUuidAndCommit(issueUuid, commit);
    }
}
