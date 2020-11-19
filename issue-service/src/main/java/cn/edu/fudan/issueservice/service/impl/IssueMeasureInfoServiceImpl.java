package cn.edu.fudan.issueservice.service.impl;

import cn.edu.fudan.issueservice.annotation.GetResource;
import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.component.RestInterfaceManagerUtil;
import cn.edu.fudan.issueservice.dao.*;
import cn.edu.fudan.issueservice.domain.IssueCountDeveloper;
import cn.edu.fudan.issueservice.domain.IssueCountMeasure;
import cn.edu.fudan.issueservice.domain.ScanResult;
import cn.edu.fudan.issueservice.domain.SpaceType;
import cn.edu.fudan.issueservice.domain.dbo.Issue;
import cn.edu.fudan.issueservice.domain.dbo.IssueScan;
import cn.edu.fudan.issueservice.domain.dbo.Location;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.domain.dto.IssueCountPo;
import cn.edu.fudan.issueservice.domain.dto.RepoResourceDTO;
import cn.edu.fudan.issueservice.domain.enums.*;
import cn.edu.fudan.issueservice.domain.statistics.CodeQualityResponse;
import cn.edu.fudan.issueservice.domain.statistics.DeveloperQuality;
import cn.edu.fudan.issueservice.domain.statistics.Quality;
import cn.edu.fudan.issueservice.domain.statistics.TimeQuality;
import cn.edu.fudan.issueservice.service.IssueMeasureInfoService;
import cn.edu.fudan.issueservice.util.DateTimeUtil;
import cn.edu.fudan.issueservice.util.JGitHelper;
import cn.edu.fudan.issueservice.util.SegmentationUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


/**
 * description:
 * @author fancying
 * create: 2019-04-02 15:27
 **/
@Slf4j
@Service
public class IssueMeasureInfoServiceImpl implements IssueMeasureInfoService {

    private String dateRegex = "([0-9]+)-([0-9]{2})-([0-9]{2})";

    private CommitDao commitDao;
    private RawIssueDao rawIssueDao;
    private IssueDao issueDao;
    private RestInterfaceManager restInterfaceManager;
    private ScanResultDao scanResultDao;
    private RestInterfaceManagerUtil restInterfaceManagerUtil;
    private IssueScanDao issueScanDao;
    private IssueRepoDao issueRepoDao;

    public IssueMeasureInfoServiceImpl(CommitDao commitDao, RawIssueDao rawIssueDao, IssueDao issueDao, RestInterfaceManager restInterfaceManager, ScanResultDao scanResultDao, RestInterfaceManagerUtil restInterfaceManagerUtil, IssueScanDao issueScanDao, IssueRepoDao issueRepoDao) {
        this.commitDao = commitDao;
        this.rawIssueDao = rawIssueDao;
        this.issueDao = issueDao;
        this.restInterfaceManager = restInterfaceManager;
        this.scanResultDao = scanResultDao;
        this.restInterfaceManagerUtil = restInterfaceManagerUtil;
        this.issueScanDao = issueScanDao;
        this.issueRepoDao = issueRepoDao;
    }

    @Override
    public JSONObject getIssueInfoOfSpecificFile(String repoId, String commitId, String tool, String filePath) {
        JSONObject result = new JSONObject ();

        //先获取该文件总的raw issue 信息
        List<RawIssue> rawIssues = rawIssueDao.getRawIssueByCommitIDAndFile (repoId, commitId,tool, filePath);


        //第一项数据 总的raw issue 数量
        int totalRawIssues = rawIssues.size ();

        //第二项数据哪些方法有哪些缺陷
        //key为 方法名， value为这个方法所包含的raw issue
        Map<String, List<RawIssue>> methodWithRawIssue = new HashMap<> ();

        for(RawIssue rawIssue : rawIssues){
            List<Location> locations = rawIssue.getLocations ();
            for(Location location : locations){
                String method = location.getMethod_name ();
                List<RawIssue> thisMethodRawIssues = methodWithRawIssue.get (method);
                if(thisMethodRawIssues == null){
                    thisMethodRawIssues = new ArrayList<> ();
                }
                if(!thisMethodRawIssues.contains (rawIssue)){
                    thisMethodRawIssues.add (rawIssue);
                }

                methodWithRawIssue.put (method,thisMethodRawIssues );
            }
        }

        //第三步组成返回数据
        JSONArray issues = new JSONArray ();

        for(Map.Entry<String, List<RawIssue>> entry : methodWithRawIssue.entrySet ()){
            JSONObject rawIssueJson = new JSONObject ();
            rawIssueJson.put ("method", entry.getKey ());
            rawIssueJson.put ("issues", entry.getValue ());

            issues.add (rawIssueJson);
        }

        result.put ("total", totalRawIssues);
        result.put ("methods", issues);

        return result;
    }




