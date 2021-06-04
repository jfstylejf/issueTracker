package cn.edu.fudan.measureservice.service;

import cn.edu.fudan.measureservice.annotation.MethodMeasureAnnotation;
import cn.edu.fudan.measureservice.aop.MethodMeasureAspect;
import cn.edu.fudan.measureservice.component.RestInterfaceManager;
import cn.edu.fudan.measureservice.dao.AccountDao;
import cn.edu.fudan.measureservice.dao.JiraDao;
import cn.edu.fudan.measureservice.dao.MeasureDao;
import cn.edu.fudan.measureservice.dao.ProjectDao;
import cn.edu.fudan.measureservice.domain.Developer;
import cn.edu.fudan.measureservice.domain.Granularity;
import cn.edu.fudan.measureservice.domain.bo.*;
import cn.edu.fudan.measureservice.domain.bo.DeveloperPortrait;
import cn.edu.fudan.measureservice.domain.dto.DeveloperRepoInfo;
import cn.edu.fudan.measureservice.domain.dto.ProjectPair;
import cn.edu.fudan.measureservice.domain.dto.Query;
import cn.edu.fudan.measureservice.domain.dto.RepoInfo;
import cn.edu.fudan.measureservice.domain.enums.GranularityEnum;
import cn.edu.fudan.measureservice.domain.enums.LevelEnum;
import cn.edu.fudan.measureservice.domain.vo.*;
import cn.edu.fudan.measureservice.mapper.RepoMeasureMapper;
import cn.edu.fudan.measureservice.portrait.*;
import cn.edu.fudan.measureservice.portrait2.Contribution;
import cn.edu.fudan.measureservice.util.DateTimeUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.util.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Slf4j
@Service
public class MeasureDeveloperService {


    private final RestInterfaceManager restInterfaceManager;
    private final RepoMeasureMapper repoMeasureMapper;
    private final ProjectDao projectDao;
    private final JiraDao jiraDao;
    private final MeasureDao measureDao;
    private final AccountDao accountDao;
    private MeasureDeveloperService measureDeveloperService;

