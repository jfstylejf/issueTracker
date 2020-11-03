package cn.edu.fudan.measureservice.service.impl;

import cn.edu.fudan.measureservice.component.RestInterfaceManager;
import cn.edu.fudan.measureservice.domain.CodeQuality;
import cn.edu.fudan.measureservice.domain.CommitBase;
import cn.edu.fudan.measureservice.domain.CommitInfoDeveloper;
import cn.edu.fudan.measureservice.domain.RepoMeasure;
import cn.edu.fudan.measureservice.mapper.RepoMeasureMapper;
import cn.edu.fudan.measureservice.portrait.*;
import cn.edu.fudan.measureservice.portrait2.Contribution;
import cn.edu.fudan.measureservice.service.MeasureDeveloperService;
import cn.edu.fudan.measureservice.util.DateTimeUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Service
//@CacheConfig(cacheNames = "portrait")
public class MeasureDeveloperServiceImpl implements MeasureDeveloperService {


    private final RestInterfaceManager restInterfaceManager;
    private final RepoMeasureMapper repoMeasureMapper;


    @Autowired
    public void setMeasureDeveloperService(MeasureDeveloperServiceImpl measureDeveloperService) {
        this.measureDeveloperService = measureDeveloperService;
    }

    private MeasureDeveloperServiceImpl measureDeveloperService;

    public MeasureDeveloperServiceImpl(RestInterfaceManager restInterfaceManager, RepoMeasureMapper repoMeasureMapper) {
        this.restInterfaceManager = restInterfaceManager;
        this.repoMeasureMapper = repoMeasureMapper;
    }

    @Override
    public Object getDeveloperWorkLoad(String developer, String since, String until, String repoUuid) {
        Map<String,Object> developerWorkLoad = new HashMap<>(0);
        List<String> repoUuidList = new ArrayList<>();
        List<String> developerList = new ArrayList<>();
        if(StringUtils.isEmptyOrNull(repoUuid)) {
            repoUuidList = null;
        }else {
            String[] repoIdArray = repoUuid.split(",");
            //先把前端给的repo加入到repoList
            repoUuidList.addAll(Arrays.asList(repoIdArray));
        }
        if(developer!=null && !"".equals(developer)) {
            developerList.add(developer);
        }else {
            developerList = repoMeasureMapper.getDeveloperList(repoUuidList,since,until);
        }
        for (String member : developerList) {
            if(member == null || "".equals(member)) {
                continue;
            }
            Map<String, Object> workLoadList = repoMeasureMapper.getWorkLoadByCondition(repoUuidList, member, since, until);
            developerWorkLoad.put(member,workLoadList);
        }
        return developerWorkLoad;
    }




    @Override
    public Object getStatementByCondition(String repoIdList, String developer, String beginDate, String endDate) throws ParseException {
        List<String> repoList = new ArrayList<>();
        if(StringUtils.isEmptyOrNull(repoIdList)) {
            repoList = null;
        }else {
            String[] repoIdArray = repoIdList.split(",");
            //先把前端给的repo加入到repoList
            repoList.addAll(Arrays.asList(repoIdArray));
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if ("".equals(beginDate) || beginDate == null){
            String dateString = repoMeasureMapper.getFirstCommitDateByCondition(repoList,developer);
            beginDate = dateString.substring(0,10);
        }
        if ("".equals(endDate) || endDate == null){
            endDate = sdf.format(new Date());
        }
        //获取开发者该情况下增、删、change逻辑行数
        int totalStatement =0;
        JSONObject developerStatements = restInterfaceManager.getStatements(repoIdList,beginDate,endDate,developer);
        if(developerStatements != null ) {
            JSONObject developerTotalStatements = developerStatements.getJSONObject("developer");
            if( developerTotalStatements!=null && !developerTotalStatements.isEmpty()) {
                totalStatement = developerTotalStatements.getJSONObject(developer).getIntValue("total");
            }
        }
        double totalDays =  ( sdf.parse(endDate).getTime()-sdf.parse(beginDate).getTime() ) / (1000*60*60*24);
        int workDays =  ((int)totalDays)*5/7;
        double dayAvgStatement = totalStatement*1.0/workDays;

        Map<String,Object> map = new HashMap<>();
        map.put("totalStatement",totalStatement);
        map.put("workDays",workDays);
        map.put("dayAvgStatement",dayAvgStatement);
        return map;
    }

    @Override
    @Cacheable(cacheNames = {"developerMetrics"})
    public DeveloperMetrics getPortrait(String repoId, String developer, String beginDate, String endDate, String token, String tool) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<String> repoList = new ArrayList<>();
        repoList.add(repoId);
        if ("".equals(beginDate) || beginDate == null){
            beginDate = repoMeasureMapper.getFirstCommitDateByCondition(repoList,null).substring(0,10);
        }
        if ("".equals(endDate) || endDate == null){
            Date nowDate = new Date();
            endDate = sdf.format(nowDate);
        }
        List<Map<String, Object>> developerList = repoMeasureMapper.getDeveloperListByRepoIdList(repoList);
        int developerNumber = developerList.size();
        JSONObject projects = restInterfaceManager.getProjectByRepoId(repoId,token);
        if(projects == null) {
            log.error("projects is empty,repoId:{}",repoId);
            return null;
        }

        String branch = null;
        if(projects.getString("branch")!=null) {
            branch = projects.getString("branch");
        }
        String repoName =null;
        if(projects.getString("repoName")!=null) {
            repoName = projects.getString("repoName").substring(1);
        }
        //获取程序员在本项目中第一次提交commit的日期
        LocalDateTime firstCommitDateTime;
        LocalDate firstCommitDate = null;
        JSONObject firstCommitDateData = restInterfaceManager.getFirstCommitDate(developer);
        if("Successful".equals(firstCommitDateData.getString("status"))){
            JSONArray repoDateList = firstCommitDateData.getJSONArray("repos");
            for(int i=0;i<repoDateList.size();i++) {
                if (repoDateList.getJSONObject(i).get("repo_id").equals(repoId)){
                    String dateString = repoDateList.getJSONObject(i).getString("first_commit_time");
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    firstCommitDateTime = LocalDateTime.parse(dateString, fmt);
                    firstCommitDateTime = firstCommitDateTime.plusHours(8);
                    firstCommitDate = firstCommitDateTime.toLocalDate();
                    break;
                }
            }
        } else {//commit接口返回有问题时，通过repo_measure表来查看commit日期
            String dateString = repoMeasureMapper.getFirstCommitDateByCondition(repoList,developer);
            dateString = dateString.substring(0,19);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            firstCommitDateTime = LocalDateTime.parse(dateString, fmt);
            firstCommitDate = firstCommitDateTime.toLocalDate();
        }
        int developerStatement = restInterfaceManager.getStatements(repoId,beginDate,endDate,developer).getIntValue("total");

        //----------------------------------开发效率相关指标-------------------------------------
        Efficiency efficiency = getDeveloperEfficiency(repoId, beginDate, endDate, developer, branch, developerNumber);
        log.info(efficiency.toString());
        int totalLOC = efficiency.getTotalLOC();
        int developerAddStatement = efficiency.getDeveloperAddStatement();
        int totalAddStatement = efficiency.getTotalAddStatement();
        int developerValidLine = efficiency.getDeveloperValidLine();
        int totalValidLine = efficiency.getTotalValidLine();
        int developerLOC = efficiency.getDeveloperLOC();
        int developerCommitCount = efficiency.getDeveloperCommitCount();

        //----------------------------------代码质量相关指标-------------------------------------
        Quality quality = getDeveloperQuality(repoId,developer,beginDate,endDate,tool,token,developerNumber,totalLOC);
        log.info(quality.toString());

        //----------------------------------开发能力相关指标-------------------------------------
        Competence competence = getDeveloperCompetence(repoId,beginDate,endDate,developer,developerNumber,developerAddStatement,totalAddStatement,developerValidLine,totalValidLine);
        log.info(competence.toString());

        return new DeveloperMetrics(firstCommitDate, developerStatement, developerCommitCount, repoName, repoId, developer, efficiency, quality, competence);
    }

