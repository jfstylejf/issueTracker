package cn.edu.fudan.issueservice.service.impl;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.dao.*;
import cn.edu.fudan.issueservice.domain.dbo.Issue;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.domain.enums.*;
import cn.edu.fudan.issueservice.service.IssueMeasureInfoService;
import cn.edu.fudan.issueservice.util.DateTimeUtil;
import cn.edu.fudan.issueservice.util.SegmentationUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description:
 * @author fancying
 * create: 2019-04-02 15:27
 **/
@Slf4j
@Service
public class IssueMeasureInfoServiceImpl implements IssueMeasureInfoService {

    private RawIssueDao rawIssueDao;

    private IssueDao issueDao;

    private RestInterfaceManager restInterfaceManager;


    @Override
    public List<Map.Entry<String, JSONObject>> getNotSolvedIssueCountByToolAndRepoUuid(List<String> repoUuids, String tool, String order, String commitUuid) {

        Map<String, JSONObject> result = new HashMap<>(32);

        List<RawIssue> rawIssues = commitUuid == null ? new ArrayList<>() : rawIssueDao.getRawIssueByRepoList(repoUuids, tool, commitUuid);

        List<Issue> issues = commitUuid != null ? new ArrayList<>() : issueDao.getNotSolvedIssueAllListByToolAndRepoId(repoUuids, tool);

        String total = "Total";

        rawIssues.forEach(rawIssue -> issues.add(issueDao.getIssueByID(rawIssue.getIssue_id())));

        issues.forEach(issue -> {
            JSONObject issueType = result.getOrDefault(issue.getType(), new JSONObject() {{
                put(total, 0);
                put(IgnoreTypeEnum.DEFAULT.getName(), 0);
                put(IgnoreTypeEnum.IGNORE.getName(), 0);
                put(IgnoreTypeEnum.MISINFORMATION.getName(), 0);
                put(IgnoreTypeEnum.TO_REVIEW.getName(), 0);
            }});
            issueType.put(total, issueType.getInteger(total) + 1);
            issueType.put(issue.getManual_status(), issueType.getInteger(issue.getManual_status()) + 1);
            result.put(issue.getType(), issueType);
        });

        List<Map.Entry<String, JSONObject>> issueTypeList = new ArrayList<>(result.entrySet());

        issueTypeList.sort((o1, o2) -> o2.getValue().getInteger(order) - o1.getValue().getInteger(order));

        return issueTypeList;
    }

    @Override
    public Map<String,Object> getDayAvgSolvedIssue(Map<String, Object> query) {

        String developer = query.get("developer").toString();

        JSONObject developerDetail = getDeveloperCodeQuality(query).get(developer);

        double days = (DateTimeUtil.stringToLocalDate(query.get("until").toString()).toEpochDay() - DateTimeUtil.stringToLocalDate(query.get("since").toString()).toEpochDay()) * 5.0 / 7;

        return new HashMap<String, Object>(6){{
            put("solvedIssuesCount", developerDetail.getInteger("solvedIssueCount"));
            put("days", days);
            put("dayAvgSolvedIssue", developerDetail.getInteger("solvedIssueCount") / days);
        }};
    }


    @Override
    public Map<String, JSONObject> getDeveloperCodeQuality(Map<String, Object> query) {

        Map<String, Integer> developerWorkload = restInterfaceManager.getDeveloperWorkload(query);

        Map<String, JSONObject> developersDetail = new HashMap<>(32);

        AtomicInteger loc = new AtomicInteger();
        AtomicInteger allAddedIssueCount = new AtomicInteger();
        AtomicInteger allSolvedIssueCount = new AtomicInteger();

        query.put("repoList", SegmentationUtil.splitStringList(query.get("repoList") == null ? null : query.get("repoList").toString()));
        developerWorkload.keySet().forEach(r -> {
            query.put("solver", null);
            query.put("developer", r);
            int developerAddIssueCount = issueDao.getIssueFilterListCount(query);

            query.put("developer", null);
            query.put("solver", r);
            int developerSolvedIssueCount = issueDao.getSolvedIssueFilterListCount(query);

            loc.addAndGet(developerWorkload.get(r));
            allAddedIssueCount.addAndGet(developerAddIssueCount);
            allSolvedIssueCount.addAndGet(developerSolvedIssueCount);

            developersDetail.put(r, new JSONObject(){{
            put("addedIssueCount", developerAddIssueCount);
            put("solvedIssueCount", developerSolvedIssueCount);
            put("loc", developerWorkload.get(r));
            put("addQuality", developerWorkload.get(r) == 0 ? 0 : developerAddIssueCount * 100.0 / developerWorkload.get(r));
            put("solveQuality", developerWorkload.get(r) == 0 ? 0 : developerSolvedIssueCount * 100.0 / developerWorkload.get(r));
            }});
        });

        developersDetail.put("all",  new JSONObject(){{
            put("loc", loc);
            put("allAddedIssueCount", allAddedIssueCount);
            put("allSolvedIssueCount", allSolvedIssueCount);
            put("E/L", loc.intValue() == 0 ? 0 : allSolvedIssueCount.intValue() * 100.0 / loc.intValue());
            put("N/L", loc.intValue() == 0 ? 0 : allAddedIssueCount.intValue() * 100.0 / loc.intValue());
        }});

        return developersDetail;
    }