    @Override
    public int numberOfRemainingIssue(String repoId, String commit, String spaceType, String detail) {
        // 项目某commit下的现有问题数
        if (SpaceType.PROJECT.getLevel().equals(spaceType)) {
            return rawIssueDao.getNumberOfRemainingIssue(repoId, commit);
        }
        if (SpaceType.PACKAGE.getLevel().equals(spaceType)) {

            // package accountName 需要做处理
            return rawIssueDao.getNumberOfRemainingIssueBasePackage(repoId, commit,
                    "%" + detail.replace('.','/') + "%");
        }
        if (SpaceType.FILE.getLevel().equals(spaceType)) {

            return rawIssueDao.getNumberOfRemainingIssueBaseFile(repoId, commit, detail);
        }
        // 需要单独引入用户问题记录表 ？

        return -1;
    }




    @Override
    public int numberOfNewIssue(String duration, String spaceType, String detail) {
        // duration: 2018.01.01-2018.12.12
        if (duration.length() < 21) {
            throw new RuntimeException("duration error!");
        }
        String start = duration.substring(0,10);
        String end = duration.substring(11,21);
        //List<String> commits = restInterfaceManager.getScanCommitsIdByDuration(detail, start, end);

/*        if (SpaceType.DEVELOPER.getLevel().equals(spaceType)) {
            return ;
        }*/

        if (SpaceType.PROJECT.getLevel().equals(spaceType)) {
            return issueDao.getNumberOfNewIssueByDuration(detail, start, end);
        }


        return -1;
    }

    @Override
    public int numberOfNewIssueByCommit(String repoId, String commitId, String spaceType,String category) {
        // 项目某commit下的现有问题数
        if (SpaceType.PROJECT.getLevel().equals(spaceType)) {
            return scanResultDao.getNumberOfNewIssueByCommit(repoId, commitId,category);
        }
        return -1;
    }

    @Override
    public int numberOfEliminateIssue(String duration, String spaceType, String detail) {
        // duration: 2018.01.01-2018.12.12
        if (duration.length() < 21) {
            throw new RuntimeException("duration error!");
        }
        String start = duration.substring(0,10);
        String end = duration.substring(11,21);

        // detail 是repoId
        if (SpaceType.PROJECT.getLevel().equals(spaceType)) {
            return issueDao.getNumberOfEliminateIssueByDuration(detail, start, end);
        }
/*        if (SpaceType.DEVELOPER.getLevel().equals(spaceType)) {
            return ;
        }*/

        return -1;
    }

    @Override
    public int numberOfEliminateIssueByCommit(String repoId, String commitId, String spaceType,String category) {
        // 项目某commit下的现有问题数
        if (SpaceType.PROJECT.getLevel().equals(spaceType)) {
            return scanResultDao.getNumberOfEliminateIssueByCommit(repoId, commitId,category);
        }
        return -1;
    }

    @Override
    public List<IssueCountPo> getIssueCountEachCommit(String repoId, String category, String since, String until) {
        return scanResultDao.getScanResultsEachCommit(repoId,category,since,until);
    }

    @Override
    public IssueCountMeasure getIssueCountMeasureByRepo(String repoId, String category, String since, String until) {
        IssueCountMeasure issueCountMeasure=new IssueCountMeasure();
        issueCountMeasure.setNewIssueCount(issueDao.getNumberOfNewIssueByDuration(repoId, since, until));
        issueCountMeasure.setEliminatedIssueCount(issueDao.getNumberOfEliminateIssueByDuration(repoId, since, until));
        return issueCountMeasure;
    }

    @Override
    public List<IssueCountDeveloper> getIssueCountMeasureByDeveloper(String repoId, String category, String since, String until) {
        return scanResultDao.getScanResultsEachDeveloper(repoId, category, since, until);
    }

