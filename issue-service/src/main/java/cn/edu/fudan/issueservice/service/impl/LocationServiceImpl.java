package cn.edu.fudan.issueservice.service.impl;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.dao.*;
import cn.edu.fudan.issueservice.domain.dbo.IssueScan;
import cn.edu.fudan.issueservice.service.LocationService;
import cn.edu.fudan.issueservice.util.JGitHelper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

/**
 * @author Beethoven
 */
@Service
public class LocationServiceImpl implements LocationService {

    private RawIssueMatchInfoDao rawIssueMatchInfoDao;

    private LocationDao locationDao;

    private RawIssueDao rawIssueDao;

    private CommitDao commitDao;

    private IssueScanDao issueScanDao;

    private RestInterfaceManager restInterfaceManager;

    @Override
    public JSONObject getMethodTraceHistory(String metaUuid, String token) {

        JSONObject methodTraceHistory = restInterfaceManager.getMethodTraceHistory(metaUuid, token);
        JSONArray commitInfoList = methodTraceHistory.getJSONArray("commitInfoList");

        Iterator<Object> iterator = commitInfoList.iterator();
        while (iterator.hasNext()) {
            JSONObject commitInfo = (JSONObject) iterator.next();
            List<String> rawIssueUuids = locationDao.getRawIssueUuidsByMethodName(commitInfo.getString("signature"), commitInfo.getString("filePath"));
            rawIssueUuids.removeIf(rawIssueUuid -> !commitInfo.getString("commitId").equals(rawIssueDao.getCommitByRawIssueUuid(rawIssueUuid)));
            if (rawIssueUuids.isEmpty()) {
                iterator.remove();
            } else {
                commitInfo.put("issueCountInMethod", rawIssueUuids.size());
            }
        }

        return methodTraceHistory;
    }

    @Override
    public Integer getIssueCountsInMethod(String methodName, String filePath, String repoUuid, String tool) {
        //get latest scan commit
        IssueScan latestIssueScan = issueScanDao.getLatestIssueScanByRepoIdAndTool(repoUuid, tool);
        if (latestIssueScan == null) {
            return 0;
        }
        String commit = latestIssueScan.getCommitId();
        //get all pre commits
        String repoPath = restInterfaceManager.getRepoPath(repoUuid);
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        List<String> parentCommits = commitDao.getParentCommits(repoUuid, commit, jGitHelper);
        //get all issues
        List<String> issueUuids = rawIssueMatchInfoDao.getIssueUuidsByCommits(parentCommits);
        //get latestVersion rawIssue uuids
        List<String> latestVersionRawIssueUuids = rawIssueDao.getLatestVersionRawIssueUuids(issueUuids);
        //get distinct rawIssue uuids by method name
        List<String> rawIssueUuids = locationDao.getRawIssueUuidsByMethodName(methodName, filePath);

        return (int) rawIssueUuids.stream().filter(latestVersionRawIssueUuids::contains).count();
    }

    @Autowired
    public void setRawIssueMatchInfoDao(RawIssueMatchInfoDao rawIssueMatchInfoDao) {
        this.rawIssueMatchInfoDao = rawIssueMatchInfoDao;
    }

    @Autowired
    public void setRawIssueDao(RawIssueDao rawIssueDao) {
        this.rawIssueDao = rawIssueDao;
    }

    @Autowired
    public void setLocationDao(LocationDao locationDao) {
        this.locationDao = locationDao;
    }

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    @Autowired
    public void setCommitDao(CommitDao commitDao) {
        this.commitDao = commitDao;
    }

    @Autowired
    public void setIssueScanDao(IssueScanDao issueScanDao) {
        this.issueScanDao = issueScanDao;
    }
}
