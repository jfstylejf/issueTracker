package cn.edu.fudan.issueservice.service.impl;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.dao.*;
import cn.edu.fudan.issueservice.domain.dbo.Location;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.domain.enums.JavaIssuePriorityEnum;
import cn.edu.fudan.issueservice.domain.enums.IssueStatusEnum;
import cn.edu.fudan.issueservice.domain.enums.IssueTypeEnum;
import cn.edu.fudan.issueservice.service.IssueService;
import cn.edu.fudan.issueservice.util.DateTimeUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

    private RawIssueDao rawIssueDao;

    private LocationDao locationDao;

    private IssueRepoDao issueRepoDao;

    private IssueScanDao issueScanDao;

    private final String open = "open", solved = "solved", all = "quantity", remainingIssueCount = "remainingIssueCount", eliminatedIssueCount ="eliminatedIssueCount", newIssueCount = "newIssueCount";

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Async
    public void deleteIssueByRepoIdAndTool(String repoUuid, String tool) {
        logger.info("start to delete issue -> repoUuid={} , tool={}", repoUuid, tool);
        //根据rawIssue来删库(便于控制每次删除的数量,防止超时锁表)
        List<RawIssue>  rawIssues = rawIssueDao.getRawIssueByRepoIdAndTool(repoUuid, tool);
        List<String> rawIssueUuidList = new ArrayList<>();
        rawIssues.forEach(rawIssue -> rawIssueUuidList.add(rawIssue.getUuid()));
        //每次删除5000条rawIssue,防止超时锁表
        int deleteRawIssueCount = 0;
        while(deleteRawIssueCount < rawIssues.size()){
            int firstIndex = deleteRawIssueCount;
            deleteRawIssueCount += 5000;
            List<String> partOfRawIssueIds = new ArrayList<>(rawIssueUuidList.subList(firstIndex, Math.min(deleteRawIssueCount, rawIssues.size())));
            locationDao.deleteLocationByRawIssueIds(partOfRawIssueIds);
            rawIssueDao.deleteRawIssueByIds (partOfRawIssueIds);
        }
        //删除issue,issue_repo,issue_scan,scan_result表记录
        issueDao.deleteIssueByRepoIdAndTool (repoUuid, tool);
        issueRepoDao.delIssueRepo (repoUuid, null, tool);
        issueScanDao.deleteIssueScanByRepoIdAndTool (repoUuid, tool);
        scanResultDao.deleteScanResultsByRepoIdAndCategory (repoUuid, tool);
        //完成删库
        logger.info("finish deleting issues -> repoUuid={} , tool={}",repoUuid,tool);
    }

    @Override
    public Map<String, List<Map<String, String>>> getRepoWithIssues(String developer) {

        List<String> repoIdList = issueDao.getRepoWithIssues(developer);

        List<Map<String, String>> repoInfo = new ArrayList<>();

        Map<String, List<Map<String, String>>> projectInfo = new HashMap<>(32);

        for (String repoId : repoIdList) {
            Map<String, String> project = restInterfaceManager.getProjectByRepoId(repoId);
            projectInfo.put(project.get("projectName"), new ArrayList<>());
            repoInfo.add(project);
        }

        for(Map<String, String> tempRepo : repoInfo){
            List<Map<String, String>> project= projectInfo.get(tempRepo.get("projectName"));
            project.add(tempRepo);
            projectInfo.put(tempRepo.get("projectName"), project);
        }

        return projectInfo;
    }

    @Override
    public List<String> getExistIssueTypes(String tool) {
        List<String> types = issueDao.getExistIssueTypes(tool);
        String clone = "clone";
        if(clone.equals(tool)){
            types.sort(Comparator.comparingInt(Integer::valueOf));
        }
        return types;
    }

    @Override
    public void updatePriority(String issueId, String severity,String token) throws Exception {
        JavaIssuePriorityEnum javaIssuePriorityEnum = JavaIssuePriorityEnum.getPriorityEnum (severity);
        if(javaIssuePriorityEnum == null){
            throw new Exception("priority is not illegal");
        }

        int priorityInt = javaIssuePriorityEnum.getRank ();
        issueDao.updateOneIssuePriority(issueId,priorityInt);
    }

    @Override
    public void updateStatus(String issueId, String status,String token) {
        issueDao.updateOneIssueStatus(issueId, status, status);
    }

    @Override
    public List<Map<String, Object>> getRepoIssueCounts(List<String> repoUuids, String since, String until, String tool) {
        List<Map<String, Object>> result = new ArrayList<>();

        LocalDate firstDate = LocalDate.parse(scanResultDao.findFirstDateByRepo(repoUuids).substring(0, 10), DateTimeUtil.Y_M_D_formatter);
        LocalDate indexDay = LocalDate.parse(since,DateTimeUtil.Y_M_D_formatter);
        LocalDate untilDay = LocalDate.parse(until,DateTimeUtil.Y_M_D_formatter);

        if(indexDay.isBefore(firstDate)){
            while(indexDay.isBefore(firstDate)){
                Map<String, Object> map = new HashMap<>(8);
                map.put("date", indexDay.toString());
                map.put(newIssueCount, 0);
                map.put(eliminatedIssueCount, 0);
                map.put(remainingIssueCount, 0);
                result.add(map);
                indexDay = indexDay.plusDays(1);
            }
        }

        LocalDate indexDay2 = LocalDate.parse(indexDay.toString(),DateTimeUtil.Y_M_D_formatter);
        Map<String, Object> firstDateScanResult = findFirstDateScanResult(repoUuids, indexDay2, firstDate, tool);
        result.add(firstDateScanResult);
        indexDay = indexDay.plusDays(1);

        while(untilDay.isAfter(indexDay) || untilDay.isEqual(indexDay)){
            Map<String, Object> map = new HashMap<>(16);
            List<Map<String, Object>> repoIssueCounts2 = scanResultDao.getRepoIssueCounts(repoUuids, indexDay.toString(), indexDay.toString(), tool, null);
            if(repoIssueCounts2.size() == 0){
                map.put(newIssueCount, 0);
                map.put(eliminatedIssueCount, 0);
                map.put(remainingIssueCount, result.get(result.size() - 1).get(remainingIssueCount));
            }else{
                BigDecimal now = new BigDecimal((String)repoIssueCounts2.get(repoIssueCounts2.size() - 1).get(remainingIssueCount));
                BigDecimal last = new BigDecimal((String)result.get(result.size() - 1).get(remainingIssueCount));
                long temp = now.longValue() - last.longValue();
                map.put(newIssueCount, temp > 0 ? temp : 0);
                map.put(eliminatedIssueCount, temp < 0 ? -temp : 0);
                map.put(remainingIssueCount, repoIssueCounts2.get(repoIssueCounts2.size() - 1).get(remainingIssueCount));
            }
            map.put("date", indexDay.toString());
            result.add(map);
            indexDay = indexDay.plusDays(1);
        }

        return result;
    }

    private Map<String, Object> findFirstDateScanResult(List<String> repoUuids, LocalDate indexDay, LocalDate firstDate, String tool) {
        Map<String, Object> map = new HashMap<>(8);
        map.put("date", indexDay.toString());
        while(indexDay.isAfter(firstDate) || indexDay.isEqual(firstDate)){
            List<Map<String, Object>> repoIssueCounts = scanResultDao.getRepoIssueCounts(repoUuids, indexDay.toString(), indexDay.toString(), tool, null);
            if(repoIssueCounts.size() != 0){
                map.put(newIssueCount, repoIssueCounts.get(repoIssueCounts.size() - 1).get(newIssueCount));
                map.put(eliminatedIssueCount, repoIssueCounts.get(repoIssueCounts.size() - 1).get(eliminatedIssueCount));
                map.put(remainingIssueCount, repoIssueCounts.get(repoIssueCounts.size() - 1).get(remainingIssueCount));
                break;
            }
            indexDay = indexDay.minusDays(1);
        }
        return map;
    }

    @Override
    public List<String> getIssueCategories(String toolName) {

        List<String> issueCategories = new ArrayList<>();

        for(IssueTypeEnum issueTypeEnum : IssueTypeEnum.values()){
            if(issueTypeEnum.getTool().equals(toolName)){
                issueCategories.add(issueTypeEnum.getCategory());
            }
        }

        return issueCategories;
    }

    @Override
    public List<String> getIssueSeverities() {

        List<String> issueSeverities = new ArrayList<> ();

        List<JavaIssuePriorityEnum> javaIssuePriorityEnums = new ArrayList<>(Arrays.asList(JavaIssuePriorityEnum.values()));

        javaIssuePriorityEnums = javaIssuePriorityEnums.stream().sorted(Comparator.comparing(JavaIssuePriorityEnum:: getRank)).collect(Collectors.toList ());

        for(JavaIssuePriorityEnum javaIssuePriorityEnum : javaIssuePriorityEnums){
            issueSeverities.add(javaIssuePriorityEnum.getName());
        }

        return issueSeverities;
    }

    @Override
    public List<String> getIssueStatus() {

        List<String> issueStatus = new ArrayList<>();

        for(IssueStatusEnum issueStatusEnum : IssueStatusEnum.values()){
            issueStatus.add(issueStatusEnum.getName());
        }

        return issueStatus;
    }

    @Override
    public List<String> getIssueIntroducers(List<String> repoUuids) {
        return issueDao.getIssueIntroducers(repoUuids);
    }

    @Override
    public Map<String, Object> getIssueFilterListCount(Map<String, Object> query) {

        Map<String, Object> issueFilterList = new HashMap<>(16);

        issueFilterList.put("total", query.get("developer") != null ? issueDao.getIssueFilterListCount(query) : query.get("solver") != null ? issueDao.getSolvedIssueFilterListCount(query) : issueDao.getIssueFilterListCount(query));

        return issueFilterList;
    }

    @Override
    public Map<String, Object> getIssueFilterList(Map<String, Object> query, Map<String, Object> issueFilterList){

        List<Map<String, Object>> issues = query.get("developer") != null ? issueDao.getIssueFilterList(query) : query.get("solver") != null ? issueDao.getSolvedIssueFilterList(query) : issueDao.getIssueFilterList(query);

        for(Map<String, Object> issue : issues){
            String issueId = (String) issue.get("uuid");
            issue.put("startCommitDate", DateTimeUtil.format((Date) issue.get("startCommitDate")));
            issue.put("endCommitDate", DateTimeUtil.format((Date) issue.get("endCommitDate")));
            issue.put("createTime", DateTimeUtil.format((Date) issue.get("createTime")));
            if("Solved".equals(issue.get("status").toString())) {
                Map<String, Object> lastSolvedInfo = rawIssueDao.getLastSolvedInfoOfOneIssue(issueId);
                if(lastSolvedInfo != null){
                    issue.put("solver", lastSolvedInfo.get("lastSolver"));
                    issue.put("solveTime", DateTimeUtil.format((Date) lastSolvedInfo.get("commit_time")));
                    issue.put("solveCommit", lastSolvedInfo.get("commit_id"));
                }
            }else{
                issue.put("solver", null);
                issue.put("solveTime", null);
                issue.put("solveCommit", null);
            }
            String priority = Objects.requireNonNull(JavaIssuePriorityEnum.getPriorityEnumByRank((Integer) issue.get("priority"))).getName();
            issue.put("priority",priority);
        }

        if(query.get("ps") != null) {
            int size = (int) query.get("ps");
            int total = (int) issueFilterList.get("total");

            issueFilterList.put("totalPage", total % size == 0 ? total / size : total / size + 1);
        }
        issueFilterList.put("issueList", issues);

        return issueFilterList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getIssueFilterListWithDetail(Map<String, Object> query, Map<String, Object> issueFilterList){
        //location: startLine endLine code issueType className methodName
        if(query.get("detail").equals(false)){
            return issueFilterList;
        }

        List<Map<String, Object>> issuesDetail = (List<Map<String, Object>>) issueFilterList.get("issueList");

        Iterator<Map<String, Object>> iterator = issuesDetail.iterator();

        String commit = query.get("commit") == null ? null : query.get("commit").toString();

        while(iterator.hasNext()){
            Map<String, Object> issue = iterator.next();
            String rawIssueUuid ="Solved".equals(issue.get("status")) ?
                    rawIssueDao.getRawIssueUuidByIssueUuidAndCommit(issue.get("uuid").toString(), (String)issue.get("endCommit")) :
                    rawIssueDao.getRawIssueUuidByIssueUuidAndCommit(issue.get("uuid").toString(), commit);
            List<Location> locations = StringUtils.isEmpty(rawIssueUuid) ? null : locationDao.getLocations(rawIssueUuid);
            issue.put("detail", locations);
        }

        return issueFilterList;
    }

    @Override
    public Map<String, Object> getIssueFilterListWithOrder(Map<String, Object> query, Map<String, Object> issueFilterList) {

        if("no".equals(query.get("order"))){
            return issueFilterList;
        }

        query.put("start", null);
        query.put("ps", null);

        List<Map<String, Object>> allIssueFilterList = query.get("developer") != null ? issueDao.getIssueFilterList(query) : query.get("solver") != null ? issueDao.getSolvedIssueFilterList(query) : issueDao.getIssueFilterList(query);

        Map<String, JSONObject> allIssueTypeInfo = new HashMap<>(64);

        for(Map<String, Object> issue : allIssueFilterList){
            String type = (String) issue.get("type");
            JSONObject issueTypeInfo = allIssueTypeInfo.getOrDefault(type, new JSONObject() {{
                put(all, 0);
                put(solved, 0);
                put(open, 0);
            }});
            issueTypeInfo.put(all, issueTypeInfo.getInteger(all) + 1);
            issueTypeInfo.put(solved, "Solved".equals(issue.get("status")) ? issueTypeInfo.getInteger(solved) + 1 : issueTypeInfo.getInteger(solved));
            issueTypeInfo.put(open, "Open".equals(issue.get("status")) ? issueTypeInfo.getInteger(open) + 1 : issueTypeInfo.getInteger(open));
            allIssueTypeInfo.put((String) issue.get("type"), issueTypeInfo);
        }

        List<Map.Entry<String, JSONObject>> allIssueTypeList = sortInDifferentConditions(allIssueTypeInfo,(String) query.get("order"), (Boolean) query.get("asc"));

        issueFilterList.put("issueListSortByType", allIssueTypeList);

        return issueFilterList;
    }

    private List<Map.Entry<String, JSONObject>> sortInDifferentConditions(Map<String, JSONObject> allIssueTypeInfo, String order, Boolean asc){

        List<Map.Entry<String, JSONObject>> allIssueTypeList = new ArrayList<>(allIssueTypeInfo.entrySet());

        if(asc){
            if(open.equals(order)){
                allIssueTypeList.sort((Comparator.comparingInt(o -> o.getValue().getInteger(open))));
            }else if(solved.equals(order)){
                allIssueTypeList.sort((Comparator.comparingInt(o -> o.getValue().getInteger(solved))));
            }else{
                allIssueTypeList.sort((Comparator.comparingInt(o -> o.getValue().getInteger(all))));
            }
            return allIssueTypeList;
        }

        if(open.equals(order)){
            allIssueTypeList.sort(((o1, o2) -> o2.getValue().getInteger(open) - o1.getValue().getInteger(open)));
        }else if(solved.equals(order)){
            allIssueTypeList.sort(((o1, o2) -> o2.getValue().getInteger(solved) - o1.getValue().getInteger(solved)));
        }else{
            allIssueTypeList.sort(((o1, o2) -> o2.getValue().getInteger(all) - o1.getValue().getInteger(all)));
        }

        return allIssueTypeList;
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
}
