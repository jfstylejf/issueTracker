package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.domain.dbo.Issue;
import cn.edu.fudan.issueservice.mapper.IssueMapper;
import cn.edu.fudan.issueservice.mapper.RawIssueMapper;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;


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

    public Integer getIssueCount(Map<String, Object> map) {
        int result = 0;
        List<Map<String, Object>> countMapList = issueMapper.getIssueCount(map);
        if(countMapList != null){
            for(Map<String, Object> countMap : countMapList){
                Long value= (Long) countMap.get("value");
                result += value.intValue ();
            }
        }

        return result;
    }

    public Integer getIssuesCount(Map<String, Object> map) {
        int result = 0;
        List<Map<String, Object>> countMapList = issueMapper.getIssuesCount(map);
        if(countMapList != null){
            for(Map<String, Object> countMap : countMapList){
                Long value= (Long) countMap.get("value");
                result += value.intValue ();
            }
        }

        return result;
    }

    public Map<String, Integer> getIssueCountWithCategory(Map<String, Object> map) {
        Map<String, Integer> result = new HashMap<> ();
        List<Map<String, Object>> countMapList = issueMapper.getIssueCount(map);
        if(countMapList != null){
            for(Map<String, Object> countMap : countMapList){
                String key = (String)countMap.get ("key");
                Long value =  (Long)countMap.get("value");
                result.put (key, value.intValue ());
            }
        }
        return result;
    }

    public List<String> getRepoWithIssues(String developer) {
        return issueMapper.getRepoWithIssues(developer);
    }

    public List<Issue> getIssueList(Map<String, Object> map) {
        return issueMapper.getIssueList(map);
    }

    public List<Map<String, Object>> getIssueWithAdder(Map<String, Object> map) {
        return issueMapper.getIssueWithAdder(map);
    }

    public List<String> getExistIssueTypes(String tool) {
        return issueMapper.getExistIssueTypes(tool);
    }

    public List<String> getIssueIdsByRepoIdAndTool(String repoId,String tool) {
        return issueMapper.getIssueIdsByRepoIdAndTool(repoId,tool);
    }

    public List<Issue> getIssuesByEndCommit(String repoId,String tool, String commitId) {
        return issueMapper.getIssuesByEndCommit(repoId,tool,commitId);
    }

    public int getIgnoredCountInMappedIssues(String ignoreId, List<String> list){
        return issueMapper.getIgnoredCountInMappedIssues(ignoreId, list);
    }

    public int getSpecificIssueCount(Map<String, Object> map){
        return issueMapper.getSpecificIssueCount(map);
    }

    public List<Issue> getSpecificIssues(Map<String, Object> map){
        return issueMapper.getSpecificIssues(map);
    }

    public Double getAvgEliminatedTime(List<String> list,String repo_id,String tool){
        return issueMapper.getAvgEliminatedTime(list, repo_id, tool);
    }

    public Long getMaxAliveTime(List<String> list, String repo_id, String tool){
        return issueMapper.getMaxAliveTime(list, repo_id, tool);
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

    public List<String> getNotSolvedIssueListByTypeAndRepoId(String repoId, String type) {
        return issueMapper.getNotSolvedIssueListByTypeAndRepoId(repoId, type);
    }

    public List<Issue> getNotSolvedIssueAllListByToolAndRepoId(String repoId, String tool) {
        return issueMapper.getNotSolvedIssueAllListByToolAndRepoId(repoId, tool);
    }

    public void batchUpdateIssueListPriority(List issueUuid, int priority) {
        issueMapper.batchUpdateIssueListPriority(issueUuid, priority);
    }

    public int getNumberOfNewIssueByDuration(String repoId, String start, String end) {
        return issueMapper.getNumberOfNewIssueByDuration(repoId, start, end);
    }

    public int getNumberOfEliminateIssueByDuration(String repoId, String start, String end) {
        return issueMapper.getNumberOfEliminateIssueByDuration(repoId, start, end);
    }

    public Map<String, Integer> getCommitNewIssue(String start, String end, String repoId) {
        Map<String, Integer> map = new ConcurrentHashMap<>();
        for (WeakHashMap<Object, Object> m : issueMapper.getCommitNewIssue(start, end, repoId)) {
            Long value = (Long)m.get("value");
            map.put((String) m.get("key"), value.intValue());
        }
        return map;
    }

    public List<Issue> getIssuesByIssueIds(List<String> issueIds) {
        return issueMapper.getIssuesByIssueIds(issueIds);
    }

    public List<String> getCommitId(String repoId, String since, String until) {
        return issueMapper.getCommitIds(repoId,since,until);
    }



    public List<Issue> getIssueByRepoIdAndToolAndStatusListAndTypeList(String repoId, String tool,
                                                                           List<String> statusList) {
        return issueMapper.getIssueByRepoIdAndToolAndStatusList(repoId, tool, statusList);
    }

    public List<Issue> getIssuesByUuids(List<String> issueIds){
        return issueMapper.getIssuesByIds(issueIds);
    }

    public List<Map<String, Object>> getIssueByRawIssueCommitViewIssueTable(List<String> repoIdList, String type, String tool, String since, String until,
                                                             String developer, String rawIssueStatus, String issueStatus){
        return issueMapper.getIssueByRawIssueCommitViewIssueTable(repoIdList,type,tool,since,until,developer,rawIssueStatus,issueStatus);
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


    public List<Map<String, Object>> getIssueStatisticByIssueIdList(List<String> issueIdList, String order, String asc){
        return issueMapper.getIssueStatisticByIssueIdList(issueIdList, order, asc);
    }

    public List<String> getAdderByIssue(List<String> repoList, String tool, String since, String until, String status) {
        return issueMapper.getAdderByIssue(repoList, tool, since, until, status);
    }

    public List<String> getSolvedIssue(List<String> repoList, String tool, String since, String until, String status) {
        return  issueMapper.getSolvedIssue(repoList, tool, since, until, status);
    }

    public List<Map<String, Object>> getIssueFilterList(Map<String, Object> query) {
        return issueMapper.getIssueFilterList(query);
    }

    public int getIssueFilterListCount(Map<String, Object> query) {
        return issueMapper.getIssueFilterListCount(query);
    }

    public void test(String uuid, String s) {
        issueMapper.test(uuid, s);
    }

    public List<String> getIssuetest() {
        return issueMapper.getIssuetest();
    }

    public List<Map<String, Object>> getIssuesByRawIssueCommitViewIssueTable(Map<String, Object> query) {
        return issueMapper.getIssueByRawIssuesCommitViewIssueTable(query);
    }

    public int getSolvedIssueFilterListCount(Map<String, Object> query) {
        return issueMapper.getSolvedIssueFilterListCount(query);
    }

    public List<Map<String, Object>> getSolvedIssueFilterList(Map<String, Object> query) {
        return issueMapper.getSolvedIssueFilterList(query);
    }
}
