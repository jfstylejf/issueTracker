package cn.edu.fudan.issueservice.service.impl;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.dao.*;
import cn.edu.fudan.issueservice.domain.enums.JavaIssuePriorityEnum;
import cn.edu.fudan.issueservice.domain.enums.IssueStatusEnum;
import cn.edu.fudan.issueservice.domain.vo.IssueFilterInfoVO;
import cn.edu.fudan.issueservice.domain.vo.IssueFilterSidebarVO;
import cn.edu.fudan.issueservice.service.IssueService;
import cn.edu.fudan.issueservice.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author WZY
 * @version 1.0
 **/
@Slf4j
@Service
public class IssueServiceImpl implements IssueService {
    private final Logger logger = LoggerFactory.getLogger(IssueServiceImpl.class);

    private RestInterfaceManager restInterfaceManager;

    private IssueDao issueDao;

    private ScanResultDao scanResultDao;

    private RawIssueMatchInfoDao rawIssueMatchInfoDao;

    private RawIssueDao rawIssueDao;

    private LocationDao locationDao;

    private IssueRepoDao issueRepoDao;

    private IssueScanDao issueScanDao;

    private static final String SOLVE_TIME = "solveTime";
    private static final String SOLVED_STR = "Solved";
    private static final String STATUS = "status";
    private static final String SOLVER = "solver";
    private static final String REMAINING_ISSUE_COUNT = "remainingIssueCount";
    private static final String ELIMINATED_ISSUE_COUNT = "eliminatedIssueCount";
    private static final String NEW_ISSUE_COUNT = "newIssueCount";
    private static final String PROJECT_NAME = "projectName";
    private static final String DEVELOPER = "developer";
    private static final String START_COMMIT_DATE = "startCommitDate";
    private static final String SOLVE_COMMIT = "solveCommit";
    private static final String PRIORITY = "priority";

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Async
    public void deleteIssueByRepoIdAndTool(String repoUuid, String tool) {
        logger.info("start to delete issue -> repoUuid={} , tool={}", repoUuid, tool);
        //根据rawIssue来删库(便于控制每次删除的数量,防止超时锁表)
        List<String> rawIssueUuidList = rawIssueDao.getRawIssueByRepoIdAndTool(repoUuid, tool);
        //每次删除5000条rawIssue,防止超时锁表
        int deleteRawIssueCount = 0;
        while (deleteRawIssueCount < rawIssueUuidList.size()) {
            int firstIndex = deleteRawIssueCount;
            deleteRawIssueCount += 5000;
            List<String> partOfRawIssueIds = new ArrayList<>(rawIssueUuidList.subList(firstIndex, Math.min(deleteRawIssueCount, rawIssueUuidList.size())));
            locationDao.deleteLocationByRawIssueIds(partOfRawIssueIds);
            rawIssueMatchInfoDao.deleteRawIssueMatchInfo(partOfRawIssueIds);
            rawIssueDao.deleteRawIssueByIds(partOfRawIssueIds);
        }
        //删除issue,issue_repo,issue_scan,scan_result表记录
        issueDao.deleteIssueByRepoIdAndTool(repoUuid, tool);
        issueRepoDao.delIssueRepo(repoUuid, null, tool);
        issueScanDao.deleteIssueScanByRepoIdAndTool(repoUuid, tool);
        scanResultDao.deleteScanResultsByRepoIdAndCategory(repoUuid, tool);
        //完成删库
        logger.info("finish deleting issues -> repoUuid={} , tool={}", repoUuid, tool);
    }

    @Override
    public Map<String, List<Map<String, String>>> getRepoWithIssues(String developer) {

        List<String> repoIdList = issueDao.getRepoWithIssues(developer);

        List<Map<String, String>> repoInfo = new ArrayList<>();

        Map<String, List<Map<String, String>>> projectInfo = new HashMap<>(32);

        for (String repoId : repoIdList) {
            Map<String, String> project = restInterfaceManager.getProjectByRepoId(repoId);
            projectInfo.put(project.get(PROJECT_NAME), new ArrayList<>());
            repoInfo.add(project);
        }

        for (Map<String, String> tempRepo : repoInfo) {
            List<Map<String, String>> project = projectInfo.get(tempRepo.get(PROJECT_NAME));
            project.add(tempRepo);
            projectInfo.put(tempRepo.get(PROJECT_NAME), project);
        }

        return projectInfo;
    }