    private MethodMeasureAspect methodMeasureAspect;

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String Statement_developer = "developer";
    private static final String Statement_repo = "repo";
    private static final String Delete = "delete";
    private static final String Change = "change";
    private static final String Live = "live";
    private static final String Loss = "loss";
    private static final String TOOL = "sonarqube";
    private static final String split = ",";
    private static final DecimalFormat df = new DecimalFormat("0.000");
    /**
     *
     * @param query 查询条件
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<DeveloperWorkLoad> getDeveloperWorkLoad(Query query , String developers) {
        List<DeveloperWorkLoad> developerWorkLoadList = new ArrayList<>();
        List<String> developerList = new ArrayList<>();
        if(query.getDeveloper()!=null && !"".equals(query.getDeveloper())) {
            developerList.add(query.getDeveloper());
        }else if(developers!=null && !"".equals(developers)) {
            developerList = Arrays.asList(developers.split(split));
        }else {
            developerList = projectDao.getDeveloperList(query);
        }
        for (String member : developerList) {
            if(member == null || "".equals(member)) {
                continue;
            }
            query.setDeveloper(member);
            DeveloperWorkLoad developerWorkLoad = measureDao.getDeveloperWorkLoadData(query);
            developerWorkLoad.setDeveloperName(member);
            developerWorkLoad.setTotalLoc(developerWorkLoad.getAddLines() + developerWorkLoad.getDeleteLines());
            developerWorkLoadList.add(developerWorkLoad);
        }
        return developerWorkLoadList;
    }

    public Object getStatementByCondition(String repoUuidList, String developer, String since, String until) throws ParseException {
        List<String> repoList = new ArrayList<>();
        if(StringUtils.isEmptyOrNull(repoUuidList)) {
            repoList = null;
        }else {
            String[] repoUuidArray = repoUuidList.split(",");
            //先把前端给的repo加入到repoList
            repoList.addAll(Arrays.asList(repoUuidArray));
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if ("".equals(since) || since == null){
            String dateString = repoMeasureMapper.getFirstCommitDateByCondition(repoList,developer);
            since = dateString.substring(0,10);
        }
        if ("".equals(until) || until == null){
            until = sdf.format(new Date());
        }
        //获取开发者该情况下增、删、change逻辑行数
        int totalStatement =0;
        List<Map<String,Object>> developerStatements = restInterfaceManager.getStatements(repoUuidList,since,until,developer,Statement_developer);
        if (developerStatements.size()>0) {
            totalStatement = (int) developerStatements.get(0).get("total");
        }
        double totalDays =  ( sdf.parse(until).getTime()-sdf.parse(since).getTime() ) / (1000*60*60*24);
        int workDays =  ((int)totalDays)*5/7;
        double dayAvgStatement = totalStatement*1.0/workDays;

        Map<String,Object> map = new HashMap<>();
        map.put("totalStatement",totalStatement);
        map.put("workDays",workDays);
        map.put("dayAvgStatement",dayAvgStatement);
        return map;
    }

    @MethodMeasureAnnotation
    @SuppressWarnings("unchecked")
    @SneakyThrows
    private Efficiency getDeveloperEfficiency(Query query, String branch,
                                              int developerNumber){
        String repoUuid = query.getRepoUuidList().get(0);
        //提交频率指标
        int totalCommitCount = repoMeasureMapper.getCommitCountsByDuration(repoUuid, query.getSince(), query.getUntil(), null);
        int developerCommitCount = repoMeasureMapper.getCommitCountsByDuration(repoUuid, query.getSince(), query.getUntil(), query.getDeveloper());
        // fixme 代码量指标 全部从repository中拿
        Query query1 = new Query(query.getToken(),query.getSince(),query.getUntil(),null,query.getRepoUuidList());
        DeveloperWorkLoad developerWorkLoad = measureDao.getDeveloperWorkLoadData(query);
        DeveloperWorkLoad developerWorkLoad1 = measureDao.getDeveloperWorkLoadData(query1);
        int developerLOC = developerWorkLoad.getAddLines() + developerWorkLoad.getDeleteLines();
        int totalLOC = developerWorkLoad1.getAddLines() + developerWorkLoad1.getDeleteLines();
        //获取代码新增、删除逻辑行数数据
        List<Map<String,Object>> allDeveloperStatements = restInterfaceManager.getStatements(repoUuid,query.getSince(),query.getUntil(),"",Statement_developer);
        int developerAddStatement = 0;
        int totalAddStatement = 0;
        int developerDelStatement = 0;
        int totalDelStatement = 0;
        int developerValidLine = 0;
        int totalValidLine = 0;
        try {
            if (allDeveloperStatements!=null && allDeveloperStatements.size()>0) {
                for (Map<String,Object> developerStatement : allDeveloperStatements) {
                    if (developerStatement.get("developerName").equals(query.getDeveloper())) {
                        developerAddStatement = (int) ((Map<String,Object>) developerStatement.get("add")).get("total");
                        developerDelStatement = (int) ((Map<String,Object>) developerStatement.get("delete")).get("total");
                        developerValidLine = (int) ((Map<String,Object>) developerStatement.get("current")).get("total");
                    }
                    totalAddStatement += (int) ((Map<String,Object>) developerStatement.get("add")).get("total");
                    totalDelStatement += (int) ((Map<String,Object>) developerStatement.get("delete")).get("total");
                    totalValidLine += (int) ((Map<String,Object>) developerStatement.get("current")).get("total");
                }
            }
        }catch (Exception e) {
            e.getMessage();
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

    @MethodMeasureAnnotation
    private Quality getDeveloperQuality(String repoUuid, String developer, String since, String until, String tool, String token, int developerNumber, int totalLOC){
        //个人规范类issue数
        int developerStandardIssueCount = restInterfaceManager.getIssueCountByConditions(developer, repoUuid, since, until, tool, "Code Smell", token);
        //个人安全类issue数
        int developerSecurityIssueCount = restInterfaceManager.getIssueCountByConditions(developer, repoUuid, since, until, tool, "Bug", token);
        //repo总issue数
        int totalIssueCount = restInterfaceManager.getIssueCountByConditions("", repoUuid, since, until, tool, "", token);
        //个人新增缺陷数
        int developerNewIssueCount = restInterfaceManager.getAddIssueCount(repoUuid,developer,since,until,tool,token);
        //总新增缺陷数
        int totalNewIssueCount = restInterfaceManager.getAddIssueCount(repoUuid,null,since,until,tool,token);
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

    @MethodMeasureAnnotation
    private Competence getDeveloperCompetence(String repoUuid, String since, String until, String developer,
                                              int developerNumber, int developerAddStatement, int totalAddStatement,
                                              int developerValidLine, int totalValidLine){
        int developerAddLine = repoMeasureMapper.getAddLinesByDuration(repoUuid, since, until, developer);
        List<Map<String,Object>> cloneMeasure = restInterfaceManager.getCloneMeasure(repoUuid, developer, since, until);
        int increasedCloneLines = 0;
        int selfIncreasedCloneLines = 0;
        int eliminateCloneLines = 0;
        int allEliminateCloneLines = 0;
        try {
            if (cloneMeasure != null){
                increasedCloneLines = (int) cloneMeasure.get(0).get("increasedCloneLines");
                selfIncreasedCloneLines = (int) cloneMeasure.get(0).get("selfIncreasedCloneLines");
                eliminateCloneLines = (int) cloneMeasure.get(0).get("eliminateCloneLines");
                allEliminateCloneLines = (int) cloneMeasure.get(0).get("allEliminateCloneLines");
            }
        }catch (Exception e) {
            e.getMessage();
        }
        List<Map<String,Object>> developerChangeCodeLifeCycle = restInterfaceManager.getCodeLifeCycle(repoUuid,developer,since,until,Statement_developer,Change);
        List<Map<String,Object>> developerDeleteCodeLifeCycle = restInterfaceManager.getCodeLifeCycle(repoUuid,developer,since,until,Statement_developer,Delete);
        double changedCodeAVGAge = 0;
        double changedCodeMAXAge = 0;
        double deletedCodeAVGAge = 0;
        double deletedCodeMAXAge = 0;
        try {
            if(developerChangeCodeLifeCycle!=null && developerChangeCodeLifeCycle.size()>0) {
                changedCodeAVGAge = (double) developerChangeCodeLifeCycle.get(0).get("average");
                changedCodeMAXAge = (double) developerChangeCodeLifeCycle.get(0).get("max");
            }
            if (developerDeleteCodeLifeCycle!=null && developerDeleteCodeLifeCycle.size()>0) {
                deletedCodeAVGAge = (double) developerDeleteCodeLifeCycle.get(0).get("average");
                deletedCodeMAXAge = (double) developerDeleteCodeLifeCycle.get(0).get("max");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        List<Map<String,Object>> focusMeasure = restInterfaceManager.getFocusFilesCount(repoUuid,null,since,until);
        int totalChangedFile = 0;
        int developerFocusFile = 0;
        try {
            if (focusMeasure!=null && focusMeasure.size()>0){
                for (Map<String,Object> focus : focusMeasure) {
                    if (focus.get("developerName").equals(developer)) {
                        developerFocusFile = (int) focus.get("num");
                    }
                    totalChangedFile += (int) focus.get("num");
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        int repoAge = repoMeasureMapper.getRepoAge(repoUuid);


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

    @SuppressWarnings("unchecked")
    @Cacheable(cacheNames = {"developerMetricsNew"})
    public cn.edu.fudan.measureservice.portrait2.DeveloperMetrics getDeveloperMetrics(String repoUuid, String developer, String since, String until, String token, String tool) {
        if ("".equals(since) || since == null){
            List<String> repoUuidList = new ArrayList<>();
            repoUuidList.add(repoUuid);
            since = repoMeasureMapper.getFirstCommitDateByCondition(repoUuidList,null).substring(0,10);
        }
        if ("".equals(until) || until == null){
            LocalDate today = LocalDate.now();
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            until = df.format(today);
        }

        Map<String,Object> projects = restInterfaceManager.getProjectByrepoUuid(repoUuid,token);
        String branch = null;
        String repoName = null;
        if (projects!=null) {
            branch = (String) projects.get("branch");
            repoName = (String) projects.get("repoName");
            repoName = repoName.replace("/","");
        }
        //获取程序员在本项目中第一次提交commit的日期
        String developerFirstCommitTime = projectDao.getDeveloperFirstCommitDate(developer,repoUuid);
        LocalDate firstCommitDate = LocalDate.parse(developerFirstCommitTime,dtf);
        firstCommitDate = firstCommitDate.plusDays(1);

        int developerLOC = repoMeasureMapper.getLOCByCondition(repoUuid,developer,since,until);
        int developerCommitCount = repoMeasureMapper.getCommitCountsByDuration(repoUuid, since, until, developer);
        int developerValidCommitCount = repoMeasureMapper.getDeveloperValidCommitCount(repoUuid,since,until,developer);
        //获取代码新增、删除,change逻辑行数数据
        int developerTotalStatement =0;
        int developerAddStatement = 0;
        int developerDelStatement = 0;
        int developerChangeStatement = 0;
        int developerValidStatement = 0;
        int totalAddStatement = 0;
        int totalValidStatement = 0;
        List<Map<String,Object>> allDeveloperStatements = restInterfaceManager.getStatements(repoUuid,since,until,"",Statement_developer);
        if (allDeveloperStatements.size()>0) {
            for (Map<String,Object> developerStatement : allDeveloperStatements) {
                if (developerStatement.get("developerName").equals(developer)) {
                    developerTotalStatement = (int) developerStatement.get("total");
                    developerAddStatement = (int) ((Map<String,Object>) developerStatement.get("add")).get("total");
                    developerChangeStatement = (int) ((Map<String,Object>) developerStatement.get("change")).get("total");
                    developerValidStatement = (int) ((Map<String,Object>) developerStatement.get("current")).get("total");
                }
                totalAddStatement += (int) ((Map<String,Object>) developerStatement.get("add")).get("total");
                totalValidStatement += (int) ((Map<String,Object>) developerStatement.get("current")).get("total");
            }
        }

        //开发效率相关指标
        cn.edu.fudan.measureservice.portrait2.Efficiency efficiency = getEfficiency(repoUuid, since, until, developer, tool, token);

        //代码质量相关指标
        cn.edu.fudan.measureservice.portrait2.Quality quality = getQuality(repoUuid,developer,since,until,tool,token,developerLOC,developerValidCommitCount);

        //贡献价值相关指标
        cn.edu.fudan.measureservice.portrait2.Contribution contribution = getContribution(repoUuid,since,until,developer,developerLOC,branch,developerAddStatement,developerChangeStatement,developerValidStatement,totalAddStatement,totalValidStatement);

        return new cn.edu.fudan.measureservice.portrait2.DeveloperMetrics(firstCommitDate, developerTotalStatement, developerCommitCount, repoName, repoUuid, branch, developer, efficiency, quality, contribution);
    }

    private cn.edu.fudan.measureservice.portrait2.Efficiency getEfficiency(String repoUuid,String since, String until, String developer, String tool, String token){

        int commitNum = 0;
        int completedJiraNum = 0;
        int jiraBug = 0;
        int jiraFeature = 0;
        int solvedSonarIssue = 0;
        int days = 0;

        JSONObject jiraResponse = restInterfaceManager.getJiraMsgOfOneDeveloper(developer, repoUuid,since,until);
        if (jiraResponse != null){
            JSONObject commitPerJira = jiraResponse.getJSONObject("commitPerJira");
            completedJiraNum = commitPerJira.getIntValue("finishedJiraSum");
            commitNum = commitPerJira.getIntValue("commitSum");
            JSONObject differentTypeSum = jiraResponse.getJSONObject("differentTypeSum");
            jiraBug = differentTypeSum.getIntValue("completedBugSum");
            jiraFeature = differentTypeSum.getIntValue("completedTaskSum");
        }

        JSONObject sonarResponse = restInterfaceManager.getDayAvgSolvedIssue(developer,repoUuid,since,until,tool,token);
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
    private cn.edu.fudan.measureservice.portrait2.Quality getQuality(String repoUuid, String developer, String since, String until, String tool, String token, int developerLOC, int developerCommitCount){
        //个人引入规范类issue数
        int developerStandardIssueCount = restInterfaceManager.getIssueCountByConditions(developer, repoUuid, since, until, tool, "Code Smell", token);
        //个人引入issue总数
        int developerNewIssueCount = restInterfaceManager.getIssueCountByConditions(developer, repoUuid, since, until, tool, null, token);
        //团队引入规范类issue数
        int totalStandardIssueCount = restInterfaceManager.getIssueCountByConditions(null, repoUuid, since, until, tool, "Code Smell", token);
        //团队引入issue总数
        int totalNewIssueCount = restInterfaceManager.getIssueCountByConditions(null, repoUuid, since, until, tool, null, token);

        int developerJiraCount = 0;
        try {
            Query query = new Query(token,since,until,developer,Collections.singletonList(repoUuid));
            DeveloperCommitStandard developerCommitStandard = getDeveloperCommitStandard(query);
            developerJiraCount = developerCommitStandard.getDeveloperJiraCommitCount();
        }catch (Exception e) {
            log.error(e.getMessage());
        }
        int developerJiraBugCount = 0;
        int totalJiraBugCount = 0;
        JSONObject jiraResponse = restInterfaceManager.getJiraMsgOfOneDeveloper(developer,repoUuid,since,until);
        if (jiraResponse!=null){
            JSONObject defectRate = jiraResponse.getJSONObject("defectRate");
            developerJiraBugCount = defectRate.getIntValue("individualBugSum");
            totalJiraBugCount = defectRate.getIntValue("teamBugSum");
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

    private cn.edu.fudan.measureservice.portrait2.Contribution getContribution(String repoUuid, String since, String until, String developer,
                                                                               int developerLOC, String branch, int developerAddStatement,
                                                                               int developerChangeStatement, int developerValidStatement,
                                                                               int totalAddStatement, int totalValidStatement){
        int totalLOC = repoMeasureMapper.getLOCByCondition(repoUuid,null,since,until);
        int developerAddLine = repoMeasureMapper.getAddLinesByDuration(repoUuid, since, until, developer);

        List<Map<String,Object>> cloneMeasure = restInterfaceManager.getCloneMeasure(repoUuid, developer, since, until);
        int increasedCloneLines = 0;
        if (cloneMeasure != null){
            increasedCloneLines = (int) cloneMeasure.get(0).get("increasedCloneLines");
        }

        int developerAssignedJiraCount = 0;//个人被分配到的jira任务个数（注意不是次数）
        int totalAssignedJiraCount = 0;//团队被分配到的jira任务个数（注意不是次数）
        int developerSolvedJiraCount = 0;//个人解决的jira任务个数（注意不是次数）
        int totalSolvedJiraCount = 0;//团队解决的jira任务个数（注意不是次数）

        JSONObject jiraResponse = restInterfaceManager.getJiraMsgOfOneDeveloper(developer,repoUuid,since,until);
        if(jiraResponse!=null) {
            JSONObject assignedJiraRate = jiraResponse.getJSONObject("assignedJiraRate");
            developerAssignedJiraCount = assignedJiraRate.getIntValue("individualJiraSum");
            totalAssignedJiraCount = assignedJiraRate.getIntValue("teamJiraSum");
            developerSolvedJiraCount = assignedJiraRate.getIntValue("solvedIndividualJiraSum");
            totalSolvedJiraCount = assignedJiraRate.getIntValue("solvedTeamJiraSum");
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

    public Object getPortraitCompetence(String developer,String repoUuidList,String since,String until, String token) throws ParseException {
        //这里是获取开发者在给定时间段内所有参与的项目
        List<String> filterdRepoList = repoMeasureMapper.getRepoListByDeveloper(developer,since,until);
        //repoList是最后用于计算画像的所有项目
        List<String> repoList = new ArrayList<>();
        if(StringUtils.isEmptyOrNull(repoUuidList)) {
            repoList = filterdRepoList;
        }else {
            String[] repoUuidArray = repoUuidList.split(",");
            //先把前端给的repo加入到repoList
            repoList.addAll(Arrays.asList(repoUuidArray));
            for (int i = repoList.size() - 1; i >= 0; i--){//注意需要倒序删除
                //如果前端给的参数里的repoUuid 不在筛选后的list里 就去掉这个repo
                if (!filterdRepoList.contains(repoList.get(i))){
                    repoList.remove(i);
                }
            }
        }
        if (repoList.size()==0){
            log.error("选定时间无项目");
            return null;
        }
        List<String> developerList = new ArrayList<>();
        if( developer == null || "".equals(developer)) {
            Query query = new Query(token,since,until,null,repoList);
            developerList = projectDao.getDeveloperList(query);
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
                Map<String,Object> projects = restInterfaceManager.getProjectByrepoUuid(repo,token);
                if(projects == null) {
                    continue;
                }
                String tool = "sonarqube";
                String repoName = (String) projects.get("repoName");
                if(repoName!=null) {
                    repoName = repoName.replace("/","");
                }
                //String until = repoMeasureMapper.getLastCommitDateOfOneRepo(repoUuid);
                log.info("Start to get portrait of " + developer + " in repo : " + repoName);
                cn.edu.fudan.measureservice.portrait2.DeveloperMetrics metrics = measureDeveloperService.getDeveloperMetrics(repo, member, since, until, token, tool);
                developerMetricsList.add(metrics);
                log.info("Successfully get portrait of " +member + " in repo : " + repoName);

            }
            developerMetricMap.put(member,developerMetricsList);
            log.info("Get portrait of " + member + " complete!" );
        }
        log.info("Get portrait complete!" );
        //获取第一次提交commit的日期
        List<cn.edu.fudan.measureservice.portrait2.DeveloperPortrait> developerPortraitList = new ArrayList<>();
        for(String key : developerMetricMap.keySet()) {
            String developerFirstCommitTime = projectDao.getDeveloperFirstCommitDate(developer,null);
            LocalDate firstCommitDate = LocalDate.parse(developerFirstCommitTime,dtf);
            firstCommitDate = firstCommitDate.plusDays(1);
            //todo 日后需要添加程序员类型接口 目前统一认为是java后端工程师
            /*String index = repoMeasureMapper.getDeveloperType(key);
            String developerType = null ;
            if("L".equals(index)) {
                developerType = "项目负责人";
            }else if("P".equals(index)) {
                developerType = "JAVA后端工程师";
            }else if ("M".equals(index)) {
                developerType= "开发经理";
            }*/
            String developerType = "JAVA后端工程师";
            // 获取开发者在所有项目中的整个的用户画像
            cn.edu.fudan.measureservice.portrait2.DeveloperMetrics totalDeveloperMetrics = getTotalDeveloperMetrics(developerMetricMap.get(key),key,firstCommitDate);
            int totalCommitCount = totalDeveloperMetrics.getTotalCommitCount();
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