    private Efficiency getDeveloperEfficiency(String repoId, String beginDate, String endDate, String developer, String branch,
                                              int developerNumber){
        //提交频率指标
        int totalCommitCount = repoMeasureMapper.getCommitCountsByDuration(repoId, beginDate, endDate, null);
        int developerCommitCount = repoMeasureMapper.getCommitCountsByDuration(repoId, beginDate, endDate, developer);
        //代码量指标
        int developerLOC = repoMeasureMapper.getRepoLOCByDuration(repoId, beginDate, endDate, developer);
        int totalLOC = repoMeasureMapper.getRepoLOCByDuration(repoId, beginDate, endDate, "");
        //获取代码新增、删除逻辑行数数据
        JSONObject allDeveloperStatements = restInterfaceManager.getStatements(repoId,beginDate,endDate,"");
        int developerAddStatement = 0;
        int totalAddStatement = 0;
        int developerDelStatement = 0;
        int totalDelStatement = 0;
        int developerValidLine = 0;
        int totalValidLine = 0;
        JSONObject repoStatements = allDeveloperStatements.getJSONObject("repo");
        if(repoStatements!=null && !repoStatements.isEmpty()) {
            totalAddStatement = repoStatements.getJSONObject(repoId).getIntValue("add");
            totalDelStatement = repoStatements.getJSONObject(repoId).getIntValue("delete");
            totalValidLine = repoStatements.getJSONObject(repoId).getIntValue("current");
        }
        JSONObject developerStatements = allDeveloperStatements.getJSONObject("developer");
        if(developerStatements!=null && !developerStatements.isEmpty()) {
            for(String member : developerStatements.keySet()) {
                if(member.equals(developer)) {
                    developerAddStatement = developerStatements.getJSONObject(developer).getIntValue("add");
                    developerDelStatement = developerStatements.getJSONObject(developer).getIntValue("delete");
                    developerValidLine = developerStatements.getJSONObject(developer).getIntValue("current");
                    break;
                }
            }
        }

        return Efficiency.builder()
                .developerNumber(developerNumber)
                .totalCommitCount(totalCommitCount)
                .developerCommitCount(developerCommitCount)
                .totalLOC(totalLOC)
                .developerLOC(developerLOC)
                .developerAddStatement(developerAddStatement)
                .totalAddStatement(totalAddStatement)
                .developerDelStatement(developerDelStatement)
                .totalDelStatement(totalDelStatement)
                .developerValidLine(developerValidLine)
                .totalValidLine(totalValidLine)
                .build();
    }

    private Quality getDeveloperQuality(String repoId, String developer, String beginDate, String endDate, String tool, String token, int developerNumber, int totalLOC){
        //个人规范类issue数
        int developerStandardIssueCount = restInterfaceManager.getIssueCountByConditions(developer, repoId, beginDate, endDate, tool, "standard", token);
        //个人安全类issue数
        int developerSecurityIssueCount = restInterfaceManager.getIssueCountByConditions(developer, repoId, beginDate, endDate, tool, "security", token);
        //repo总issue数
        int totalIssueCount = restInterfaceManager.getIssueCountByConditions("", repoId, beginDate, endDate, tool, "", token);
        JSONArray issueList = restInterfaceManager.getNewElmIssueCount(repoId, beginDate, endDate, tool, token);
        int developerNewIssueCount = 0;//个人新增缺陷数
        int totalNewIssueCount = 0;//总新增缺陷数
        if (!issueList.isEmpty()){
            for (int i = 0; i < issueList.size(); i++){
                JSONObject each = issueList.getJSONObject(i);
                String developerName = each.getString("developer");
                int newIssueCount = each.getIntValue("newIssueCount");
                if (developer.equals(developerName)){
                    developerNewIssueCount = newIssueCount;
                }
                totalNewIssueCount += newIssueCount;
            }
        }
        return Quality.builder()
                .developerNumber(developerNumber)
                .developerNewIssueCount(developerNewIssueCount)
                .developerSecurityIssueCount(developerSecurityIssueCount)
                .developerStandardIssueCount(developerStandardIssueCount)
                .totalIssueCount(totalIssueCount)
                .totalNewIssueCount(totalNewIssueCount)
                .totalLOC(totalLOC)
                .build();
    }

