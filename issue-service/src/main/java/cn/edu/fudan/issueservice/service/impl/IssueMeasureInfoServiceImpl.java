package cn.edu.fudan.issueservice.service.impl;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.dao.IssueDao;
import cn.edu.fudan.issueservice.dao.ProjectDao;
import cn.edu.fudan.issueservice.domain.dbo.Issue;
import cn.edu.fudan.issueservice.domain.enums.IgnoreTypeEnum;
import cn.edu.fudan.issueservice.domain.enums.JavaIssuePriorityEnum;
import cn.edu.fudan.issueservice.domain.enums.*;
import cn.edu.fudan.issueservice.domain.vo.DeveloperLivingIssueVO;
import cn.edu.fudan.issueservice.domain.vo.IssueTopVO;
import cn.edu.fudan.issueservice.service.IssueMeasureInfoService;
import cn.edu.fudan.issueservice.util.DateTimeUtil;
import cn.edu.fudan.issueservice.util.JGitHelper;
import cn.edu.fudan.issueservice.domain.vo.PagedGridResult;
import cn.edu.fudan.issueservice.util.StringsUtil;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.model.Developer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * description:
 *
 * @author fancying
 * create: 2019-04-02 15:27
 **/
@Slf4j
@Service
public class IssueMeasureInfoServiceImpl implements IssueMeasureInfoService {

    private IssueDao issueDao;

    private ProjectDao projectDao;

    private RestInterfaceManager restInterfaceManager;

    private static final String REPO_LIST = "repoList";
    private static final String DEVELOPER = "developer";
    private static final String QUANTITY = "quantity";
    private static final String DEVELOPER_NAME = "developerName";
    private static final String ADD = "add";
    private static final String SOLVE = "solve";
    private static final String ISSUE_COUNT = "issueCount";
    private static final String LOC = "loc";
    private static final String TOTAL = "Total";
    private static final String LIVING_ISSUE_COUNT = "livingIssueCount";
    private static final String SOLVED = "solved";
    private static final String OPEN = "open";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String PRODUCER = "producer";

    @Override
    public List<Map.Entry<String, JSONObject>> getNotSolvedIssueCountByToolAndRepoUuid(List<String> repoUuids, String tool, String order) {

        Map<String, JSONObject> result = new HashMap<>(32);

        List<Issue> issues = issueDao.getNotSolvedIssueAllListByToolAndRepoId(repoUuids, tool);

        issues.forEach(issue -> {
            JSONObject issueType = result.getOrDefault(issue.getType(), new JSONObject() {{
                put(TOTAL, 0);
                put(IgnoreTypeEnum.DEFAULT.getName(), 0);
                put(IgnoreTypeEnum.IGNORE.getName(), 0);
                put(IgnoreTypeEnum.MISINFORMATION.getName(), 0);
                put(IgnoreTypeEnum.TO_REVIEW.getName(), 0);
            }});
            issueType.put(TOTAL, issueType.getInteger(TOTAL) + 1);
            issueType.put(issue.getManualStatus(), issueType.getInteger(issue.getManualStatus()) + 1);
            result.put(issue.getType(), issueType);
        });

        List<Map.Entry<String, JSONObject>> issueTypeList = new ArrayList<>(result.entrySet());

        issueTypeList.sort((o1, o2) -> o2.getValue().getInteger(order) - o1.getValue().getInteger(order));

        return issueTypeList;
    }

