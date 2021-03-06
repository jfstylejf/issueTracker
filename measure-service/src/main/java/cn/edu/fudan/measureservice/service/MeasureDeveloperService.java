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
import cn.edu.fudan.measureservice.domain.enums.TagMetricEnum;
import cn.edu.fudan.measureservice.domain.metric.RepoTagMetric;
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

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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
    private static final DecimalFormat df = new DecimalFormat("0.00");
    /**
     * ????????????????????????
     * @param query ????????????
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
            developerWorkLoad.setTotalLoc(developerWorkLoad.getAddLines());
            developerWorkLoadList.add(developerWorkLoad);
        }
        return developerWorkLoadList;
    }

    /**
     * ??????????????????????????????????????????
     * @param query ????????????
     * @return
     */
    public DeveloperWorkLoad getDeveloperWorkLoadWithLevel(Query query) {
        // ????????????????????????
        int developerAddLines = 0;
        // ????????????????????????
        int developerDeleteLines = 0;
        // ????????????????????????
        int developerTotalLoc = 0;
        // ????????????????????????
        int developerCommitCount = 0;
        // ???????????????????????????
        int developerChangedFiles = 0;
        // ?????????????????????
        int developerInvolvedRepoNum = query.getRepoUuidList().size();
        // ?????????
        double totalLevel = 0;
        for (String repoUuid : query.getRepoUuidList()) {
            // ??????????????????????????????????????????
            log.info("start to get {} in repo : {}",query.getDeveloper(),repoUuid);
            Query tempQuery = new Query(query.getToken(),query.getSince(),query.getUntil(),query.getDeveloper(),Collections.singletonList(repoUuid));
            DeveloperWorkLoad repoDeveloperWorkLoad = measureDao.getDeveloperWorkLoadData(tempQuery);
            int repoDeveloperAddLines = repoDeveloperWorkLoad.getAddLines();
            int repoDeveloperDeleteLines = repoDeveloperWorkLoad.getDeleteLines();
            int repoTotalLoc = repoDeveloperAddLines + repoDeveloperDeleteLines;
            developerAddLines += repoDeveloperAddLines;
            developerDeleteLines += repoDeveloperDeleteLines;
            developerTotalLoc += repoTotalLoc;
            developerCommitCount += repoDeveloperWorkLoad.getCommitCount();
            developerChangedFiles += repoDeveloperWorkLoad.getChangedFiles();
            if (repoDeveloperAddLines < 1000 ) {
                // todo ?????????????????? 1000 ?????? ????????????????????? level
                developerInvolvedRepoNum --;
                continue;
            }
            totalLevel += getRepoWorkLoadLevel(repoDeveloperAddLines,repoUuid);
        }
        // ?????? DeveloperWorkLoad
        DeveloperWorkLoad developerWorkLoad = new DeveloperWorkLoad();
        developerWorkLoad.setDeveloperName(query.getDeveloper());
        totalLevel = developerInvolvedRepoNum == 0 ? 1 : totalLevel * 1.0 / developerInvolvedRepoNum;
        developerWorkLoad.setAddLines(developerAddLines);
        developerWorkLoad.setDeleteLines(developerDeleteLines);
        developerWorkLoad.setTotalLoc(developerTotalLoc);
        developerWorkLoad.setChangedFiles(developerChangedFiles);
        developerWorkLoad.setCommitCount(developerCommitCount);
        LevelEnum levelEnum = developerInvolvedRepoNum != 0 ? getLevel(totalLevel) : LevelEnum.NoNeedToEvaluate;
        // ???????????????????????????
        developerWorkLoad.setLevel(levelEnum);
        return developerWorkLoad;
    }

    private int getRepoWorkLoadLevel(int totalLoc, String repoUuid) {
        RepoTagMetric repoTagMetric = measureDao.getRepoMetric(repoUuid, TagMetricEnum.WorkLoad.name());
        if (totalLoc >= repoTagMetric.getBestMin() && totalLoc <= repoTagMetric.getBestMax()) {
            return 5;
        }else if (totalLoc >= repoTagMetric.getBetterMin() && totalLoc <= repoTagMetric.getBetterMax()) {
            return 4;
        }else if (totalLoc >= repoTagMetric.getNormalMin() && totalLoc <= repoTagMetric.getBetterMax()) {
            return 3;
        }else if (totalLoc >= repoTagMetric.getWorseMin() && totalLoc <= repoTagMetric.getWorseMax()) {
            return 2;
        }else {
            return 1;
        }
    }


    public Object getStatementByCondition(String repoUuidList, String developer, String since, String until) throws ParseException {
        List<String> repoList = new ArrayList<>();
        if(StringUtils.isEmptyOrNull(repoUuidList)) {
            repoList = null;
        }else {
            String[] repoUuidArray = repoUuidList.split(",");
            //??????????????????repo?????????repoList
            repoList.addAll(Arrays.asList(repoUuidArray));
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if ("".equals(since) || since == null){
            String dateString = projectDao.getDeveloperFirstCommitDate(developer,null);
            since = dateString.substring(0,10);
        }
        if ("".equals(until) || until == null){
            until = sdf.format(new Date());
        }
        //???????????????????????????????????????change????????????
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
        //??????????????????
        int totalCommitCount = repoMeasureMapper.getCommitCountsByDuration(repoUuid, query.getSince(), query.getUntil(), null);
        int developerCommitCount = repoMeasureMapper.getCommitCountsByDuration(repoUuid, query.getSince(), query.getUntil(), query.getDeveloper());
        // fixme ??????????????? ?????????repository??????
        Query query1 = new Query(query.getToken(),query.getSince(),query.getUntil(),null,query.getRepoUuidList());
        DeveloperWorkLoad developerWorkLoad = measureDao.getDeveloperWorkLoadData(query);
        DeveloperWorkLoad developerWorkLoad1 = measureDao.getDeveloperWorkLoadData(query1);
        int developerLOC = developerWorkLoad.getAddLines() + developerWorkLoad.getDeleteLines();
        int totalLOC = developerWorkLoad1.getAddLines() + developerWorkLoad1.getDeleteLines();
        //?????????????????????????????????????????????
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
        //???????????????issue???
        int developerStandardIssueCount = restInterfaceManager.getIssueCountByConditions(developer, repoUuid, since, until, tool, "Code Smell", token);
        //???????????????issue???
        int developerSecurityIssueCount = restInterfaceManager.getIssueCountByConditions(developer, repoUuid, since, until, tool, "Bug", token);
        //repo???issue???
        int totalIssueCount = restInterfaceManager.getIssueCountByConditions("", repoUuid, since, until, tool, "", token);
        //?????????????????????
        int developerNewIssueCount = restInterfaceManager.getAddIssueCount(repoUuid,developer,since,until,tool,token);
        //??????????????????
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
            since = projectDao.getDeveloperFirstCommitDate(developer,repoUuid);
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
        //?????????????????????????????????????????????commit?????????
        String developerFirstCommitTime = projectDao.getDeveloperFirstCommitDate(developer,repoUuid);
        LocalDate firstCommitDate = LocalDate.parse(developerFirstCommitTime,dtf);
        firstCommitDate = firstCommitDate.plusDays(1);

        int developerLOC = repoMeasureMapper.getLOCByCondition(repoUuid,developer,since,until);
        int developerCommitCount = repoMeasureMapper.getCommitCountsByDuration(repoUuid, since, until, developer);
        int developerValidCommitCount = repoMeasureMapper.getDeveloperValidCommitCount(repoUuid,since,until,developer);
        //???????????????????????????,change??????????????????
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

        //????????????????????????
        cn.edu.fudan.measureservice.portrait2.Efficiency efficiency = getEfficiency(repoUuid, since, until, developer, tool, token);

        //????????????????????????
        cn.edu.fudan.measureservice.portrait2.Quality quality = getQuality(repoUuid,developer,since,until,tool,token,developerLOC,developerValidCommitCount);

        //????????????????????????
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
        //?????????????????????issue???
        int developerStandardIssueCount = restInterfaceManager.getIssueCountByConditions(developer, repoUuid, since, until, tool, "Code Smell", token);
        //????????????issue??????
        int developerNewIssueCount = restInterfaceManager.getIssueCountByConditions(developer, repoUuid, since, until, tool, null, token);
        //?????????????????????issue???
        int totalStandardIssueCount = restInterfaceManager.getIssueCountByConditions(null, repoUuid, since, until, tool, "Code Smell", token);
        //????????????issue??????
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

        int developerAssignedJiraCount = 0;//?????????????????????jira????????????????????????????????????
        int totalAssignedJiraCount = 0;//?????????????????????jira????????????????????????????????????
        int developerSolvedJiraCount = 0;//???????????????jira????????????????????????????????????
        int totalSolvedJiraCount = 0;//???????????????jira????????????????????????????????????

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
        //??????????????????????????????????????????????????????????????????
        List<String> filterdRepoList = repoMeasureMapper.getRepoListByDeveloper(developer,since,until);
        //repoList??????????????????????????????????????????
        List<String> repoList = new ArrayList<>();
        if(StringUtils.isEmptyOrNull(repoUuidList)) {
            repoList = filterdRepoList;
        }else {
            String[] repoUuidArray = repoUuidList.split(",");
            //??????????????????repo?????????repoList
            repoList.addAll(Arrays.asList(repoUuidArray));
            for (int i = repoList.size() - 1; i >= 0; i--){//????????????????????????
                //??????????????????????????????repoUuid ??????????????????list??? ???????????????repo
                if (!filterdRepoList.contains(repoList.get(i))){
                    repoList.remove(i);
                }
            }
        }
        if (repoList.size()==0){
            log.error("?????????????????????");
            return null;
        }
        List<String> developerList = new ArrayList<>();
        if( developer == null || "".equals(developer)) {
            Query query = new Query(token,since,until,null,repoList);
            developerList = projectDao.getDeveloperList(query);
        }else {
            developerList.add(developer);
        }
        //??????developerMetricsList
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
        //?????????????????????commit?????????
        List<cn.edu.fudan.measureservice.portrait2.DeveloperPortrait> developerPortraitList = new ArrayList<>();
        for(String key : developerMetricMap.keySet()) {
            String developerFirstCommitTime = projectDao.getDeveloperFirstCommitDate(developer,null);
            LocalDate firstCommitDate = LocalDate.parse(developerFirstCommitTime,dtf);
            firstCommitDate = firstCommitDate.plusDays(1);
            //todo ??????????????????????????????????????? ?????????????????????java???????????????
            /*String index = repoMeasureMapper.getDeveloperType(key);
            String developerType = null ;
            if("L".equals(index)) {
                developerType = "???????????????";
            }else if("P".equals(index)) {
                developerType = "JAVA???????????????";
            }else if ("M".equals(index)) {
                developerType= "????????????";
            }*/
            String developerType = "JAVA???????????????";
            // ?????????????????????????????????????????????????????????
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
     * @param developerMetricsList ???????????????????????????
     * @param developer ?????????
     * @return ?????????????????????????????????????????????????????????
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
        int developerNewIssueCount = 0;//??????????????????
        int totalNewIssueCount = 0;//??????????????????
        int developerLOC = 0;//??????addLines+delLines
        int developerValidCommitCount = 0;//???????????????commit??????
        int developerJiraCount = 0;//???????????????commit?????? ?????????jira?????????
        int developerJiraBugCount = 0;//??????jira???????????????bug???????????????
        int totalJiraBugCount = 0;//??????jira???????????????bug???????????????

        //contribution
        int developerAddLine = 0;
        //int developerLOC
        int totalLOC = 0;
        int developerAddStatement = 0;
        int developerChangeStatement = 0;
        int developerValidLine = 0;//??????????????????
        int totalAddStatement = 0;//??????????????????
        int totalValidLine = 0;//??????????????????
        int increasedCloneLines = 0;//??????????????????????????????
        int developerAssignedJiraCount = 0;//?????????????????????jira????????????????????????????????????
        int totalAssignedJiraCount = 0;//?????????????????????jira????????????????????????????????????
        int developerSolvedJiraCount = 0;//???????????????jira????????????????????????????????????
        int totalSolvedJiraCount = 0;//???????????????jira????????????????????????????????????

        //?????????????????????????????????????????????????????????????????????
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
     * ???????????????????????????
     * @param repoUuids
     * @param developer
     * @param since
     * @param until
     * @return
     */
    public Object getDeveloperRecentNews(String repoUuids, String developer, String since, String until) {
        List<String> repoUuidList = repoUuids == null ? new ArrayList<>() : Arrays.asList(repoUuids.split(split));
        // ???????????????????????????
        List<DeveloperRecentNews> developerRecentNewsList = projectDao.getDeveloperRecentNews(developer,repoUuidList,since,until);
        for (DeveloperRecentNews developerRecentNews : developerRecentNewsList) {
            String developerUniqueName = accountDao.getDeveloperName(developerRecentNews.getDeveloperName());
            developerRecentNews.setDeveloperName(developerUniqueName);
            //???????????????????????????jira??????
            String commitMessage = developerRecentNews.getMessage();
            String jiraID = jiraDao.getJiraIDFromCommitMsg(commitMessage);
            if("noJiraID".equals(jiraID)) {
                developerRecentNews.setJiraInfo("??????commit??????jira??????");
            }else {
                try {
                    JSONArray jiraResponse = restInterfaceManager.getJiraInfoByKey("key",jiraID);
                    if (jiraResponse == null || jiraResponse.isEmpty()){
                        developerRecentNews.setJiraInfo("jira??????????????????");
                    }else {
                        developerRecentNews.setJiraInfo(jiraResponse.get(0));
                    }
                }catch (Exception e) {
                    log.info("cannot request Jira");
                    e.getMessage();
                }
            }
        }
        return developerRecentNewsList;
    }


    /**
     * ???????????????????????????
     * @param
     * @return Developer
     */
    public DeveloperPortrait getDeveloperPortrait(Query query) {
        Objects.requireNonNull(query.getDeveloper());
        // ???????????????????????????????????????????????????
        String firstCommitDate = projectDao.getDeveloperFirstCommitDate(query.getDeveloper(),null);
        String developerType = "JAVA???????????????";
        int totalCommitCount = 0;
        int totalStatement = 0;
        List<DeveloperRepositoryMetric> developerRepositoryMetrics = new ArrayList<>();
        // ???????????????????????????????????????
        for (String repoUuid : query.getRepoUuidList()) {
            log.info("get portrait of {} in repo : {}",query.getDeveloper(),repoUuid);
            DeveloperRepositoryMetric developerRepoMetric = ((MeasureDeveloperService) AopContext.currentProxy()).getDeveloperRepositoryMetric(query.getDeveloper(),repoUuid,query.getToken());
            totalCommitCount += developerRepoMetric.getTotalCommitCount();
            totalStatement += developerRepoMetric.getTotalStatement();
            developerRepositoryMetrics.add(developerRepoMetric);
        }
        long days = DateTimeUtil.getSumDays(query.getUntil(),firstCommitDate);
        int dayAverageStatement = (int) (totalStatement/days);
        return new DeveloperPortrait(firstCommitDate,totalStatement,dayAverageStatement,totalCommitCount,query.getDeveloper(),developerType,developerRepositoryMetrics);
    }

    /**
     * ?????????????????????????????????
     * @param developer ???????????????
     * @param repoUuid ????????? id
     * @return DeveloperRepositoryMetric ??????????????????
     */
    @SneakyThrows
    @Cacheable(value = "developerRepositoryMetric",key = "#developer+'_'+#repoUuid")
    public DeveloperRepositoryMetric getDeveloperRepositoryMetric(String developer,String repoUuid,String token) {
        String projectName = projectDao.getProjectName(repoUuid);
        String repoName = projectDao.getRepoName(repoUuid);
        String firstCommitDate = projectDao.getDeveloperFirstCommitDate(developer,repoUuid);
        int developerStatement = 0;
        // ???????????????????????????????????????
        try {
            List<Map<String,Object>> developerStatements = restInterfaceManager.getStatements(repoUuid,null,null,developer,Statement_developer);
            if (developerStatements.size()>0) {
                developerStatement = (int) developerStatements.get(0).get("total");
            }
        }catch (Exception e) {
            log.error(e.getMessage());
        }
        // ???????????????????????????????????????
        int developerCommitCount = 0;
        try {
            developerCommitCount = measureDao.getDeveloperRepoCommitCount(developer,repoUuid,null,null);
        }catch (Exception e) {
            log.error(e.getMessage());
        }
        return DeveloperRepositoryMetric.builder()
                .firstCommitDate(firstCommitDate)
                .developer(developer)
                .totalCommitCount(developerCommitCount)
                .totalStatement(developerStatement)
                .repoName(repoName)
                .repoUuid(repoUuid).build();
    }

    @CacheEvict(value = "developerRepositoryMetric", allEntries=true, beforeInvocation = true)
    public void deleteDeveloperRepositoryMetric() {

    }

    /**
     *
     * @param
     * @return ??????????????????????????? key : DutyType,involvedRepoCount,totalLevel,value,quality,efficiency
     * @throws ParseException
     */
    public synchronized void getDeveloperList(Query query) throws ParseException {
        if(query.getRepoUuidList().size()==0) {
            log.warn("do not have any authorized repo to see");
        }
        List<DeveloperLevel> developerLevelList = new ArrayList<>();
        Set<String> developerSet = projectDao.getDeveloperList(query.getRepoUuidList());
        // ????????????????????????????????????
        for(String developer : developerSet) {
            log.info("start to get portrait of {}",developer);
            Query temp = new Query(query.getToken(),null,null,developer,query.getRepoUuidList());
            // ???????????????????????????
            String dutyType = projectDao.getDeveloperDutyType(developer);
            int involvedRepoCount = projectDao.getDeveloperInvolvedRepoNum(developer);
            DeveloperPortrait developerPortrait = getDeveloperPortrait(temp);
            // fixme ????????????????????????????????? level????????????????????? 0?????????????????????????????????
            /*double totalLevel = developerPortrait.getLevel();
            double value = developerPortrait.getValue();
            double quality = developerPortrait.getQuality();
            double efficiency = developerPortrait.getEfficiency();*/
            DeveloperLevel developerLevel = new DeveloperLevel(developer,0,0,0,0,involvedRepoCount,dutyType);
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
     * ??????????????????????????????????????????
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
     * ??????????????????????????????????????????
     * @param query ????????????
     * @return
     */
    @SuppressWarnings("unchecked")
    public DeveloperCommitStandard getDeveloperCommitStandard(Query query) {
        assert query.getDeveloper() != null && !"".equals(query.getDeveloper());
        // ??????????????????????????????????????????
        log.info("start to get {}",query.getDeveloper());
        List<Map<String,Object>> developerValidCommitInfo = measureDao.getProjectValidCommitMsg(query);
        // ?????? DeveloperCommitStandard
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
     * ??????????????????????????????????????????, ???????????????????????????????????????
     * @param query ????????????
     * @return
     */
    public DeveloperCommitStandard getDeveloperCommitStandardWithLevel(Query query) {
        int developerValidCommitCount = 0;
        int developerJiraCommitCount = 0;
        int developerInvalidCommitCount = 0;
        int developerInvolvedRepoNum = query.getRepoUuidList().size();
        // ?????????
        double totalLevel = 0;
        for (String repoUuid : query.getRepoUuidList()) {
            // ?????????????????????????????????????????????
            log.info("start to get {} in repo : {}",query.getDeveloper(),repoUuid);
            Query tempQuery = new Query(query.getToken(),query.getSince(),query.getUntil(),query.getDeveloper(),Collections.singletonList(repoUuid));
            List<Map<String,Object>> developerValidCommitInfo = measureDao.getProjectValidCommitMsg(tempQuery);
            if (developerValidCommitInfo == null || developerValidCommitInfo.size() == 0) {
                // todo ?????????????????? 1000 ?????? ????????????????????? level
                developerInvolvedRepoNum --;
                continue;
            }
            int repoDeveloperValidCommitCount = developerValidCommitInfo.size();
            List<Map<String,Object>> developerValidJiraCommitInfo = measureDao.getProjectValidJiraCommitMsg(tempQuery);
            int repoDeveloperJiraCommitCount = developerValidJiraCommitInfo == null ? 0 : developerValidJiraCommitInfo.size();
            int repoDeveloperInvalidCommitCount = repoDeveloperValidCommitCount - repoDeveloperJiraCommitCount;

            double repoCommitStandard =  repoDeveloperJiraCommitCount * 1.0 / repoDeveloperValidCommitCount ;
            totalLevel += getRepoCommitStandardLevel(Double.parseDouble(df.format(repoCommitStandard)),repoUuid);
            developerValidCommitCount += repoDeveloperValidCommitCount;
            developerJiraCommitCount += repoDeveloperJiraCommitCount;
            developerInvalidCommitCount += repoDeveloperInvalidCommitCount;
        }
        // ?????? DeveloperCommitStandard
        DeveloperCommitStandard developerCommitStandard = new DeveloperCommitStandard();
        totalLevel = developerInvolvedRepoNum == 0 ? 1 : totalLevel * 1.0 / developerInvolvedRepoNum;
        developerCommitStandard.setDeveloperName(query.getDeveloper());
        developerCommitStandard.setDeveloperInvalidCommitCount(developerInvalidCommitCount);
        developerCommitStandard.setDeveloperJiraCommitCount(developerJiraCommitCount);
        developerCommitStandard.setDeveloperValidCommitCount(developerValidCommitCount);
        double commitStandard = developerValidCommitCount != 0 ? developerJiraCommitCount * 1.0 / developerValidCommitCount : 0;
        developerCommitStandard.setCommitStandard(Double.parseDouble(df.format(commitStandard)));
        LevelEnum levelEnum = developerInvolvedRepoNum != 0 ? getLevel(totalLevel) : LevelEnum.NoNeedToEvaluate;
        // ???????????????????????????
        developerCommitStandard.setLevel(levelEnum);
        return developerCommitStandard;
    }

    private LevelEnum getLevel(double level) {
        if (level >= 4.2 && level <= 5)  {
            return LevelEnum.Best;
        }else if (level >= 3.4 && level <= 4.2) {
            return LevelEnum.Better;
        }else if (level >= 2.6 && level <= 3.4) {
            return LevelEnum.Normal;
        }else if (level >= 1.8 && level <= 2.6) {
            return LevelEnum.Worse;
        }else {
            return LevelEnum.Worst;
        }
    }

    private int getRepoCommitStandardLevel(double commitStandard, String repoUuid) {
        RepoTagMetric repoTagMetric = measureDao.getRepoMetric(repoUuid, TagMetricEnum.CommitStandard.name());
        if (commitStandard >= repoTagMetric.getBestMin() && commitStandard <= repoTagMetric.getBestMax()) {
            return 5;
        }else if (commitStandard >= repoTagMetric.getBetterMin() && commitStandard <= repoTagMetric.getBetterMax()) {
            return 4;
        }else if (commitStandard >= repoTagMetric.getNormalMin() && commitStandard <= repoTagMetric.getBetterMax()) {
            return 3;
        }else if (commitStandard >= repoTagMetric.getWorseMin() && commitStandard <= repoTagMetric.getWorseMax()) {
            return 2;
        }else {
            return 1;
        }
    }

    /**
     * ????????????????????????????????????????????? interval ??????????????? ????????????
     * @param projectIds ??????????????????
     * @param since ??????????????????
     * @param until ??????????????????
     * @param interval ????????????
     * @return new ArrayList<{@link ProjectCommitStandardTrendChart}>
     */
    @SneakyThrows
    @MethodMeasureAnnotation
    public synchronized List<ProjectCommitStandardTrendChart> getCommitStandardTrendChartIntegratedByProject(String projectIds,String since,String until,String token,String interval) {
        List<ProjectCommitStandardTrendChart> results = new ArrayList<>();
        // ???????????? projectIds ????????????????????????
        List<ProjectPair> projectPairList = projectDao.getVisibleProjectPairListByProjectIds(projectIds,token);

        LocalDate endTime = LocalDate.parse(until,dtf);
        // ???????????? until ?????? 1??? ?????????????????? until???????????????+1????????????????????????????????????????????????????????????????????????????????????????????????????????????
        endTime = endTime.minusDays(1);
        LocalDate beginTime;
        if (since!=null && !"".equals(since)) {
            beginTime = LocalDate.parse(since,dtf);
        }else {
            // ?????? beginTime ??????
            beginTime = endTime.minusWeeks(1);
        }
        // ?????? interval ??? beginTime ??? endTime ????????????????????? ?????? ??? ??????
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
                // ??????????????????????????????
                int projectId = projectPair.getProjectId();
                // note ?????????????????????????????????????????????
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
     * ??????????????????????????????????????? {@link ProjectCommitStandardTrendChart}
     * @param query ????????????
     * @return ProjectCommitStandardTrendChart ?????????????????????
     */
    @Cacheable(value = "projectCommitStandardChart", key = "#projectPair.projectName+'_'+#query.until")
    public ProjectCommitStandardTrendChart getSingleProjectCommitStandardChart(Query query,ProjectPair projectPair) {
        ProjectCommitStandardTrendChart projectCommitStandardTrendChart = new ProjectCommitStandardTrendChart();
        // ??????????????????????????????
        List<Map<String,Object>> projectValidCommitMsgList = measureDao.getProjectValidCommitMsg(query);
        // validCommitCountNum : ??????Merge?????????????????? ??? jiraCommitCountNum ??????Jira????????????????????????
        long validCommitCountNum = projectValidCommitMsgList.size(), jiraCommitCountNum = 0;
        for (int i = 0; i < projectValidCommitMsgList.size(); i++) {
            Map<String,Object> commitMsg = projectValidCommitMsgList.get(i);
            boolean isValid = (int) commitMsg.get("is_compliance") == 1;
            if (isValid) {
                jiraCommitCountNum++;
            }
        }
        double num = 0.0;
        // ?????????Merge????????????????????? 0 ?????????????????????????????? 0
        if(validCommitCountNum!=0) {
            num = jiraCommitCountNum * 1.0 / validCommitCountNum;
        }
        // ???????????????????????????????????????
        projectCommitStandardTrendChart.setNum(Double.parseDouble(df.format(num)));
        projectCommitStandardTrendChart.setOption(jiraCommitCountNum,validCommitCountNum);
        return projectCommitStandardTrendChart;
    }


    @CacheEvict(value = "projectCommitStandardChart", allEntries=true, beforeInvocation = true)
    public void deleteProjectCommitStandardChart() {

    }

    /**
     * ??????????????????????????????????????????????????????
     * @param projectNameList ??????????????????
     * @param repoUuidList ???????????????
     * @param token ????????????
     * @return <{@link ProjectCommitStandardDetail}>
     */
    @SneakyThrows
    public synchronized ProjectFrontEnd<ProjectCommitStandardDetail> getCommitStandardDetailIntegratedByProject(String projectNameList,String repoUuidList,String committer,String token,int page,int ps,Boolean isValid) {
        // ???????????????????????????????????????
        List<String> visibleRepoList = projectDao.getVisibleRepoListByProjectNameAndRepo(projectNameList,repoUuidList,token);
        // ????????????????????????????????????????????? ??????????????????
        int totalMsgSize = projectDao.getRepoListMsgNum(visibleRepoList,isValid);
        int totalPage = totalMsgSize % ps == 0 ? totalMsgSize / ps : totalMsgSize / ps + 1;
        // ??????????????????
        int initialBeginIndex = (page-1) * ps;
        //?????? ps ?????????????????????
        List<ProjectCommitStandardDetail> selectedProjectCommitStandardDetail = getProjectValidCommitStandardDetail(visibleRepoList,committer,initialBeginIndex,ps,isValid);
        List<ProjectCommitStandardDetail> projectCommitStandardDetailList = new ArrayList<>(selectedProjectCommitStandardDetail);
        // ???????????????????????????
        return new ProjectFrontEnd<>(page,totalPage,totalMsgSize,projectCommitStandardDetailList);
     }

    /**
     * ????????????????????????????????????
     * @param repoUuidList ???????????????
     * @param committer ???????????????
     * @param beginIndex ??????????????????
     * @param size ????????????
     * @param selectOrNot ?????????????????? Jira ??????????????????
     * @return ???????????????????????????????????????
     */
     public List<ProjectCommitStandardDetail> getProjectValidCommitStandardDetail(List<String> repoUuidList, String committer, int beginIndex, int size, Boolean selectOrNot) {
         List<ProjectCommitStandardDetail> projectCommitStandardDetailList = new ArrayList<>();
         // ????????????????????????????????????
         Query query = new Query(null,null,null,null,repoUuidList);
         List<Map<String,Object>> projectValidCommitMsg;
         if (committer != null && !"".equals(committer)) {
             query.setDeveloper(committer);
         }
         // ?????? ???????????????????????????????????????
         if (selectOrNot == null) {
            projectValidCommitMsg = measureDao.getProjectValidCommitMsg(query,beginIndex,size);
         }else if (selectOrNot){
            projectValidCommitMsg = measureDao.getProjectValidJiraCommitMsg(query,beginIndex,size);
         }else {
             projectValidCommitMsg = measureDao.getProjectValidNotJiraCommitMsg(query,beginIndex,size);
         }
         //????????????
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
     * ??????????????????????????????
     * @param repoUuidList ???????????????
     * @param committer ???????????????
     * @return ???????????????????????????????????????
     */
    public List<ProjectCommitStandardDetail> getProjectValidCommitStandardDetail(List<String> repoUuidList, String committer) {
        List<ProjectCommitStandardDetail> projectCommitStandardDetailList = new ArrayList<>();
        // ????????????????????????????????????
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
     * ??????????????????????????? ???????????????????????????
     * note ???????????????????????????????????????????????????????????????????????????????????????
     * @param projectNameList ??????????????????
     * @param repoUuidList ???????????????
     * @return Set<String> commiter
     */
     public Set<String> getCommitStandardCommitterList(String projectNameList,String repoUuidList,String token) {
         List<String> checkedRepoList = projectDao.getVisibleRepoListByProjectNameAndRepo(projectNameList,repoUuidList,token);
         return projectDao.getDeveloperList(checkedRepoList);
     }


    /**
     * ??????????????????????????????
     * @param projectIds ????????????id??????
     * @param since ????????????
     * @param until ????????????
     * @param token ????????????
     * @param interval ??????
     * @return
     */
     public List<ProjectBigFileTrendChart> getHugeLocRemainedFile(String projectIds,String since,String until,String token,String interval) {
         List<ProjectBigFileTrendChart> results = new ArrayList<>();
         // ???????????????????????????
         List<ProjectPair> checkedProjectPairList = projectDao.getVisibleProjectPairListByProjectIds(projectIds,token);

         LocalDate endTime = LocalDate.parse(until,dtf);
         // ??????????????????????????????????????????????????? 1
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
             // ???????????????????????????
             List<String> repoUuidList = projectDao.getProjectRepoList(projectPair.getProjectName());
             int projectId = projectPair.getProjectId();
             // ???????????????????????? ?????????????????????
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
     * ???????????????????????????
     * @param projectNameList ??????????????????
     * @param repoUuidList ???????????????
     * @param token ????????????
     * @return
     */
     public List<ProjectBigFileDetail> getHugeLocRemainedDetail(String projectNameList,String repoUuidList,String token) {
         List<ProjectBigFileDetail> result = new ArrayList<>();
         List<String> visibleRepoList = projectDao.getVisibleRepoListByProjectNameAndRepo(projectNameList,repoUuidList,token);
         for (String repoUuid : visibleRepoList) {
             String projectName = projectDao.getProjectName(repoUuid);
             String repoName = projectDao.getRepoName(repoUuid);
             int projectId = projectDao.getProjectIdByName(projectName);
             // fixme ??????????????????????????????
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
     * ?????????????????????????????????????????????????????????
     * @param projectNameList ??????????????????
     * @param developers ???????????????
     * @param token ????????????
     * @param since ????????????
     * @param until ????????????
     * @return ??????????????????????????????????????? ??????????????????
     */
     @SneakyThrows
     public  List<DeveloperDataCcn> getDeveloperDataCcn(String projectNameList, String developers, String token , String since, String until) {
        List<DeveloperDataCcn> developerDataCcnList = new ArrayList<>();
        List<String> visibleRepoList = projectDao.getVisibleRepoListByProjectName(projectNameList,token);
        String[] developerList = developers.split(split);
        for (String developer : developerList) {
            List<DeveloperProjectCcn> developerProjectCcnList = new ArrayList<>();
            List<String> developerRepoList = projectDao.getDeveloperVisibleRepo(visibleRepoList,developer,since,until);
            // ?????????????????? ?????? ??? ???????????????????????????????????????
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
                // ??????????????????????????????????????????????????????
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
                // ??? projectDiffCcn ???????????????
                developerProjectCcn.cal();
                developerProjectCcnList.add(developerProjectCcn);
            }
            // totalDiffCCn?????????
            DeveloperDataCcn developerDataCcn = DeveloperDataCcn.builder()
                            .developerName(developer)
                            .developerProjectCcnList(developerProjectCcnList)
                            .since(since).until(until)
                            .totalDiffCcn(0)
                            .level(LevelEnum.Normal.getType()).build();
            developerDataCcn.cal();
            developerDataCcnList.add(developerDataCcn);
        }
        return developerDataCcnList;
     }

    /**
     * ????????????????????????????????????????????????
     * @param projectNameList ??????????????????
     * @param developers ???????????????
     * @param token ????????????
     * @param since ????????????
     * @param until ????????????
     * @return
     */
     @SneakyThrows
     public List<DeveloperDataCommitStandard> getDeveloperDataCommitStandard(String projectNameList, String developers, String token , String since, String until) {
         List<DeveloperDataCommitStandard> developerDataCommitStandardList = new ArrayList<>();
         // ???????????????????????????????????????
         List<String> visibleRepoList = projectDao.getVisibleRepoListByProjectName(projectNameList,token);
         // ?????????????????????????????????
         Set<String> developerNameList = !"".equals(developers) ? new HashSet<>(Arrays.asList(developers.split(split))) : projectDao.getDeveloperList(visibleRepoList);
         List<DeveloperCommitStandard> developerCommitStandardList = new ArrayList<>();
         for (String developer : developerNameList) {
             // ???????????????????????????????????????
             List<String> developerRepoList = projectDao.getDeveloperVisibleRepo(visibleRepoList,developer,null,null);
             DeveloperCommitStandard developerCommitStandard = ((MeasureDeveloperService) AopContext.currentProxy()).getDeveloperCommitStandardWithLevel(new Query(token,since,until,developer,developerRepoList));
             developerCommitStandardList.add(developerCommitStandard);
         }
         // ????????????????????????????????????
         for (DeveloperCommitStandard developerCommitStandard : developerCommitStandardList) {
             DeveloperDataCommitStandard developerDataCommitStandard = DeveloperDataCommitStandard.builder()
                     .developerName(developerCommitStandard.getDeveloperName())
                     .since(since)
                     .until(until)
                     .developerJiraCommitCount(developerCommitStandard.getDeveloperJiraCommitCount())
                     .developerValidCommitCount(developerCommitStandard.getDeveloperValidCommitCount())
                     .commitStandard(developerCommitStandard.getCommitStandard())
                     .detail(null)
                     .level(developerCommitStandard.getLevel().getType()).build();
             developerDataCommitStandardList.add(developerDataCommitStandard);
         }
         return developerDataCommitStandardList;
     }


    /**
     * ??????????????????????????????????????????
     * @param projectNameList ??????????????????
     * @param developers ???????????????
     * @param token ????????????
     * @param since ????????????
     * @param until ????????????
     * @return
     */
    @SneakyThrows
    public List<DeveloperDataWorkLoad> getDeveloperDataWorkLoad(String projectNameList, String developers, String token , String since, String until) {
        List<DeveloperDataWorkLoad> developerDataWorkLoadList = new ArrayList<>();
        // ???????????????????????????????????????
        List<String> visibleRepoList = projectDao.getVisibleRepoListByProjectName(projectNameList,token);
        // ?????????????????????????????????
        Set<String> developerNameList = !"".equals(developers) ? new HashSet<>(Arrays.asList(developers.split(split))) : projectDao.getDeveloperList(visibleRepoList);
        List<DeveloperWorkLoad> developerWorkLoadList = new ArrayList<>();
        for (String developer : developerNameList) {
            // ???????????????????????????????????????
            List<String> developerRepoList = projectDao.getDeveloperVisibleRepo(visibleRepoList,developer,null,null);
            DeveloperWorkLoad developerWorkLoad = ((MeasureDeveloperService) AopContext.currentProxy()).getDeveloperWorkLoadWithLevel(new Query(token,since,until,developer,developerRepoList));
            developerWorkLoadList.add(developerWorkLoad);
        }
        // ????????????????????????????????????
        for (DeveloperWorkLoad developerWorkLoad : developerWorkLoadList) {
            DeveloperDataWorkLoad developerDataWorkLoad = DeveloperDataWorkLoad.builder()
                    .developerName(developerWorkLoad.getDeveloperName())
                    .since(since)
                    .until(until)
                    .addLines(developerWorkLoad.getAddLines())
                    .deleteLines(developerWorkLoad.getDeleteLines())
                    .totalLoc(developerWorkLoad.getAddLines())
                    .detail(null)
                    .level(developerWorkLoad.getLevel().getType()).build();
            developerDataWorkLoadList.add(developerDataWorkLoad);
        }
        return developerDataWorkLoadList;
    }




    /**
     * ????????? repo ????????????????????????
     * @param repoUuid ?????????
     * @return ???????????????????????? true
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

    @CacheEvict(cacheNames = {}, allEntries=true, beforeInvocation = true)
    public void clearCache() {
        log.info("Successfully clear redis cache in db6.");
    }



}