    /**
     * 获取开发者最新动态
     * @param repoUuid
     * @param developer
     * @param since
     * @param until
     * @return
     */
    public Object getDeveloperRecentNews(String repoUuids, String developer, String since, String until) {
        List<String> repoUuidList = repoUuids == null ? new ArrayList<>() : Arrays.asList(repoUuids.split(split));
        // 获取开发者最新动态
        List<Map<String, Object>> commitMsgList = repoMeasureMapper.getCommitMsgByCondition(repoUuid, developer, since, until);
        for (Map<String, Object> map : commitMsgList) {
            //将数据库中timeStamp/dateTime类型转换成指定格式的字符串 map.get("commit_time") 这个就是数据库中dateTime类型
            String commitTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(map.get("commit_time"));
            map.put("commit_time", commitTime);
            //以下操作是为了获取jira信息
            String commitMessage = map.get("message").toString();
            String jiraID = jiraDao.getJiraIDFromCommitMsg(commitMessage);
            if("noJiraID".equals(jiraID)) {
                map.put("jira_info", "本次commit不含jira单号");
            }else {
                try {
                    JSONArray jiraResponse = restInterfaceManager.getJiraInfoByKey("key",jiraID);
                    if (jiraResponse == null || jiraResponse.isEmpty()){
                        map.put("jira_info", "jira单号内容为空");
                    }else {
                        map.put("jira_info", jiraResponse.get(0));
                    }
                }catch (Exception e) {
                    log.info("cannot request Jira");
                    e.getMessage();
                }
            }
        }
        return commitMsgList;
    }


