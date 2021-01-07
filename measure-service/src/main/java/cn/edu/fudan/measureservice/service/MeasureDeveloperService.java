package cn.edu.fudan.measureservice.service;

import cn.edu.fudan.measureservice.annotation.MethodMeasureAnnotation;
import cn.edu.fudan.measureservice.aop.MethodMeasureAspect;
import cn.edu.fudan.measureservice.component.RestInterfaceManager;
import cn.edu.fudan.measureservice.dao.JiraDao;
import cn.edu.fudan.measureservice.dao.MeasureDao;
import cn.edu.fudan.measureservice.dao.ProjectDao;
import cn.edu.fudan.measureservice.domain.bo.DeveloperCommitStandard;
import cn.edu.fudan.measureservice.domain.bo.DeveloperWorkLoad;
import cn.edu.fudan.measureservice.domain.dto.DeveloperRepoInfo;
import cn.edu.fudan.measureservice.domain.dto.Query;
import cn.edu.fudan.measureservice.domain.dto.RepoInfo;
import cn.edu.fudan.measureservice.mapper.RepoMeasureMapper;
import cn.edu.fudan.measureservice.domain.bo.DeveloperPortrait;
import cn.edu.fudan.measureservice.domain.bo.DeveloperRepositoryMetric;
import cn.edu.fudan.measureservice.portrait.*;
import cn.edu.fudan.measureservice.portrait2.Contribution;
import cn.edu.fudan.measureservice.util.DateTimeUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    private static final String TOOL = "sonarqube";
    /**
     *
     * @param query 查询条件
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<DeveloperWorkLoad> getDeveloperWorkLoad(Query query , List<String> developers) {
        List<DeveloperWorkLoad> developerWorkLoadList = new ArrayList<>();
        List<String> developerList = new ArrayList<>();
        if(query.getDeveloper()!=null && !"".equals(query.getDeveloper())) {
            developerList.add(query.getDeveloper());
        }else if(developers!=null && developers.size()>0) {
            developerList = developers;
        }else {
            developerList = projectDao.getDeveloperList(query);
        }
        for (String member : developerList) {
            if(member == null || "".equals(member)) {
                continue;
            }
            // fixme 通过measureDao来获取数据
            query.setDeveloper(member);
            DeveloperWorkLoad developerWorkLoad = measureDao.getDeveloperWorkLoadData(query);
            developerWorkLoad.setDeveloperName(member);
            developerWorkLoad.setTotalLoc(developerWorkLoad.getAddLines() + developerWorkLoad.getDelLines());
            developerWorkLoadList.add(developerWorkLoad);
        }
        return developerWorkLoadList;
    }

    private int getFixedTypeByMapper(Object object) {
        if (object instanceof BigDecimal) {
            return ((BigDecimal) object).intValue();
        }else if(object instanceof Long){
            return ((Long) object).intValue();
        }else {
            return (int) object;
        }
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
        JSONObject developerStatements = restInterfaceManager.getStatements(repoUuidList,since,until,developer);
        if(developerStatements != null ) {
            JSONObject developerTotalStatements = developerStatements.getJSONObject("developer");
            if( developerTotalStatements!=null && !developerTotalStatements.isEmpty()) {
                totalStatement = developerTotalStatements.getJSONObject(developer).getIntValue("total");
            }
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
        int developerLOC = developerWorkLoad.getAddLines() + developerWorkLoad.getDelLines();
        int totalLOC = developerWorkLoad1.getAddLines() + developerWorkLoad1.getDelLines();
        //获取代码新增、删除逻辑行数数据
        JSONObject allDeveloperStatements = restInterfaceManager.getStatements(repoUuid,query.getSince(),query.getUntil(),"");
        int developerAddStatement = 0;
        int totalAddStatement = 0;
        int developerDelStatement = 0;
        int totalDelStatement = 0;
        int developerValidLine = 0;
        int totalValidLine = 0;
        JSONObject repoStatements = allDeveloperStatements.getJSONObject("repo");
        if(repoStatements!=null && !repoStatements.isEmpty()) {
            totalAddStatement = repoStatements.getJSONObject(repoUuid).getIntValue("add");
            totalDelStatement = repoStatements.getJSONObject(repoUuid).getIntValue("delete");
            totalValidLine = repoStatements.getJSONObject(repoUuid).getIntValue("current");
        }
        JSONObject developerStatements = allDeveloperStatements.getJSONObject("developer");
        if(developerStatements!=null && !developerStatements.isEmpty()) {
            for(String member : developerStatements.keySet()) {
                if(member.equals(query.getDeveloper())) {
                    developerAddStatement = developerStatements.getJSONObject(query.getDeveloper()).getIntValue("add");
                    developerDelStatement = developerStatements.getJSONObject(query.getDeveloper()).getIntValue("delete");
                    developerValidLine = developerStatements.getJSONObject(query.getDeveloper()).getIntValue("current");
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
        JSONObject cloneMeasure = restInterfaceManager.getCloneMeasure(repoUuid, developer, since, until);
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
        JSONObject developerCodeLifeCycle = restInterfaceManager.getCodeLifeCycle(repoUuid,developer,since,until);
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

        JSONObject focusMeasure = restInterfaceManager.getFocusFilesCount(repoUuid,null,since,until);
        int totalChangedFile = 0;
        int developerFocusFile = 0;
        if (focusMeasure != null){
            totalChangedFile = focusMeasure.getIntValue("total");
            developerFocusFile = focusMeasure.getJSONObject("developer").getIntValue(developer);
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

        JSONObject projects = restInterfaceManager.getProjectByrepoUuid(repoUuid,token);
        String branch = null;
        String repoName = null;
        if(projects!=null) {
            branch = projects.getString("branch");
            repoName = projects.getString("repoName");
            repoName = repoName.replace("/","");
        }
        //获取程序员在本项目中第一次提交commit的日期
        LocalDateTime firstCommitDateTime;
        LocalDate firstCommitDate = null;
        JSONObject firstCommitDateData = restInterfaceManager.getFirstCommitDate(developer);
        if("Successful".equals(firstCommitDateData.getString("status"))){
            JSONArray repoDateList = firstCommitDateData.getJSONArray("repos");
            for(int i=0;i<repoDateList.size();i++) {
                if (repoDateList.getJSONObject(i).get("repo_id").equals(repoUuid)){
                    String dateString = repoDateList.getJSONObject(i).getString("first_commit_time");
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    firstCommitDateTime = LocalDateTime.parse(dateString, fmt);
                    firstCommitDateTime = firstCommitDateTime.plusHours(8);
                    firstCommitDate = firstCommitDateTime.toLocalDate();
                    break;
                }
            }
        } else {//commit接口返回有问题时，通过repo_measure表来查看commit日期
            List<String> repoUuidList = new ArrayList<>();
            repoUuidList.add(repoUuid);
            String dateString = repoMeasureMapper.getFirstCommitDateByCondition(repoUuidList,developer);
            dateString = dateString.substring(0,19);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            firstCommitDateTime = LocalDateTime.parse(dateString, fmt);
            firstCommitDate = firstCommitDateTime.toLocalDate();
        }

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
        JSONObject developerStatements = restInterfaceManager.getStatements(repoUuid, since, until, developer);
        JSONObject allDeveloperStatements = restInterfaceManager.getStatements(repoUuid,since,until,"");
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

        JSONObject cloneMeasure = restInterfaceManager.getCloneMeasure(repoUuid, developer, since, until);
        int increasedCloneLines = 0;
        if (cloneMeasure != null){
            increasedCloneLines = Integer.parseInt(cloneMeasure.getString("increasedCloneLines"));
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
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
                JSONObject projects = restInterfaceManager.getProjectByrepoUuid(repo,token);
                if(projects == null) {
                    continue;
                }
                String tool = "sonarqube";
                String repoName = projects.getString("repoName");
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
        Long days = getSumDays(query.getUntil(),firstCommitDate);
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
        int developerStatement = restInterfaceManager.getStatements(repoUuid,query.getSince(),query.getUntil(),query.getDeveloper()).getIntValue("total");

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
    @Cacheable(cacheNames = {"developerList"})
    public synchronized Object getDeveloperList(String repoUuid,String projectName,String since,String until,String token) throws ParseException {
        List<String> repoUuidList;
        if(projectName!=null && !"".equals(projectName)) {
            repoUuidList = projectDao.getProjectRepoList(projectName,token);
        }else {
            repoUuidList = projectDao.involvedRepoProcess(repoUuid,token);
        }
        Query query = new Query(token,since,until,null,repoUuidList);
        if(query.getRepoUuidList().size()==0) {
            log.warn("do not have any authorized repo to see");
            return null;
        }
        Map<String,List<DeveloperRepoInfo>> developerRepoInfos = projectDao.getDeveloperRepoInfoList(query);
        Map<String,String> developerDutyType = projectDao.getDeveloperDutyType(developerRepoInfos.keySet());
        List<Map<String, Object>> result = new ArrayList<>();
        // fixme 单个人员的画像补全 , 需要建一个全局变量，来保存developer相关的developerRepoInfo
        for(String developer : developerRepoInfos.keySet()) {
            Map<String,Object> dev = new HashMap<>();
            log.info("start to get portrait of {}",developer);
            query.setDeveloper(developer);
            dev.put("developer",developer);
            String dutyType = developerDutyType.get(developer);
            if("1".equals(dutyType)) {
                dev.put("DutyType","在职");
            } else{
                dev.put("DutyType","离职");
            }
            int involvedRepoCount = developerRepoInfos.get(developer).size();
            dev.put("involvedRepoCount",involvedRepoCount);
            DeveloperPortrait developerPortrait = getDeveloperPortrait(query,developerRepoInfos);
            double totalLevel = developerPortrait.getLevel();
            double value = developerPortrait.getValue();
            double quality = developerPortrait.getQuality();
            double efficiency = developerPortrait.getEfficiency();
            dev.put("totalLevel",totalLevel);
            dev.put("value",value);
            dev.put("quality",quality);
            dev.put("efficiency",efficiency);
            result.add(dev);
        }
        log.info(methodMeasureAspect.toString());
        return result;
    }


    @SuppressWarnings("unchecked")
    @Cacheable(cacheNames = {"commitStandard"})
    public List<DeveloperCommitStandard> getCommitStandard(Query query , List<String> developers) {
        List<DeveloperCommitStandard> developerCommitStandardList = new ArrayList<>();
        List<String> developerList = new ArrayList<>();
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
            developerCommitStandard.setDeveloperInvalidCommitInfo(developerInvalidCommitInfo);
            double commitStandard = developerCommitStandard.getDeveloperJiraCommitCount() * 1.0 / developerCommitStandard.getDeveloperValidCommitCount();
            developerCommitStandard.setCommitStandard(commitStandard);
            developerCommitStandardList.add(developerCommitStandard);
        }
        return developerCommitStandardList;
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

    @CacheEvict(cacheNames = {"developerPortrait","developerMetricsNew","portraitCompetence","developerRecentNews","developerList","commitStandard"}, allEntries=true, beforeInvocation = true)
    public void clearCache() {
        log.info("Successfully clear redis cache in db6.");
    }

}