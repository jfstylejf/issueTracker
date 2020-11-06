package cn.edu.fudan.issueservice.service.impl;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.dao.*;
import cn.edu.fudan.issueservice.domain.IssueCount;
import cn.edu.fudan.issueservice.domain.IssueStatisticInfo;
import cn.edu.fudan.issueservice.domain.dbo.Issue;
import cn.edu.fudan.issueservice.domain.dbo.IssueScan;
import cn.edu.fudan.issueservice.domain.dbo.Location;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.domain.dto.IssueCountPo;
import cn.edu.fudan.issueservice.domain.dto.IssueParam;
import cn.edu.fudan.issueservice.domain.enums.IssuePriorityEnum;
import cn.edu.fudan.issueservice.domain.enums.IssueStatusEnum;
import cn.edu.fudan.issueservice.domain.enums.IssueTypeEnum;
import cn.edu.fudan.issueservice.scheduler.QuartzScheduler;
import cn.edu.fudan.issueservice.service.IssueService;
import cn.edu.fudan.issueservice.util.DateTimeUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
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
    private Logger logger = LoggerFactory.getLogger(IssueServiceImpl.class);

    @Value("${solved.tag_id}")
    private String solvedTagId;
    @Value("${ignore.tag_id}")
    private String ignoreTagId;

    private RestInterfaceManager restInterfaceManager;

    private QuartzScheduler quartzScheduler;

    private StringRedisTemplate stringRedisTemplate;

    private IssueDao issueDao;

    private ScanResultDao scanResultDao;

    private RawIssueDao rawIssueDao;

    private LocationDao locationDao;

    private IssueRepoDao issueRepoDao;

    private IssueScanDao issueScanDao;

    private final String open = "open", solved = "solved", all = "quantity";

    /**
     *  todo 更新删除逻辑，并且结合事务管理
     * @param repoUuid
     * @param tool
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Async
    public void deleteIssueByRepoIdAndTool(String repoUuid,String tool) {
        logger.info("start to delete issue -> repoUuid={} , tool={}",repoUuid,tool);

        //先获取该repo 相应tool 的所有rawIssue
        List<RawIssue>  rawIssues = rawIssueDao.getRawIssueByRepoIdAndTool(repoUuid, tool);
        int rawIssueListSize = rawIssues.size ();
        String[] rawIssueIds = new String[rawIssueListSize];
        for(int i = 0 ; i < rawIssues.size () ; i++){
            rawIssueIds[i] = rawIssues.get (i).getUuid ();
        }



        int deleteLocationByRawIssueCount = 0;
        //先删除location 和 raw issue,一次性删除太多会超时
        while(deleteLocationByRawIssueCount < rawIssueListSize){
            int maxLength = 0;
            int firstIndex = deleteLocationByRawIssueCount;
            deleteLocationByRawIssueCount += 4000;
            if(deleteLocationByRawIssueCount > rawIssueListSize){
                maxLength = rawIssueListSize;
            }else{
                maxLength = deleteLocationByRawIssueCount;
            }

            List<String> partOfRawIssueIds = new ArrayList<> ();
            for(int j = firstIndex ; j < maxLength ; j++){
                partOfRawIssueIds.add (rawIssueIds[j]);
            }


            locationDao.deleteLocationByRawIssueIds(partOfRawIssueIds);
            rawIssueDao.deleteRawIssueByIds (partOfRawIssueIds);
        }

        //再删除issue  以及其他信息
        issueDao.deleteIssueByRepoIdAndTool (repoUuid, tool);

        scanResultDao.deleteScanResultsByRepoIdAndCategory (repoUuid, tool);

        issueRepoDao.delIssueRepo (repoUuid, null, tool);

        issueScanDao.deleteIssueScanByRepoIdAndTool (repoUuid, tool);

        logger.info("finish deleting issues -> repoUuid={} , tool={}",repoUuid,tool);

    }

    @Override
    public Issue getIssueByID(String uuid) {
        return issueDao.getIssueByID(uuid);
    }

    @Override
    public Map<String, List<Map<String, String>>> getRepoWithIssues(String developer) {

        List<String> repoIdList = issueDao.getRepoWithIssues(developer);

        List<Map<String, String>> repoInfo = new ArrayList<>();

        Map<String, List<Map<String, String>>> projectInfo = new HashMap<>();

        for (int i = 0; i < repoIdList.size(); i++){
            String repoId = repoIdList.get(i);
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
    public Map<String, Object> getFilteredIssueList(JSONObject requestParam, String userToken) {

        Map<String, Object> result = new HashMap<>();

        Map<String, Object> query = new HashMap<>();

        /*
         * 1.仅项目筛选 module="...",repoList="" => project-service => repoList="..."  最后根据repoList筛选
         * 2.项目下的某些库筛选 module="...",repoList="..." 根据repoList筛选
         * 3.仅对库筛选 module="",repoList="..." 根据repoList筛选
         * 4.什么都不筛选 module="",repoList=""
         * */
        String module = requestParam.getString("module");

        String repoList = requestParam.getString("repoList");

        List<String> allRepo = new ArrayList<>();

        if(repoList != null && repoList.length() > 0){
            String[] tempList = repoList.split(",");
            for(String str : tempList){
                allRepo.add(str);
            }
        }else{
            if(module != null && module.length() > 0){
                JSONObject allProject = restInterfaceManager.getAllRepo(userToken);
                if(allProject != null){
                    JSONArray repo = allProject.getJSONArray(module);
                    for(int i = 0;i < repo.size(); i++){
                        String tempRepo = repo.getJSONObject(i).getString("repo_id");
                        if(tempRepo != null){
                            allRepo.add(tempRepo);
                        }
                    }
                }
            }
        }

        int size = requestParam.getIntValue("size");
        int page = requestParam.getIntValue("page");
        //默认查询所有repo

        if (size == 0 || page == 0) {
            size = 10;
            page = 1;
        }

        String toolName = requestParam.getString("toolName");
        JSONArray types = requestParam.getJSONArray("types");
        JSONArray issueStatus = requestParam.getJSONArray("issueStatus");
        String since = requestParam.getString ("since");
        String until = requestParam.getString ("until");
        String developer = requestParam.getString ("developer");
        String issueCategory = requestParam.getString ("category");
        String tempPriority = requestParam.getString("priority");
        int priority = -1;
        if(tempPriority != null){
            priority = IssuePriorityEnum.getPriorityEnum(tempPriority).getRank();
        }


        if(since != null ){
            if(!since.matches("([0-9]+)-([0-9]{2})-([0-9]{2})")){
                throw new RuntimeException(" The input format newInstance since should be like 2019-10-01") ;
            }
        }

        if(until  != null ){
            if(!until.matches("([0-9]+)-([0-9]{2})-([0-9]{2})")){
                throw new RuntimeException(" The input format newInstance until should be like 2019-10-01") ;
            }
            until = DateTimeUtil.stringToLocalDate(until).plusDays(1).toString();
        }

        query.put ("toolName", toolName);
        query.put ("developer", developer);

        List<Issue> allIssues = issueDao.getIssueList(query);


        query.put ("priority", priority);
        query.put ("issueCategory", issueCategory);
        if(types!=null && !types.isEmpty()){
            query.put("types", types.toJavaList(String.class));
        }
        if(issueStatus!=null && !issueStatus.isEmpty()){
            query.put("issue_status", issueStatus.toJavaList(String.class));
        }
        query.put ("since",since);
        query.put ("until",until);
        if(allRepo.size() > 0){
            query.put("repo_id",allRepo);
        }
        int count = issueDao.getIssueCount(query);
        query.put("size", size);
        query.put("start", (page - 1) * size);
