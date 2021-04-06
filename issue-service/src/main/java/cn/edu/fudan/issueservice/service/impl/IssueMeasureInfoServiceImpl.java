package cn.edu.fudan.issueservice.service.impl;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.dao.IssueDao;
import cn.edu.fudan.issueservice.dao.ProjectDao;
import cn.edu.fudan.issueservice.dao.RawIssueDao;
import cn.edu.fudan.issueservice.domain.dbo.Issue;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.domain.enums.IgnoreTypeEnum;
import cn.edu.fudan.issueservice.domain.enums.JavaIssuePriorityEnum;
import cn.edu.fudan.issueservice.service.IssueMeasureInfoService;
import cn.edu.fudan.issueservice.util.DateTimeUtil;
import cn.edu.fudan.issueservice.util.PagedGridResult;
import cn.edu.fudan.issueservice.util.SegmentationUtil;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    private ProjectDao projectDao;

    private RestInterfaceManager restInterfaceManager;

    private final static String REPO_LIST = "repoList", DEVELOPER = "developer", QUANTITY = "quantity",  DEVELOPER_NAME = "developerName",
            ADD = "add", SOLVE = "solve", ISSUE_COUNT = "issueCount", LOC ="loc", LIVING_ISSUE_COUNT = "livingIssueCount", DATE_FORMAT = "yyyy-MM-dd";

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
    public Map<String,Object> getDayAvgSolvedIssue(Map<String, Object> query, String token) {
        Map<String, Object> developerCodeQuality = getDeveloperCodeQuality(query, false, token);
        JSONObject solvedDetail = (JSONObject) developerCodeQuality.get(SOLVE);

        double days = (DateTimeUtil.stringToLocalDate(query.get("until").toString()).toEpochDay() - DateTimeUtil.stringToLocalDate(query.get("since").toString()).toEpochDay()) * 5.0 / 7;

        return new HashMap<String, Object>(6){{
            put("solvedIssuesCount", solvedDetail.getInteger(ISSUE_COUNT));
            put("days", days);
            put("dayAvgSolvedIssue", solvedDetail.getInteger(ISSUE_COUNT) / days);
        }};
    }

    @Override
    public Map<String, Object> getDeveloperCodeQuality(Map<String, Object> query, Boolean needAll, String token) {

        Map<String, Integer> developerWorkload = restInterfaceManager.getDeveloperWorkload(query, token);

        Map<String, Object> developersDetail = new HashMap<>(32);

        AtomicInteger loc = new AtomicInteger();
        AtomicInteger allAddedIssueCount = new AtomicInteger();
        AtomicInteger allSolvedIssueCount = new AtomicInteger();

        if(query.get(REPO_LIST) instanceof String) {
            query.put(REPO_LIST, SegmentationUtil.splitStringList(query.get(REPO_LIST) == null ? null : query.get(REPO_LIST).toString()));
        }

        developerWorkload.keySet().forEach(r -> {
            query.put("solver", null);
            query.put(DEVELOPER, r);
            int developerAddIssueCount = issueDao.getIssueFilterListCount(query);

            query.put(DEVELOPER, null);
            query.put("solver", r);
            int developerSolvedIssueCount = issueDao.getSolvedIssueFilterListCount(query);

            loc.addAndGet(developerWorkload.get(r));
            allAddedIssueCount.addAndGet(developerAddIssueCount);
            allSolvedIssueCount.addAndGet(developerSolvedIssueCount);

            developersDetail.put(DEVELOPER_NAME, r);
            developersDetail.put(LOC, developerWorkload.get(r));
            developersDetail.put(ADD, new JSONObject(){{
                put(QUANTITY, developerWorkload.get(r) == 0 ? 0 : developerAddIssueCount * 100.0 / developerWorkload.get(r));
                put(ISSUE_COUNT, developerAddIssueCount);
            }});
            developersDetail.put(SOLVE, new JSONObject(){{
                put(QUANTITY, developerWorkload.get(r) == 0 ? 0 : developerSolvedIssueCount * 100.0 / developerWorkload.get(r));
                put(ISSUE_COUNT, developerSolvedIssueCount);
            }});
        });

        if(Boolean.TRUE.equals(needAll)) {
            return new HashMap<String, Object>(8){{
                put("eliminatedIssuePerHundredLine", loc.intValue() == 0 ? 0 : allSolvedIssueCount.doubleValue() / loc.intValue());
                put("notedIssuePreHundredLine", loc.intValue() == 0 ? 0 : allAddedIssueCount.doubleValue() / loc.intValue());
                put("addedIssueCount", allAddedIssueCount);
                put("solvedIssueCount", allSolvedIssueCount);
                put("loc", loc.intValue());
            }};
        }
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
        if(issuesLifeCycle == null || issuesLifeCycle.isEmpty()){
            return new JSONObject(){{
                put(QUANTITY, 0);
                put("min", 0);
                put("max", 0);
                put("mid", 0);
            }};
        }
        issuesLifeCycle.sort(Comparator.comparingInt(o -> o));
        return new JSONObject(){{
            put(QUANTITY, issuesLifeCycle.size());
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
                return new ArrayList<>();
        }
    }

    private List<JSONObject> handleLifeCycleDetail(List<JSONObject> issuesDetail, String token) {
        if(issuesDetail == null){
            return new ArrayList<>();
        }
        //init repoUuid to repoName and projectName
        Map<String, Map<String, String>> repoName = restInterfaceManager.getAllRepoToProjectName(token);
        //handle issues detail
        issuesDetail.forEach(r -> {
            r.put("repoName", repoName.get(r.getString("repoUuid")).get("repoName"));
            r.put("projectName", repoName.get(r.getString("repoUuid")).get("projectName"));
            r.put("severity", Objects.requireNonNull(JavaIssuePriorityEnum.getPriorityEnumByRank(r.getInteger("priority"))).getName());
        });

        return issuesDetail;
    }

    @Override
    @CacheEvict(cacheNames = {"issueLifeCycleCount","developerCodeQuality"}, allEntries=true, beforeInvocation = true)
    public void clearCache() {
        log.info("Successfully clear redis cache:issueLifeCycleCount,developerCodeQuality in db1.");
    }

    @Override
    public List<Map<String, JSONObject>> handleSortDeveloperLifecycle(List<Map<String, JSONObject>> developersLifecycle, Boolean isAsc, int ps, int page) {
        if(isAsc == null){
            return developersLifecycle;
        }
        developersLifecycle.sort((o1, o2) -> isAsc ? o1.values().stream().mapToInt(detail -> detail.getIntValue(QUANTITY)).sum() - o2.values().stream().mapToInt(detail -> detail.getIntValue(QUANTITY)).sum()
                    : o1.values().stream().mapToInt(detail -> detail.getIntValue(QUANTITY)).sum() - o2.values().stream().mapToInt(detail -> detail.getIntValue(QUANTITY)).sum());
        return developersLifecycle.subList(ps * (page -1), Math.min(developersLifecycle.size(), ps * page));
    }

    @Override
    public Map<String, Object> handleSortCodeQuality(List<Map<String, Object>> result, Boolean isAsc, int ps, int page) {
        result.sort((o1, o2) -> {
            JSONObject temp1 = (JSONObject) o1.get(ADD);
            JSONObject temp2 = (JSONObject) o2.get(ADD);
            return isAsc ? temp1.getIntValue(ISSUE_COUNT) - temp2.getIntValue(ISSUE_COUNT)
                    : temp2.getIntValue(ISSUE_COUNT) - temp1.getIntValue(ISSUE_COUNT);
        });

        return new HashMap<String, Object>(8){{
            put("page", page);
            put("total", result.size() / ps + 1);
            put("record", (page - 1) * ps > result.size() ? 0 : Math.min(result.size(), ps));
            put("rows", (page - 1) * ps > result.size() ? new ArrayList<>() :
                    result.subList((page - 1) * ps, Math.min(result.size(), page * ps)));
        }};
    }

    @Override
    public Object getSelfIntroducedLivingIssueCount(int page, int ps, String order, Boolean isAsc, Map<String, Object> query, Boolean isPagination, List<String> producerList) {
        // 不分页，一般会传入指定的producerList
        if (!isPagination) {
            List<JSONObject> result = issueDao.getSelfIntroduceLivingIssueCount(query);
            result = addBlackData(result, producerList);
            return result;
        }
        // 需要做分页
        PagedGridResult.handlePageHelper(page, ps, order, isAsc);
        List<JSONObject> result = issueDao.getSelfIntroduceLivingIssueCount(query);
        result = addBlackData(result, producerList);
        return setterPagedGrid(result, page);
    }

    @Override
    public Object getLivingIssueTendency(String beginDate, String endDate, String projectIds, String interval, String showDetail) {
        List<Map<String, Object>> result = new ArrayList<>();
        String time1 = " 00:00:00";
        String time2 = " 24:00:00";
        beginDate = beginDate + time1;
        endDate = endDate + time2;
        if (StringUtils.isEmpty(projectIds)) {
            projectIds = projectDao.getAllProjectIds();
        }
        for (String projectId : projectIds.split(",")) {
            if (projectId.length() != 0) {
                String tempDateBegin = beginDate.split(" ")[0] + time1;
                String tempDateEnd;
                switch (interval) {
                    case "day":
                        tempDateEnd = beginDate.split(" ")[0] + time2;
                        while (tempDateBegin.compareTo(endDate) < 1) {
                            result.add(issueDao.getLivingIssueTendency(tempDateEnd, projectId, showDetail));
                            tempDateBegin = DateTimeUtil.datePlus(tempDateBegin.split(" ")[0]) + time1;
                            tempDateEnd = tempDateBegin.split(" ")[0] + time2;
                        }
                        break;
                    case "month":
                        while (tempDateBegin.compareTo(endDate) < 1) {
                            tempDateEnd = tempDateBegin;
                            int year = Integer.parseInt(tempDateEnd.split(" ")[0].split("-")[0]);
                            int month = Integer.parseInt(tempDateEnd.split(" ")[0].split("-")[1]);
                            tempDateEnd = DateTimeUtil.lastDayOfMonth(year, month) + time2;
                            result.add(issueDao.getLivingIssueTendency(tempDateEnd, projectId, showDetail));
                            tempDateBegin = DateTimeUtil.datePlus(tempDateEnd).split(" ")[0] + time1;
                        }
                        break;
                    case "year":
                        while (tempDateBegin.compareTo(endDate) < 1) {
                            tempDateEnd = tempDateBegin;
                            int year = Integer.parseInt(tempDateEnd.split(" ")[0].split("-")[0]);
                            tempDateEnd = DateTimeUtil.lastDayOfMonth(year, 12) + time2;
                            result.add(issueDao.getLivingIssueTendency(tempDateEnd, projectId, showDetail));
                            tempDateBegin = DateTimeUtil.datePlus(tempDateEnd).split(" ")[0] + time1;
                        }
                        break;
                    default:
                        while (tempDateBegin.compareTo(endDate) < 1) {
                            tempDateEnd = tempDateBegin;
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                                Calendar cal = Calendar.getInstance();
                                Date time = sdf.parse(tempDateEnd.split(" ")[0]);
                                cal.setTime(time);
                                int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
                                if (1 == dayWeek) {
                                    cal.add(Calendar.DAY_OF_MONTH, -1);
                                }
                                cal.setFirstDayOfWeek(Calendar.MONDAY);
                                int day = cal.get(Calendar.DAY_OF_WEEK);
                                cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);
                                cal.add(Calendar.DATE, 6);
                                tempDateEnd = sdf.format(cal.getTime()) + time2;
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            result.add(issueDao.getLivingIssueTendency(tempDateEnd, projectId, showDetail));
                            tempDateBegin = DateTimeUtil.datePlus(tempDateEnd).split(" ")[0] + time1;
                        }
                        break;
                }
            }
        }
        return result;
    }

    private PagedGridResult setterPagedGrid(List<?> list, Integer page) {
        PageInfo<?> pageList = new PageInfo<>(list);
        PagedGridResult grid = new PagedGridResult();
        grid.setPage(page);
        grid.setRows(list);
        grid.setTotal(pageList.getPages());
        grid.setRecords(pageList.getTotal());
        return grid;
    }

    private List<JSONObject> addBlackData(List<JSONObject> result, List<String> producerList) {
        if (producerList.size() == 0) {
            return new ArrayList<>();
        }
        // key 为 producer value 表示这个producer在result中是否存在 1表示存在 0表示不存在
        Map<String, Integer> producerMap = new HashMap();
        for (String producer : producerList) {
            // 初始化 默认都不存在
            producerMap.put(producer, 0);
        }
        // 对result中有记录的 将对应的producer的状态设置为1
        for (JSONObject re : result) {
            producerMap.put(re.getString(DEVELOPER_NAME), 1);
        }

        // 遍历map，对于那些value为0的 把这些人的数据（0）添加到 返回的result中
        for (Map.Entry<String, Integer> entry : producerMap.entrySet()) {
            if (entry.getValue() == 0) {
                JSONObject obj = new JSONObject();
                obj.put(DEVELOPER_NAME, entry.getKey());
                obj.put(LIVING_ISSUE_COUNT, 0);
                result.add(obj);
            }
        }
        return result;
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
    public void setProjectDao(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }
}