    @Override
    public List<JSONObject> getNotSolvedIssueCountByCategoryAndRepoId(String repoId, String category,String commitId) {
        Map<String,Integer> issueCount = new HashMap<>();
        List<JSONObject> result = new ArrayList<JSONObject>();

        if(commitId == null){
            List<Issue> issues = issueDao.getNotSolvedIssueAllListByToolAndRepoId(repoId,category);

            for (Issue issue:
                    issues) {
                String issueType = issue.getType();
                int count = issueCount.get(issueType) != null ? issueCount.get(issueType) : 0;
                issueCount.put(issueType,++count);
            }
        }else{
            List<RawIssue> rawIssues = rawIssueDao.getRawIssueByCommitIDAndTool(repoId,category,commitId);
            for (RawIssue rawIssue:
                    rawIssues) {
                String rawIssueType = rawIssue.getType();
                int count = issueCount.get(rawIssueType) != null ? issueCount.get(rawIssueType) : 0;
                issueCount.put(rawIssueType,++count);
            }
        }

        List<Map.Entry<String,Integer>> list = new ArrayList<Map.Entry<String,Integer>>(issueCount.entrySet());
        Collections.sort(list,(o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        for(Map.Entry<String,Integer> entry : list){
            JSONObject obj = new JSONObject();
            obj.put("Issue_Type",entry.getKey());
            obj.put("Total",entry.getValue());
            result.add(obj);
        }
        return result;
    }

    @Override
    //fixme 此方法返回的是scanresult表里的数据，统计的是每次commit下新增和消除的次数。与目前（20200923）人员页面里开发者能力下的缺陷数量统计不一致（一个是过程中累计的次数，一个是最新状态的个数）
    public CodeQualityResponse getQualityChangesByCondition(String developer, String timeGranularity, String since, String until, String repoUuid, String tool, int page, int ps) throws RuntimeException{

        if(!isDateCompliant(since) || !isDateCompliant(until)){
            throw new RuntimeException(" The input format newInstance since should be like 2019-10-01");
        }
        //todo ??? 因为repo_measure表的commit_time存储时间格式与scan_result的commit_date不同，故需要将repo_measure表的时间日期加1天。
        //String repoMeasureUntil = DateTimeUtil.stringToLocalDate(until).plusDays(1).toString();

        List<ScanResult> scanResults = scanResultDao.getScanResultByCondition(repoUuid, since, until, tool, developer);
        if(scanResults == null || scanResults.isEmpty()){
            return new CodeQualityResponse();
        }
        Map<String, List<ScanResult>> developerMap = scanResults.stream().collect(Collectors.groupingBy(ScanResult::getDeveloper));

        List<String> repoIdList = StringUtils.isEmpty(repoUuid) ? issueRepoDao.getAllScannedRepoId() : Collections.singletonList(repoUuid);
        int totalChangedLines = 0;

        List<DeveloperQuality> developerQualities = new ArrayList<>(16);
        for (String repoId : repoIdList) {
            JSONObject developerWorkInfo = restInterfaceManager.getDeveloperListByDuration(developer, since, until, repoId);
            if (developerWorkInfo == null) {
                log.error("developerWorkInfo is null,repoId:{} developer:{}", repoId, developer);
                continue;
            }
            List<DeveloperQuality> cDeveloperQualities = getDeveloperQualities(developerWorkInfo.getJSONObject("data").getJSONArray("commitInfoList"));
            unionDeveloperList(developerQualities, cDeveloperQualities);
        }
        for (DeveloperQuality d : developerQualities) {
            List<ScanResult> scanResultList = developerMap.get(d.getAuthor());
            int devNewIssues = 0;
            int devEliminateIssues = 0;
            if(scanResultList != null){
                for(ScanResult scanResultDev : scanResultList){
                    devNewIssues += scanResultDev.getNew_count();
                    devEliminateIssues += scanResultDev.getEliminated_count();
                }
            }
            d.setNewIssues(devNewIssues);
            d.setEliminateIssues(devEliminateIssues);

            d.setEliminateIssueQualityThroughCalculate(devEliminateIssues);
            d.setAddIssueQualityThroughCalculate(devNewIssues);
            totalChangedLines = d.getAddLines() + d.getDelLines();
        }
        developerQualities.sort(Comparator.comparing(DeveloperQuality::getAuthor));

        CodeQualityResponse codeQualityResponse = new CodeQualityResponse();
        codeQualityResponse.setDevelopers(developerQualities);

        // 进行总的缺陷以及缺陷质量统计
        int totalAddIssues = 0;
        int totalEliminateIssues = 0;
        for(ScanResult scanResult : scanResults){
            totalAddIssues += scanResult.getNew_count();
            totalEliminateIssues += scanResult.getEliminated_count();
        }

        Quality totalQuality = new Quality();
        totalQuality.setNewIssues(totalAddIssues);
        totalQuality.setEliminateIssues(totalEliminateIssues);
        totalQuality.setEliminateIssueQualityThroughCalculate(totalEliminateIssues, totalChangedLines);
        totalQuality.setAddIssueQualityThroughCalculate(totalAddIssues, totalChangedLines);
        codeQualityResponse.setTotalQuality(totalQuality);

        return codeQualityResponse;
    }

    private void unionDeveloperList(List<DeveloperQuality> developerQualities, List<DeveloperQuality> cDeveloperQualities) {
        for (DeveloperQuality d : cDeveloperQualities) {
            if (developerQualities.contains(d)) {
                DeveloperQuality newD = developerQualities.get(developerQualities.indexOf(d));
                newD.setAddLines(d.getAddLines() + newD.getAddLines());
                newD.setDelLines(d.getDelLines() + newD.getDelLines());
                newD.setCommitCount(d.getCommitCount() + newD.getCommitCount());
                newD.setChangedFiles(d.getChangedFiles() + newD.getChangedFiles());
            } else {
                developerQualities.add(d);
            }
        }
    }

    private List<DeveloperQuality> getDeveloperQualities(JSONArray jsonArray) {
        List<DeveloperQuality> developerQualities = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject developerInfo = jsonArray.getJSONObject(i);
            DeveloperQuality developerQuality = DeveloperQuality.builder()
                    .author(developerInfo.getString("author")).email(developerInfo.getString("email"))
                    .addLines(developerInfo.getIntValue("add")).delLines(developerInfo.getIntValue("del"))
                    .commitCount(developerInfo.getIntValue("commit_counts")).changedFiles(developerInfo.getIntValue("changed_files")).build();
            developerQualities.add(developerQuality);
        }
        return developerQualities;
    }


    private List<TimeQuality> getTimeQualitiesByTimeGranularity(String timeGranularity, List<LocalDate> dates, Map<LocalDate,List<ScanResult>> granularityMap,String repoId,String since,String until){
        List<TimeQuality> tTimeQualities = new LinkedList<>();

        TimeQuality timeQuality = new TimeQuality();

        int dateSize = dates.size();
        LocalDate nextTime = getNextLocalDate(DateTimeUtil.stringToLocalDate(since),timeGranularity);
        int i = 0;
        int addIssues=0;
        int delIssues=0;
        while(i<dateSize) {
            LocalDate localDate = dates.get(i);
            if (localDate.isBefore(nextTime)) {
                List<ScanResult> scanResults = granularityMap.get(localDate);
                for (ScanResult scanResult : scanResults) {
                    if (timeQuality.getDate() == null) {
                        timeQuality.setDate(getPreLocalDate(nextTime,timeGranularity));
                    }
                    addIssues += scanResult.getNew_count();
                    delIssues += scanResult.getEliminated_count();
                }
                i++;
            } else {
                if(timeQuality.getDate() == null){
                    timeQuality.setDate(getPreLocalDate(nextTime,timeGranularity));
                }
                //当条件不符合时，仅表示一个月的统计结束了，下面的代码表示统计加入队列，且此时i不增。
                insertTimeQuality(timeQuality,nextTime,repoId,addIssues,delIssues,tTimeQualities);

                //当插入了一条数据以后对数据进行初始化，且此时i不增
                timeQuality = new TimeQuality();
                addIssues = 0;
                delIssues = 0;
                nextTime =getNextLocalDate(nextTime,timeGranularity);
            }
        }
        //当最后不满一周或者一月时，另外也需做统计插入
        insertTimeQuality(timeQuality,DateTimeUtil.stringToLocalDate(until).plusDays(1),repoId,addIssues,delIssues,tTimeQualities);

        return tTimeQualities;
    }


    private LocalDate getNextLocalDate(LocalDate preLocalDate,String timeGranularity){
        LocalDate nextLocalDate =null;
        if(TimeGranularity.MONTH.getType().equals(timeGranularity)){
            nextLocalDate = preLocalDate.plusMonths(1);
        }else if(TimeGranularity.WEEK.getType().equals(timeGranularity)){
            nextLocalDate = preLocalDate.plusWeeks(1);
        }else if(TimeGranularity.DAY.getType().equals(timeGranularity)){
            nextLocalDate = preLocalDate.plusDays(1);
        }else{
            throw new RuntimeException("please input correct time granularity ,such like day,week and month.");
        }
        return nextLocalDate;
    }

    private LocalDate getPreLocalDate(LocalDate nextLocalDate,String timeGranularity){
        LocalDate preLocalDate =null;
        if(TimeGranularity.MONTH.getType().equals(timeGranularity)){
            preLocalDate = nextLocalDate.minusMonths(1);
        }else if(TimeGranularity.WEEK.getType().equals(timeGranularity)){
            preLocalDate = nextLocalDate.minusWeeks(1);
        }else if(TimeGranularity.DAY.getType().equals(timeGranularity)){
            preLocalDate = nextLocalDate.minusDays(1);
        }else{
            throw new RuntimeException("please input correct time granularity ,such like day,week and month.");
        }
        return preLocalDate;
    }

    private void insertTimeQuality(TimeQuality timeQuality,LocalDate nextTime,String repoId,int addIssues,int delIssues,List<TimeQuality> tTimeQualities){
        int repoAddLines = 0;
        int repoDelLines = 0;
        int timeChangedLines = 0;
        LocalDate since = timeQuality.getDate();
        LocalDate until = nextTime;
        JSONObject codeChangesResponse = restInterfaceManager.getCodeChangesByDurationAndDeveloperName(null, since==null?null:since.toString(), until.toString(), null, repoId);
        if (codeChangesResponse != null && codeChangesResponse.getInteger("code") == 200) {
            JSONObject commitBase = codeChangesResponse.getJSONObject("data");
            repoAddLines = commitBase.getIntValue("addLines");
            repoDelLines = commitBase.getIntValue("delLines");
        } else {
            log.error("request /measure/developer/code-change failed");
            throw new RuntimeException("request /measure/developer/code-change failed");
        }
        timeChangedLines = repoAddLines + repoDelLines;

        timeQuality.setNewIssues(addIssues);
        timeQuality.setEliminateIssues(delIssues);
        if (timeChangedLines != 0) {
            timeQuality.setEliminateIssueQualityThroughCalculate(delIssues, timeChangedLines);
            timeQuality.setAddIssueQualityThroughCalculate(addIssues, timeChangedLines);
        } else {
            log.info("the code  hadn't had any change during {} --> {}  ", since, until);
            if (delIssues != 0 || addIssues != 0) {
                log.error("code did not changed but issues changed");
                throw new RuntimeException("code did not changed but issues changed");
            }
            timeQuality.setEliminateIssueQuality(0);
            timeQuality.setAddIssueQuality(0);
        }
        tTimeQualities.add(timeQuality);

    }




    @Override
    public Integer getIssueCountByConditions(String developer, String repoId, String since, String until, String tool, String generalCategory) {
        Map<String, Object> map = new HashMap<>();
        if(! StringUtils.isEmpty(since)){
            if(!since.matches(dateRegex)){
                log.error(" The input format newInstance since should be like 2019-10-01, since time is {}", since);
                // fixme default date 后续设置成全局变量  抽成一个专门检测时间的函数
                since = "2000-01-01";
            }
            map.put("since",since);
        }

        if(until  != null ){
            if(!until.matches(dateRegex)){
                log.error(" The input format newInstance since should be like 2019-10-01, until time is {}", until);
                until = "2022-01-01";
            }
            until = DateTimeUtil.stringToLocalDate(until).plusDays(1).toString();
            map.put("until",until);
        }
        List<String> repoIdList = new ArrayList<>();
        repoIdList.add(repoId);
        map.put("repo_id", repoIdList);
        if(developer != null){
            map.put("developer", developer);
        }
        if(tool != null){
            map.put("toolName", tool);
        }

        List<Issue> issues = issueDao.getIssueList(map);

        if(generalCategory == null || "".equals(generalCategory)){
            return issues.size();
        }

        List<Issue> result = new ArrayList<>();
        for(Issue issue : issues){
            if(issue.getIssueType () == null){
                log.error ("{}  -- > issue type has not been recorded", issue.getType ());
                continue;
            }
            IssueTypeEnum issueTypeEnum = IssueTypeEnum.getIssueTypeEnum(issue.getIssueType().getCategory());
            if(issueTypeEnum == null ){
                log.error ("this category   -->  {} has not been recorded! ", issue.getIssueType().getCategory());
            }else{
                if(issueTypeEnum.getCategory().equals(generalCategory)){
                    result.add(issue) ;
                }
            }

        }
        return result.size();
    }

    @Override
    public Map<String,Object> getDayAvgSolvedIssue(String developer, String repoId, String since, String until, String tool) {
        if(StringUtils.isEmpty(since) || !since.matches(dateRegex)){
            log.error(" The input format newInstance since should be like 2019-10-01, since time is {}", since);
        }
        //since 如果为空，则默认为开发者第一次提交commit的时间
        JSONObject firstCommitDateData = restInterfaceManager.getFirstCommitDate(developer);
        String dateString = firstCommitDateData.getJSONObject("repos_summary").getString("first_commit_time_summary");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime firstCommitDateTime = LocalDateTime.parse(dateString, fmt);
        firstCommitDateTime = firstCommitDateTime.plusHours(8);
        LocalDate firstCommitDate = firstCommitDateTime.toLocalDate();
        since = firstCommitDate.toString();

        if(until != null ){
            if(!until.matches(dateRegex)){
                log.error(" The input format newInstance since should be like 2019-10-01, until time is {}", until);
                LocalDate today = LocalDate.now();
                until = today.toString();
            }
            until = DateTimeUtil.stringToLocalDate(until).plusDays(1).toString();
        }else {
            LocalDate today = LocalDate.now();
            until = today.toString();
        }

        //开发者曾经解决过的缺陷的数量（不一定是最终解决的）
        Map<String, Object> query = new HashMap(10);
        query.put("repoList", repoId == null ? null : new ArrayList(){{
            add(repoId);
        }});
        query.put("developer", developer);
        query.put("tool", "sonarqube");
        query.put("since", since);
        query.put("until", until);

        JSONObject developerDetail = getDeveloperCodeQuality(query).get(developer);

        double days = (DateTimeUtil.stringToLocalDate(until).toEpochDay()-DateTimeUtil.stringToLocalDate(since).toEpochDay()) * 5.0 / 7;
        Map<String,Object> resultMap = new HashMap(){{
            put("solvedIssuesCount",developerDetail.getInteger("solvedIssueCount"));
            put("days",days);
            put("dayAvgSolvedIssue",developerDetail.getInteger("solvedIssueCount")/days);
        }};
        return resultMap;
    }

    @Override
    public JSONObject getScanStatusCount(String repoId, String tool, String date){

        JSONObject result = new JSONObject ();
        String since = null;
        String until = null;
        if (date != null){
            if(!date.matches("([0-9]+)\\.([0-9]{2})\\.([0-9]{2})-([0-9]+)\\.([0-9]{2})\\.([0-9]{2})")){
                throw new RuntimeException("date format error! please input date like this: 2020.05.01-2020.07.01");
            }
            since = date.split("-")[0];
            until = date.split("-")[1];
            if(since.compareTo (until) > 0){
                throw new RuntimeException("The start date should not be greater than the end date !");
            }
        }
        int compileFailedCount = 0;
        int successesCount = 0;
        int scanFailedCount = 0;
        List<IssueScan> issueScans = issueScanDao.getScannedCommitsByRepoIdAndTool (repoId, tool, since, until );
        for(IssueScan issueScan : issueScans){
            if(ScanStatusEnum.getByType (issueScan.getStatus ()) == null){
                continue;
            }
            switch (ScanStatusEnum.getByType (issueScan.getStatus ())){
                case COMPILE_FAILED :
                    compileFailedCount++;
                    break;
                case DONE :
                    successesCount++;
                    break;
                default:
                    scanFailedCount++;
            }
        }

        result.put ("success_commit_counts", successesCount);
        result.put ("compile_failed_commit_counts", compileFailedCount);
        result.put ("failed_commit_counts", scanFailedCount);

        return result;
    }



    private Map<String,Double> getIssueLifeTime(List<Long> times){
        Map<String,Double> result = new HashMap<> ();
        int timesSize = times.size ();
        Collections.sort (times);
        Long allTime = 0L;
        int middleMod = timesSize % 2;
        int middleIndex = 0;
        boolean needSumAvg = false;
        if(middleMod == 0){
            middleIndex = timesSize / 2 -1;
            needSumAvg = true;
        }else{
            middleIndex = timesSize / 2;
        }
        double middleTime = 0;
        if(needSumAvg){
            middleTime = (times.get (middleIndex) + times.get (middleIndex + 1)) * 1.0 / 2 ;
        }else{
            middleTime = times.get (middleIndex);
        }
        result.put ("middle_time", middleTime);

        for(Long time : times){
            allTime += time;
        }

        Double avgSolvedTime = allTime * 1.0 / times.size ();

        result.put ("avg_time", avgSolvedTime);

        result.put ("min_time", times.get (0).doubleValue ());

        result.put ("max_time", times.get (timesSize - 1).doubleValue ());
        return result;
    }

    @GetResource
    private boolean judgeWhetherFirstScannedCommitHasParent(RepoResourceDTO repoResourceDTO, String commit){
        String repoPath = null;
        try{
            repoPath = repoResourceDTO.getRepoPath ();
            if(repoPath == null){
                return false;
            }
            JGitHelper jGitHelper = new JGitHelper (repoPath);
            String[] parentCommits = jGitHelper.getCommitParents (commit);
            if(parentCommits == null || parentCommits.length == 0){
                return false;
            }else{
                return true;
            }

        }finally{
            restInterfaceManager.freeRepoPath (repoResourceDTO.getRepoId (),repoResourceDTO.getRepoPath ());
        }
    }

    @Override
    public Map<String, Integer> getIssueQuantityByConditions(String developer, String repoId, String date, String tool) {
        Map<String, Integer> result = new HashMap<>();
        if("total".equals(repoId)){
            repoId = null;
        }
        String since = null;
        String until = null;
        if (date != null){
            if(!date.matches("([0-9]+)\\.([0-9]{2})\\.([0-9]{2})-([0-9]+)\\.([0-9]{2})\\.([0-9]{2})")){
                throw new RuntimeException("date format error! please input date like this: 2020.05.01-2020.07.01");
            }
            since = date.split("-")[0].replace('.','-');
            until = date.split("-")[1].replace('.','-');
            until = DateTimeUtil.stringToLocalDate(until).plusDays(1).toString();
            if(since.compareTo (until) > 0){
                throw new RuntimeException("The start date should not be greater than the end date !");
            }
        }

        List<Map<String, Object>> addedRawIssues = rawIssueDao.getRawIssuesByCondition(developer, repoId, since, until, tool, RawIssueStatus.ADD.getType (), null);
        //开发者引入的缺陷数量
        int addedRawIssuesCount = addedRawIssues.size();

        List<Map<String, Object>> solvedRawIssues = rawIssueDao.getIssueIdAndGroupCountFromRawIssue(developer, repoId, since, until, tool, RawIssueStatus.SOLVED.getType ());

        //开发者解决缺陷的数量
        int solvedIssuesCount = solvedRawIssues.size();
        //开发者解决缺陷的次数
        int solvedRawIssuesCount = 0;
        //reopen的次数
        int reopen = 0;
        //重复解决缺陷的数量
        int redundancyNum = 0;
        int solvedSelfIssuesCount = 0;
        int solvedOtherIssuesCount = 0;
        for (int i = 0; i < solvedIssuesCount; i++){
            Map<String, Object> map = solvedRawIssues.get(i);
            solvedRawIssuesCount += Integer.parseInt(map.get("issueCount").toString());
            Issue issue = issueDao.getIssueByID(map.get("issue_id").toString());
            if ("Solved".equals(issue.getStatus())){
                redundancyNum += (Integer.parseInt(map.get("issueCount").toString()) - 1);
            }
            if ("Open".equals(issue.getStatus())){
                reopen++;
            }
            //判断issue是谁引入的
            String adder = rawIssueDao.getAdderOfOneIssue(map.get("issue_id").toString());
            //fixme 这里因为commit表不全，导致查询数据库没办法得到adder，就会出现adder为空的情况，目前只统计了adder不为空的情况，所以会有稍许不准
            if (!StringUtils.isEmpty(adder) && adder.equals(developer)){
                solvedSelfIssuesCount++;
            } else if (!StringUtils.isEmpty(adder)){
                solvedOtherIssuesCount++;
            }
            //若adder为空（查询不到adder），默认看作adder为他人，而不是自己
            if (StringUtils.isEmpty(adder)) {
                solvedOtherIssuesCount++;
                log.warn("issue/quantity api: Can't find the adder newInstance issue_id :{}, so we assume this issue is added by other people, not by {}",map.get("issue_id").toString(),developer);
            }
        }

        result.put("selfAddedIssue",addedRawIssuesCount);
        result.put("solvedIssue",solvedIssuesCount);
        result.put("solvedNumber",solvedRawIssuesCount);
        result.put("reopen",reopen);
        result.put("redundancyNum",redundancyNum);
        result.put("solvedSelfIssue",solvedSelfIssuesCount);
        result.put("solvedOtherIssue",solvedOtherIssuesCount);

        return result;
    }

    @Cacheable(cacheNames = {"issueLifeCycleCount"})
    @Override
    public Object getIssueLifecycle(String developer, String repoIdList, String since, String until, String tool, String status, Double percent, String type, String target) {
        List<Map<String, Object>> issueLifeList = new ArrayList<>();
        //repoList是最后sql中查询用的repoId列表
        List<String> repoList = new ArrayList<>();
        if(StringUtils.isEmpty(repoIdList)) {
            repoList = null;
        }else {
            String[] repoIdArray = repoIdList.split(",");
            //先把前端给的repo加入到repoList
            repoList.addAll(Arrays.asList(repoIdArray));
        }

        if ("self-solved".equals(status)){
            List<Map<String, Object>> selfSolvedIssueLife = issueDao.getSolvedIssueLifeCycle(repoList,type,tool,since,until,developer,RawIssueStatus.SOLVED.getType ());
            if ("self".equals(target)){
                for (int i = selfSolvedIssueLife.size()-1; i >= 0; i--) {
                    Map<String, Object> map = selfSolvedIssueLife.get(i);

                    // 判断最终状态是否已解决
                    if (!map.get("status").equals("Solved")){
                        selfSolvedIssueLife.remove(i);
                        continue;
                    }
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
                issueLifeList = selfSolvedIssueLife;
            } else if ("other".equals(target)) {
                for (int i = selfSolvedIssueLife.size() - 1; i >= 0; i--) {
                    Map<String, Object> map = selfSolvedIssueLife.get(i);
                    // 判断最终状态是否已解决
                    if (!map.get("status").equals("Solved")){
                        selfSolvedIssueLife.remove(i);
                        continue;
                    }
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
                issueLifeList = selfSolvedIssueLife;
            } else if ("all".equals(target)){
                for (int i = selfSolvedIssueLife.size()-1; i >= 0; i--) {
                    Map<String, Object> map = selfSolvedIssueLife.get(i);
                    // 判断最终状态是否已解决
                    if (!map.get("status").equals("Solved")){
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
                issueLifeList = selfSolvedIssueLife;
            }
        } else if ("living".equals(status)){
            List<Map<String, Object>> addedIssueLife = issueDao.getOpenIssueLifeCycle(repoList,type,tool,since,until,developer,RawIssueStatus.ADD.getType(),IssueStatusEnum.OPEN.getName());
            for (int i = addedIssueLife.size()-1; i >= 0; i--){
                Map<String, Object> map = addedIssueLife.get(i);
                if ("Solved".equals(map.get("status"))){
                    addedIssueLife.remove(i);
                    continue;
                }
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
            issueLifeList = addedIssueLife;
        } else if ("other-solved".equals(status)){
            List<Map<String, Object>> otherSolvedIssueLife = issueDao.getSolvedIssueLifeCycleByOtherSolved(repoList,type,tool,since,until,developer,RawIssueStatus.SOLVED.getType());
            for (int i = otherSolvedIssueLife.size()-1; i >= 0; i--){
                Map<String, Object> map = otherSolvedIssueLife.get(i);
                //判断最终状态是否已解决
                if (!map.get("status").equals("Solved")){
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
            issueLifeList = otherSolvedIssueLife;
        }else if ("all".equals(status) || status == null){
            List<Map<String, Object>> addedIssueLife = issueDao.getSolvedIssueLifeCycle(repoList,type,tool,since,until,developer,RawIssueStatus.ADD.getType ());
            for (int i = addedIssueLife.size()-1; i >= 0; i--){
                Map<String, Object> map = addedIssueLife.get(i);
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
            issueLifeList = addedIssueLife;
        }

        if (issueLifeList.size()==0){
            Map<String, Integer> percentMap = new HashMap<>();
            percentMap.put("max",0);
            percentMap.put("min",0);
            percentMap.put("avg",0);
            percentMap.put("mid",0);
            percentMap.put("upperQuartile",0);
            percentMap.put("lowerQuartile",0);
            percentMap.put("multiple",0);
            percentMap.put("quantity", 0);
            return percentMap;
        }

        //下面开始处理返回的格式
        List<Integer> lifeCycle = new ArrayList<>();
        for (int i = 0; i < issueLifeList.size(); i++){
            lifeCycle.add(Integer.parseInt(issueLifeList.get(i).get("lifeCycle").toString()));
        }
        DoubleSummaryStatistics statistics = lifeCycle.stream().mapToDouble(Number::doubleValue).summaryStatistics();
        Map<String, Double> percentMap = new HashMap<>();
        if(percent == -1){
            return issueLifeList;
        } else if (percent == -2){
            double mid;//中位数
            double upperQuartile;//上四分位数
            double lowerQuartile;//下四分位数
            double multiple;//众数
            if (lifeCycle.size()%2 != 0){//当元素个数为奇数时
                mid = lifeCycle.get((lifeCycle.size()+1)/2-1);
                upperQuartile = lifeCycle.get(Math.min((int) ((lifeCycle.size()+1)*0.75) - 1,lifeCycle.size()-1));
                lowerQuartile = lifeCycle.get(Math.max((int) ((lifeCycle.size()+1)*0.25) - 1,0));
            }else {//当元素个数为偶数时
                mid = (lifeCycle.get(Math.max((lifeCycle.size() + 1) / 2 - 1, 0)) + lifeCycle.get((lifeCycle.size()+1)/2))/2.0;
                upperQuartile = (lifeCycle.get((int) ((lifeCycle.size()+1)*0.75) - 1) + lifeCycle.get(Math.min((int)((lifeCycle.size()+1)*0.75),lifeCycle.size()-1)))/2.0;
                lowerQuartile = (lifeCycle.get(Math.max((int) ((lifeCycle.size()+1)*0.25) - 1,0)) + lifeCycle.get((int) ((lifeCycle.size()+1)*0.25)))/2.0;
            }
            //求众数
            HashSet<Integer> uniqueData = new HashSet<>(lifeCycle);
            HashMap<Integer,Integer> mass = new HashMap<>();
            int[] count = new int[uniqueData.size()];
            int j=0;
            for (Integer integer1 : uniqueData) {
                for (Integer integer2 : lifeCycle) {
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
            multiple = mass.get(k);
//            System.out.println("数组data的众数为："+mass.get(k));


            percentMap.put("max",statistics.getMax());
            percentMap.put("min",statistics.getMin());
            percentMap.put("avg",statistics.getAverage());
            percentMap.put("mid",mid);
            percentMap.put("upperQuartile",upperQuartile);
            percentMap.put("lowerQuartile",lowerQuartile);
            percentMap.put("multiple",multiple);
            percentMap.put("quantity", (double) issueLifeList.size());
            return percentMap;
        }else if (percent >= 0 && percent <= 100){
            int index = (int) Math.round(percent);
            return getStatisticFromIntegerList(lifeCycle,index);
        }
        return null;
    }

    //根据index（0-100） 获取一个排好序的list（全是数字）,对应位置的元素值
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

        query.put("repoList", SegmentationUtil.splitStringList(query.get("repoList") == null ? null : query.get("repoList").toString()));
        developerWorkload.keySet().forEach(r -> {
            query.put("solver", null);
            query.put("developer", r);
            int developerAddIssueCount = issueDao.getIssueFilterListCount(query);
            query.put("developer", null);
            query.put("solver", r);
            int developerSolvedIssueCount = issueDao.getSolvedIssueFilterListCount(query);
            developersDetail.put(r, new JSONObject(){{
                put("addedIssueCount", developerAddIssueCount);
                put("solvedIssueCount", developerSolvedIssueCount);
                put("loc", developerWorkload.get(r));
                put("addQuality", developerWorkload.get(r) == 0 ? 0 : developerAddIssueCount * 100.0 / developerWorkload.get(r));
                put("solveQuality", developerWorkload.get(r) == 0 ? 0 : developerSolvedIssueCount * 100.0 / developerWorkload.get(r));
            }});
        });

        return developersDetail;
    }

    @Override
    public Map<String, Integer> getDeveloperQuantity(String repoIdList, String tool, String status, Boolean isAdd, String since, String until) {
        Map<String, Integer> result = new HashMap<>();
        List<String> repoList = new ArrayList<>();
        if(StringUtils.isEmpty(repoIdList)) {
            repoList = null;
        }else {
            String[] repoIdArray = repoIdList.split(",");
            //先把前端给的repo加入到repoList
            repoList.addAll(Arrays.asList(repoIdArray));
        }

        if (since != null){
            if(!since.matches(dateRegex)){
                throw new RuntimeException("date format error! please input date like this: 2020-05-01");
            }
        }
        if (until != null){
            if(!until.matches(dateRegex)){
                throw new RuntimeException("date format error! please input date like this: 2020-05-01");
            }
            until = DateTimeUtil.stringToLocalDate(until).plusDays(1).toString();
        }

        // 1.获取所给repo-id-list下的所有开发者
        List<String> developerList = commitDao.getDevelopersByRepoIdList(repoList);

        // 2.获取每个开发者的对应的issue数量
        if (isAdd) {//由这些开发者引入的缺陷
            if ("solved".equals(status) || "Solved".equals(status)){//最终状态是Solved
                for (String developer : developerList) {
                    List<Map<String,Object>> issueList = issueDao.getIssueByRawIssueCommitViewIssueTable(repoList,null,tool,since,until,developer,RawIssueStatus.ADD.getType(),IssueStatusEnum.SOLVED.getName());
                    result.put(developer,issueList.size());
                }
            }
            if ("open".equals(status) || "Open".equals(status)){//最终状态是Open
                for (String developer : developerList) {
                    List<Map<String,Object>> issueList = issueDao.getIssueByRawIssueCommitViewIssueTable(repoList,null,tool,since,until,developer,RawIssueStatus.ADD.getType(),IssueStatusEnum.OPEN.getName());
                    result.put(developer,issueList.size());
                }
            }
        }
        return result;
    }

    private boolean isDateCompliant(String date){
        final String specification = dateRegex;
        return !StringUtils.isEmpty(date) && date.matches(specification);
    }

    @Override
    @CacheEvict(cacheNames = {"issueLifeCycleCount","developerCodeQuality"}, allEntries=true, beforeInvocation = true)
    public void clearCache() {
        log.info("Successfully clear redis cache:issueLifeCycleCount,developerCodeQuality in db1.");
    }
}