//        List<Issue> issues = issueDao.getIssueList(query);
//        updateIssueListSeverity(issues);
        //updateIssueListSurvivalTime(issues,repoId,toolName);
        List<Map<String, Object>> issues = issueDao.getIssueWithAdder(query);
        for (Map<String, Object> issue : issues){
            String issueId = (String) issue.get("uuid");
            Map<String, Object> lastSolvedInfo = rawIssueDao.getLastSolvedInfoOfOneIssue(issueId);
            String lastSolver;
            Timestamp lastSolveTime;
            String lastSolveCommit;
            if (lastSolvedInfo == null) {
                lastSolver = null;
                lastSolveTime = null;
                lastSolveCommit = null;
            } else {
                lastSolver = (String) lastSolvedInfo.get("lastSolver");
                lastSolveTime = (Timestamp) lastSolvedInfo.get("commit_time");
                lastSolveCommit = (String) lastSolvedInfo.get("commit_id");
            }
            String severity = IssuePriorityEnum.getPriorityEnumByRank((Integer) issue.get("priority")).getName();
            issue.put("lastSolver",lastSolver);
            issue.put("lastSolveTime",lastSolveTime);
            issue.put("lastSolveCommit",lastSolveCommit);
            issue.put("severity",severity);
        }
        Map<String, JSONObject> issueType = new HashMap<>();
        for(Issue issue : allIssues){
            if(issueType.get(issue.getType()) != null){
                JSONObject issueObj = issueType.get(issue.getType());
                if("Open".equals(issue.getStatus())){
                    issueObj.put("open", issueObj.getInteger("open") + 1);
                }else{
                    issueObj.put("solved", issueObj.getInteger("solved") + 1);
                }
                issueObj.put("all", issueObj.getInteger("all") + 1);
                issueType.put(issue.getType(), issueObj);
            }else{
                JSONObject issueObj = new JSONObject();
                if("Open".equals(issue.getStatus())) {
                    issueObj.put("open", 1);
                    issueObj.put("solved",0);
                }else{
                    issueObj.put("open", 0);
                    issueObj.put("solved",1);
                }
                issueObj.put("all",1);
                issueObj.put("checked", false);
                issueType.put(issue.getType(), issueObj);
            }
        }
        List<JSONObject> top5 = new ArrayList<>();
        for(int i = 0;i < 5 && i < issueType.size(); i++){
            JSONObject maxIssue =new JSONObject();
            maxIssue.put("all", -1);
            String maxIssueTypeName = "";
            for(Map.Entry<String, JSONObject> entry : issueType.entrySet()){
                if(entry.getValue().getBoolean("checked") == false && maxIssue.getInteger("all") < entry.getValue().getInteger("all")){
                    maxIssue.put("all", entry.getValue().getInteger("all"));
                    maxIssue.put("open", entry.getValue().getInteger("open"));
                    maxIssue.put("solved", entry.getValue().getInteger("solved"));
                    maxIssueTypeName = entry.getKey();
                }
            }
            maxIssue.put("issueName",maxIssueTypeName);
            issueType.get(maxIssueTypeName).put("checked",true);
            top5.add(maxIssue);
        }
        result.put("issueTop5",top5);
        result.put("totalPage", count % size == 0 ? count / size : count / size + 1);
        result.put("totalCount", count);
        result.put("issueList", issues);
        return result;
    }

    @Override
    public Map<String, Object> getIssueCountWithCategoryByCondition(JSONObject requestParam, String userToken) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> query = new HashMap<>();

        String module = requestParam.getString("module");
        String repoList = requestParam.getString("repoList");
        List<String> allRepo = new ArrayList<>();

        if(repoList != null && repoList.length() > 0){
            String[] tempList = repoList.split(",");
            for(String str : tempList){
                allRepo.add(str);
            }
        }else{
            if(module != null && module.length() > 0){
                JSONObject allProject = restInterfaceManager.getAllRepo(userToken);
                if(allProject != null){
                    JSONArray repo = allProject.getJSONArray(module);
                    if(repo == null){
                        throw new RuntimeException("module error!") ;
                    }else{
                        for(int i = 0;i < repo.size(); i++){
                            String tempRepo = repo.getJSONObject(i).getString("repo_id");
                            if(tempRepo != null){
                                allRepo.add(tempRepo);
                            }
                        }
                    }
                }
            }
        }

        String toolName = requestParam.getString("toolName");
        JSONArray types = requestParam.getJSONArray("types");
        JSONArray issueStatus = requestParam.getJSONArray("issueStatus");
        String since = requestParam.getString ("since");
        String until = requestParam.getString ("until");
        String severity = requestParam.getString ("severity");
        String developer = requestParam.getString ("developer");


        if(since != null ){
            if(!since.matches("([0-9]+)-([0-9]{2})-([0-9]{2})")){
                throw new RuntimeException(" The input format newInstance since should be like 2019-10-01") ;
            }
        }

        if(until  != null ){
            if(!until.matches("([0-9]+)-([0-9]{2})-([0-9]{2})")){
                throw new RuntimeException(" The input format newInstance until should be like 2019-10-01") ;
            }
            until = DateTimeUtil.stringToLocalDate(until).plusDays(1).toString();
        }
        Integer severityInt = null;
        if(severity != null && !severity.isEmpty ()){
            severityInt = IssuePriorityEnum.getPriorityEnum (severity).getRank ();
        }

        query.put ("repo_id", allRepo.size() == 0 ? null : allRepo);
        query.put ("toolName", toolName);
        query.put ("priority", severityInt);
        query.put ("developer", developer);
        query.put ("since",since);
        query.put ("until",until);
        if(types!=null && !types.isEmpty()){
            query.put("types", types.toJavaList(String.class));
        }
        if(issueStatus!=null && !issueStatus.isEmpty()){
            query.put("issue_status", issueStatus.toJavaList(String.class));
        }
        List<Issue> issues = issueDao.getIssueList(query);
        Map<String, Map<String, Integer>> issueCountByType =new HashMap<>();
        for(Issue issue : issues){
            if(issue.getIssueCategory() == null){
                continue;
            }
            if(issueCountByType.get(issue.getIssueCategory()) == null){
                Map<String, Integer> tempMap = new HashMap<>();
                tempMap.put(issue.getType(), 1);
                issueCountByType.put(issue.getIssueCategory(), tempMap);
            }else if(issueCountByType.get(issue.getIssueCategory()).get(issue.getType()) == null){
                Map<String, Integer> tempMap = issueCountByType.get(issue.getIssueCategory());
                tempMap.put(issue.getType(), 1);
                issueCountByType.put(issue.getIssueCategory(), tempMap);
            }else{
                Map<String, Integer> tempMap = issueCountByType.get(issue.getIssueCategory());
                tempMap.put(issue.getType(), tempMap.get(issue.getType()) + 1);
                issueCountByType.put(issue.getIssueCategory(), tempMap);
            }
        }
        result.put("issueCountByType", issueCountByType);
        Map<String, Integer> issueCountWithCategory = issueDao.getIssueCountWithCategory(query);
        if(issueCountWithCategory != null){
            for(Map.Entry<String, Integer> entry : issueCountWithCategory.entrySet ()){
                String key ;
                if(entry.getKey () == null){
                    key = "null";
                }else{
                    key = entry.getKey ();
                }
                result.put (key, entry.getValue ());
            }
        }
        return result;
    }

    @Override
    public List<String> getExistIssueTypes(String tool) {
        List<String> types = issueDao.getExistIssueTypes(tool);
        if(tool.equals("clone")){
            types.sort(Comparator.comparingInt(Integer::valueOf));
        }
        return types;
    }

    @Override
    public void updateIssueCount(String time) {
        quartzScheduler.updateIssueCount(time);
    }

    @Override
    public void updatePriority(String issueId, String severity,String token) throws Exception {
        IssuePriorityEnum issuePriorityEnum = IssuePriorityEnum.getPriorityEnum (severity);
        if(issuePriorityEnum == null){
            throw new Exception("priority is not illegal");
        }

        int priorityInt = issuePriorityEnum.getRank ();
        issueDao.updateOneIssuePriority(issueId,priorityInt);
    }

    @Override
    public void updateStatus(String issueId, String status,String token) {
        issueDao.updateOneIssueStatus(issueId, status, status);
    }

    @Override
    public List<Map<String, Object>> getRepoIssueCounts(String repo_id, String since, String until, String tool) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate indexDay = LocalDate.parse(since,DateTimeUtil.Y_M_D_formatter);
        LocalDate untilDay = LocalDate.parse(until,DateTimeUtil.Y_M_D_formatter);
        while(untilDay.isAfter(indexDay) || untilDay.isEqual(indexDay)){
            List<Map<String, Object>> queryResultList = scanResultDao.getRepoIssueCounts(repo_id, indexDay.toString(), indexDay.toString(), tool, null);
            Map<String, Object> map = new HashMap<>();
            //没有提交commit的所有数据都置为0（newIssue, eliminatedIssue可以直接返回0，但是remainingIssue需要做处理）
            if (queryResultList.size() == 0){
                map.put("date", indexDay.toString());
                map.put("newIssueCount", 0);
                map.put("eliminatedIssueCount", 0);
                map.put("remainingIssueCount", 0);
            }else {
                map = queryResultList.get(0);
            }
            result.add(map);
            indexDay = indexDay.plusDays(1);
        }

        //若since这一天为空数据，则查找所选日期范围内的第一次正式提交commit的index，并以此来修改since这一天的数据
        if (Integer.parseInt(result.get(0).get("remainingIssueCount").toString()) == 0){
            int firstCommitIndex = 0;//第一次正式提交commit的index
            for (int i = 0; i < result.size(); i++){
                if (Integer.parseInt(result.get(i).get("remainingIssueCount").toString()) != 0){
                    firstCommitIndex = i;
                    break;
                }
            }
            //修改since这一天的数据
            Map<String, Object> sinceDayMap = new HashMap<>();
            sinceDayMap.put("date", result.get(0).get("date"));
            sinceDayMap.put("newIssueCount", 0);
            sinceDayMap.put("eliminatedIssueCount", 0);
            int sinceDayRemainingCount = Integer.parseInt(result.get(firstCommitIndex).get("remainingIssueCount").toString()) - Integer.parseInt(result.get(firstCommitIndex).get("newIssueCount").toString()) + Integer.parseInt(result.get(firstCommitIndex).get("eliminatedIssueCount").toString());
            sinceDayMap.put("remainingIssueCount", sinceDayRemainingCount);
            result.set(0,sinceDayMap);
        }
        //修改其余日期中的remaining_count为0的情况，直接继承上一天的remaining_count
        for (int i = 1; i < result.size(); i++){
            if (Integer.parseInt(result.get(i).get("remainingIssueCount").toString()) == 0){
                Map<String, Object> newMap = new HashMap<>();
                newMap.put("date",result.get(i).get("date"));
                newMap.put("newIssueCount", 0);
                newMap.put("eliminatedIssueCount", 0);
                newMap.put("remainingIssueCount",result.get(i-1).get("remainingIssueCount"));
                result.set(i,newMap);
            }
        }

        return result;
        //下面是只返回有commit的数据