    /**
     * 返回两时间相差天数
     * @param date1 待查时间
     * @param date2 比较时间
     * @return long 相差天数
     */
    private long getSumDays(String date1,String date2) {
        return LocalDate.parse(date1,dtf).toEpochDay() - LocalDate.parse(date2,dtf).toEpochDay();
    }

    /**
     * 获得单个开发者画像
     * @param
     * @return Developer
     */
    // fixme 新版本 获得一个开发者的画像 (Contribution,Quality,Effiency)
    public DeveloperPortrait getDeveloperPortrait(Query query) {
        Objects.requireNonNull(query.getDeveloper());
        // 获取开发者所参与库下最早的提交时间
        String firstCommitDate = projectDao.getDeveloperFirstCommitDate(query.getDeveloper(),null);
        String developerType = "JAVA后端工程师";
        int totalCommitCount = 0;
        int totalStatement = 0;
        List<DeveloperRepositoryMetric> developerRepositoryMetrics = new ArrayList<>();
        // 获取开发者在每个库下的画像
        for (String repoUuid : query.getRepoUuidList()) {
            log.info("get portrait of {} in repo : {}",query.getDeveloper(),repoUuid);
            DeveloperRepositoryMetric developerRepoMetric = ((MeasureDeveloperService) AopContext.currentProxy()).getDeveloperRepositoryMetric(query.getDeveloper(),repoUuid,query.getToken());
            totalCommitCount += developerRepoMetric.getTotalCommitCount();
            totalStatement += developerRepoMetric.getTotalStatement();
            developerRepositoryMetrics.add(developerRepoMetric);
        }
        long days = getSumDays(query.getUntil(),firstCommitDate);
        int dayAverageStatement = (int) (totalStatement/days);
        return new DeveloperPortrait(firstCommitDate,totalStatement,dayAverageStatement,totalCommitCount,query.getDeveloper(),developerType,developerRepositoryMetrics);
    }

