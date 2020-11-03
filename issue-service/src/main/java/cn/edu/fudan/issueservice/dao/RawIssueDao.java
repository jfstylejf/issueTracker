package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.mapper.RawIssueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

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

    public void deleteRawIssueByRepoIdAndTool(String repoId,String tool) {
        rawIssueMapper.deleteRawIssueByRepoIdAndTool(repoId,tool);
    }

    public void deleteRawIssueByIds(List<String> rawIssueIds) {
        if(rawIssueIds == null || rawIssueIds.isEmpty ()){
            return;
        }
        rawIssueMapper.deleteRawIssueByIds(rawIssueIds);
    }

    public void batchUpdateIssueId(List<RawIssue> list) {
        rawIssueMapper.batchUpdateIssueId(list);
    }

    public Integer getIssueCountBeforeSpecificTime(String account_id, String specificTime) {
        return rawIssueMapper.getIssueCountBeforeSpecificTime(account_id, specificTime);
    }

    public List<RawIssue> getRawIssueByCommitIDAndTool(String repo_id,String tool,String commit_id) {
        return rawIssueMapper.getRawIssueByCommitIDAndTool(repo_id,tool, commit_id);
    }

    public List<RawIssue> getRawIssueByIssueId(String issueId) {
        return rawIssueMapper.getRawIssueByIssueId(issueId);
    }

    public List<String> getTypesByCommit(String tool,String commit_id){
        return rawIssueMapper.getTypesByCommit(tool, commit_id);
    }

    public List<RawIssue> getRawIssueByCommitIDAndFile(String repo_id,String commit_id,String tool,String file){
        return rawIssueMapper.getRawIssueByCommitIDAndFile(repo_id,commit_id, tool, file);
    }

    public Integer getNumberOfRemainingIssue(String repoId, String commit) {
        return rawIssueMapper.getNumberOfRemainingIssue(repoId, commit);
    }

    public Integer getNumberOfRemainingIssueBaseFile(String repoId, String commit, String fileName) {
        return rawIssueMapper.getNumberOfRemainingIssueBaseFile(repoId, commit, fileName);
    }

    public List<WeakHashMap<String,String>> getRankOfFileBaseIssueQuantity(String repoId, String commitId) {
        return rawIssueMapper.getRankOfFileBaseIssueQuantity(repoId, commitId);
    }

    public List<WeakHashMap<String,String>> getRankOfFileBaseDensity(String repoId, String commitId) {
        return rawIssueMapper.getRankOfFileBaseDensity(repoId, commitId);
    }

    public Map<String, Integer> getRepoAndIssueNum(Map repoCommit) {
        return null;
    }

    public int getNumberOfRemainingIssueBasePackage(String repoId, String commit, String packageName) {
        return rawIssueMapper.getNumberOfRemainingIssueBasePackage(repoId, commit, packageName);
    }

    public int getNumberOfRawIssuesByIssueId(String issueId) {
        return rawIssueMapper.getNumberOfRawIssuesByIssueId(issueId);
    }

    public List<RawIssue> getRawIssueListByIssueId(Map<String, Object> map) {
        return rawIssueMapper.getRawIssueListByIssueId(map);
    }

    public List<String> getRawIssueIdByCommitId(String repoId, String commit, String tool){
        return rawIssueMapper.getRawIssueIdByCommitId(repoId,commit,tool);
    }

    public String getLatestScannedCommitId(String repoId, String tool){
        return rawIssueMapper.getLatestScannedCommitId(repoId,tool);
    }

    /**
     * 根据issue uuid 获取 location发生变化的rawIssue列表
     * @param issueId
     * @return
     */
    public List<RawIssue> getChangedRawIssues(String issueId) {
        return rawIssueMapper.getChangedRawIssues(issueId);
    }


    /**
     * 获取rawIssue 表中指定commit的前一条commit id
     * @param repoId
     * @param tool
     * @param currentCommitId
     * @return
     */
    public String getPreCommitIdByCurrentCommitId(String repoId,String tool,String currentCommitId){
        String commitTime = rawIssueMapper.getRawIssueCommitTimeByRepoIdAndTool(repoId,currentCommitId,tool);
        return rawIssueMapper.getCommitIdWhichBeforeDesignatedTime(repoId,commitTime,tool);
    }

    public int getNumberOfRawIssuesByIssueIdAndStatus(String issueId,List status) {
        return rawIssueMapper.getNumberOfRawIssuesByIssueIdAndStatus(issueId,status);
    }

    public List<RawIssue> getRawIssueByRepoIdAndTool(String repoId,String tool) {
        return rawIssueMapper.getRawIssueByRepoIdAndTool(repoId,tool);
    }

    public List<Map<String, Object>> getRawIssuesByCondition(String developer, String repoId, String since, String until, String tool, String status, String type){
        return rawIssueMapper.getRawIssuesByCondition(developer, repoId, since, until, tool, status, type);
    }

    public List<Map<String, Object>> getIssueIdAndGroupCountFromRawIssue(String developer, String repoId, String since, String until, String tool, String status){
        return rawIssueMapper.getIssueIdAndGroupCountFromRawIssue(developer, repoId, since, until, tool, status);
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

    public List<RawIssue> getRawIssueByRepoIdFileNameTool(String repoId, List<String> preFiles, String toolName) {
        return new ArrayList<>(0);
    }
}