//        return scanResultDao.getScanResultsGroupByDay(Collections.singletonList(repo_id),category, since, until);
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

        List<IssuePriorityEnum> issuePriorityEnums = new ArrayList<> ();


        for(IssuePriorityEnum issuePriorityEnum : IssuePriorityEnum.values()){
            issuePriorityEnums.add(issuePriorityEnum);
        }

        issuePriorityEnums = issuePriorityEnums.stream().sorted(Comparator.comparing(IssuePriorityEnum :: getRank)).collect(Collectors.toList ());

        for(IssuePriorityEnum issuePriorityEnum : issuePriorityEnums){
            issueSeverities.add(issuePriorityEnum.getName());
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




    @Deprecated
    @Override
    public void deleteScanResultsByRepoIdAndTool(String repId, String tool) {
        scanResultDao.deleteScanResultsByRepoIdAndCategory(repId, tool);
    }

    /**
     * 等待后续的更新 ，目前不做修改
     * @param issueParam
     * @param userToken
     * @return
     */
    @Override
    public Object getSpecificIssues(IssueParam issueParam, String userToken) {
        Map<String, Object> result = new HashMap<>();

        //必须的几个参数
        String duration=issueParam.getDuration();
        int size=issueParam.getSize();
        int page=issueParam.getPage();
        String category=issueParam.getCategory();
        boolean onlyNew=issueParam.isOnlyNew();
        boolean onlyEliminated=issueParam.isOnlyEliminated();

        //可有可无的几个参数
        String projectId=issueParam.getProjectId();
        List<String> types=issueParam.getTypes();
        List<String> tags=issueParam.getTags();


        Map<String, Object> query = new HashMap<>();
        List<String> issueIds=new ArrayList<>();
        if(onlyNew||onlyEliminated){
            //是通过dashboard点击查询的
            if(projectId!=null&&!projectId.equals("")){
                //查询单个项目
                String repoId = restInterfaceManager.getRepoIdOfProject(projectId);
                query.put("repo_id",repoId);
                addSpecificIssueIdsForRepo(onlyNew,onlyEliminated,repoId,duration,category,issueIds);
            }else{
                //查询该用户的所有项目
                String account_id = restInterfaceManager.getAccountId(userToken);
                JSONArray repoIds = restInterfaceManager.getRepoIdsOfAccount(account_id,category);
                if (repoIds != null&&!repoIds.isEmpty()) {
                    for (int i = 0; i < repoIds.size(); i++) {
                        String currentRepoId = repoIds.getString(i);
                        addSpecificIssueIdsForRepo(onlyNew,onlyEliminated,currentRepoId,duration,category,issueIds);
                    }
                }else{
                    //当前用户没有项目
                    return empty;
                }
            }
            //如果从dashboard过来只查新增和干掉的，但redis并没有相应的issue id,返回empty
            if(issueIds.isEmpty()) {
                return empty;
            }
        }else{
            //点击项目列表查询的，必定是单个项目
            String repoId = restInterfaceManager.getRepoIdOfProject(projectId);
            query.put("repo_id",repoId);
        }
        //根据tag来筛选
        if(tags!=null&&!tags.isEmpty()){
            //特定tag的issue ids
            JSONArray specificTaggedIssueIds = restInterfaceManager.getSpecificTaggedIssueIds(tags);
            if(specificTaggedIssueIds==null||specificTaggedIssueIds.isEmpty()){
                return empty;
            }
            List<String> issueIdsAfterFilter=new ArrayList<>();
            for(String issueId:issueIds){
                //筛选掉那些不是特定tag的issue id
                if(specificTaggedIssueIds.contains(issueId)){
                    issueIdsAfterFilter.add(issueId);
                }
            }
            if(!issueIdsAfterFilter.isEmpty()) {
                query.put("list",issueIdsAfterFilter);
            } else{
                if(!specificTaggedIssueIds.isEmpty()) {
                    query.put("list",specificTaggedIssueIds.toJavaList(String.class));
                }
            }
        }else{
            //不根据tag来筛选时需要自动过滤solved的和ignore的issue_ids
            if(!issueIds.isEmpty()) {
                query.put("list",issueIds);
            }

            List<String> tag_ids = new ArrayList<>();
            tag_ids.add(solvedTagId);
            tag_ids.add(ignoreTagId);
            JSONArray solved_issue_ids = restInterfaceManager.getSolvedIssueIds(tag_ids);
            if (solved_issue_ids != null && solved_issue_ids.size() > 0) {
                query.put("solved_issue_ids", solved_issue_ids.toJavaList(String.class));
            }
        }
        //根据类型来筛选
        if(types!=null&&!types.isEmpty()){
            query.put("types",types);
        }

        query.put("tool",issueParam.getCategory());
        int count=issueDao.getSpecificIssueCount(query);
        query.put("size", size);
        query.put("start", (page - 1) * size);
        List<Issue> issues = issueDao.getSpecificIssues(query);
        addTagInfo(issues);
        result.put("totalPage", count % size == 0 ? count / size : count / size + 1);
        result.put("totalCount", count);
        result.put("issueList", issues);
        return result;
    }

    /**
     * 等待后续的更新 ，目前不做修改
     * @param month
     * @param project_id
     * @param userToken
     * @param tool
     * @return
     */
    @Override
    public Object getNewTrend(Integer month, String project_id, String userToken, String tool) {
        List<IssueCountPo> result=new ArrayList<>();
        String account_id = restInterfaceManager.getAccountId(userToken);
        LocalDate end=LocalDate.now();
        if(project_id==null||project_id.equals("")){
            //需要查询该用户所有项目的扫描情况
            JSONArray repoIds=restInterfaceManager.getRepoIdsOfAccount(account_id,tool);
            if(repoIds!=null&&!repoIds.isEmpty()){
                if(month==1){
                    //过去30天
                    LocalDate start=end.minusMonths(1);
                    return scanResultDao.getScanResultsGroupByDay(repoIds.toJavaList(String.class),tool, DateTimeUtil.y_m_d_format(start),DateTimeUtil.y_m_d_format(end));
                }else if(month==3||month==6){
                    //过去3个月
                    LocalDate start=end.minusMonths(month);
                    while(start.isBefore(end)){
                        LocalDate temp=start.plusWeeks(1);
                        IssueCountPo issueCountPo=scanResultDao.getMergedScanResult(repoIds.toJavaList(String.class),tool, DateTimeUtil.y_m_d_format(start),DateTimeUtil.y_m_d_format(temp));
                        if(issueCountPo!=null) {
                            result.add(issueCountPo);
                        }
                        start=temp;
                    }
                }else{
                    throw new IllegalArgumentException("month should be 1 or 3 or 6");
                }
            }
        }else{
            //只需要查询该项目的扫描情况
            String repoId=restInterfaceManager.getRepoIdOfProject(project_id);
            if(repoId==null) {
                throw new IllegalArgumentException("this project id not exist!");
            }
            if(month==1){
                //过去30天
                LocalDate start=end.minusMonths(1);
                return scanResultDao.getScanResultsGroupByDay(Collections.singletonList(repoId),tool, DateTimeUtil.y_m_d_format(start),DateTimeUtil.y_m_d_format(end));
            }else if(month==3||month==6){
                //过去3个月
                LocalDate start=end.minusMonths(month);
                while(start.isBefore(end)){
                    LocalDate temp=start.plusWeeks(1);
                    IssueCountPo issueCountPo=scanResultDao.getMergedScanResult(Collections.singletonList(repoId),tool, DateTimeUtil.y_m_d_format(start),DateTimeUtil.y_m_d_format(temp));
                    if(issueCountPo!=null) {
                        result.add(issueCountPo);
                    }
                    start=temp;
                }
            }else{
                throw new IllegalArgumentException("month should be 1 or 3 or 6");
            }
        }
        return result;
    }

    /**
     * 等待后续的更新 ，目前不做修改
     * @param month
     * @param project_id
     * @param userToken
     * @param category
     * @return
     */
    @Override
    public Object getStatisticalResults(Integer month, String project_id, String userToken,String category) {
        Map<String, Object> result = new HashMap<>();
        String account_id = restInterfaceManager.getAccountId(userToken);
        String newKey;
        String remainingKey;
        String eliminatedKey;
        if (project_id == null) {
            if (month == 1) {
                newKey = "trend:"+category+":day:new:" + account_id;
                remainingKey = "trend:"+category+":day:remaining:" + account_id;
                eliminatedKey = "trend:"+category+":day:eliminated:" + account_id;
            } else {
                newKey = "trend:"+category+":week:new:" + account_id;
                remainingKey = "trend:"+category+":week:remaining:" + account_id;
                eliminatedKey = "trend:"+category+":week:eliminated:" + account_id;
            }
        } else {
            String repoId = restInterfaceManager.getRepoIdOfProject(project_id);
            if (month == 1) {
                newKey = "trend:"+category+":day:new:" + account_id + ":" + repoId;
                remainingKey = "trend:"+category+":day:remaining:" + account_id + ":" + repoId;
                eliminatedKey = "trend:"+category+":day:eliminated:" + account_id + ":" + repoId;
            } else {
                newKey = "trend:"+category+":week:new:" + account_id + ":" + repoId;
                remainingKey = "trend:"+category+":week:remaining:" + account_id + ":" + repoId;
                eliminatedKey = "trend:"+category+":week:eliminated:" + account_id + ":" + repoId;
            }
        }
        List<String> newList = stringRedisTemplate.opsForList().range(newKey, 0, -1);
        List<String> remainingList = stringRedisTemplate.opsForList().range(remainingKey, 0, -1);
        List<String> eliminatedList = stringRedisTemplate.opsForList().range(eliminatedKey, 0, -1);
        result.put("data", getFormatData(newList, remainingList, eliminatedList));
        return result;
    }

    /**
     * 等待后续的更新 ，目前不做修改
     * @param project_id
     * @param category
     * @return
     */
    @Override
    public Object getAliveAndEliminatedInfo(String project_id, String category) {
        String repoId=restInterfaceManager.getRepoIdOfProject(project_id);
        JSONArray solvedIssueIds=restInterfaceManager.getSpecificTaggedIssueIds(solvedTagId);
        double avgEliminatedTime=0.00;
        long maxAliveTime=0;
        if(solvedIssueIds!=null&&!solvedIssueIds.isEmpty()){
            List<String> solvedIssueIdList=solvedIssueIds.toJavaList(String.class);
            Double avgEliminatedTimeObject=issueDao.getAvgEliminatedTime(solvedIssueIdList,repoId,category);
            avgEliminatedTime=avgEliminatedTimeObject==null?0.00:avgEliminatedTimeObject;
            maxAliveTime=issueDao.getMaxAliveTime(solvedIssueIdList,repoId,category);
        }else{
            //所有issue都是存活的
            maxAliveTime=issueDao.getMaxAliveTime(null,repoId,category);
        }
        return new IssueStatisticInfo(avgEliminatedTime/3600/24,maxAliveTime/3600/24);
    }

    /**
     *  等待后续的更新 ，目前不做修改
     * @param duration
     * @param project_id
     * @param userToken
     * @param category
     * @return
     */
    @Override
    public Object getDashBoardInfo(String duration, String project_id, String userToken,String category) {
        IssueCount result=new IssueCount(0,0,0);
        String account_id = restInterfaceManager.getAccountId(userToken);
        if (project_id == null||project_id.equals("")) {
            //未选择某一个project,显示该用户所有project的dashboard信息
            JSONArray repoIds = restInterfaceManager.getRepoIdsOfAccount(account_id,category);
            if (repoIds != null&&!repoIds.isEmpty()) {
                for (int i = 0; i < repoIds.size(); i++) {
                    String currentRepoId = repoIds.getString(i);
                    result.issueCountUpdate(getOneRepoDashBoardInfo(duration,currentRepoId,category));
                }
            }
        } else {
            //只显示当前所选project的相关dashboard信息
            String currentRepoId = restInterfaceManager.getRepoIdOfProject(project_id);
            return getOneRepoDashBoardInfo(duration,currentRepoId,category);
        }
        return result;
    }

    @Override
    public List<String> getIssueIntroducers(List<String> repoUuids) {
        return rawIssueDao.getIssueIntroducers(repoUuids);
    }

    private List<IssueCount> getFormatData(List<String> newList, List<String> remainingList, List<String> eliminatedList) {
        if (newList == null || remainingList == null || eliminatedList == null) {
            return Collections.emptyList();
        }
        List<IssueCount> list = new ArrayList<>();
        for (int i = 0; i < newList.size(); i++) {
            String[] str1=newList.get(i).split(":");
            String[] str2=eliminatedList.get(i).split(":");
            String[] str3=remainingList.get(i).split(":");
            list.add(new IssueCount(str1[0],Integer.parseInt(str1[1]),Integer.parseInt(str2[1]),Integer.parseInt(str3[1])));
        }
        return list;
    }


    private void addTagInfo(List<Issue> issues) {
        for (Issue issue : issues) {
            JSONArray tags = restInterfaceManager.getTagsOfIssue(issue.getUuid());
            issue.setTags(tags);
        }
    }

    private void addSpecificIssueIdsForRepo(boolean onlyNew,boolean onlyEliminated,String repoId,String duration,String category,List<String> issueIds){
        if(onlyNew){
            List<String> newIssueIds=stringRedisTemplate.opsForList().range("dashboard:"+category+":"+duration+":new:"+ repoId,0,-1);
            if(newIssueIds!=null&&!newIssueIds.isEmpty()) {
                issueIds.addAll(newIssueIds);
            }
        }else if(onlyEliminated){
            List<String> eliminatedIssueIds=stringRedisTemplate.opsForList().range("dashboard:"+category+":"+duration+":eliminated:"+ repoId,0,-1);
            if(eliminatedIssueIds!=null&&!eliminatedIssueIds.isEmpty()) {
                issueIds.addAll(eliminatedIssueIds);
            }
        }
    }


    private IssueCount getOneRepoDashBoardInfo(String duration,String repoId,String category){
        Object newObject=stringRedisTemplate.opsForHash().get("dashboard:"+category+":"+ duration + ":" + repoId, "new");
        int newCount = newObject==null?0:Integer.parseInt((String)newObject);
        Object remainingObject=stringRedisTemplate.opsForHash().get("dashboard:"+category+":" + duration + ":" + repoId, "remaining");
        int remainingCount = remainingObject==null?0:Integer.parseInt((String)remainingObject);
        Object eliminatedObject=stringRedisTemplate.opsForHash().get("dashboard:"+category+":" + duration + ":" + repoId, "eliminated");
        int eliminatedCount = eliminatedObject==null?0:Integer.parseInt((String)eliminatedObject);
        List<String> newIssueIds=stringRedisTemplate.opsForList().range("dashboard:"+category+":"+duration+":new:"+ repoId,0,-1);
        List<String> eliminatedIssueIds=stringRedisTemplate.opsForList().range("dashboard:"+category+":"+duration+":eliminated:"+ repoId,0,-1);
        return new IssueCount(newCount,eliminatedCount,remainingCount,newIssueIds,eliminatedIssueIds);
    }

    private final static Map<String,Object> empty=new HashMap<>();

    static {
        empty.put("totalPage", 0);
        empty.put("totalCount", 0);
        empty.put("issueList", Collections.emptyList());
    }

    private void updateIssueListSeverity(List<Issue> issues){
        for(Issue issue : issues){
            IssuePriorityEnum issuePriorityEnum = IssuePriorityEnum.getPriorityEnumByRank (issue.getPriority ()) ;
            if(issuePriorityEnum != null){
                issue.setSeverity (issuePriorityEnum.getName ());
            }
        }
    }

    private void updateIssueListSurvivalTime(List<Issue> issues, String repoId, String toolName){
        IssueScan issueScan  = issueScanDao.getLatestIssueScanByRepoIdAndTool (repoId, toolName);
        if (issueScan == null || issueScan.getCommitTime () == null) {
            return;
        }
        long latestScannedCommit = issueScan.getCommitTime ().getTime ();
        for(Issue issue : issues){
            Long survivalTime = 0L;
            if(IssueStatusEnum.SOLVED.getName ().equals (issue.getStatus ())){
                survivalTime =  issue.getEnd_commit_date ().getTime () - issue.getStart_commit_date ().getTime ();
            }else{
                survivalTime = latestScannedCommit - issue.getStart_commit_date ().getTime ();
            }

            issue.setSurvivalTime (updateSurvivalTime(survivalTime.intValue () / 1000));
        }

    }

    public String updateSurvivalTime(int survivalTime){
        int day = survivalTime / (3600 * 24);
        int dayMod = survivalTime % (3600 * 24);

        int hours = dayMod / 3600;
        int hoursMod = dayMod % 3600;

        int minutes = hoursMod / 60;
        int minutesMod = hoursMod % 60;

        StringBuilder stringBuilder = new StringBuilder ();
        if(day != 0){
            if(day == 1){
                stringBuilder.append (day + " day ");
            }else{
                stringBuilder.append (day + " days ");
            }
        }
        if(hours != 0){
            if(hours == 1){
                stringBuilder.append (hours + " hour ");
            }else{
                stringBuilder.append (hours + " hours ");
            }
        }
        if(minutes != 0){
            if(minutes == 1){
                stringBuilder.append (minutes + " minute ");
            }else{
                stringBuilder.append (minutes + " minutes ");
            }
        }
        if(minutesMod != 0){
            if(minutesMod == 1){
                stringBuilder.append (minutesMod + " second ");
            }else{
                stringBuilder.append (minutesMod + " seconds ");
            }
        }

        String result = stringBuilder.toString ();
        if(result.isEmpty ()){
            result = "0 second" ;
        }

        return result;
    }


    @Override
    public Map<String, Object> getIssueFilterListCount(Map<String, Object> query) {
        /*
         steps:
            1.total  -> no matter ps=0/ps!=0 should be done first
            2.ps=0 return;  ps!=0 do select;
            do select:
            3.options  (commit ? do select commit : pass)
                       (solver ? select introducer and solver : select introducer)
            4.always should be done(since,until,status,types,filespath,repolist,priority,toolname,category,start,ps)
         */
        Map<String, Object> issueFilterList = new HashMap<>(16);

        issueFilterList.put("total", query.get("developer") != null ? issueDao.getIssueFilterListCount(query) : query.get("solver") != null ? issueDao.getSolvedIssueFilterListCount(query) : issueDao.getIssueFilterListCount(query));

        return issueFilterList;
    }

    @Override
    public Map<String, Object> getIssueFilterList(Map<String, Object> query, Map<String, Object> issueFilterList){

        List<Map<String, Object>> issues = query.get("developer") != null ? issueDao.getIssueFilterList(query) : query.get("solver") != null ? issueDao.getSolvedIssueFilterList(query) : issueDao.getIssueFilterList(query);

        for(Map<String, Object> issue : issues){
            String issueId = (String) issue.get("uuid");
            if("Solved".equals(issue.get("status").toString())) {
                Map<String, Object> lastSolvedInfo = rawIssueDao.getLastSolvedInfoOfOneIssue(issueId);
                issue.put("solver", (String) lastSolvedInfo.get("lastSolver"));
                issue.put("solveTime", (Timestamp) lastSolvedInfo.get("commit_time"));
                issue.put("solveCommit", (String) lastSolvedInfo.get("commit_id"));
            }else{
                issue.put("solver", null);
                issue.put("solveTime", null);
                issue.put("solveCommit", null);
            }
            String priority = IssuePriorityEnum.getPriorityEnumByRank((Integer) issue.get("priority")).getName();
            issue.put("priority",priority);
        }

        int size = (int) query.get("ps");
        int total = (int) issueFilterList.get("total");

        issueFilterList.put("totalPage", total % size == 0 ? total / size : total / size + 1);
        issueFilterList.put("issueList", issues);

        return issueFilterList;
    }

    @Override
    public Map<String, Object> getIssueFilterListWithDetail(Map<String, Object> query, Map<String, Object> issueFilterList){
        //todo startLine endLine code issueType className methodName
        if(query.get("detail").equals(false)){
            return issueFilterList;
        }

        List<Map<String, Object>> issuesDetail = (List<Map<String, Object>>) issueFilterList.get("issueList");

        Iterator<Map<String, Object>> iterator = issuesDetail.iterator();

        String commit = query.get("commit") == null ? null : query.get("commit").toString();

        while(iterator.hasNext()){
            Map<String, Object> issue = iterator.next();
            String rawIssueUuid = rawIssueDao.getRawIssueUuidByIssueUuidAndCommit(issue.get("uuid").toString(), commit);
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
            JSONObject issueTypeInfo = allIssueTypeInfo.getOrDefault(issue.get("type"), new JSONObject() {{
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
                Collections.sort(allIssueTypeList, ((o1, o2) -> o1.getValue().getInteger(open) - o2.getValue().getInteger(open)));
            }else if(solved.equals(order)){
                Collections.sort(allIssueTypeList, ((o1, o2) -> o1.getValue().getInteger(solved) - o2.getValue().getInteger(solved)));
            }else{
                Collections.sort(allIssueTypeList, ((o1, o2) -> o1.getValue().getInteger(all) - o2.getValue().getInteger(all)));
            }
            return allIssueTypeList;
        }

        if(open.equals(order)){
            Collections.sort(allIssueTypeList, ((o1, o2) -> o2.getValue().getInteger(open) - o1.getValue().getInteger(open)));
        }else if(solved.equals(order)){
            Collections.sort(allIssueTypeList, ((o1, o2) -> o2.getValue().getInteger(solved) - o1.getValue().getInteger(solved)));
        }else{
            Collections.sort(allIssueTypeList, ((o1, o2) -> o2.getValue().getInteger(all) - o1.getValue().getInteger(all)));
        }

        return allIssueTypeList;
    }

    @Override
    public String test() {
        List<String> uuids = issueDao.getIssuetest();
        for(String issue : uuids){
            String lastSolverOfOneIssue = rawIssueDao.getLastSolverOfOneIssue(issue);
            issueDao.test(issue, lastSolverOfOneIssue);
        }
        return null;
    }

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    @Autowired
    public void setQuartzScheduler(QuartzScheduler quartzScheduler) {
        this.quartzScheduler = quartzScheduler;
    }

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
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