    /**
     * 开发者单库下的画像数据
     * @param developer 查询开发者
     * @param repoUuid 查询库 id
     * @return DeveloperRepositoryMetric 开发者库画像
     */
    @SneakyThrows
    @Cacheable(value = "developerRepositoryMetric",key = "#developer+'_'+#repoUuid")
    public DeveloperRepositoryMetric getDeveloperRepositoryMetric(String developer,String repoUuid,String token) {
        String projectName = projectDao.getProjectName(repoUuid);
        String repoName = projectDao.getRepoName(repoUuid);
        int developerNumber = projectDao.getDeveloperList(repoUuid).size();
        String firstCommitDate = projectDao.getDeveloperFirstCommitDate(developer,repoUuid);
        // fixme 数据经由Repostory类自动获取
        // Repository repository = new Repository(query,repoName,projectName);
        int developerStatement = 0;
        List<Map<String,Object>> developerStatements = restInterfaceManager.getStatements(repoUuid,null,null,developer,Statement_developer);
        if (developerStatements.size()>0) {
            developerStatement = (int) developerStatements.get(0).get("total");
        }
        //----------------------------------开发效率相关指标-------------------------------------
        // fixme 这边等project/all接口更新后加入branch字段
        Efficiency efficiency = getDeveloperEfficiency(new Query(token,null,null,developer,Collections.singletonList(repoUuid)), "", developerNumber);
        log.info(efficiency.toString());
        int totalLOC = efficiency.getTotalLOC();
        int developerAddStatement = efficiency.getDeveloperAddStatement();
        int totalAddStatement = efficiency.getTotalAddStatement();
        int developerValidLine = efficiency.getDeveloperValidLine();
        int totalValidLine = efficiency.getTotalValidLine();
        int developerLOC = efficiency.getDeveloperLOC();
        int developerCommitCount = efficiency.getDeveloperCommitCount();

        //----------------------------------代码质量相关指标-------------------------------------
        Quality quality = getDeveloperQuality(repoUuid,developer,null,null,TOOL,token,developerNumber,totalLOC);
        log.info(quality.toString());

        //----------------------------------开发能力相关指标-------------------------------------
        Competence competence = getDeveloperCompetence(repoUuid,null,null,developer,developerNumber,developerAddStatement,totalAddStatement,developerValidLine,totalValidLine);
        log.info(competence.toString());

        return new DeveloperRepositoryMetric(firstCommitDate,developerStatement,developerCommitCount,repoName,repoUuid,developer,efficiency,quality,competence);
    }


    /**
     *
     * @param
     * @return 开发者画像相关数据 key : DutyType,involvedRepoCount,totalLevel,value,quality,efficiency
     * @throws ParseException
     */
    public synchronized void getDeveloperList(Query query) throws ParseException {
        if(query.getRepoUuidList().size()==0) {
            log.warn("do not have any authorized repo to see");
        }
        List<DeveloperLevel> developerLevelList = new ArrayList<>();
        Set<String> developerSet = projectDao.getDeveloperList(query.getRepoUuidList());
        // 获取每个开发者的画像等级
        for(String developer : developerSet) {
            log.info("start to get portrait of {}",developer);
            Query temp = new Query(query.getToken(),null,null,developer,query.getRepoUuidList());
            // 获取开发者任职状态
            String dutyType = projectDao.getDeveloperDutyType(developer);
            int involvedRepoCount = projectDao.getDeveloperInvolvedRepoNum(developer);
            DeveloperPortrait developerPortrait = ((MeasureDeveloperService) AopContext.currentProxy()).getDeveloperPortrait(temp);
            double totalLevel = developerPortrait.getLevel();
            double value = developerPortrait.getValue();
            double quality = developerPortrait.getQuality();
            double efficiency = developerPortrait.getEfficiency();
            DeveloperLevel developerLevel = new DeveloperLevel(developer,efficiency,quality,value,totalLevel,involvedRepoCount,dutyType);
            developerLevelList.add(developerLevel);
        }
        try {
            if(projectDao.insertDeveloperLevel(developerLevelList)){
                log.info("INSERT developerLevel SUCCESS!\n");
            }else {
                log.info("FAILED TO INSERT developerLevel\n");
            }
        }catch (Exception e) {
            e.getMessage();
        }
        log.info(methodMeasureAspect.toString());
    }

