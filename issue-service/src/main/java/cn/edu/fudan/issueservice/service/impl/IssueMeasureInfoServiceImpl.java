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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    public List<Map.Entry<String, JSONObject>> getNotSolvedIssueCountByToolAndRepoUuid(String repoUuid, String tool, String order, String commitUuid) {

        Map<String, JSONObject> result = new HashMap<>(32);

        List<RawIssue> rawIssues = commitUuid == null ? new ArrayList<>() : rawIssueDao.getRawIssueByCommitIDAndTool(repoUuid,tool,commitUuid);

        List<Issue> issues = commitUuid != null ? new ArrayList<>() : issueDao.getNotSolvedIssueAllListByToolAndRepoId(repoUuid, tool);

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

        double days = (DateTimeUtil.stringToLocalDate(query.get("until").toString()).toEpochDay()-DateTimeUtil.stringToLocalDate(query.get("since").toString()).toEpochDay()) * 5.0 / 7;

        return new HashMap<String, Object>(6){{
            put("solvedIssuesCount", developerDetail.getInteger("solvedIssueCount"));
            put("days", days);
            put("dayAvgSolvedIssue", developerDetail.getInteger("solvedIssueCount") / days);
        }};
    }

    @Cacheable(cacheNames = {"issueLifeCycleCount"})
    @Override
    public Object getIssueLifecycle(String developer, List<String> repoList, String since, String until, String tool, String status, Double percent, String type, String target) {
        List<Map<String, Object>> issueLifeList = new ArrayList<>();

        if ("self-solved".equals(status)) {
            //这里是获取自己曾经在rawIssue表里解决过的issue，并且最终状态是“Solved”
            List<Map<String, Object>> selfSolvedIssueLife = issueDao.getSolvedIssueLifeCycle(repoList,type,tool,since,until,developer,RawIssueStatus.SOLVED.getType ());
            //筛选issue，只保留自己引入并且最终是自己解决的issue
            // 1.获取自己引入的并且最终由自己解决的issue
            if ("self".equals(target)){
                filterIssueAddedBySelfAndSolvedBySelf(selfSolvedIssueLife,developer);
                issueLifeList = selfSolvedIssueLife;
            } else if ("other".equals(target)) {
                // 2.获取他人引入的并且最终由自己解决的issue
                filterIssueAddedByOtherAndSolvedBySelf(selfSolvedIssueLife,developer);
                issueLifeList = selfSolvedIssueLife;
            } else if ("all".equals(target)) {
                // 3.获取自己解决的issue，不区分谁引入
                filterIssueAddedByAnyAndSolvedBySelf(selfSolvedIssueLife,developer);
                issueLifeList = selfSolvedIssueLife;
            }
        } else if ("living".equals(status)){
            // 4.获取自己引入的并且最终状态是未解决的issue
            List<Map<String, Object>> addedIssueLife = issueDao.getOpenIssueLifeCycle(repoList,type,tool,since,until,developer,RawIssueStatus.ADD.getType(),IssueStatusEnum.OPEN.getName());
            filterIssueAddedBySelfAndNotSolved(addedIssueLife);
            issueLifeList = addedIssueLife;
        } else if ("other-solved".equals(status)){
            // 5.获取自己引入的并且最终由他人解决的issue
            List<Map<String, Object>> otherSolvedIssueLife = issueDao.getSolvedIssueLifeCycleByOtherSolved(repoList,type,tool,since,until,developer,RawIssueStatus.SOLVED.getType());
            filterIssueAddedBySelfAndSolvedByOther(otherSolvedIssueLife, developer);
            issueLifeList = otherSolvedIssueLife;
        }else if ("all".equals(status) || status == null){
            List<Map<String, Object>> addedIssueLife = issueDao.getSolvedIssueLifeCycle(repoList,type,tool,since,until,developer,RawIssueStatus.ADD.getType ());
            for (int i = addedIssueLife.size()-1; i >= 0; i--){
                Map<String, Object> map = addedIssueLife.get(i);
                setPriorityAndProjectInfoForIssue(map);
            }
            issueLifeList = addedIssueLife;
        }

        //下面开始处理返回的格式
        if (issueLifeList.size() == 0){
            return new HashMap<String, Integer>(16){{
                put("max",0);
                put("min",0);
                put("avg",0);
                put("mid",0);
                put("upperQuartile",0);
                put("lowerQuartile",0);
                put("multiple",0);
                put("quantity", 0);
            }};
        }

        List<Integer> lifeCycle = new ArrayList<>();
        issueLifeList.forEach(issueLife -> lifeCycle.add(Integer.parseInt(issueLife.get("lifeCycle").toString())));

        Map<String, Double> percentMap = new HashMap<>(16);
        if(percent == -1){
            return issueLifeList;
        } else if (percent == -2){
            dealTheResultOfStatistic(lifeCycle, percentMap, issueLifeList);
            return percentMap;
        }else if (percent >= 0 && percent <= 100){
            int index = (int) Math.round(percent);
            return getStatisticFromIntegerList(lifeCycle,index);
        }
        return null;
    }


    private void filterIssueAddedBySelfAndSolvedBySelf(List<Map<String, Object>> selfSolvedIssueLife, String developer) {
        for (int i = selfSolvedIssueLife.size()-1; i >= 0; i--) {
            Map<String, Object> map = selfSolvedIssueLife.get(i);

            String adder = rawIssueDao.getAdderOfOneIssue(map.get("uuid").toString());
            // 删除由他人引入的缺陷，只保留自己引入的缺陷
            //fixme 这里因为commit表不全，导致查询数据库有时没办法得到adder，就会出现adder为空的情况，目前只统计了adder不为空的情况，所以会有稍许不准
            if (!StringUtils.isEmpty(adder) && !adder.equals(developer)) {
                selfSolvedIssueLife.remove(i);
                continue;
            }
            // 若adder为空（查询不到adder），默认看作adder为他人，而不是自己
            if (StringUtils.isEmpty(adder)) {
                selfSolvedIssueLife.remove(i);
                log.warn("issue/lifecycle api: Self-add & self-solved: Can't find the adder newInstance issue_id :{}, so we assume this issue is added by other people, not by {}",map.get("uuid").toString(),developer);
                continue;
            }
            // 获取状态为Solved的问题的最后一次被解决的developer（lastSolver）
            String lastSolver = rawIssueDao.getLastSolverOfOneIssue(map.get("uuid").toString());
            if (!StringUtils.isEmpty(lastSolver) && !lastSolver.equals(developer)) {
                selfSolvedIssueLife.remove(i);
                continue;
            }
            //lastSolver（lastSolver），默认看作lastSolver为他人，而不是自己
            if (StringUtils.isEmpty(lastSolver)) {
                selfSolvedIssueLife.remove(i);
                log.warn("issue/lifecycle api: Self-add & self-solved: Can't find the lastSolver newInstance issue_id :{}, so we assume this issue is solved by other people, not by {}",map.get("uuid").toString(),developer);
            }
            setPriorityAndProjectInfoForIssue(map);
        }
    }

    private void filterIssueAddedByOtherAndSolvedBySelf(List<Map<String, Object>> selfSolvedIssueLife, String developer) {
        for (int i = selfSolvedIssueLife.size() - 1; i >= 0; i--) {
            Map<String, Object> map = selfSolvedIssueLife.get(i);
            String adder = rawIssueDao.getAdderOfOneIssue(map.get("uuid").toString());
            // 删除由自己引入的缺陷，只保留他人引入的缺陷
            // fixme 这里因为commit表不全，导致查询数据库没办法得到adder，就会出现adder为空的情况，目前只统计了adder不为空的情况，所以会有稍许不准
            if (!StringUtils.isEmpty(adder) && adder.equals(developer)) {
                selfSolvedIssueLife.remove(i);
                continue;
            }else{
                selfSolvedIssueLife.get(i).put("adder",adder);
            }
            // 若adder为空（查询不到adder），默认看作adder为他人，而不是自己
            if (StringUtils.isEmpty(adder)) {
                log.warn("issue/lifecycle api: Other-add & self-solved: Can't find the adder newInstance issue_id :{}, so we assume this issue is added by other people, not by {}",map.get("uuid").toString(),developer);
            }
            // 获取状态为Solved的问题的最后一次被解决的developer（lastSolver）
            String lastSolver = rawIssueDao.getLastSolverOfOneIssue(map.get("uuid").toString());
            if (!StringUtils.isEmpty(lastSolver) && !lastSolver.equals(developer)) {
                selfSolvedIssueLife.remove(i);
                continue;
            }else{
                selfSolvedIssueLife.get(i).put("solver",lastSolver);
            }
            // lastSolver（lastSolver），默认看作lastSolver为他人，而不是自己
            if (StringUtils.isEmpty(lastSolver)) {
                selfSolvedIssueLife.remove(i);
                log.warn("issue/lifecycle api: Self-add & self-solved: Can't find the lastSolver newInstance issue_id :{}, so we assume this issue is solved by other people, not by {}",map.get("uuid").toString(),developer);
            }
            setPriorityAndProjectInfoForIssue(map);
        }
    }

    private void filterIssueAddedByAnyAndSolvedBySelf(List<Map<String, Object>> selfSolvedIssueLife, String developer) {
        for (int i = selfSolvedIssueLife.size()-1; i >= 0; i--) {
            Map<String, Object> map = selfSolvedIssueLife.get(i);
            // 判断最终状态是否已解决
            if (!"Solved".equals(map.get("status"))){
                selfSolvedIssueLife.remove(i);
                continue;
            }
            // 获取状态为Solved的问题的最后一次被解决的developer（lastSolver）
            String lastSolver = rawIssueDao.getLastSolverOfOneIssue(map.get("uuid").toString());
            if (!StringUtils.isEmpty(lastSolver) && !lastSolver.equals(developer)) {
                selfSolvedIssueLife.remove(i);
                continue;
            }
            //lastSolver（lastSolver），默认看作lastSolver为他人，而不是自己
            if (StringUtils.isEmpty(lastSolver)) {
                selfSolvedIssueLife.remove(i);
                log.warn("issue/lifecycle api: Self-add & self-solved: Can't find the lastSolver newInstance issue_id :{}, so we assume this issue is solved by other people, not by {}",map.get("uuid").toString(),developer);
            }
            setPriorityAndProjectInfoForIssue(map);
        }
    }

    private void filterIssueAddedBySelfAndNotSolved(List<Map<String, Object>> addedIssueLife) {
        for (int i = addedIssueLife.size()-1; i >= 0; i--){
            Map<String, Object> map = addedIssueLife.get(i);
            if ("Solved".equals(map.get("status"))){
                addedIssueLife.remove(i);
                continue;
            }
            setPriorityAndProjectInfoForIssue(map);
        }
    }

    private void filterIssueAddedBySelfAndSolvedByOther(List<Map<String, Object>> otherSolvedIssueLife, String developer) {
        for (int i = otherSolvedIssueLife.size()-1; i >= 0; i--){
            Map<String, Object> map = otherSolvedIssueLife.get(i);
            //判断最终状态是否已解决
            if (!"Solved".equals(map.get("status"))){
                otherSolvedIssueLife.remove(i);
                continue;
            }
            String adder = rawIssueDao.getAdderOfOneIssue(map.get("uuid").toString());
            //删除由他人引入的缺陷，只保留自己引入的缺陷
            //fixme 这里因为commit表不全，导致查询数据库没办法得到adder，就会出现adder为空的情况，目前只统计了adder不为空的情况，所以会有稍许不准
            if (!StringUtils.isEmpty(adder) && !adder.equals(developer)){
                otherSolvedIssueLife.remove(i);
                continue;
            }else{
                otherSolvedIssueLife.get(i).put("adder",adder);
            }
            //若adder为空（查询不到adder），默认看作adder为他人，而不是自己
            if (StringUtils.isEmpty(adder)) {
                otherSolvedIssueLife.remove(i);
                log.warn("issue/lifecycle api: Self-add & other-solved: Can't find the adder newInstance issue_id :{}, so we assume this issue is added by other people, not by {}",map.get("uuid").toString(),developer);
                continue;
            }
            //获取状态为Solved的问题的最后一次被解决的developer（lastSolver）
            String lastSolver = rawIssueDao.getLastSolverOfOneIssue(map.get("uuid").toString());
            if (!StringUtils.isEmpty(lastSolver) && lastSolver.equals(developer)) {
                otherSolvedIssueLife.remove(i);
                continue;
            }else{
                otherSolvedIssueLife.get(i).put("solver",lastSolver);
            }
            //lastSolver（lastSolver），默认看作lastSolver为他人，而不是自己
            if (StringUtils.isEmpty(lastSolver)) {
                log.warn("issue/lifecycle api: Self-add & self-solved: Can't find the lastSolver newInstance issue_id :{}, so we assume this issue is solved by other people, not by {}",map.get("uuid").toString(),developer);
            }
            setPriorityAndProjectInfoForIssue(map);
        }
    }

    private void setPriorityAndProjectInfoForIssue(Map<String, Object> map) {
        // 获取优先级紧急程度
        IssuePriorityEnum issuePriorityEnum = IssuePriorityEnum.getPriorityEnumByRank ((Integer) map.get("priority"));
        assert issuePriorityEnum != null;
        String severity = issuePriorityEnum.getName();
        map.put("severity",severity);

        // 根据repoId获取issue所在项目和库的名称
        Map<String, String> project = restInterfaceManager.getProjectByRepoId((String) map.get("repoId"));
        String repoName = project.get("repoName");
        String projectName = project.get("projectName");
        map.put("repoName",repoName);
        map.put("projectName",projectName);
    }

    private double getMultipleOfOneIntegerList(List<Integer> list) {
        HashSet<Integer> uniqueData = new HashSet<>(list);
        HashMap<Integer,Integer> mass = new HashMap<>();
        int[] count = new int[uniqueData.size()];
        int j=0;
        for (Integer integer1 : uniqueData) {
            for (Integer integer2 : list) {
                if(integer1.equals(integer2)) {
                    count[j]++;
                }
            }
            mass.put(count[j],integer1);
            j++;
        }
        int k=0;
        for (int i : count) {
            k = Math.max(k, i);
        }
        return mass.get(k);
    }

    private void dealTheResultOfStatistic(List<Integer> lifeCycle, Map<String, Double> percentMap, List<Map<String, Object>> issueLifeList) {
        DoubleSummaryStatistics statistics = lifeCycle.stream().mapToDouble(Number::doubleValue).summaryStatistics();

        double mid;
        double upperQuartile;
        double lowerQuartile;
        double multiple;
        if (lifeCycle.size() % 2 != 0){
            mid = lifeCycle.get((lifeCycle.size()+1)/2-1);
            upperQuartile = lifeCycle.get(Math.min((int) ((lifeCycle.size()+1)*0.75) - 1,lifeCycle.size()-1));
            lowerQuartile = lifeCycle.get(Math.max((int) ((lifeCycle.size()+1)*0.25) - 1,0));
        }else {
            mid = (lifeCycle.get(Math.max((lifeCycle.size() + 1) / 2 - 1, 0)) + lifeCycle.get((lifeCycle.size()+1)/2))/2.0;
            upperQuartile = (lifeCycle.get((int) ((lifeCycle.size()+1)*0.75) - 1) + lifeCycle.get(Math.min((int)((lifeCycle.size()+1)*0.75),lifeCycle.size()-1)))/2.0;
            lowerQuartile = (lifeCycle.get(Math.max((int) ((lifeCycle.size()+1)*0.25) - 1,0)) + lifeCycle.get((int) ((lifeCycle.size()+1)*0.25)))/2.0;
        }
        //求众数
        multiple = getMultipleOfOneIntegerList(lifeCycle);

        percentMap.put("max",statistics.getMax());
        percentMap.put("min",statistics.getMin());
        percentMap.put("avg",statistics.getAverage());
        percentMap.put("mid",mid);
        percentMap.put("upperQuartile",upperQuartile);
        percentMap.put("lowerQuartile",lowerQuartile);
        percentMap.put("multiple",multiple);
        percentMap.put("quantity", (double) issueLifeList.size());
    }


    /**
     * 根据index（0-100） 获取一个排好序的list（全是数字）,对应位置的元素值
     */
    private Double getStatisticFromIntegerList(List<Integer> list, int index){
        if (index == 0){
            return Double.valueOf(list.get(0));
        }
        if (index == 100){
            return Double.valueOf(list.get(list.size()-1));
        }
        double location = index*1.0/100;
        int idx = (int) Math.round(location*list.size());
        if (idx >= list.size()){
            idx = list.size()-1;
        }
        return Double.valueOf(list.get(idx));
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