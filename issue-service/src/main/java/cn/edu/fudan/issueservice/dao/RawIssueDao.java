package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.mapper.RawIssueMapper;
import cn.edu.fudan.issueservice.mapper.RawIssueMatchInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author WZY
 * @version 1.0
 **/
@Repository
public class RawIssueDao {

    private RawIssueMapper rawIssueMapper;

    private RawIssueMatchInfoMapper rawIssueMatchInfoMapper;

    @Autowired
    public void setRawIssueMapper(RawIssueMapper rawIssueMapper) {
        this.rawIssueMapper = rawIssueMapper;
    }

    @Autowired
    public void setRawIssueMatchInfoMapper(RawIssueMatchInfoMapper rawIssueMatchInfoMapper) {
        this.rawIssueMatchInfoMapper = rawIssueMatchInfoMapper;
    }

    public void insertRawIssueList(List<RawIssue> list) {
        if (list.isEmpty()) {
            return;
        }
        rawIssueMapper.insertRawIssueList(list);
    }

    public void deleteRawIssueByIds(List<String> rawIssueIds) {
        if (rawIssueIds == null || rawIssueIds.isEmpty()) {
            return;
        }
        rawIssueMapper.deleteRawIssueByIds(rawIssueIds);
    }

    public List<String> getRawIssueByRepoIdAndTool(String repoId, String tool) {
        return rawIssueMapper.getRawIssueByRepoIdAndTool(repoId, tool);
    }

    public String getCommitByRawIssueUuid(String rawIssueUuid) {
        return rawIssueMapper.getCommitByRawIssueUuid(rawIssueUuid);
    }

    public List<RawIssue> getLastVersionRawIssues(List<String> issueUuids) {
        if (issueUuids.isEmpty()) {
            return new ArrayList<>();
        }
        List<RawIssue> rawIssues = new ArrayList<>();
        issueUuids.forEach(issueUuid -> rawIssues.add(rawIssueMapper.getLastVersionRawIssue(issueUuid)));
        return rawIssues;
    }

    public List<Map<String, Object>> getRawIssueByUuids(List<String> rawIssuesUuid) {
        if (rawIssuesUuid.isEmpty()) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> rawIssues = rawIssueMapper.getRawIssueByUuids(rawIssuesUuid);
        return rawIssues == null ? new ArrayList<>() : rawIssues;
    }

    public List<String> getLatestVersionRawIssueUuids(List<String> issueUuids) {
        return rawIssueMapper.getLatestVersionRawIssueUuids(issueUuids);
    }
}