    /**
     * 从数据库中获取开发者星级数据
     * @param query
     * @return
     */
    public List<DeveloperLevel> getDeveloperLevelList(Query query) {
        try {
            List<String> developerList = projectDao.getDeveloperList(query);
            List<DeveloperLevel> developerLevelList = projectDao.getDeveloperLevelList(developerList);
            if (developerLevelList.size()>0) {
                return developerLevelList;
            }else {
                getDeveloperList(query);
                return projectDao.getDeveloperLevelList(developerList);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 按照开发者聚合获取提交规范性
     * @param query 查询条件
     * @return
     */
    @SuppressWarnings("unchecked")
    public DeveloperCommitStandard getDeveloperCommitStandard(Query query) {
        assert query.getDeveloper() != null && !"".equals(query.getDeveloper());
        // 获取开发者对应的合法提交信息
        log.info("start to get {}",query.getDeveloper());
        List<Map<String,Object>> developerValidCommitInfo = measureDao.getProjectValidCommitMsg(query);
        // 封装 DeveloperCommitStandard
        DeveloperCommitStandard developerCommitStandard = new DeveloperCommitStandard();
        developerCommitStandard.setDeveloperName(query.getDeveloper());
        developerCommitStandard.setDeveloperValidCommitCount(developerValidCommitInfo.size());
        int developerJiraCommitCount = 0, developerNotJiraCommitCount = 0;
        for(Map<String,Object> commitInfo : developerValidCommitInfo) {
            boolean isValid = (int) commitInfo.get("is_compliance") == 1;
            if (isValid) {
                developerJiraCommitCount++;
            }else {
                developerNotJiraCommitCount++;
            }
        }
        developerCommitStandard.setDeveloperJiraCommitCount(developerJiraCommitCount);
        developerCommitStandard.setDeveloperInvalidCommitCount(developerNotJiraCommitCount);
        double commitStandard = developerCommitStandard.getDeveloperValidCommitCount() != 0 ? developerCommitStandard.getDeveloperJiraCommitCount() * 1.0 / developerCommitStandard.getDeveloperValidCommitCount() : 0;
        developerCommitStandard.setCommitStandard(Double.parseDouble(df.format(commitStandard)));

        return developerCommitStandard;
    }

    /**
     * 按照项目对开发者提交数信息按照 interval 对查询时间 分组聚合
     * @param projectIds 查询项目列表
     * @param since 查询起始时间
     * @param until 查询截止时间
     * @param interval 聚合间隔
     * @return new ArrayList<{@link ProjectCommitStandardTrendChart}>
     */
    @SneakyThrows
    @MethodMeasureAnnotation
    public synchronized List<ProjectCommitStandardTrendChart> getCommitStandardTrendChartIntegratedByProject(String projectIds,String since,String until,String token,String interval) {
        List<ProjectCommitStandardTrendChart> results = new ArrayList<>();
        // 由传入的 projectIds 获取可查询库列表
        List<ProjectPair> projectPairList = projectDao.getVisibleProjectPairListByProjectIds(projectIds,token);

        LocalDate endTime = LocalDate.parse(until,dtf);
        // 由于之前 until 已加 1， 如果待处理前 until是周日，则+1后变为下一周的周一，在后续操作会变为下一周的周日导致加了一周，所以要减一
        endTime = endTime.minusDays(1);
        LocalDate beginTime;
        if (since!=null && !"".equals(since)) {
            beginTime = LocalDate.parse(since,dtf);
        }else {
            // 默认 beginTime 时间
            beginTime = endTime.minusWeeks(1);
        }
        // 根据 interval 对 beginTime 及 endTime 处理为当前周的 周一 和 周日
        beginTime = DateTimeUtil.initBeginTimeByInterval(beginTime,interval);
        endTime = DateTimeUtil.initEndTimeByInterval(endTime,interval);
        if(beginTime == null || endTime == null) {
            return new ArrayList<>();
        }
        while (beginTime.isBefore(endTime)) {
            LocalDate tempTime = DateTimeUtil.selectTimeIncrementByInterval(beginTime,interval);
            if(tempTime == null) {
                break;
            }
            if(tempTime.isAfter(endTime)) {
                tempTime = endTime;
            }
            for (ProjectPair projectPair : projectPairList) {
                List<String> repoUuidList = new ArrayList<>();
                    List<RepoInfo> repoInfoList = projectDao.getProjectInvolvedRepoInfo(projectPair.getProjectName(),token);
                for (RepoInfo repoInfo : repoInfoList) {
                    repoUuidList.add(repoInfo.getRepoUuid());
                }
                Query query = new Query(token,beginTime.format(dtf),tempTime.format(dtf),null,repoUuidList);
                // 构造项目提交规范性类
                int projectId = projectPair.getProjectId();
                // note 内部调用需要使用代理使缓存生效
                ProjectCommitStandardTrendChart projectCommitStandardTrendChart = ((MeasureDeveloperService) AopContext.currentProxy()).getSingleProjectCommitStandardChart(query,projectPair);
                projectCommitStandardTrendChart.setProjectId(String.valueOf(projectId));
                projectCommitStandardTrendChart.setProjectName(projectPair.getProjectName());
                projectCommitStandardTrendChart.setDate(tempTime.format(dtf));
                results.add(projectCommitStandardTrendChart);
            }
            beginTime = tempTime;

        }
        return results;
    }


    /**
     * 封装单个项目合法提交信息为 {@link ProjectCommitStandardTrendChart}
     * @param query 查询信息
     * @return ProjectCommitStandardTrendChart 项目提交规范性
     */
    @Cacheable(value = "projectCommitStandardChart", key = "#projectPair.projectName+'_'+#query.until")
    public ProjectCommitStandardTrendChart getSingleProjectCommitStandardChart(Query query,ProjectPair projectPair) {
        ProjectCommitStandardTrendChart projectCommitStandardTrendChart = new ProjectCommitStandardTrendChart();
        // 获取项目合法提交信息
        List<Map<String,Object>> projectValidCommitMsgList = measureDao.getProjectValidCommitMsg(query);
        // validCommitCountNum : 不含Merge的总提交次数 ， jiraCommitCountNum 包含Jira单号的总提交次数
        long validCommitCountNum = projectValidCommitMsgList.size(), jiraCommitCountNum = 0;
        for (int i = 0; i < projectValidCommitMsgList.size(); i++) {
            Map<String,Object> commitMsg = projectValidCommitMsgList.get(i);
            boolean isValid = (int) commitMsg.get("is_compliance") == 1;
            if (isValid) {
                jiraCommitCountNum++;
            }
        }
        double num = 0.0;
        // 当不含Merge的总提交次数为 0 时，提交规范性特判为 0
        if(validCommitCountNum!=0) {
            num = jiraCommitCountNum * 1.0 / validCommitCountNum;
        }
        // 提交规范性比率保留三位小数
        projectCommitStandardTrendChart.setNum(Double.parseDouble(df.format(num)));
        projectCommitStandardTrendChart.setOption(jiraCommitCountNum,validCommitCountNum);
        return projectCommitStandardTrendChart;
    }


    @CacheEvict(value = "projectCommitStandardChart", allEntries=true, beforeInvocation = true)
    public void deleteProjectCommitStandardChart() {

    }

    /**
     * 分页获取提交规范性按照项目聚合的明细
     * @param projectNameList 查询项目列表
     * @param repoUuidList 查询库列表
     * @param token 查询权限
     * @return <{@link ProjectCommitStandardDetail}>
     */
    @SneakyThrows
    public synchronized ProjectFrontEnd<ProjectCommitStandardDetail> getCommitStandardDetailIntegratedByProject(String projectNameList,String repoUuidList,String committer,String token,int page,int ps,Boolean isValid) {
        // 获取查询条件下可见的库列表
        List<String> visibleRepoList = projectDao.getVisibleRepoListByProjectNameAndRepo(projectNameList,repoUuidList,token);
        // 获取查询条件下的提交规范性明细 （分页查询）
        int totalMsgSize = projectDao.getRepoListMsgNum(visibleRepoList);
        int totalPage = totalMsgSize % ps == 0 ? totalMsgSize / ps : totalMsgSize / ps + 1;
        // 起始查询位置
        int initialBeginIndex = (page-1) * ps;
        //获取 ps 条合法提交数据
        List<ProjectCommitStandardDetail> selectedProjectCommitStandardDetail = getProjectValidCommitStandardDetail(visibleRepoList,committer,initialBeginIndex,ps,isValid);
        List<ProjectCommitStandardDetail> projectCommitStandardDetailList = new ArrayList<>(selectedProjectCommitStandardDetail);
        // 封装返回前端的明细
        return new ProjectFrontEnd<>(page,totalPage,totalMsgSize,projectCommitStandardDetailList);
     }

    /**
     * 分页获取项目最近提交明细
     * @param repoUuidList 查询库列表
     * @param committer 查询开发者
     * @param beginIndex 查询起始位置
     * @param size 查询条数
     * @param selectOrNot 是否筛选包含 Jira 单号的提交数
     * @return 项目查询条件下最新提交明细
     */
     public List<ProjectCommitStandardDetail> getProjectValidCommitStandardDetail(List<String> repoUuidList, String committer, int beginIndex, int size, Boolean selectOrNot) {
         List<ProjectCommitStandardDetail> projectCommitStandardDetailList = new ArrayList<>();
         // 获取查询条件下的提交明细
         Query query = new Query(null,null,null,null,repoUuidList);
         List<Map<String,Object>> projectValidCommitMsg;
         if (committer != null && !"".equals(committer)) {
             query.setDeveloper(committer);
         }
         // 根据 筛选条件获取相应的提交明细
         if (selectOrNot == null) {
            projectValidCommitMsg = measureDao.getProjectValidCommitMsg(query,beginIndex,size);
         }else if (selectOrNot){
            projectValidCommitMsg = measureDao.getProjectValidJiraCommitMsg(query,beginIndex,size);
         }else {
             projectValidCommitMsg = measureDao.getProjectValidNotJiraCommitMsg(query,beginIndex,size);
         }
         //数据封装
         for (Map<String,Object> map : projectValidCommitMsg) {
             String repoId = (String) map.get("repo_id");
             String commitId = (String) map.get("commit_id");
             String commitTime = (String) map.get("commit_time");
             String message = (String) map.get("message");
             String developer = (String) map.get("developer");
             boolean isValid = (int) map.get("is_compliance") == 1;
             String projectName = projectDao.getProjectName(repoId);
             String projectId = String.valueOf(projectDao.getProjectIdByName(projectName));
             String repoName = projectDao.getRepoName(repoId);
             ProjectCommitStandardDetail projectCommitStandardDetail = ProjectCommitStandardDetail.builder()
                     .projectName(projectName).projectId(projectId)
                     .repoName(repoName).repoUuid(repoId)
                     .committer(developer).commitTime(commitTime).commitId(commitId)
                     .message(message)
                     .isValid(isValid)
                     .build();
             projectCommitStandardDetailList.add(projectCommitStandardDetail);
         }
         return projectCommitStandardDetailList;
     }

    /**
     * 获取项目最近提交明细
     * @param repoUuidList 查询库列表
     * @param committer 查询开发者
     * @return 项目查询条件下全部提交明细
     */
    public List<ProjectCommitStandardDetail> getProjectValidCommitStandardDetail(List<String> repoUuidList, String committer) {
        List<ProjectCommitStandardDetail> projectCommitStandardDetailList = new ArrayList<>();
        // 获取查询条件下的提交明细
        Query query = new Query(null,null,null,null,repoUuidList);
        List<Map<String,Object>> projectValidCommitMsg;
        if (committer != null && !"".equals(committer)) {
            query.setDeveloper(committer);
        }
        projectValidCommitMsg = measureDao.getProjectValidCommitMsg(query);
        for (Map<String,Object> map : projectValidCommitMsg) {
            String repoId = (String) map.get("repo_id");
            String commitId = (String) map.get("commit_id");
            String commitTime = (String) map.get("commit_time");
            String message = (String) map.get("message");
            String developer = (String) map.get("developer");
            boolean isValid = (int) map.get("is_compliance") == 1;
            String projectName = projectDao.getProjectName(repoId);
            String projectId = String.valueOf(projectDao.getProjectIdByName(projectName));
            String repoName = projectDao.getRepoName(repoId);
            ProjectCommitStandardDetail projectCommitStandardDetail = ProjectCommitStandardDetail.builder()
                    .projectName(projectName).projectId(projectId)
                    .repoName(repoName).repoUuid(repoId)
                    .committer(developer).commitTime(commitTime).commitId(commitId)
                    .message(message)
                    .isValid(isValid)
                    .build();
            projectCommitStandardDetailList.add(projectCommitStandardDetail);
        }
        return projectCommitStandardDetailList;
    }


    /**
     * 前端项目总览界面， 添加提交者列表功能
     * note 这里的开发者是参与项目所在库中的全部开发者，不区分提交时间
     * @param projectNameList 查询项目列表
     * @param repoUuidList 查询库列表
     * @return Set<String> commiter
     */
     public Set<String> getCommitStandardCommitterList(String projectNameList,String repoUuidList,String token) {
         List<String> checkedRepoList = projectDao.getVisibleRepoListByProjectNameAndRepo(projectNameList,repoUuidList,token);
         return projectDao.getDeveloperList(checkedRepoList);
     }


    /**
     * 获取超大文件数趋势图
     * @param projectIds 查询项目id列表
     * @param since 起始时间
     * @param until 截止时间
     * @param token 查询权限
     * @param interval 间隔
     * @return
     */
     public List<ProjectBigFileTrendChart> getHugeLocRemainedFile(String projectIds,String since,String until,String token,String interval) {
         List<ProjectBigFileTrendChart> results = new ArrayList<>();
         // 获取可查询项目列表
         List<ProjectPair> checkedProjectPairList = projectDao.getVisibleProjectPairListByProjectIds(projectIds,token);

         LocalDate endTime = LocalDate.parse(until,dtf);
         // 这边最后一天还是按照前端传值，不加 1
         endTime = endTime.minusDays(1);
         LocalDate beginTime;
         if (since!=null && !"".equals(since)) {
             beginTime = LocalDate.parse(since,dtf);
         }else {
             beginTime = endTime.minusWeeks(1);
         }
         beginTime = DateTimeUtil.initBeginTimeByInterval(beginTime,interval);
         endTime = DateTimeUtil.initEndTimeByInterval(endTime,interval);
         if(beginTime == null || endTime == null) {
             return new ArrayList<>();
         }
         while (beginTime.isBefore(endTime)) {
             LocalDate tempTime = DateTimeUtil.selectTimeIncrementByInterval(beginTime,interval);
             if(tempTime == null) {
                 break;
             }
             if(tempTime.isAfter(endTime)) {
                 tempTime = endTime;
             }
             List<ProjectBigFileTrendChart> projectBigFileTrendChartList = ((MeasureDeveloperService) AopContext.currentProxy()).getAllProjectBigFileDetail(checkedProjectPairList,DateTimeUtil.dtf.format(tempTime));
             results.addAll(projectBigFileTrendChartList);
             beginTime = tempTime;
         }
         return results;
     }

     @Cacheable(value = "projectBigFileTrendChart",key = "#until")
     public List<ProjectBigFileTrendChart> getAllProjectBigFileDetail(List<ProjectPair> projectPairList,String until) {
         List<ProjectBigFileTrendChart> projectBigFileTrendChartList = new ArrayList<>();
         for (ProjectPair projectPair : projectPairList) {
             // 获取项目下的参与库
             List<String> repoUuidList = projectDao.getProjectRepoList(projectPair.getProjectName());
             int projectId = projectPair.getProjectId();
             // 获取这个间断内的 超大文件数明细
             List<ProjectBigFileDetail> projectBigFileDetailList = measureDao.getCurrentBigFileInfo(repoUuidList,until);
             ProjectBigFileTrendChart projectBigFileTrendChart = ProjectBigFileTrendChart.builder()
                     .projectId(String.valueOf(projectId))
                     .date(until)
                     .projectName(projectPair.getProjectName())
                     .num(projectBigFileDetailList.size())
                     .build();
             projectBigFileTrendChartList.add(projectBigFileTrendChart);
         }
         return projectBigFileTrendChartList;
     }

    @CacheEvict(value = "projectBigFileTrendChart", allEntries=true, beforeInvocation = true)
    public void deleteProjectBigFileTrendChart() {

    }

    /**
     * 获取超大文件数明细
     * @param projectNameList 查询项目列表
     * @param repoUuidList 查询库列表
     * @param token 查询权限
     * @return
     */
    @SneakyThrows
     public List<ProjectBigFileDetail> getHugeLocRemainedDetail(String projectNameList,String repoUuidList,String token) {
         List<ProjectBigFileDetail> result = new ArrayList<>();
         List<String> visibleRepoList = projectDao.getVisibleRepoListByProjectNameAndRepo(projectNameList,repoUuidList,token);
         for (String repoUuid : visibleRepoList) {
             String projectName = projectDao.getProjectName(repoUuid);
             String repoName = projectDao.getRepoName(repoUuid);
             int projectId = projectDao.getProjectIdByName(projectName);
             List<ProjectBigFileDetail> projectBigFileDetailList = measureDao.getCurrentBigFileInfo(repoUuid,null);
             for (ProjectBigFileDetail projectBigFileDetail : projectBigFileDetailList) {
                 projectBigFileDetail.setProjectId(String.valueOf(projectId));
                 projectBigFileDetail.setProjectName(projectName);
                 projectBigFileDetail.setRepoName(repoName);
             }
             result.addAll(projectBigFileDetailList);
         }
         return result;
     }

    /**
     * 获取开发者修改圈复杂度，并按照项目聚合
     * @param projectNameList 查询项目列表
     * @param developers 查询开发者
     * @param token 查询权限
     * @param since 起始时间
     * @param until 截止时间
     * @return 开发者按照项目为单位聚合后 修改圈复杂度
     */
     @SneakyThrows
     public  List<DeveloperDataCcn> getDeveloperDataCcn(String projectNameList, String developers, String token , String since, String until) {
        List<DeveloperDataCcn> developerDataCcnList = new ArrayList<>();
        List<String> visibleRepoList = projectDao.getVisibleRepoListByProjectName(projectNameList,token);
        String[] developerList = developers.split(split);
        for (String developer : developerList) {
            List<DeveloperProjectCcn> developerProjectCcnList = new ArrayList<>();
            List<String> developerRepoList = projectDao.getDeveloperVisibleRepo(visibleRepoList,developer,since,until);
            // 暂存该开发者 项目 与 库下圈复杂度变化的匹配关系
            Map<String,List<DeveloperRepoCcn>> map = new HashMap<>();
            for (String repoUuid : developerRepoList) {
                if (!projectDao.getRepoInfoMap().containsKey(repoUuid)) {
                    projectDao.insertProjectInfo(token);
                }
                RepoInfo repoInfo = projectDao.getRepoInfoMap().get(repoUuid);
                String projectName = repoInfo.getProjectName();
                String repoName = repoInfo.getRepoName();
                if (!map.containsKey(projectName)) {
                    map.put(projectName,new ArrayList<>());
                }
                // 查询开发者指定时间库下的修改圈复杂度
                int developerRepoDiffCcn = measureDao.getDeveloperDiffCcn(repoUuid,since,until,developer);
                map.get(projectName).add(new DeveloperRepoCcn(developer,since,until,projectName,repoUuid,repoName,developerRepoDiffCcn));
            }
            for (String projectName : map.keySet()) {
                DeveloperProjectCcn developerProjectCcn = DeveloperProjectCcn.builder()
                                        .developerName(developer)
                                        .developerRepoCcnList(map.get(projectName))
                                        .projectName(projectName)
                                        .since(since).until(until)
                                        .projectDiffCcn(0).build();
                // 对 projectDiffCcn 初始化计算
                developerProjectCcn.cal();
                developerProjectCcnList.add(developerProjectCcn);
            }
            // totalDiffCCn的计算
            DeveloperDataCcn developerDataCcn = DeveloperDataCcn.builder()
                            .developerName(developer)
                            .developerProjectCcnList(developerProjectCcnList)
                            .since(since).until(until)
                            .totalDiffCcn(0)
                            .level(LevelEnum.Medium.getType()).build();
            developerDataCcn.cal();
            developerDataCcnList.add(developerDataCcn);
        }
        return developerDataCcnList;
     }

    /**
     * 获得开发者人员总览等级及相应数值
     * @param projectNameList 查询项目列表
     * @param developers 查询开发者
     * @param token 查询权限
     * @param since 起始时间
     * @param until 截止时间
     * @return
     */
     @SneakyThrows
     public List<DeveloperDataCommitStandard> getDeveloperDataCommitStandard(String projectNameList, String developers, String token , String since, String until) {
         List<DeveloperDataCommitStandard> developerDataCommitStandardList = new ArrayList<>();
         // 获取开发者查询项目下可看库
         List<String> visibleRepoList = projectDao.getVisibleRepoListByProjectName(projectNameList,token);
         // 获取开发者提交规范列表
         Query query = new Query(token,since,until,null,visibleRepoList);
         Set<String> developerNameList = !"".equals(developers) ? new HashSet<>(Arrays.asList(developers.split(split))) : projectDao.getDeveloperList(visibleRepoList);
         List<DeveloperCommitStandard> developerCommitStandardList = new ArrayList<>();
         for (String developer : developerNameList) {
             DeveloperCommitStandard developerCommitStandard = ((MeasureDeveloperService) AopContext.currentProxy()).getDeveloperCommitStandard(new Query(token,since,until,developer,visibleRepoList));
             developerCommitStandardList.add(developerCommitStandard);
         }
         // 构建人员总览提交规范性类
         for (DeveloperCommitStandard developerCommitStandard : developerCommitStandardList) {
             DeveloperDataCommitStandard developerDataCommitStandard = DeveloperDataCommitStandard.builder()
                     .developerName(developerCommitStandard.getDeveloperName())
                     .since(since)
                     .until(until)
                     .developerJiraCommitCount(developerCommitStandard.getDeveloperJiraCommitCount())
                     .developerValidCommitCount(developerCommitStandard.getDeveloperValidCommitCount())
                     .commitStandard(developerCommitStandard.getCommitStandard())
                     .detail(null)
                     .level(LevelEnum.Medium.getType()).build();
             developerDataCommitStandardList.add(developerDataCommitStandard);
         }
         return developerDataCommitStandardList;
     }


    /**
     * 判断该 repo 信息是否被初始化
     * @param repoUuid 查询库
     * @return 若已初始化则返回 true
     */
     private boolean isInit(String repoUuid) {
        return projectDao.getRepoInfoMap().containsKey(repoUuid);
     }

    @Autowired
    public void setMeasureDeveloperServiceImpl(MeasureDeveloperService measureDeveloperServiceImpl) {
        this.measureDeveloperService = measureDeveloperServiceImpl;
    }


    @Autowired
    public MeasureDeveloperService(RestInterfaceManager restInterfaceManager, RepoMeasureMapper repoMeasureMapper, ProjectDao projectDao, JiraDao jiraDao, MeasureDao measureDao,AccountDao accountDao) {
        this.restInterfaceManager = restInterfaceManager;
        this.repoMeasureMapper = repoMeasureMapper;
        this.projectDao = projectDao;
        this.jiraDao = jiraDao;
        this.measureDao = measureDao;
        this.accountDao = accountDao;
    }

    @Autowired
    public void setMethodMeasureAspect(MethodMeasureAspect methodMeasureAspect) {
        this.methodMeasureAspect = methodMeasureAspect;
    }

    @CacheEvict(cacheNames = {"developerPortrait","developerMetricsNew","portraitCompetence","developerRecentNews","commitStandard"}, allEntries=true, beforeInvocation = true)
    public void clearCache() {
        log.info("Successfully clear redis cache in db6.");
    }



}