    @Override
    public List<String> getExistIssueTypes(String tool) {
        List<String> types = issueDao.getExistIssueTypes(tool);
        String clone = "clone";
        if (clone.equals(tool)) {
            types.sort(Comparator.comparingInt(Integer::valueOf));
        }
        return types;
    }

    @Override
    public void updatePriority(String issueId, String severity, String token) throws Exception {
        JavaIssuePriorityEnum javaIssuePriorityEnum = JavaIssuePriorityEnum.getPriorityEnum(severity);
        if (javaIssuePriorityEnum == null) {
            throw new Exception("priority is not illegal");
        }
        int priorityInt = javaIssuePriorityEnum.getRank();
        issueDao.updateOneIssuePriority(issueId, priorityInt);
    }

    @Override
    public void updateStatus(String issueId, String status, String manualStatus) {
        issueDao.updateOneIssueStatus(issueId, status, manualStatus);
    }

    @Override
    public List<Map<String, Object>> getRepoIssueCounts(List<String> repoUuids, String since, String until, String tool) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate indexDay = LocalDate.parse(since, DateTimeUtil.Y_M_D_formatter);
        LocalDate untilDay = LocalDate.parse(until, DateTimeUtil.Y_M_D_formatter);

        String firstDateByRepo = scanResultDao.findFirstDateByRepo(repoUuids);
        if (firstDateByRepo == null) {
            while (indexDay.isBefore(untilDay) || indexDay.isEqual(untilDay)) {
                indexDay = getInitInfoInLocalDate(result, indexDay);
            }
            return result;
        }
        LocalDate firstDate = LocalDate.parse(firstDateByRepo.substring(0, 10), DateTimeUtil.Y_M_D_formatter);

        while (indexDay.isBefore(firstDate)) {
            indexDay = getInitInfoInLocalDate(result, indexDay);
        }

        LocalDate indexDay2 = LocalDate.parse(indexDay.toString(), DateTimeUtil.Y_M_D_formatter);
        Map<String, Object> firstDateScanResult = findFirstDateScanResult(repoUuids, indexDay2, firstDate, tool);
        firstDateScanResult.put(REMAINING_ISSUE_COUNT, Integer.valueOf(firstDateScanResult.get(REMAINING_ISSUE_COUNT).toString()));
        result.add(firstDateScanResult);
        indexDay = indexDay.plusDays(1);

        while (untilDay.isAfter(indexDay) || untilDay.isEqual(indexDay)) {
            Map<String, Object> map = new HashMap<>(16);
            List<Map<String, Object>> repoIssueCounts2 = scanResultDao.getRepoIssueCounts(repoUuids, indexDay.toString(), indexDay.toString(), tool, null);
            if (repoIssueCounts2.isEmpty()) {
                map.put(NEW_ISSUE_COUNT, 0);
                map.put(ELIMINATED_ISSUE_COUNT, 0);
                map.put(REMAINING_ISSUE_COUNT, Integer.valueOf(result.get(result.size() - 1).get(REMAINING_ISSUE_COUNT).toString()));
            } else {
                BigDecimal now = new BigDecimal((String) repoIssueCounts2.get(repoIssueCounts2.size() - 1).get(REMAINING_ISSUE_COUNT));
                BigDecimal last = new BigDecimal((int) result.get(result.size() - 1).get(REMAINING_ISSUE_COUNT));
                long temp = now.longValue() - last.longValue();
                map.put(NEW_ISSUE_COUNT, Math.max(temp, 0));
                map.put(ELIMINATED_ISSUE_COUNT, temp < 0 ? -temp : 0);
                map.put(REMAINING_ISSUE_COUNT, Integer.valueOf(repoIssueCounts2.get(repoIssueCounts2.size() - 1).get(REMAINING_ISSUE_COUNT).toString()));
            }
            map.put("date", indexDay.toString());
            result.add(map);
            indexDay = indexDay.plusDays(1);
        }