    private Competence getDeveloperCompetence(String repoId, String beginDate, String endDate, String developer,
                                              int developerNumber, int developerAddStatement, int totalAddStatement,
                                              int developerValidLine, int totalValidLine){
        int developerAddLine = repoMeasureMapper.getAddLinesByDuration(repoId, beginDate, endDate, developer);
        JSONObject cloneMeasure = restInterfaceManager.getCloneMeasure(repoId, developer, beginDate, endDate);
        int increasedCloneLines = 0;
        int selfIncreasedCloneLines = 0;
        int eliminateCloneLines = 0;
        int allEliminateCloneLines = 0;
        if (cloneMeasure != null){
            increasedCloneLines = Integer.parseInt(cloneMeasure.getString("increasedCloneLines"));
            selfIncreasedCloneLines = Integer.parseInt(cloneMeasure.getString("selfIncreasedCloneLines"));
            eliminateCloneLines = Integer.parseInt(cloneMeasure.getString("eliminateCloneLines"));
            allEliminateCloneLines = Integer.parseInt(cloneMeasure.getString("allEliminateCloneLines"));
        }
        JSONObject developerCodeLifeCycle = restInterfaceManager.getCodeLifeCycle(repoId,developer,beginDate,endDate);
        double changedCodeAVGAge = 0;
        int changedCodeMAXAge = 0;
        double deletedCodeAVGAge = 0;
        int deletedCodeMAXAge = 0;
        if(developerCodeLifeCycle!=null) {
            JSONObject change = developerCodeLifeCycle.getJSONObject("change");
            JSONObject delete = developerCodeLifeCycle.getJSONObject("delete");
            if(change!=null && change.getJSONObject("developer")!=null && !change.getJSONObject("developer").isEmpty())  {
                if(change.getJSONObject("developer").getJSONObject(developer) != null && !change.getJSONObject("developer").getJSONObject(developer).isEmpty()) {
                    changedCodeAVGAge = change.getJSONObject("developer").getJSONObject(developer).getDoubleValue("average");
                }
                if(change.getJSONObject("developer").getJSONObject(developer) != null && !change.getJSONObject("developer").getJSONObject(developer).isEmpty()) {
                    changedCodeAVGAge = change.getJSONObject("developer").getJSONObject(developer).getIntValue("max");
                }
            }
            if(delete!=null && delete.getJSONObject("developer")!=null && !delete.getJSONObject("developer").isEmpty()) {
                if(delete.getJSONObject("developer").getJSONObject(developer) != null && !delete.getJSONObject("developer").getJSONObject(developer).isEmpty()) {
                    changedCodeAVGAge = change.getJSONObject("developer").getJSONObject(developer).getDoubleValue("average");
                }
                if(delete.getJSONObject("developer").getJSONObject(developer) != null && !delete.getJSONObject("developer").getJSONObject(developer).isEmpty()) {
                    changedCodeAVGAge = change.getJSONObject("developer").getJSONObject(developer).getIntValue("max");
                }
            }
        }

        JSONObject focusMeasure = restInterfaceManager.getFocusFilesCount(repoId,null,beginDate,endDate);
        int totalChangedFile = 0;
        int developerFocusFile = 0;
        if (focusMeasure != null){
            totalChangedFile = focusMeasure.getIntValue("total");
            developerFocusFile = focusMeasure.getJSONObject("developer").getIntValue(developer);
        }

        int repoAge = repoMeasureMapper.getRepoAge(repoId);


        return Competence.builder()
                .developerNumber(developerNumber)
                .developerAddStatement(developerAddStatement)
                .totalAddStatement(totalAddStatement)
                .developerAddLine(developerAddLine)
                .increasedCloneLines(increasedCloneLines)
                .selfIncreasedCloneLines(selfIncreasedCloneLines)
                .eliminateCloneLines(eliminateCloneLines)
                .allEliminateCloneLines(allEliminateCloneLines)
                .totalChangedFile(totalChangedFile)
                .developerFocusFile(developerFocusFile)
                .changedCodeAVGAge(changedCodeAVGAge)
                .changedCodeMAXAge(changedCodeMAXAge)
                .deletedCodeAVGAge(deletedCodeAVGAge)
                .deletedCodeMAXAge(deletedCodeMAXAge)
                .repoAge(repoAge)
                .developerValidLine(developerValidLine)
                .totalValidLine(totalValidLine)
                .build();
    }

