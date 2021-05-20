package cn.edu.fudan.issueservice.service.impl;

import cn.edu.fudan.issueservice.dao.CommitDao;
import cn.edu.fudan.issueservice.dao.LocationDao;
import cn.edu.fudan.issueservice.dao.RawIssueDao;
import cn.edu.fudan.issueservice.dao.RawIssueMatchInfoDao;
import cn.edu.fudan.issueservice.domain.dbo.RawIssueMatchInfo;
import cn.edu.fudan.issueservice.service.RawIssueService;
import cn.edu.fudan.issueservice.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;

/**
 * @author WZY
 * @version 1.0
 **/
@Slf4j
@Service
public class RawIssueServiceImpl implements RawIssueService {

    private CommitDao commitDao;

    private RawIssueDao rawIssueDao;

    private LocationDao locationDao;

    private RawIssueMatchInfoDao rawIssueMatchInfoDao;

    private static final String CUR_RAW_ISSUE_UUID = "curRawIssueUuid";
    private static final String STATUS = "status";
    private static final String UUID = "uuid";
    private static final String COMMIT_TIME = "commitTime";

    @Override
    public List<Map<String, Object>> getRawIssueByIssueUuid(String issueUuid) {

        List<Map<String, String>> rawIssueMatchInfos = rawIssueMatchInfoDao.getMatchInfoByIssueUuid(issueUuid);
        Map<String, String> rawIssueStatus = new HashMap<>(32);
        List<String> rawIssuesUuid = new ArrayList<>();

        //change or add
        rawIssueMatchInfos.stream()
                .filter(rawIssueMatchInfo -> !RawIssueMatchInfo.EMPTY.equals(rawIssueMatchInfo.get(CUR_RAW_ISSUE_UUID)))
                .forEach(rawIssueMatchInfo -> {
                    rawIssuesUuid.add(rawIssueMatchInfo.get(CUR_RAW_ISSUE_UUID));
                    rawIssueStatus.put(rawIssueMatchInfo.get(CUR_RAW_ISSUE_UUID), rawIssueMatchInfo.get(STATUS));
                });
        //get change or add rawIssues' detail
        List<Map<String, Object>> rawIssueList = rawIssueDao.getRawIssueByUuids(rawIssuesUuid);
        //get locations
        rawIssueList.forEach(rawIssue -> {
            rawIssue.put("message", commitDao.getCommitMessageByCommitIdAndRepoUuid((String) rawIssue.get("commitId"), (String) rawIssue.get("repoUuid")));
            rawIssue.put("location", locationDao.getLocationsByRawIssueUuid((String) rawIssue.get(UUID)));
        });
        //add rawIssues to result
        List<Map<String, Object>> result = new ArrayList<>(rawIssueList);
        //add status to result
        result.forEach(rawIssue -> {
            String uuid = (String) rawIssue.get(UUID);
            rawIssue.put(STATUS, rawIssueStatus.get(uuid));
        });
        result.sort((o1, o2) -> (int) o1.get("version") - (int) o2.get("version"));
        //get repo uuid
        String repoUuid = result.isEmpty() ? "" : (String) result.get(0).get("repoUuid");
        //solved or merge solved
        rawIssueMatchInfos.stream()
                .filter(rawIssueMatchInfo -> RawIssueMatchInfo.EMPTY.equals(rawIssueMatchInfo.get(CUR_RAW_ISSUE_UUID)))
                .forEach(rawIssueMatchInfo -> result.add(new HashMap<String, Object>(32) {{
                    put("repoUuid", repoUuid);
                    put("commitId", rawIssueMatchInfo.get("curCommitId"));
                    put(STATUS, rawIssueMatchInfo.get(STATUS));
                    put("location", new ArrayList<>());
                    put("fileName", "");
                    try {
                        put(COMMIT_TIME, DateTimeUtil.parse(commitDao.getCommitTimeByCommitId(rawIssueMatchInfo.get("curCommitId"), repoUuid)));
                    } catch (ParseException e) {
                        put(COMMIT_TIME, "check the database, parse commit time error");
                        e.printStackTrace();
                    }
                }}));
        result.sort((o1, o2) -> {
            Date commitTime1 = (Date) o1.get(COMMIT_TIME);
            Date commitTime2 = (Date) o2.get(COMMIT_TIME);
            return commitTime1.compareTo(commitTime2);
        });
        return result;
    }

    @Autowired
    public void setLocationDao(LocationDao locationDao) {
        this.locationDao = locationDao;
    }

    @Autowired
    public void setRawIssueDao(RawIssueDao rawIssueDao) {
        this.rawIssueDao = rawIssueDao;
    }

    @Autowired
    public void setRawIssueMatchInfoDao(RawIssueMatchInfoDao rawIssueMatchInfoDao) {
        this.rawIssueMatchInfoDao = rawIssueMatchInfoDao;
    }

    @Autowired
    public void setCommitDao(CommitDao commitDao) {
        this.commitDao = commitDao;
    }
}
