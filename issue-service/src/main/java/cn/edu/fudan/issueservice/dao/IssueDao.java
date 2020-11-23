package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.domain.dbo.Issue;
import cn.edu.fudan.issueservice.mapper.IssueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;


@Repository
public class IssueDao {

    private IssueMapper issueMapper;

    @Autowired
    public void setIssueMapper(IssueMapper issueMapper) {
        this.issueMapper = issueMapper;
    }

    public void insertIssueList(List<Issue> list) {
        issueMapper.insertIssueList(list);
    }

    public void deleteIssueByRepoIdAndTool(String repoId,String tool) {
        issueMapper.deleteIssueByRepoIdAndTool(repoId,tool);
    }

    public void batchUpdateIssue(List<Issue> list) {
        issueMapper.batchUpdateIssue(list);
    }


    public Issue getIssueByID(String uuid) {
        return issueMapper.getIssueByID(uuid);
    }

    public List<String> getRepoWithIssues(String developer) {
        return issueMapper.getRepoWithIssues(developer);
    }

    public List<String> getExistIssueTypes(String tool) {
        return issueMapper.getExistIssueTypes(tool);
    }

    public void updateOneIssuePriority(String issueId, int priority) {
        issueMapper.updateOneIssuePriority(issueId,priority);
    }

    public void updateOneIssueStatus(String issueId,String status, String manualStatus) {
        issueMapper.updateOneIssueStatus(issueId,status,manualStatus);
    }

    public Integer getMaxIssueDisplayId(String repoId) {
        return issueMapper.getMaxIssueDisplayId(repoId);
    }

    public List<Issue> getNotSolvedIssueAllListByToolAndRepoId(String repoId, String tool) {
        return issueMapper.getNotSolvedIssueAllListByToolAndRepoId(repoId, tool);
    }

    public List<Issue> getIssueByRepoIdAndToolAndStatusListAndTypeList(String repoId, String tool,
                                                                           List<String> statusList) {
        return issueMapper.getIssueByRepoIdAndToolAndStatusList(repoId, tool, statusList);
    }

    public List<Issue> getIssuesByUuids(List<String> issueIds){
        return issueMapper.getIssuesByIds(issueIds);
    }

    public List<Map<String, Object>> getSolvedIssueLifeCycle(List<String> repoIdList, String type, String tool, String since, String until,
                                                       String developer, String status){
        return issueMapper.getSolvedIssueLifeCycle(repoIdList,type,tool,since,until,developer,status);
    }

    public List<Map<String, Object>> getSolvedIssueLifeCycleByOtherSolved(List<String> repoIdList,String type,String tool,String since,String until,
                                                       String developer,String status){
        return issueMapper.getSolvedIssueLifeCycleByOtherSolved(repoIdList,type,tool,since,until,developer,status);
    }

    public List<Map<String, Object>> getOpenIssueLifeCycle(List<String> repoIdList,String type,String tool,String since,String until,
                                                                          String developer,String rawIssueStatus,String issueStatus){
        return issueMapper.getOpenIssueLifeCycle(repoIdList,type,tool,since,until,developer,rawIssueStatus,issueStatus);
    }

    public List<Map<String, Object>> getIssueFilterList(Map<String, Object> query) {
        return issueMapper.getIssueFilterList(query);
    }

    public int getIssueFilterListCount(Map<String, Object> query) {
        return issueMapper.getIssueFilterListCount(query);
    }

    public int getSolvedIssueFilterListCount(Map<String, Object> query) {
        return issueMapper.getSolvedIssueFilterListCount(query);
    }

    public List<Map<String, Object>> getSolvedIssueFilterList(Map<String, Object> query) {
        return issueMapper.getSolvedIssueFilterList(query);
    }

    public void updateIssueManualStatus(String repoUuid, String issueUuid, String manualStatus, String issueType, String tool, String currentTime) {
        issueMapper.updateIssueManualStatus(repoUuid, issueUuid, manualStatus, issueType, tool, currentTime);
    }

}