    @Cacheable(cacheNames = {"portraitLevel"})
    @Override
    public Object getPortraitLevel(String developer, String since, String until, String token) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date nowDate = new Date();
        String tool = "sonarqube";
        //获取developerMetricsList
        List<String> repoList = repoMeasureMapper.getRepoListByDeveloper(developer,since,until);
        if (repoList.size()==0){
            return "选定时间段内无参与项目！";
        }
        List<DeveloperMetrics> developerMetricsList = new ArrayList<>();
        if(until==null) {
            until = sdf.format(nowDate);
        }
        for (String repoId : repoList) {
            JSONObject projects = restInterfaceManager.getProjectByRepoId(repoId,token);
            String repoName = projects.getString("repoName").substring(1);
            if (StringUtils.isEmptyOrNull(since)){
                List<String> repoIdList = new ArrayList<>();
                repoIdList.add(repoId);
                since = repoMeasureMapper.getFirstCommitDateByCondition(repoIdList,null).substring(0,10);
            }
            log.info("Start to get portrait of " + developer + " in repo : " + repoName);
            DeveloperMetrics metrics = measureDeveloperService.getPortrait(repoId, developer, since, until, token, tool);
            developerMetricsList.add(metrics);
            log.info("Successfully get portrait of " + developer + " in repo : " + repoName);

        }
        log.info("Get portrait of " + developer + " complete!" );
        //获取第一次提交commit的日期
        Date firstCommitDate = sdf.parse(restInterfaceManager.getFirstCommitDate(developer).getJSONObject("repos_summary").getString("first_commit_time_summary"));
        String firstDate = sdf.format(firstCommitDate);
        int totalCommitCount = repoMeasureMapper.getCommitCountsByDuration(null, null, null, developer);
        log.info("totalCommitCount:"+ totalCommitCount);
        int totalStatement = restInterfaceManager.getStatements("",null,null,developer).getIntValue("total");
        log.info("totalStatement:"+totalStatement);
        int days = ((int) ((nowDate.getTime()-firstCommitDate.getTime()) / (1000 * 60 * 60 *24)) )* 5/7 ;
        int dayAverageStatement = totalStatement/days;
        //todo 日后需要添加程序员类型接口 目前统一认为是java后端工程师
        String index = repoMeasureMapper.getDeveloperType(developer);
        String developerType = null ;
        if("L".equals(index)) {
            developerType = "项目负责人";
        }else if("P".equals(index)) {
            developerType = "JAVA后端工程师";
        }else if ("M".equals(index)) {
            developerType= "开发经理";
        }
        return new DeveloperPortrait(firstDate,totalStatement,dayAverageStatement,totalCommitCount,developer,developerType,developerMetricsList);
    }


    @Cacheable(cacheNames = {"developerMetricsNew"})
    public cn.edu.fudan.measureservice.portrait2.DeveloperMetrics getDeveloperMetrics(String repoId, String developer, String beginDate, String endDate, String token, String tool) {
        if ("".equals(beginDate) || beginDate == null){
            List<String> repoIdList = new ArrayList<>();
            repoIdList.add(repoId);
            beginDate = repoMeasureMapper.getFirstCommitDateByCondition(repoIdList,null).substring(0,10);
        }
        if ("".equals(endDate) || endDate == null){
            LocalDate today = LocalDate.now();
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            endDate = df.format(today);
        }

        JSONObject projects = restInterfaceManager.getProjectByRepoId(repoId,token);
        String branch = null;
        String repoName = null;
        if(projects!=null) {
            branch = projects.getString("branch");
            repoName = projects.getString("repoName").substring(1);
        }
        //获取程序员在本项目中第一次提交commit的日期
        LocalDateTime firstCommitDateTime;
        LocalDate firstCommitDate = null;
        JSONObject firstCommitDateData = restInterfaceManager.getFirstCommitDate(developer);
        if("Successful".equals(firstCommitDateData.getString("status"))){
            JSONArray repoDateList = firstCommitDateData.getJSONArray("repos");
            for(int i=0;i<repoDateList.size();i++) {
                if (repoDateList.getJSONObject(i).get("repo_id").equals(repoId)){
                    String dateString = repoDateList.getJSONObject(i).getString("first_commit_time");
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    firstCommitDateTime = LocalDateTime.parse(dateString, fmt);
                    firstCommitDateTime = firstCommitDateTime.plusHours(8);
                    firstCommitDate = firstCommitDateTime.toLocalDate();
                    break;
                }
            }
        } else {//commit接口返回有问题时，通过repo_measure表来查看commit日期
            List<String> repoIdList = new ArrayList<>();
            repoIdList.add(repoId);
            String dateString = repoMeasureMapper.getFirstCommitDateByCondition(repoIdList,developer);
            dateString = dateString.substring(0,19);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            firstCommitDateTime = LocalDateTime.parse(dateString, fmt);
            firstCommitDate = firstCommitDateTime.toLocalDate();
        }

        int developerLOC = repoMeasureMapper.getLOCByCondition(repoId,developer,beginDate,endDate);
        int developerCommitCount = repoMeasureMapper.getCommitCountsByDuration(repoId, beginDate, endDate, developer);
        int developerValidCommitCount = repoMeasureMapper.getDeveloperValidCommitCount(repoId,beginDate,endDate,developer);
        //获取代码新增、删除,change逻辑行数数据
        int developerTotalStatement =0;
        int developerAddStatement = 0;
        int developerDelStatement = 0;
        int developerChangeStatement = 0;
        int developerValidStatement = 0;
        int totalAddStatement = 0;
        int totalValidStatement = 0;
        JSONObject developerStatements = restInterfaceManager.getStatements(repoId, beginDate, endDate, developer);
        JSONObject allDeveloperStatements = restInterfaceManager.getStatements(repoId,beginDate,endDate,"");
        if  (developerStatements != null){
            developerTotalStatement = developerStatements.getIntValue("total");
            if(developerStatements.getJSONObject("developer")!= null && !developerStatements.getJSONObject("developer").isEmpty()) {
                developerAddStatement = developerStatements.getJSONObject("developer").getJSONObject(developer).getIntValue("add");
                developerDelStatement = developerStatements.getJSONObject("developer").getJSONObject(developer).getIntValue("delete");
                developerChangeStatement = developerStatements.getJSONObject("developer").getJSONObject(developer).getIntValue("change");
                developerValidStatement = developerStatements.getJSONObject("developer").getJSONObject(developer).getIntValue("current");
            }
        }
        if(allDeveloperStatements != null && (allDeveloperStatements.getJSONObject("developer") !=null) ) {
            for (String member : allDeveloperStatements.getJSONObject("developer").keySet()) {
                totalAddStatement += allDeveloperStatements.getJSONObject("developer").getJSONObject(member).getIntValue("add");
                totalValidStatement += allDeveloperStatements.getJSONObject("developer").getJSONObject(member).getIntValue("current");
            }
        }

        //开发效率相关指标
        cn.edu.fudan.measureservice.portrait2.Efficiency efficiency = getEfficiency(repoId, beginDate, endDate, developer, tool, token);

        //代码质量相关指标
        cn.edu.fudan.measureservice.portrait2.Quality quality = getQuality(repoId,developer,beginDate,endDate,tool,token,developerLOC,developerValidCommitCount);

        //贡献价值相关指标
        cn.edu.fudan.measureservice.portrait2.Contribution contribution = getContribution(repoId,beginDate,endDate,developer,developerLOC,branch,developerAddStatement,developerChangeStatement,developerValidStatement,totalAddStatement,totalValidStatement);

        return new cn.edu.fudan.measureservice.portrait2.DeveloperMetrics(firstCommitDate, developerTotalStatement, developerCommitCount, repoName, repoId, branch, developer, efficiency, quality, contribution);
    }

    private cn.edu.fudan.measureservice.portrait2.Efficiency getEfficiency(String repoId,String beginDate, String endDate, String developer, String tool, String token){

        int commitNum = 0;
        int completedJiraNum = 0;
        int jiraBug = 0;
        int jiraFeature = 0;
        int solvedSonarIssue = 0;
        int days = 0;

        JSONArray jiraResponse = restInterfaceManager.getJiraMsgOfOneDeveloper(developer, repoId);
        if (jiraResponse != null){
            JSONObject commitPerJiraData = jiraResponse.getJSONObject(0);
            JSONObject jiraBugData = jiraResponse.getJSONObject(4);
            JSONObject jiraFeatureData = jiraResponse.getJSONObject(5);
            commitNum = commitPerJiraData.getIntValue("commitNum");
            completedJiraNum = commitPerJiraData.getIntValue("completedJiraNum");
            jiraBug = jiraBugData.getIntValue("completedBugNum");
            jiraFeature = jiraFeatureData.getIntValue("completedFeatureNum");
        }

        JSONObject sonarResponse = restInterfaceManager.getDayAvgSolvedIssue(developer,repoId,beginDate,endDate,tool,token);
        if (sonarResponse != null){
            solvedSonarIssue = sonarResponse.getIntValue("solvedIssuesCount");
            days = (int) sonarResponse.getDoubleValue("days");
        }
        return cn.edu.fudan.measureservice.portrait2.Efficiency.builder()
                .jiraBug(jiraBug)
                .jiraFeature(jiraFeature)
                .solvedSonarIssue(solvedSonarIssue)
                .days(days)
                .commitNum(commitNum)
                .completedJiraNum(completedJiraNum)
                .build();
    }
    private cn.edu.fudan.measureservice.portrait2.Quality getQuality(String repoId, String developer, String beginDate, String endDate, String tool, String token, int developerLOC, int developerCommitCount){
        //个人引入规范类issue数
        int developerStandardIssueCount = restInterfaceManager.getIssueCountByConditions(developer, repoId, beginDate, endDate, tool, "standard", token);
        //个人引入issue总数
        int developerNewIssueCount = restInterfaceManager.getIssueCountByConditions(developer, repoId, beginDate, endDate, tool, null, token);
        //团队引入规范类issue数
        int totalStandardIssueCount = restInterfaceManager.getIssueCountByConditions(null, repoId, beginDate, endDate, tool, "standard", token);
        //团队引入issue总数
        int totalNewIssueCount = restInterfaceManager.getIssueCountByConditions(null, repoId, beginDate, endDate, tool, null, token);

        int developerJiraCount = repoMeasureMapper.getJiraCount(developer,repoId,beginDate,endDate);
        int developerJiraBugCount = 0;
        int totalJiraBugCount = 0;
        JSONObject jiraBugData = restInterfaceManager.getDefectRate(developer,repoId,beginDate,endDate);
        if (jiraBugData!=null){
            developerJiraBugCount = jiraBugData.getIntValue("individual_bugs");
            totalJiraBugCount = jiraBugData.getIntValue("team_bugs");
        }
        return cn.edu.fudan.measureservice.portrait2.Quality.builder()
                .developerStandardIssueCount(developerStandardIssueCount)
                .totalStandardIssueCount(totalStandardIssueCount)
                .developerNewIssueCount(developerNewIssueCount)
                .totalNewIssueCount(totalNewIssueCount)
                .developerLOC(developerLOC)
                .developerValidCommitCount(developerCommitCount)
                .developerJiraCount(developerJiraCount)
                .developerJiraBugCount(developerJiraBugCount)
                .totalJiraBugCount(totalJiraBugCount)
                .build();
    }

    private cn.edu.fudan.measureservice.portrait2.Contribution getContribution(String repoId, String beginDate, String endDate, String developer,
                                                                               int developerLOC, String branch, int developerAddStatement,
                                                                               int developerChangeStatement, int developerValidStatement,
                                                                               int totalAddStatement, int totalValidStatement){
        int totalLOC = repoMeasureMapper.getLOCByCondition(repoId,null,beginDate,endDate);
        int developerAddLine = repoMeasureMapper.getAddLinesByDuration(repoId, beginDate, endDate, developer);

        JSONObject cloneMeasure = restInterfaceManager.getCloneMeasure(repoId, developer, beginDate, endDate);
        int increasedCloneLines = 0;
        if (cloneMeasure != null){
            increasedCloneLines = Integer.parseInt(cloneMeasure.getString("increasedCloneLines"));
        }

        int developerAssignedJiraCount = 0;//个人被分配到的jira任务个数（注意不是次数）
        int totalAssignedJiraCount = 0;//团队被分配到的jira任务个数（注意不是次数）
        int developerSolvedJiraCount = 0;//个人解决的jira任务个数（注意不是次数）
        int totalSolvedJiraCount = 0;//团队解决的jira任务个数（注意不是次数）

        JSONObject assignedJiraData = restInterfaceManager.getAssignedJiraRate(developer,repoId,beginDate,endDate);
        if (assignedJiraData!=null){
            developerAssignedJiraCount = assignedJiraData.getIntValue("individual_assigned_jira_num");
            totalAssignedJiraCount = assignedJiraData.getIntValue("team_assigned_jira_num");
        }
        JSONObject solvedAssignedJiraData = restInterfaceManager.getSolvedAssignedJiraRate(developer,repoId,beginDate,endDate);
        if (solvedAssignedJiraData!=null){
            developerSolvedJiraCount = solvedAssignedJiraData.getIntValue("individual_solved_assigned_jira_num");
            totalSolvedJiraCount = solvedAssignedJiraData.getIntValue("team_solved_assigned_jira_num");
        }
        return Contribution.builder()
                .developerAddLine(developerAddLine)
                .developerLOC(developerLOC)
                .totalLOC(totalLOC)
                .developerAddStatement(developerAddStatement)
                .developerChangeStatement(developerChangeStatement)
                .developerValidLine(developerValidStatement)
                .totalAddStatement(totalAddStatement)
                .totalValidLine(totalValidStatement)
                .increasedCloneLines(increasedCloneLines)
                .developerAssignedJiraCount(developerAssignedJiraCount)
                .totalAssignedJiraCount(totalAssignedJiraCount)
                .developerSolvedJiraCount(developerSolvedJiraCount)
                .totalSolvedJiraCount(totalSolvedJiraCount)
                .build();
    }


    @Cacheable(cacheNames = {"portraitCompetence"})
    @Override
    public Object getPortraitCompetence(String developer,String repoIdList,String since,String until, String token) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //这里是获取开发者在给定时间段内所有参与的项目
        List<String> filterdRepoList = repoMeasureMapper.getRepoListByDeveloper(developer,since,until);
        //repoList是最后用于计算画像的所有项目
        List<String> repoList = new ArrayList<>();
        if(StringUtils.isEmptyOrNull(repoIdList)) {
            repoList = filterdRepoList;
        }else {
            String[] repoIdArray = repoIdList.split(",");
            //先把前端给的repo加入到repoList
            repoList.addAll(Arrays.asList(repoIdArray));
            for (int i = repoList.size() - 1; i >= 0; i--){//注意需要倒序删除
                //如果前端给的参数里的repoId 不在筛选后的list里 就去掉这个repo
                if (!filterdRepoList.contains(repoList.get(i))){
                    repoList.remove(i);
                }
            }
        }
        if (repoList.size()==0){
            return "选定时间段内无参与项目！";
        }
        List<String> developerList = new ArrayList<>();
        if( developer == null || "".equals(developer)) {
            developerList = repoMeasureMapper.getDeveloperList(repoList,since,until);
        }else {
            developerList.add(developer);
        }
        //获取developerMetricsList
        Map<String,List<cn.edu.fudan.measureservice.portrait2.DeveloperMetrics>> developerMetricMap = new HashMap<>();
        LocalDate today = LocalDate.now();
        if (StringUtils.isEmptyOrNull(until)){
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            until = df.format(today);
        }
        for( String member : developerList) {
            List<cn.edu.fudan.measureservice.portrait2.DeveloperMetrics> developerMetricsList = new ArrayList<>();
            for (String repo : repoList) {
                JSONObject projects = restInterfaceManager.getProjectByRepoId(repo,token);
                for (int i = 0; i < projects.size(); i++){
                    String tool = "sonarqube";
                    String repoName = projects.getString("repoName").substring(1);
                    if(StringUtils.isEmptyOrNull(since)){
                        List<String> repoIdList1 = new ArrayList<>();
                        repoIdList1.add(repo);
                        since = repoMeasureMapper.getFirstCommitDateByCondition(repoIdList1,null).substring(0,10);
                    }
                    since = sdf.format(sdf.parse(since));
                    //String endDate = repoMeasureMapper.getLastCommitDateOfOneRepo(repoId);
                    log.info("Start to get portrait of " + developer + " in repo : " + repoName);
                    cn.edu.fudan.measureservice.portrait2.DeveloperMetrics metrics = measureDeveloperService.getDeveloperMetrics(repo, member, since, until, token, tool);
                    developerMetricsList.add(metrics);
                    log.info("Successfully get portrait of " +member + " in repo : " + repoName);
                }

            }
            developerMetricMap.put(member,developerMetricsList);
            log.info("Get portrait of " + member + " complete!" );
        }
        log.info("Get portrait complete!" );
        //获取第一次提交commit的日期
        LocalDateTime firstCommitDateTime;
        LocalDate firstCommitDate = null;
        List<cn.edu.fudan.measureservice.portrait2.DeveloperPortrait> developerPortraitList = new ArrayList<>();
        for(String key : developerMetricMap.keySet()) {
            JSONObject firstCommitDateData = restInterfaceManager.getFirstCommitDate(key);
            if("Successful".equals(firstCommitDateData.getString("status"))){
                JSONObject repoDateList = firstCommitDateData.getJSONObject("repos_summary");
                String dateString = repoDateList.getString("first_commit_time_summary");
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                firstCommitDateTime = LocalDateTime.parse(dateString, fmt);
                firstCommitDateTime = firstCommitDateTime.plusHours(8);
                firstCommitDate = firstCommitDateTime.toLocalDate();
            }else {
                //commit接口返回有问题时，通过repo_measure表来查看commit日期
                String dateString = repoMeasureMapper.getFirstCommitDateByCondition(null,key);
                dateString = dateString.substring(0,19);
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                firstCommitDateTime = LocalDateTime.parse(dateString, fmt);
                firstCommitDate = firstCommitDateTime.toLocalDate();
            }
            int totalCommitCount = repoMeasureMapper.getCommitCountsByDuration(null, null, null, key);
            //todo 日后需要添加程序员类型接口 目前统一认为是java后端工程师
            String index = repoMeasureMapper.getDeveloperType(key);
            String developerType = null ;
            if("L".equals(index)) {
                developerType = "项目负责人";
            }else if("P".equals(index)) {
                developerType = "JAVA后端工程师";
            }else if ("M".equals(index)) {
                developerType= "开发经理";
            }
            // 获取开发者在所有项目中的整个的用户画像
            cn.edu.fudan.measureservice.portrait2.DeveloperMetrics totalDeveloperMetrics = getTotalDeveloperMetrics(developerMetricMap.get(key),key,firstCommitDate);
            int totalStatement = totalDeveloperMetrics.getTotalStatement();
            int totalDays = (int) (today.toEpochDay()-firstCommitDate.toEpochDay());
            int workDays =  totalDays*5/7;
            int dayAverageStatement = totalStatement/workDays;
            developerPortraitList.add(new cn.edu.fudan.measureservice.portrait2.DeveloperPortrait(firstCommitDate,totalStatement,dayAverageStatement,totalCommitCount,key,developerType,developerMetricMap.get(key),totalDeveloperMetrics));
        }
        return developerPortraitList;
    }

    /**
     *
     * @param developerMetricsList 每个项目的画像数据
     * @param developer 开发者
     * @return 返回开发者在所有项目当中的整体画像数据
     */
    private cn.edu.fudan.measureservice.portrait2.DeveloperMetrics getTotalDeveloperMetrics(List<cn.edu.fudan.measureservice.portrait2.DeveloperMetrics> developerMetricsList, String developer, LocalDate firstCommitDate){
        int totalStatement = 0;
        int totalCommitCount = 0;

        //efficiency
        int totalDays = (int) (LocalDate.now().toEpochDay()-firstCommitDate.toEpochDay());
        int workDays =  totalDays*5/7;
        int jiraBug = 0;
        int jiraFeature = 0;
        int solvedSonarIssue = 0;
        int commitNum = 0;
        int completedJiraNum = 0;

        //quality
        int developerStandardIssueCount = 0;
        int totalStandardIssueCount = 0;
        int developerNewIssueCount = 0;//个人引入问题
        int totalNewIssueCount = 0;//团队引入问题
        int developerLOC = 0;//个人addLines+delLines
        int developerValidCommitCount = 0;//个人提交的commit总数
        int developerJiraCount = 0;//个人提交的commit当中 关联有jira的个数
        int developerJiraBugCount = 0;//个人jira任务中属于bug类型的数量
        int totalJiraBugCount = 0;//团队jira任务中属于bug类型的数量

        //contribution
        int developerAddLine = 0;
        //int developerLOC
        int totalLOC = 0;
        int developerAddStatement = 0;
        int developerChangeStatement = 0;
        int developerValidLine = 0;//个人存活语句
        int totalAddStatement = 0;//团队新增语句
        int totalValidLine = 0;//团队存活语句
        int increasedCloneLines = 0;//个人新增重复代码行数
        int developerAssignedJiraCount = 0;//个人被分配到的jira任务个数（注意不是次数）
        int totalAssignedJiraCount = 0;//团队被分配到的jira任务个数（注意不是次数）
        int developerSolvedJiraCount = 0;//个人解决的jira任务个数（注意不是次数）
        int totalSolvedJiraCount = 0;//团队解决的jira任务个数（注意不是次数）

        //对每个项目的数据进行累加，便于求整体的画像数据
        for (int i = 0; i < developerMetricsList.size(); i++){
            cn.edu.fudan.measureservice.portrait2.DeveloperMetrics metric = developerMetricsList.get(i);
            cn.edu.fudan.measureservice.portrait2.Efficiency efficiency = metric.getEfficiency();
            cn.edu.fudan.measureservice.portrait2.Quality quality = metric.getQuality();
            Contribution contribution = metric.getContribution();

            totalStatement += metric.getTotalStatement();
            totalCommitCount += metric.getTotalCommitCount();

            jiraBug += efficiency.getJiraBug();
            jiraFeature += efficiency.getJiraFeature();
            solvedSonarIssue += efficiency.getSolvedSonarIssue();
            commitNum += efficiency.getCommitNum();
            completedJiraNum += efficiency.getCompletedJiraNum();

            developerStandardIssueCount += quality.getDeveloperStandardIssueCount();
            totalStandardIssueCount += quality.getTotalStandardIssueCount();
            developerNewIssueCount += quality.getDeveloperNewIssueCount();
            totalNewIssueCount += quality.getTotalNewIssueCount();
            developerLOC += quality.getDeveloperLOC();
            developerValidCommitCount += quality.getDeveloperValidCommitCount();
            developerJiraCount += quality.getDeveloperJiraCount();
            developerJiraBugCount += quality.getDeveloperJiraBugCount();
            totalJiraBugCount += quality.getTotalJiraBugCount();

            totalLOC += contribution.getTotalLOC();
            developerAddStatement += contribution.getDeveloperAddStatement();
            developerChangeStatement += contribution.getDeveloperChangeStatement();
            totalAddStatement += contribution.getTotalAddStatement();
            developerAddLine += contribution.getDeveloperAddLine();
            developerValidLine += contribution.getDeveloperValidLine();
            totalValidLine += contribution.getTotalValidLine();
            increasedCloneLines += contribution.getIncreasedCloneLines();
            developerAssignedJiraCount += contribution.getDeveloperAssignedJiraCount();
            totalAssignedJiraCount += contribution.getTotalAssignedJiraCount();
            developerSolvedJiraCount += contribution.getDeveloperSolvedJiraCount();
            totalSolvedJiraCount += contribution.getTotalSolvedJiraCount();
        }
        cn.edu.fudan.measureservice.portrait2.Efficiency totalEfficiency = cn.edu.fudan.measureservice.portrait2.Efficiency.builder()
                .jiraBug(jiraBug).jiraFeature(jiraFeature).solvedSonarIssue(solvedSonarIssue).days(workDays)
                .commitNum(commitNum).completedJiraNum(completedJiraNum).build();

        cn.edu.fudan.measureservice.portrait2.Quality totalQuality = cn.edu.fudan.measureservice.portrait2.Quality.builder()
                .developerStandardIssueCount(developerStandardIssueCount).totalStandardIssueCount(totalStandardIssueCount)
                .developerNewIssueCount(developerNewIssueCount).totalNewIssueCount(totalNewIssueCount)
                .developerLOC(developerLOC).developerValidCommitCount(developerValidCommitCount).developerJiraCount(developerJiraCount)
                .developerJiraBugCount(developerJiraBugCount).totalJiraBugCount(totalJiraBugCount).build();

        Contribution totalContribution = Contribution.builder().developerLOC(developerLOC).totalLOC(totalLOC)
                .developerAddStatement(developerAddStatement).developerChangeStatement(developerChangeStatement)
                .totalAddStatement(totalAddStatement).developerAddLine(developerAddLine)
                .developerValidLine(developerValidLine).totalValidLine(totalValidLine)
                .increasedCloneLines(increasedCloneLines).developerAssignedJiraCount(developerAssignedJiraCount)
                .totalAssignedJiraCount(totalAssignedJiraCount).developerSolvedJiraCount(developerSolvedJiraCount)
                .totalSolvedJiraCount(totalSolvedJiraCount).build();

        return new cn.edu.fudan.measureservice.portrait2.DeveloperMetrics(totalStatement, totalCommitCount, totalEfficiency,totalQuality,totalContribution);

    }


    @Cacheable(cacheNames = {"developerRecentNews"})
    @Override
    public Object getDeveloperRecentNews(String repoId, String developer, String beginDate, String endDate) {
        if (endDate != null) {
            endDate = DateTimeUtil.stringToLocalDate(endDate).plusDays(1).toString();
        }
        List<Map<String, Object>> commitMsgList = repoMeasureMapper.getCommitMsgByCondition(repoId, developer, beginDate, endDate);
        for (Map<String, Object> map : commitMsgList) {
            //将数据库中timeStamp/dateTime类型转换成指定格式的字符串 map.get("commit_time") 这个就是数据库中dateTime类型
            String commitTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(map.get("commit_time"));
            map.put("commit_time", commitTime);
            //以下操作是为了获取jira信息
            String commitMessage = map.get("message").toString();
            String jiraID = getJiraIDFromCommitMsg(commitMessage);
            if("noJiraID".equals(jiraID)) {
                map.put("jira_info", "本次commit不含jira单号");
            }else {
                JSONArray jiraResponse = restInterfaceManager.getJiraInfoByKey("key",jiraID);
                if (jiraResponse == null || jiraResponse.isEmpty()){
                    map.put("jira_info", "jira单号内容为空");
                }else {
                    map.put("jira_info", jiraResponse.get(0));
                }
            }
        }
        return commitMsgList;
    }

    /**
     * 根据commit message 返回 对应的 jira 单号
     */
    private String getJiraIDFromCommitMsg(String commitMsg){
        // 使用Pattern类的compile方法，传入jira单号的正则表达式，得到一个Pattern对象
        Pattern pattern = Pattern.compile("[A-Z][A-Z0-9]*-[0-9]+");
        // 调用pattern对象的matcher方法，传入需要匹配的字符串， 得到一个匹配器对象
        Matcher matcher = pattern.matcher(commitMsg);

        // 从字符串开头，返回匹配到的第一个字符串
        if (matcher.find()) {
            // 输出第一次匹配的内容
            log.info("jira ID is : {}",matcher.group());
            return matcher.group();
        }
        return "noJiraID" ;
    }

    @Cacheable(cacheNames = {"developerList"})
    @Override
    public Object getDeveloperList(String repoIdList, String token) throws ParseException {
        List<String> repoList = new ArrayList<>();
        if(repoIdList!=null && !"".equals(repoIdList)) {
            String[] repoArray = repoIdList.split(",");
            repoList.addAll(Arrays.asList(repoArray));
        }else {
            repoList = null;
        }
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> developerList = repoMeasureMapper.getDeveloperDutyTypeListByRepoId(repoList);
        for (int i = 0; i < developerList.size(); i++){
            Map<String,Object> dev = new HashMap<>();
            Map<String,Object> map = developerList.get(i);
            String developerName = map.get("name").toString();
            dev.put("developer_name",developerName);
            String developerDutyType = map.get("account_status").toString();
            if("1".equals(developerDutyType)) {
                dev.put("DutyType","在职");
            } else{
                dev.put("DutyType","离职");
            }
            int involvedRepoCount = (int) getDeveloperInvolvedRepoCount(developerName);
            dev.put("involvedRepoCount",involvedRepoCount);
            DeveloperPortrait portrait = (DeveloperPortrait) measureDeveloperService.getPortraitLevel(developerName,null,null,token);
            double totalLevel = portrait.getLevel();
            double value = portrait.getValue();
            double quality = portrait.getQuality();
            double efficiency = portrait.getEfficiency();
            dev.put("totalLevel",totalLevel);
            dev.put("value",value);
            dev.put("quality",quality);
            dev.put("efficiency",efficiency);
            result.add(dev);
        }
        log.info("Successfully return developerList!");
        return result;
    }


    private Object getDeveloperInvolvedRepoCount(String developer) {
        List<String> repoList = repoMeasureMapper.getRepoListByDeveloper(developer,null,null);
        return repoList.size();
    }

    @Override
    public List<Map<String,Object>> getCommitStandard(String developer, String repoUuid, String since, String until, String token ,String condition) {
        List<Map<String,Object>> list = new ArrayList<>();
        String split = ",";
        List<String> repoUuidList;
        List<String> developerList ;
        if(repoUuid!=null && !"".equals(repoUuid)) {
            repoUuidList = Arrays.asList(repoUuid.split(split));
        }else {
            repoUuidList = repoMeasureMapper.getRepoListByDeveloper(developer,since,until);
        }
        if("1".equals(condition)) {
            developerList = repoMeasureMapper.getDeveloperList(repoUuidList,since,until);
            for(String member : developerList) {
                Map<String,Object> map = new HashMap<>();
                int developerValidCommitCount = repoMeasureMapper.getValidCommitCountsByAllRepo(repoUuidList,since,until,member);
                int developerJiraCount = repoMeasureMapper.getJiraCountByCondition(repoUuidList,since,until,member);
                map.put("developer",member);
                map.put("developerJiraCount",developerJiraCount);
                map.put("developerCommitCount",developerValidCommitCount);
                if(developerValidCommitCount!=0) {
                    map.put("commitStandard",developerJiraCount*1.0/developerValidCommitCount);
                }else {
                    map.put("commitStandard",0);
                }
                list.add(map);
            }
            return list;
        }else if("2".equals(condition)) {
            return repoMeasureMapper.getInvalidCommitMsg(repoUuidList,since,until,developer);
        }
        return null;
    }


    @Override
    @CacheEvict(cacheNames = {"portraitLevel","developerMetrics","developerMetricsNew","portraitCompetence","developerRecentNews","developerList","jiraInfoByJiraID"}, allEntries=true, beforeInvocation = true)
    public void clearCache() {
        log.info("Successfully clear redis cache in db6.");
    }

}