    @Override
    public List<Map.Entry<String, JSONObject>> getNotSolvedIssueCountByCommit(List<String> repoList, String tool, String order, String commitUuid) {
        //1.get the pre commits
        repoList.forEach(repoUuid -> {
            //get the repo path
            String repoPath = restInterfaceManager.getRepoPath(repoUuid);
            //init jgit
            JGitHelper jGitHelper = new JGitHelper(repoPath);
            //get all pre commits
            List<String> allCommitParents = jGitHelper.getAllCommitParents(commitUuid);
            allCommitParents.remove(commitUuid);
            //get all pre rawIssues
            //todo
        });
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getDayAvgSolvedIssue(Map<String, Object> query, String token) {
        Map<String, Object> developerCodeQuality = getDeveloperCodeQuality(query, false, token);

        JSONObject solvedDetail = (JSONObject) developerCodeQuality.get(SOLVE);

        double days = (DateTimeUtil.stringToLocalDate(query.get("until").toString()).toEpochDay() - DateTimeUtil.stringToLocalDate(query.get("since").toString()).toEpochDay()) * 5.0 / 7;

        return new HashMap<String, Object>(6) {{
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

        if (query.get(REPO_LIST) instanceof String) {
            query.put(REPO_LIST, StringsUtil.splitStringList(query.get(REPO_LIST) == null ? null : query.get(REPO_LIST).toString()));
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
            developersDetail.put(ADD, new JSONObject() {{
                put(QUANTITY, developerWorkload.get(r) == 0 ? 0 : developerAddIssueCount * 100.0 / developerWorkload.get(r));
                put(ISSUE_COUNT, developerAddIssueCount);
            }});
            developersDetail.put(SOLVE, new JSONObject() {{
                put(QUANTITY, developerWorkload.get(r) == 0 ? 0 : developerSolvedIssueCount * 100.0 / developerWorkload.get(r));
                put(ISSUE_COUNT, developerSolvedIssueCount);
            }});
        });

        if (Boolean.TRUE.equals(needAll)) {
            if (loc.intValue() == 0) {
                return new HashMap<String, Object>(8) {{
                    put("eliminatedIssuePerHundredLine", 0);
                    put("notedIssuePreHundredLine", 0);
                    put("addedIssueCount", allAddedIssueCount);
                    put("solvedIssueCount", allSolvedIssueCount);
                    put("loc", loc.intValue());
                }};
            }
            return new HashMap<String, Object>(8) {{
                put("eliminatedIssuePerHundredLine", allSolvedIssueCount.doubleValue() / loc.intValue());
                put("notedIssuePreHundredLine", allAddedIssueCount.doubleValue() / loc.intValue());
                put("addedIssueCount", allAddedIssueCount);
                put("solvedIssueCount", allSolvedIssueCount);
                put("loc", loc.intValue());
            }};
        }

        return developersDetail;
    }

    @Override
    public JSONObject getIssuesLifeCycle(String status, String target, Map<String, Object> query) {
        switch (target + "-" + status) {
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
        if (issuesLifeCycle == null || issuesLifeCycle.isEmpty()) {
            return new JSONObject() {{
                put(QUANTITY, 0);
                put("min", 0);
                put("max", 0);
                put("mid", 0);
            }};
        }
        issuesLifeCycle.sort(Comparator.comparingInt(o -> o));
        return new JSONObject() {{
            put(QUANTITY, issuesLifeCycle.size());
            put("min", issuesLifeCycle.get(0));
            put("max", issuesLifeCycle.get(issuesLifeCycle.size() - 1));
            put("mid", issuesLifeCycle.get(issuesLifeCycle.size() / 2));
        }};
    }

    @Override
    public List<JSONObject> getLifeCycleDetail(String status, String target, Map<String, Object> query, String token) {
        switch (target + "-" + status) {
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
        if (issuesDetail == null) {
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
    @CacheEvict(cacheNames = {"issueLifeCycleCount", "developerCodeQuality"}, allEntries = true, beforeInvocation = true)
    public void clearCache() {
        log.info("Successfully clear redis cache:issueLifeCycleCount,developerCodeQuality in db1.");
    }

    @Override
    public List<Map<String, JSONObject>> handleSortDeveloperLifecycle(List<Map<String, JSONObject>> developersLifecycle, Boolean isAsc, int ps, int page) {
        if (isAsc == null) {
            return developersLifecycle;
        }
        developersLifecycle.sort((o1, o2) -> isAsc ? o1.values().stream().mapToInt(detail -> detail.getIntValue(QUANTITY)).sum() - o2.values().stream().mapToInt(detail -> detail.getIntValue(QUANTITY)).sum()
                : o1.values().stream().mapToInt(detail -> detail.getIntValue(QUANTITY)).sum() - o2.values().stream().mapToInt(detail -> detail.getIntValue(QUANTITY)).sum());
        return developersLifecycle.subList(ps * (page - 1), Math.min(developersLifecycle.size(), ps * page));
    }

    @Override
    public Map<String, Object> handleSortCodeQuality(List<Map<String, Object>> result, Boolean isAsc, int ps, int page) {
        result.sort((o1, o2) -> {
            JSONObject temp1 = (JSONObject) o1.get(ADD);
            JSONObject temp2 = (JSONObject) o2.get(ADD);
            return isAsc ? temp1.getIntValue(ISSUE_COUNT) - temp2.getIntValue(ISSUE_COUNT)
                    : temp2.getIntValue(ISSUE_COUNT) - temp1.getIntValue(ISSUE_COUNT);
        });

        return new HashMap<String, Object>(8) {{
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

        // since 可能为空 所以需要判断一下
        if  (!StringUtils.isEmpty(beginDate)) {
            beginDate = beginDate + time1;
        }

        // until 约定了必须要传
        endDate = endDate + time2;

        if (StringUtils.isEmpty(projectIds)) {
            projectIds = projectDao.getAllProjectIds();
        }
        for (String projectId : projectIds.split(",")) {
            if (projectId.length() != 0) {
                String tempDateBegin;
                String tempDateEnd;
                switch (interval) {
                    case "day":
                        if  (StringUtils.isEmpty(beginDate)) {
                            // 不传since的情况
                            result.add(issueDao.getLivingIssueTendency(endDate, projectId, showDetail));
                        } else {
                            tempDateBegin = beginDate.split(" ")[0] + time1;
                            tempDateEnd = beginDate.split(" ")[0] + time2;
                            while (tempDateBegin.compareTo(endDate) < 1) {
                                result.add(issueDao.getLivingIssueTendency(tempDateEnd, projectId, showDetail));
                                tempDateBegin = DateTimeUtil.datePlus(tempDateBegin.split(" ")[0]) + time1;
                                tempDateEnd = tempDateBegin.split(" ")[0] + time2;
                            }
                        }
                        break;
                    case "month":
                        if  (StringUtils.isEmpty(beginDate)) {
                            // 不传since的情况
                            result.add(issueDao.getLivingIssueTendency(endDate, projectId, showDetail));
                        } else {
                            tempDateBegin = beginDate.split(" ")[0] + time1;
                            tempDateEnd = beginDate.split(" ")[0] + time2;
                            while (tempDateBegin.compareTo(endDate) < 1) {
                                tempDateEnd = tempDateBegin;
                                int year = Integer.parseInt(tempDateEnd.split(" ")[0].split("-")[0]);
                                int month = Integer.parseInt(tempDateEnd.split(" ")[0].split("-")[1]);
                                tempDateEnd = DateTimeUtil.lastDayOfMonth(year, month) + time2;
                                result.add(issueDao.getLivingIssueTendency(tempDateEnd, projectId, showDetail));
                                tempDateBegin = DateTimeUtil.datePlus(tempDateEnd).split(" ")[0] + time1;
                            }
                        }
                        break;
                    case "year":
                        if  (StringUtils.isEmpty(beginDate)) {
                            // 不传since的情况
                            result.add(issueDao.getLivingIssueTendency(endDate, projectId, showDetail));
                        } else {
                            tempDateBegin = beginDate.split(" ")[0] + time1;
                            tempDateEnd = beginDate.split(" ")[0] + time2;
                            while (tempDateBegin.compareTo(endDate) < 1) {
                                tempDateEnd = tempDateBegin;
                                int year = Integer.parseInt(tempDateEnd.split(" ")[0].split("-")[0]);
                                tempDateEnd = DateTimeUtil.lastDayOfMonth(year, 12) + time2;
                                result.add(issueDao.getLivingIssueTendency(tempDateEnd, projectId, showDetail));
                                tempDateBegin = DateTimeUtil.datePlus(tempDateEnd).split(" ")[0] + time1;
                            }
                        }
                        break;
                    default:
                        if  (StringUtils.isEmpty(beginDate)) {
                            // 不传since的情况
                            result.add(issueDao.getLivingIssueTendency(endDate, projectId, showDetail));
                        } else {
                            // 传since的情况
                            tempDateBegin = beginDate.split(" ")[0] + time1;
                            tempDateEnd = beginDate.split(" ")[0] + time2;
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
        if (producerList.isEmpty()) {
            return new ArrayList<>();
        }
        // key 为 producer value 表示这个producer在result中是否存在 1表示存在 0表示不存在
        Map<String, Integer> producerMap = new HashMap<>(16);
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

    @Override
    public List<IssueTopVO> getDeveloperIntroduceIssueTop5(String developer, String order) {
        List<IssueTopVO> issueTop5List = new ArrayList<>();
        //get the issues
        List<Issue> issues = issueDao.getIssueCountByIntroducerAndTool(developer);
        //group by type
        Map<String, List<Issue>> issueTypeMap = issues.stream().collect(Collectors.groupingBy(Issue::getType));
        //new IssueTop5
        for (Map.Entry<String, List<Issue>> issueType : issueTypeMap.entrySet()) {
            int open = (int) issueType.getValue().stream().filter(issue -> IssueStatusEnum.OPEN.getName().equals(issue.getStatus())).count();
            int solved = (int) issueType.getValue().stream().filter(issue -> IssueStatusEnum.SOLVED.getName().equals(issue.getStatus())).count();
            IssueTopVO issueTop5 = IssueTopVO.builder()
                    .issueType(issueType.getKey())
                    .quantity(issueType.getValue().size())
                    .open(open)
                    .solved(solved)
                    .build();
            issueTop5List.add(issueTop5);
        }
        //sort top5
        switch (order) {
            case QUANTITY:
                issueTop5List.sort((o1, o2) -> o2.getQuantity() - o1.getQuantity());
                break;
            case SOLVED:
                issueTop5List.sort((o1, o2) -> o2.getSolved() - o1.getSolved());
                break;
            case OPEN:
                issueTop5List.sort((o1, o2) -> o2.getOpen() - o1.getOpen());
                break;
            default:
        }

        return issueTop5List.subList(0, Math.min(issueTop5List.size(), 5));
    }

    @Override
    public PagedGridResult<DeveloperLivingIssueVO> getDeveloperListLivingIssue(String since, String until, List<String> repoUuids, List<String> developers, int page, int ps, Boolean asc) {
        PagedGridResult.handlePageHelper(page, ps, PRODUCER, true);

        List<Map<String, Object>> developersDetail = issueDao.getDeveloperListLivingIssue(since, until, repoUuids, developers);

//        Map<String, Long> developerIssueCount = new HashMap<>(32);
//        for (Map<String, Object> developerDetail : developersDetail) {
//            developerIssueCount.put((String) developerDetail.get(PRODUCER), (Long) developerDetail.get(ISSUE_COUNT));
//        }
//        Map<String, Integer> sortMap = new HashMap<>();
//        developerIssueCount.entrySet().stream()
//                .sorted(Map.Entry.comparingByValue())
//                .forEachOrdered(b -> sortMap.put(b.getKey(), Math.toIntExact(b.getValue())));

        List<DeveloperLivingIssueVO> developerListLivingIssue = new ArrayList<>();
        for (String developer : developers) {
            long issueCount = 0;
            for (Map<String, Object> developerDetail : developersDetail) {
                if (developerDetail.get(PRODUCER).equals(developer)) {
                    issueCount = (long) developerDetail.get(ISSUE_COUNT);
                }
            }

            DeveloperLivingIssueVO developerLivingIssueVO = DeveloperLivingIssueVO.builder()
                    .developerName(developer)
                    .num(issueCount)
                    .build();
            developerListLivingIssue.add(developerLivingIssueVO);
        }

        if (asc != null) {
            developerListLivingIssue.sort((o1, o2) -> (int) (asc ? o1.getNum() - o2.getNum() : o2.getNum() - o1.getNum()));
        }

        PagedGridResult<DeveloperLivingIssueVO> pagedGridResult = new PagedGridResult<>();
        return pagedGridResult.setterPagedGrid(developerListLivingIssue, page);
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