    @Override
    public JSONObject getIssuesLifeCycle(String status, String target, Map<String, Object> query) {
        switch (target + "-" + status){
            case "self-self_solved":
                return handleIssuesLifeCycle(issueDao.getSelfIntroduceSelfSolvedIssueInfo(query));
            case "other-self_solved":
                return handleIssuesLifeCycle(issueDao.getOtherIntroduceSelfSolvedIssueInfo(query));
            case "self-living":
                return handleIssuesLifeCycle(issueDao.getSelfIntroduceLivingIssueInfo(query));
            case "self-other_solved":
                return handleIssuesLifeCycle(issueDao.getSelfIntroduceOtherSolvedIssueInfo(query));
            default:
                return null;
        }
    }

    private JSONObject handleIssuesLifeCycle(List<Integer> issuesLifeCycle) {
        if(issuesLifeCycle == null){
            return new JSONObject(){{
                put("quantity", 0);
                put("min", 0);
                put("max", 0);
                put("mid", 0);
            }};
        }
        issuesLifeCycle.sort(Comparator.comparingInt(o -> o));
        return new JSONObject(){{
            put("quantity", issuesLifeCycle.size());
            put("min", issuesLifeCycle.get(0));
            put("max", issuesLifeCycle.get(issuesLifeCycle.size() - 1));
            put("mid", issuesLifeCycle.get(issuesLifeCycle.size() / 2));
        }};
    }

    @Override
    public List<JSONObject> getLifeCycleDetail(String status, String target, Map<String, Object> query, String token) {
        switch (target + "-" + status){
            case "self-self_solved":
                return handleLifeCycleDetail(issueDao.getSelfIntroduceSelfSolvedIssueDetail(query), token);
            case "other-self_solved":
                return handleLifeCycleDetail(issueDao.getOtherIntroduceSelfSolvedIssueDetail(query), token);
            case "self-living":
                return handleLifeCycleDetail(issueDao.getSelfIntroduceLivingIssueDetail(query), token);
            case "self-other_solved":
                return handleLifeCycleDetail(issueDao.getSelfIntroduceOtherSolvedIssueDetail(query), token);
            default:
                return null;
        }
    }

    private List<JSONObject> handleLifeCycleDetail(List<JSONObject> issuesDetail, String token) {
        if(issuesDetail == null){
            return null;
        }
        //init repoUuid to repoName
        Map<String, String> repoName = restInterfaceManager.getAllRepoToRepoName(token);
        //handle issues detail
        issuesDetail.forEach(r -> {
            r.put("projectName", repoName.get(r.getString("repoUuid")));
            r.put("severity", Objects.requireNonNull(IssuePriorityEnum.getPriorityEnumByRank(r.getInteger("priority"))).getName());
        });

        return issuesDetail;
    }

    @Override
    @CacheEvict(cacheNames = {"issueLifeCycleCount","developerCodeQuality"}, allEntries=true, beforeInvocation = true)
    public void clearCache() {
        log.info("Successfully clear redis cache:issueLifeCycleCount,developerCodeQuality in db1.");
    }

    @Autowired
    public void setRawIssueDao(RawIssueDao rawIssueDao) {
        this.rawIssueDao = rawIssueDao;
    }

    @Autowired
    public void setIssueDao(IssueDao issueDao) {
        this.issueDao = issueDao;
    }

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }
}