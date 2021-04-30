package cn.edu.fudan.measureservice.service;

import cn.edu.fudan.measureservice.annotation.MethodMeasureAnnotation;
import cn.edu.fudan.measureservice.aop.MethodMeasureAspect;
import cn.edu.fudan.measureservice.component.RestInterfaceManager;
import cn.edu.fudan.measureservice.dao.JiraDao;
import cn.edu.fudan.measureservice.dao.MeasureDao;
import cn.edu.fudan.measureservice.dao.ProjectDao;
import cn.edu.fudan.measureservice.domain.Granularity;
import cn.edu.fudan.measureservice.domain.bo.*;
import cn.edu.fudan.measureservice.domain.bo.DeveloperPortrait;
import cn.edu.fudan.measureservice.domain.dto.DeveloperRepoInfo;
import cn.edu.fudan.measureservice.domain.dto.ProjectPair;
import cn.edu.fudan.measureservice.domain.dto.Query;
import cn.edu.fudan.measureservice.domain.dto.RepoInfo;
import cn.edu.fudan.measureservice.domain.enums.GranularityEnum;
import cn.edu.fudan.measureservice.domain.vo.ProjectBigFileDetail;
import cn.edu.fudan.measureservice.domain.vo.ProjectBigFileTrendChart;
import cn.edu.fudan.measureservice.domain.vo.ProjectCommitStandardDetail;
import cn.edu.fudan.measureservice.domain.vo.ProjectCommitStandardTrendChart;
import cn.edu.fudan.measureservice.mapper.RepoMeasureMapper;
import cn.edu.fudan.measureservice.portrait.*;
import cn.edu.fudan.measureservice.portrait2.Contribution;
import cn.edu.fudan.measureservice.util.DateTimeUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.util.StringUtils;
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
        if (cloneMeasure != null){
            increasedCloneLines = (int) cloneMeasure.get(0).get("increasedCloneLines");
            selfIncreasedCloneLines = (int) cloneMeasure.get(0).get("selfIncreasedCloneLines");
            eliminateCloneLines = (int) cloneMeasure.get(0).get("eliminateCloneLines");
            allEliminateCloneLines = (int) cloneMeasure.get(0).get("allEliminateCloneLines");
        }
        List<Map<String,Object>> developerChangeCodeLifeCycle = restInterfaceManager.getCodeLifeCycle(repoUuid,developer,since,until,Statement_developer,Change);
        List<Map<String,Object>> developerDeleteCodeLifeCycle = restInterfaceManager.getCodeLifeCycle(repoUuid,developer,since,until,Statement_developer,Delete);
        double changedCodeAVGAge = 0;
        int changedCodeMAXAge = 0;
        double deletedCodeAVGAge = 0;
        int deletedCodeMAXAge = 0;
        if(developerChangeCodeLifeCycle!=null && developerChangeCodeLifeCycle.size()>0) {
            changedCodeAVGAge = (double) developerChangeCodeLifeCycle.get(0).get("average");
            changedCodeMAXAge = (int) developerChangeCodeLifeCycle.get(0).get("max");
        }
        if (developerDeleteCodeLifeCycle!=null && developerDeleteCodeLifeCycle.size()>0) {
            deletedCodeAVGAge = (double) developerDeleteCodeLifeCycle.get(0).get("average");
            deletedCodeMAXAge = (int) developerDeleteCodeLifeCycle.get(0).get("max");
        }

        List<Map<String,Object>> focusMeasure = restInterfaceManager.getFocusFilesCount(repoUuid,null,since,until);
        int totalChangedFile = 0;
        int developerFocusFile = 0;
        if (focusMeasure!=null && focusMeasure.size()>0){
            for (Map<String,Object> focus : focusMeasure) {
                if (focus.get("developerName").equals(developer)) {
                    developerFocusFile = (int) focus.get("num");
                }
                totalChangedFile += (int) focus.get("num");
            }
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
        String developerFirstCommitTime = projectDao.getDeveloperFirstCommitDate(developer,since,until,repoUuid);
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
            DeveloperCommitStandard developerCommitStandard = getCommitStandard(query,null).get(0);
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
            String developerFirstCommitTime = projectDao.getDeveloperFirstCommitDate(key,null,null,null);
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


    @Cacheable(cacheNames = {"developerRecentNews"})
    public Object getDeveloperRecentNews(String repoUuid, String developer, String since, String until) {
        if (until != null) {
            until = DateTimeUtil.stringToLocalDate(until).plusDays(1).toString();
        }
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
     * 根据开发者参与库内第一次提交时间排序
     * @param developerRepoInfos 开发者库信息
     */
    private void orderByDeveloperFirstCommitDate(List<DeveloperRepoInfo> developerRepoInfos) {
        Collections.sort(developerRepoInfos, (o1, o2) -> o1.getFirstCommitDate().compareTo(o2.getFirstCommitDate()));
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
    @Cacheable(cacheNames = {"developerPortrait"})
    public DeveloperPortrait getDeveloperPortrait(Query query,Map<String,List<DeveloperRepoInfo>> developerRepoInfos) {
        if(!developerRepoInfos.containsKey(query.getDeveloper())) {
            log.warn("查询库中无该开发者 {}：",query.getDeveloper());
            return null;
        }
        List<DeveloperRepoInfo> developerRepoInfoList = developerRepoInfos.get(query.getDeveloper());
        orderByDeveloperFirstCommitDate(developerRepoInfoList);
        String firstCommitDate = developerRepoInfoList.get(0).getFirstCommitDate();
        String developerType = "JAVA后端工程师";
        int totalCommitCount = 0;
        int totalStatement = 0;
        List<DeveloperRepositoryMetric> developerRepositoryMetrics = new ArrayList<>();
        for(DeveloperRepoInfo repoInfo : developerRepoInfoList) {
            log.info("get portrait of {} in repo : {}",query.getDeveloper(),repoInfo.getRepoInfo().getRepoName());
            DeveloperRepositoryMetric developerRepoMetric = getDeveloperRepositoryMetric(repoInfo,query);
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
     * @param developerRepoInfo 开发者库信息
     * @param query 查询条件
     * @return DeveloperRepositoryMetric 开发者库画像
     */
    public DeveloperRepositoryMetric getDeveloperRepositoryMetric(DeveloperRepoInfo developerRepoInfo,Query query) {
        RepoInfo repoInfo = developerRepoInfo.getRepoInfo();
        String projectName = repoInfo.getProjectName();
        String repoName = repoInfo.getRepoName();
        String repoUuid = repoInfo.getRepoUuid();
        int developerNumber = repoInfo.getInvolvedDeveloperNumber();
        String firstCommitDate = developerRepoInfo.getFirstCommitDate();
        query.setRepoUuidList(Collections.singletonList(repoUuid));
        // fixme 数据经由Repostory类自动获取
        // Repository repository = new Repository(query,repoName,projectName);
        int developerStatement = 0;
        List<Map<String,Object>> developerStatements = restInterfaceManager.getStatements(repoUuid,query.getSince(),query.getUntil(),query.getDeveloper(),Statement_developer);
        if (developerStatements.size()>0) {
            developerStatement = (int) developerStatements.get(0).get("total");
        }
        //----------------------------------开发效率相关指标-------------------------------------
        // fixme 这边等project/all接口更新后加入branch字段
        Efficiency efficiency = getDeveloperEfficiency(query, "", developerNumber);
        log.info(efficiency.toString());
        int totalLOC = efficiency.getTotalLOC();
        int developerAddStatement = efficiency.getDeveloperAddStatement();
        int totalAddStatement = efficiency.getTotalAddStatement();
        int developerValidLine = efficiency.getDeveloperValidLine();
        int totalValidLine = efficiency.getTotalValidLine();
        int developerLOC = efficiency.getDeveloperLOC();
        int developerCommitCount = efficiency.getDeveloperCommitCount();

        //----------------------------------代码质量相关指标-------------------------------------
        Quality quality = getDeveloperQuality(repoUuid,query.getDeveloper(),query.getSince(),query.getUntil(),TOOL,query.getToken(),developerNumber,totalLOC);
        log.info(quality.toString());

        //----------------------------------开发能力相关指标-------------------------------------
        Competence competence = getDeveloperCompetence(repoUuid,query.getSince(),query.getUntil(),query.getDeveloper(),developerNumber,developerAddStatement,totalAddStatement,developerValidLine,totalValidLine);
        log.info(competence.toString());

        return new DeveloperRepositoryMetric(firstCommitDate,developerStatement,developerCommitCount,repoName,repoUuid,query.getDeveloper(),efficiency,quality,competence);
    }


    /**
     *
     * @param
     * @return 开发者画像相关数据 key : DutyType,involvedRepoCount,totalLevel,value,quality,efficiency
     * @throws ParseException
     */
    public synchronized void getDeveloperList(Query redisQuery) throws ParseException {
        Query query = new Query(redisQuery);
        if(query.getRepoUuidList().size()==0) {
            log.warn("do not have any authorized repo to see");
        }
        Map<String,List<DeveloperRepoInfo>> developerRepoInfos = projectDao.getDeveloperRepoInfoList(query);
        Map<String,String> developerDutyType = projectDao.getDeveloperDutyType(developerRepoInfos.keySet());
        List<DeveloperLevel> developerLevelList = new ArrayList<>();
        // fixme 单个人员的画像补全 , 需要建一个全局变量，来保存developer相关的developerRepoInfo
        for(String developer : developerRepoInfos.keySet()) {
            Map<String,Object> dev = new HashMap<>();
            log.info("start to get portrait of {}",developer);
            query.setDeveloper(developer);
            dev.put("developer",developer);
            String dutyType = developerDutyType.get(developer);
            if("1".equals(dutyType)) {
                dutyType = "在职";
            } else{
                dutyType = "离职";
            }
            int involvedRepoCount = developerRepoInfos.get(developer).size();
            dev.put("involvedRepoCount",involvedRepoCount);
            DeveloperPortrait developerPortrait = getDeveloperPortrait(query,developerRepoInfos);
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


    @SuppressWarnings("unchecked")
    @Cacheable(cacheNames = {"commitStandard"})
    public List<DeveloperCommitStandard> getCommitStandard(Query query , List<String> developers) {
        List<DeveloperCommitStandard> developerCommitStandardList = new ArrayList<>();
        List<String> developerList = new ArrayList<>();
        // 根据查询条件对 查询人列表 处理
        if(query.getDeveloper()!=null && !"".equals(query.getDeveloper())) {
            developerList.add(query.getDeveloper());
        }else if(developers!=null && developers.size()>0) {
            developerList = developers;
        }else {
            developerList = projectDao.getDeveloperList(query);
        }
        for(String developer : developerList) {
            query.setDeveloper(developer);
            // fixme validCommitMsg 判断是否是Merge应该通过父节点来判断
            List<Map<String,String>> developerValidCommitInfo = projectDao.getValidCommitMsg(query);

            DeveloperCommitStandard developerCommitStandard = new DeveloperCommitStandard();
            developerCommitStandard.setDeveloperName(developer);
            developerCommitStandard.setDeveloperValidCommitCount(developerValidCommitInfo.size());
            List<Map<String,String>> developerJiraCommitInfo = new ArrayList<>();
            List<Map<String,String>> developerInvalidCommitInfo = new ArrayList<>();
            for(Map<String,String> commitInfo : developerValidCommitInfo) {
                String message = commitInfo.get("message");
                if(!"noJiraID".equals(jiraDao.getJiraIDFromCommitMsg(message))) {
                    developerJiraCommitInfo.add(commitInfo);
                }else {
                    developerInvalidCommitInfo.add(commitInfo);
                }
            }
            developerCommitStandard.setDeveloperJiraCommitInfo(developerJiraCommitInfo);
            developerCommitStandard.setDeveloperJiraCommitCount(developerJiraCommitInfo.size());
            developerCommitStandard.setDeveloperInvalidCommitCount(developerInvalidCommitInfo.size());
            developerCommitStandard.setDeveloperInvalidCommitInfo(developerInvalidCommitInfo);
            double commitStandard = developerCommitStandard.getDeveloperJiraCommitCount() * 1.0 / developerCommitStandard.getDeveloperValidCommitCount();
            developerCommitStandard.setCommitStandard(commitStandard);
            developerCommitStandardList.add(developerCommitStandard);
        }
        return developerCommitStandardList;
    }

    /**
     * 按照项目对开发者提交数信息按照 interval 对查询时间 分组聚合
     * @param projectIds 查询项目列表
     * @param since 查询起始时间
     * @param until 查询截止时间
     * @param interval 聚合间隔
     * @param showDetail 是否展示明细
     * @return new ArrayList<{@link ProjectCommitStandardTrendChart}>
     */
    @SneakyThrows
    public synchronized List<ProjectCommitStandardTrendChart> getCommitStandardTrendChartIntegratedByProject(String projectIds,String since,String until,String token,String interval,boolean showDetail) {
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
            // 默认 begintime 时间
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
                List<DeveloperCommitStandard> developerCommitStandardList = getCommitStandard(query,null);
                // 构造项目提交规范性类
                int projectId = projectPair.getProjectId();
                ProjectCommitStandardTrendChart projectCommitStandardTrendChart = listDeveloperCommandStandardToProjectCommitStandardTrendChart(developerCommitStandardList,showDetail);
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
     * 由 List<{@link DeveloperCommitStandard}> 封装为 {@link ProjectCommitStandardTrendChart}
     * @param developerCommitStandardList 开发者提交规范性列表
     * @return ProjectCommitStandardTrendChart 项目提交规范性
     */
    private ProjectCommitStandardTrendChart listDeveloperCommandStandardToProjectCommitStandardTrendChart(List<DeveloperCommitStandard> developerCommitStandardList,boolean showDetail) {
        ProjectCommitStandardTrendChart projectCommitStandardTrendChart = new ProjectCommitStandardTrendChart();
        // validCommitCountNum : 不含Merge的总提交次数 ， jiraCommitCountNum 包含Jira单号的总提交次数
        long validCommitCountNum = 0, jiraCommitCountNum = 0;
        for (DeveloperCommitStandard developerCommitStandard : developerCommitStandardList) {
            validCommitCountNum += developerCommitStandard.getDeveloperValidCommitCount();
            jiraCommitCountNum += developerCommitStandard.getDeveloperJiraCommitCount();
        }
        double num = 0.0;
        // 当不含Merge的总提交次数为 0 时，提交规范性特判为 0
        if(validCommitCountNum!=0) {
            num = jiraCommitCountNum * 1.0 / validCommitCountNum;
        }
        // 提交规范性比率保留三位小数
        projectCommitStandardTrendChart.setNum(Double.parseDouble(df.format(num)));
        projectCommitStandardTrendChart.setOption(jiraCommitCountNum,validCommitCountNum);
        if (showDetail) {
            projectCommitStandardTrendChart.setDetail(developerCommitStandardList);
        }
        return projectCommitStandardTrendChart;
    }

    /**
     * 获取提交规范性按照项目聚合的明细
     * @param projectNameList 查询项目列表
     * @param repoUuidList 查询库列表
     * @param since 查询起始时间
     * @param until 查询截至时间
     * @param token 查询权限
     * @return new ArrayList<{@link ProjectCommitStandardDetail}>
     */
    @SneakyThrows
    public synchronized List<ProjectCommitStandardDetail> getCommitStandardDetailIntegratedByProject(String projectNameList,String repoUuidList,String committer,String since,String until,String token) {
        List<ProjectCommitStandardDetail> projectCommitStandardDetailList = new ArrayList<>();
        List<String> visibleRepoList = projectDao.getVisibleRepoListByProjectNameAndRepo(projectNameList,repoUuidList,token);
        for (String repoUuid : visibleRepoList) {
            Query query = new Query(token,since,until,committer,Collections.singletonList(repoUuid));
            if (!projectDao.getRepoInfoMap().containsKey(repoUuid)) {
                projectDao.insertProjectInfo(token);
            }
            RepoInfo repoInfo = projectDao.getRepoInfoMap().get(repoUuid);
            String projectName = repoInfo.getProjectName();
            String repoName = repoInfo.getRepoName();
            int projectId = projectDao.getProjectIdByName(projectName);
            List<DeveloperCommitStandard> developerCommitStandardList = getCommitStandard(query,null);
            for (DeveloperCommitStandard developerCommitStandard : developerCommitStandardList) {
                projectCommitStandardDetailList.addAll(dealWithDeveloperCommitStandardDetail(developerCommitStandard,projectName,projectId,repoUuid,repoName));
            }
        }
        return projectCommitStandardDetailList;
     }

    /**
     * 转换相关数据
     * @see DeveloperCommitStandard
     * @see ProjectCommitStandardDetail
     * @param developerCommitStandard 开发者提交规范性明细
     * @param projectName 项目名
     * @param projectId 项目id
     * @param repoUuid 库id
     * @param repoName 库名称
     * @return new ArrayList<{@link ProjectCommitStandardDetail}>
     */
     private List<ProjectCommitStandardDetail> dealWithDeveloperCommitStandardDetail(DeveloperCommitStandard developerCommitStandard,String projectName, int projectId,String repoUuid,String repoName) {
         List<ProjectCommitStandardDetail> projectCommitStandardDetailList = new ArrayList<>();
         List<Map<String,String>> developerJiraCommitInfo = developerCommitStandard.getDeveloperJiraCommitInfo();
         List<Map<String,String>> developerInvalidCommitInfo = developerCommitStandard.getDeveloperInvalidCommitInfo();
         // 添加项目规范提交明细
         projectCommitStandardDetailList.addAll(commitInfoToProjectCommitStandardDetail(developerJiraCommitInfo,projectName,projectId,repoUuid,repoName,true));
         // 添加项目不规范提交明细
         projectCommitStandardDetailList.addAll(commitInfoToProjectCommitStandardDetail(developerInvalidCommitInfo,projectName,projectId,repoUuid,repoName,false));
         return projectCommitStandardDetailList;
     }

    /**
     * 提交信息 转换为 项目提交明细
     * @see ProjectCommitStandardDetail
     * @param commitInfo 提交明细
     * @param projectName 项目名
     * @param projectId 项目id
     * @param repoUuid 库id
     * @param repoName 库名
     * @param isValid 是否规范
     * @return List<ProjectCommitStandardDetail>
     */
     private List<ProjectCommitStandardDetail> commitInfoToProjectCommitStandardDetail(List<Map<String,String>> commitInfo,String projectName, int projectId,String repoUuid,String repoName,boolean isValid) {
         List<ProjectCommitStandardDetail> projectCommitStandardDetailList = new ArrayList<>();
         for (Map<String, String> stringStringMap : commitInfo) {
             String message = stringStringMap.get("message");
             String commitTime = stringStringMap.get("commit_time");
             String commitId = stringStringMap.get("commit_id");
             String developerName;
             developerName = stringStringMap.getOrDefault("developer_unique_name", "");
             ProjectCommitStandardDetail projectCommitStandardDetail = ProjectCommitStandardDetail.builder()
                     .committer(developerName)
                     .commitId(commitId)
                     .repoUuid(repoUuid)
                     .repoName(repoName)
                     .commitTime(commitTime)
                     .message(message)
                     .projectId(String.valueOf(projectId))
                     .projectName(projectName)
                     .isValid(isValid).build();
             projectCommitStandardDetailList.add(projectCommitStandardDetail);
         }
         return projectCommitStandardDetailList;
     }

    /**
     * 前端项目总览界面， 添加提交者列表功能
     * note 这里的开发者是参与项目所在库中的全部开发者，不区分提交时间
     * @param projectNameList 查询项目列表
     * @param repoUuidList 查询库列表
     * @return List<String> commiter
     */
     public List<String> getCommitStandardCommitterList(String projectNameList,String repoUuidList,String token) {
         List<String> checkedRepoList = projectDao.getVisibleRepoListByProjectNameAndRepo(projectNameList,repoUuidList,token);
         return projectDao.getDeveloperList(new Query(token,null,null,null,checkedRepoList));
     }


    /**
     *
     * @param projectIds
     * @param since
     * @param until
     * @param token
     * @param interval
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
             for (ProjectPair projectPair : checkedProjectPairList) {
                 List<String> repoUuidList = new ArrayList<>();
                 List<RepoInfo> repoInfoList = projectDao.getProjectInvolvedRepoInfo(projectPair.getProjectName(),token);
                 for (RepoInfo repoInfo : repoInfoList) {
                     repoUuidList.add(repoInfo.getRepoUuid());
                 }
                 int projectId = projectPair.getProjectId();
                 List<ProjectBigFileDetail> projectBigFileDetailList = measureDao.getCurrentBigFileInfo(repoUuidList,tempTime.format(DateTimeUtil.dtf));
                 ProjectBigFileTrendChart projectBigFileTrendChart = ProjectBigFileTrendChart.builder()
                         .projectId(String.valueOf(projectId))
                         .date(tempTime.format(dtf))
                         .projectName(projectPair.getProjectName())
                         .num(projectBigFileDetailList.size())
                         .build();
                 results.add(projectBigFileTrendChart);
             }
             beginTime = tempTime;
         }
         return results;
     }

    /**
     *
     * @param projectNameList
     * @param repoUuidList
     * @param token
     * @return
     */
    @SneakyThrows
     public List<ProjectBigFileDetail> getHugeLocRemainedDetail(String projectNameList,String repoUuidList,String token) {
         List<ProjectBigFileDetail> result = new ArrayList<>();
         List<String> visibleRepoList = projectDao.getVisibleRepoListByProjectNameAndRepo(projectNameList,repoUuidList,token);
         for (String repoUuid : visibleRepoList) {
             if (!projectDao.getRepoInfoMap().containsKey(repoUuid)) {
                 projectDao.insertProjectInfo(token);
             }
             RepoInfo repoInfo = projectDao.getRepoInfoMap().get(repoUuid);
             String projectName = repoInfo.getProjectName();
             String repoName = repoInfo.getRepoName();
             int projectId = projectDao.getProjectIdByName(projectName);
             List<ProjectBigFileDetail> projectBigFileDetailList = measureDao.getCurrentBigFileInfo(Collections.singletonList(repoUuid),null);
             for (ProjectBigFileDetail projectBigFileDetail : projectBigFileDetailList) {
                 projectBigFileDetail.setProjectId(String.valueOf(projectId));
                 projectBigFileDetail.setProjectName(projectName);
                 projectBigFileDetail.setRepoName(repoName);
             }
             result.addAll(projectBigFileDetailList);
         }
         return result;
     }


    @Autowired
    public void setMeasureDeveloperServiceImpl(MeasureDeveloperService measureDeveloperServiceImpl) {
        this.measureDeveloperService = measureDeveloperServiceImpl;
    }


    @Autowired
    public MeasureDeveloperService(RestInterfaceManager restInterfaceManager, RepoMeasureMapper repoMeasureMapper, ProjectDao projectDao, JiraDao jiraDao, MeasureDao measureDao) {
        this.restInterfaceManager = restInterfaceManager;
        this.repoMeasureMapper = repoMeasureMapper;
        this.projectDao = projectDao;
        this.jiraDao = jiraDao;
        this.measureDao = measureDao;
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