        return result;
    }

    private LocalDate getInitInfoInLocalDate(List<Map<String, Object>> result, LocalDate indexDay) {
        Map<String, Object> map = new HashMap<>(8);
        map.put("date", indexDay.toString());
        map.put(NEW_ISSUE_COUNT, 0);
        map.put(ELIMINATED_ISSUE_COUNT, 0);
        map.put(REMAINING_ISSUE_COUNT, 0);
        result.add(map);
        indexDay = indexDay.plusDays(1);
        return indexDay;
    }

    private Map<String, Object> findFirstDateScanResult(List<String> repoUuids, LocalDate indexDay, LocalDate firstDate, String tool) {
        Map<String, Object> map = new HashMap<>(8);
        map.put("date", indexDay.toString());
        while (indexDay.isAfter(firstDate) || indexDay.isEqual(firstDate)) {
            List<Map<String, Object>> repoIssueCounts = scanResultDao.getRepoIssueCounts(repoUuids, indexDay.toString(), indexDay.toString(), tool, null);
            if (!repoIssueCounts.isEmpty()) {
                map.put(NEW_ISSUE_COUNT, repoIssueCounts.get(repoIssueCounts.size() - 1).get(NEW_ISSUE_COUNT));
                map.put(ELIMINATED_ISSUE_COUNT, repoIssueCounts.get(repoIssueCounts.size() - 1).get(ELIMINATED_ISSUE_COUNT));
                map.put(REMAINING_ISSUE_COUNT, repoIssueCounts.get(repoIssueCounts.size() - 1).get(REMAINING_ISSUE_COUNT));
                break;
            }
            indexDay = indexDay.minusDays(1);
        }
        return map;
    }

    @Override
    public List<String> getIssueSeverities() {

        List<String> issueSeverities = new ArrayList<>();

        List<JavaIssuePriorityEnum> javaIssuePriorityEnums = new ArrayList<>(Arrays.asList(JavaIssuePriorityEnum.values()));

        javaIssuePriorityEnums = javaIssuePriorityEnums.stream().sorted(Comparator.comparing(JavaIssuePriorityEnum::getRank)).collect(Collectors.toList());

        for (JavaIssuePriorityEnum javaIssuePriorityEnum : javaIssuePriorityEnums) {
            issueSeverities.add(javaIssuePriorityEnum.getName());
        }

        return issueSeverities;
    }

    @Override
    public List<String> getIssueStatus() {

        List<String> issueStatus = new ArrayList<>();

        for (IssueStatusEnum issueStatusEnum : IssueStatusEnum.values()) {
            issueStatus.add(issueStatusEnum.getName());
        }

        return issueStatus;
    }

    @Override
    public List<String> getIssueIntroducers(List<String> repoUuids) {
        return issueDao.getIssueIntroducers(repoUuids);
    }

    @Override
    public List<IssueFilterInfoVO> getIssuesOverview(Map<String, Object> query, String token) {
        List<IssueFilterInfoVO> issueFilterInfos = new ArrayList<>();
        //get issues overview
        List<Map<String, Object>> issuesOverview = issueDao.getIssuesOverview(query);
        //get repo uuid to repo name
        Map<String, String> allRepoToRepoName = restInterfaceManager.getAllRepoToRepoName(token);
        //new issue filter info
        for (Map<String, Object> issueOverview : issuesOverview) {
            String repoUuid = (String) issueOverview.get("repoUuid");
            IssueFilterInfoVO issueFilterInfo = IssueFilterInfoVO.builder()
                    .uuid((String) issueOverview.get("uuid"))
                    .displayId((int) issueOverview.get("displayId"))
                    .type((String) issueOverview.get("type"))
                    .issueCategory((String) issueOverview.get("issueCategory"))
                    .repoName(allRepoToRepoName.get(repoUuid))
                    .fileName((String) issueOverview.get("fileName"))
                    .producer((String) issueOverview.get("producer"))
                    .startCommitDate((Date) issueOverview.get(START_COMMIT_DATE))
                    .status((String) issueOverview.get(STATUS))
                    .priority(Objects.requireNonNull(JavaIssuePriorityEnum.getPriorityEnumByRank((Integer) issueOverview.get(PRIORITY))).getName())
                    .solver((String) issueOverview.get(SOLVER))
                    .solveTime((Date) issueOverview.get(SOLVE_TIME))
                    .solveCommit((String) issueOverview.get(SOLVE_COMMIT))
                    .build();
            issueFilterInfos.add(issueFilterInfo);
        }

        return issueFilterInfos;
    }

    @Override
    public List<IssueFilterSidebarVO> getIssuesFilterSidebar(Map<String, Object> query) {
        List<IssueFilterSidebarVO> result = new ArrayList<>();
        //get issue count info list
        List<Map<String, Object>> issueCountInfoList = issueDao.getIssueCountByCategoryAndType(query);
        //build issue filter side bar
        long codeSmellTotal = 0, bugTotal = 0, warnTotal = 0, errorTotal = 0;
        //new categories
        List<IssueFilterSidebarVO.IssueSideBarInfo> codeSmellCategories = new ArrayList<>();
        List<IssueFilterSidebarVO.IssueSideBarInfo> bugCategories = new ArrayList<>();
        List<IssueFilterSidebarVO.IssueSideBarInfo> warnCategories = new ArrayList<>();
        List<IssueFilterSidebarVO.IssueSideBarInfo> errorCategories = new ArrayList<>();
        for (Map<String, Object> issueCountInfo : issueCountInfoList) {
            long count = (long) issueCountInfo.get("count");
            String type = (String) issueCountInfo.get("type");
            String category = (String) issueCountInfo.get("category");
            // 这是属于java的issue
            if ("Code smell".equals(category)) {
                codeSmellTotal += count;
                codeSmellCategories.add(IssueFilterSidebarVO.IssueSideBarInfo.getSidebarInfo(count, type));
            }
            if ("Bug".equals(category)) {
                bugTotal += count;
                bugCategories.add(IssueFilterSidebarVO.IssueSideBarInfo.getSidebarInfo(count, type));
            }
            // 这是属于js的issue
            if ("Warn".equals(category)) {
                warnTotal += count;
                warnCategories.add(IssueFilterSidebarVO.IssueSideBarInfo.getSidebarInfo(count, type));
            }
            if ("Error".equals(category)) {
                errorTotal += count;
                errorCategories.add(IssueFilterSidebarVO.IssueSideBarInfo.getSidebarInfo(count, type));
            }
        }
        IssueFilterSidebarVO.IssueFilterSidebar codeSmellSidebar = IssueFilterSidebarVO.IssueFilterSidebar.builder()
                .total(codeSmellTotal)
                .name("Code smell")
                .types(codeSmellCategories)
                .build();
        IssueFilterSidebarVO.IssueFilterSidebar bugSidebar = IssueFilterSidebarVO.IssueFilterSidebar.builder()
                .total(bugTotal)
                .name("Bug")
                .types(bugCategories)
                .build();
        IssueFilterSidebarVO.IssueFilterSidebar warnSidebar = IssueFilterSidebarVO.IssueFilterSidebar.builder()
                .total(warnTotal)
                .name("Warn")
                .types(warnCategories)
                .build();
        IssueFilterSidebarVO.IssueFilterSidebar errorSidebar = IssueFilterSidebarVO.IssueFilterSidebar.builder()
                .total(errorTotal)
                .name("Error")
                .types(errorCategories)
                .build();

        IssueFilterSidebarVO javaSideBarVO = IssueFilterSidebarVO.builder()
                .language("java")
                .categories(new ArrayList<IssueFilterSidebarVO.IssueFilterSidebar>() {{
                    add(codeSmellSidebar);
                    add(bugSidebar);
                }})
                .build();
        IssueFilterSidebarVO jsSideBarVO = IssueFilterSidebarVO.builder()
                .language("js")
                .categories(new ArrayList<IssueFilterSidebarVO.IssueFilterSidebar>() {{
                    add(warnSidebar);
                    add(errorSidebar);
                }})
                .build();

        result.add(javaSideBarVO);
        result.add(jsSideBarVO);

        return result;
    }

    @Override
    public Map<String, Object> getIssueFilterListCount(Map<String, Object> query) {

        Map<String, Object> issueFilterList = new HashMap<>(16);

        issueFilterList.put("total", query.get(DEVELOPER) != null ? issueDao.getIssueFilterListCount(query) : query.get(SOLVER) != null ? issueDao.getSolvedIssueFilterListCount(query) : issueDao.getIssueFilterListCount(query));

        return issueFilterList;
    }

    @Override
    public Map<String, Object> getIssueFilterList(Map<String, Object> query, Map<String, Object> issueFilterList) {

        List<Map<String, Object>> issues = query.get(DEVELOPER) != null ? issueDao.getIssueFilterList(query) : query.get(SOLVER) != null ? issueDao.getSolvedIssueFilterList(query) : issueDao.getIssueFilterList(query);

        for (Map<String, Object> issue : issues) {
            issue.put(START_COMMIT_DATE, DateTimeUtil.format((Date) issue.get(START_COMMIT_DATE)));
            issue.put("endCommitDate", DateTimeUtil.format((Date) issue.get("endCommitDate")));
            issue.put("createTime", DateTimeUtil.format((Date) issue.get("createTime")));
            if (SOLVED_STR.equals(issue.get(STATUS).toString())) {
                issue.put(SOLVER, issue.get(SOLVER));
                issue.put(SOLVE_TIME, issue.get("commit_time"));
                issue.put(SOLVE_COMMIT, issue.get("commit_id"));
            } else {
                issue.put(SOLVER, null);
                issue.put(SOLVE_TIME, null);
                issue.put(SOLVE_COMMIT, null);
            }
            String priority = Objects.requireNonNull(JavaIssuePriorityEnum.getPriorityEnumByRank((Integer) issue.get(PRIORITY))).getName();
            issue.put(PRIORITY, priority);
        }

        if (query.get("ps") != null) {
            int size = (int) query.get("ps");
            int total = (int) issueFilterList.get("total");

            issueFilterList.put("totalPage", total % size == 0 ? total / size : total / size + 1);
        }
        issueFilterList.put("issueList", issues);

        return issueFilterList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getIssueFilterListWithDetail(Map<String, Object> query, Map<String, Object> issueFilterList) {
        //location: startLine endLine code issueType className methodName
        if (query.get("detail").equals(false)) {
            return issueFilterList;
        }

        List<Map<String, Object>> issuesDetail = (List<Map<String, Object>>) issueFilterList.get("issueList");

        for (Map<String, Object> issue : issuesDetail) {
            String uuid = (String) issue.get("uuid");
            List<String> latestVersionRawIssueUuids = rawIssueDao.getLatestVersionRawIssueUuids(new ArrayList<String>() {{
                add(uuid);
            }});
            issue.put("detail", locationDao.getLocations(latestVersionRawIssueUuids.get(0)));
        }

        return issueFilterList;
    }

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    @Autowired
    public void setIssueDao(IssueDao issueDao) {
        this.issueDao = issueDao;
    }

    @Autowired
    public void setScanResultDao(ScanResultDao scanResultDao) {
        this.scanResultDao = scanResultDao;
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
    public void setIssueRepoDao(IssueRepoDao issueRepoDao) {
        this.issueRepoDao = issueRepoDao;
    }

    @Autowired
    public void setIssueScanDao(IssueScanDao issueScanDao) {
        this.issueScanDao = issueScanDao;
    }

    @Autowired
    public void setRawIssueMatchInfoDao(RawIssueMatchInfoDao rawIssueMatchInfoDao) {
        this.rawIssueMatchInfoDao = rawIssueMatchInfoDao;
    }
}
