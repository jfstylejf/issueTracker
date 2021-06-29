package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.domain.dbo.Issue;
import cn.edu.fudan.issueservice.domain.dbo.Location;
import cn.edu.fudan.issueservice.domain.vo.DeveloperLivingIssueVO;
import cn.edu.fudan.issueservice.mapper.*;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * @author beethoven
 */
@Repository
public class IssueDao {

    private IssueMapper issueMapper;
    private RawIssueMapper rawIssueMapper;

    @Autowired
    public void setRawIssueMapper(RawIssueMapper rawIssueMapper) {
        this.rawIssueMapper = rawIssueMapper;
    }

    @Autowired
    public void setIssueMapper(IssueMapper issueMapper) {
        this.issueMapper = issueMapper;
    }

    public void insertIssueList(List<Issue> list) {
        if (list.isEmpty()) {
            return;
        }
        issueMapper.insertIssueList(list);
    }

    public void deleteIssueByRepoIdAndTool(String repoId, String tool) {
        issueMapper.deleteIssueByRepoIdAndTool(repoId, tool);
    }

    public void batchUpdateIssue(List<Issue> issues) {
        issues.forEach(issue -> issueMapper.batchUpdateIssue(issue));
    }

    public List<String> getRepoWithIssues(String developer) {
        return issueMapper.getRepoWithIssues(developer);
    }

    public List<String> getExistIssueTypes(String tool) {
        return issueMapper.getExistIssueTypes(tool);
    }

    public void updateOneIssuePriority(String issueId, int priority) {
        issueMapper.updateOneIssuePriority(issueId, priority);
    }

    public List<Issue> getIssuesByUuid(List<String> issueIds) {
        return issueMapper.getIssuesByIds(issueIds);
    }

    public void updateOneIssueStatus(String issueId, String status, String manualStatus) {
        issueMapper.updateOneIssueStatus(issueId, status, manualStatus);
    }

    public List<Issue> getNotSolvedIssueAllListByToolAndRepoId(List<String> repoUuids, String tool) {
        return issueMapper.getNotSolvedIssueAllListByToolAndRepoId(repoUuids, tool);
    }

    public int getIssueFilterListCount(Map<String, Object> query) {
        return issueMapper.getIssueFilterListCount(query);
    }

    public int getSolvedIssueFilterListCount(Map<String, Object> query) {
        return issueMapper.getSolvedIssueFilterListCount(query);
    }

    public void updateIssueManualStatus(String repoUuid, String issueUuid, String manualStatus, String issueType, String tool, String currentTime) {
        issueMapper.updateIssueManualStatus(repoUuid, issueUuid, manualStatus, issueType, tool, currentTime);
    }

    public List<Integer> getSelfIntroduceSelfSolvedIssueInfo(Map<String, Object> query) {
        return issueMapper.getSelfIntroduceSelfSolvedIssueInfo(query);
    }

    public List<Integer> getOtherIntroduceSelfSolvedIssueInfo(Map<String, Object> query) {
        return issueMapper.getOtherIntroduceSelfSolvedIssueInfo(query);
    }

    public List<Integer> getSelfIntroduceLivingIssueInfo(Map<String, Object> query) {
        return issueMapper.getSelfIntroduceLivingIssueInfo(query);
    }

    public List<Integer> getSelfIntroduceOtherSolvedIssueInfo(Map<String, Object> query) {
        return issueMapper.getSelfIntroduceOtherSolvedIssueInfo(query);
    }

    public List<JSONObject> getSelfIntroduceSelfSolvedIssueDetail(Map<String, Object> query) {
        return issueMapper.getSelfIntroduceSelfSolvedIssueDetail(query);
    }

    public List<JSONObject> getOtherIntroduceSelfSolvedIssueDetail(Map<String, Object> query) {
        return issueMapper.getOtherIntroduceSelfSolvedIssueDetail(query);
    }

    public List<JSONObject> getSelfIntroduceLivingIssueDetail(Map<String, Object> query) {
        return issueMapper.getSelfIntroduceLivingIssueDetail(query);
    }

    public List<JSONObject> getSelfIntroduceOtherSolvedIssueDetail(Map<String, Object> query) {
        return issueMapper.getSelfIntroduceOtherSolvedIssueDetail(query);
    }

    public List<String> getIssueIntroducers(List<String> repoUuids) {
        return issueMapper.getIssueIntroducers(repoUuids);
    }

    public int getRemainingIssueCount(String repoUuid) {
        return issueMapper.getRemainingIssueCount(repoUuid);
    }

    public List<JSONObject> getSelfIntroduceLivingIssueCount(Map<String, Object> query) {
        return issueMapper.getSelfIntroduceLivingIssueCount(query);
    }

    public List<Issue> getIssueCountByIntroducerAndTool(String developer) {
        return issueMapper.getIssueCountByIntroducerAndTool(developer);
    }

    public List<Map<String, Object>> getIssuesOverview(Map<String, Object> query) {
        return issueMapper.getIssuesOverview(query);
    }

    public List<Map<String, Object>> getIssueCountByCategoryAndType(Map<String, Object> query) {
        return issueMapper.getIssueCountByCategoryAndType(query);
    }

    public Map<String, Object> getLivingIssueTendency(String until, String projectId, String showDetail) {
        String date = until.split(" ")[0];
        Map<String, Object> map = issueMapper.getLivingIssueTendency(until, projectId);
        map.put("date", date);
        if (Boolean.TRUE.toString().equals(showDetail)) {
            List<Map<String, Object>> detail = issueMapper.getLivingIssueTendencyDetail(until, projectId);
            map.put("detail", detail);
        }
        return map;
    }

    public List<Map<String, Object>> getIssueFilterList(Map<String, Object> query) {
        return issueMapper.getIssueFilterList(query);
    }

    public List<Map<String, Object>> getSolvedIssueFilterList(Map<String, Object> query) {
        return issueMapper.getSolvedIssueFilterList(query);
    }

    public List<String> getIssuesByFilesToolAndRepo(List<String> preFiles, String repoId, String toolName) {
        if (preFiles.isEmpty()) {
            return new ArrayList<>();
        }
        return issueMapper.getIssuesByFilesToolAndRepo(preFiles, repoId, toolName);
    }

    public void updateIssuesForIgnore(List<String> ignoreFiles, String repoUuid) {
        if (ignoreFiles.isEmpty()) {
            return;
        }
        issueMapper.updateIssuesForIgnore(ignoreFiles, repoUuid);
    }

    public boolean checkDeleteSuccess(String repoUuid, String tool) {
        int issueCount = issueMapper.getIssueCount(repoUuid, tool);
        int rawIssueCount = rawIssueMapper.getRawIssueCount(repoUuid, tool);
        return issueCount == 0 && rawIssueCount == 0;
    }

    public List<Map<String, Object>> getDeveloperListLivingIssue(String since, String until, String repoUuid, List<String> developers) {
        return issueMapper.getDeveloperListLivingIssue(since, until, repoUuid, developers);
    }
}
