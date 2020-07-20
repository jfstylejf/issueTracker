package cn.edu.fudan.measureservice.service;

import cn.edu.fudan.measureservice.component.RestInterfaceManager;
import cn.edu.fudan.measureservice.domain.*;
import cn.edu.fudan.measureservice.mapper.RepoMeasureMapper;
import cn.edu.fudan.measureservice.portrait.*;
import cn.edu.fudan.measureservice.util.DateTimeUtil;
import cn.edu.fudan.measureservice.util.GitUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.Console;
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
public class MeasureDeveloperServiceImpl implements MeasureDeveloperService {

    private Logger logger = LoggerFactory.getLogger(MeasureDeveloperServiceImpl.class);

    @Value("${repoHome}")
    private String repoHome;
    @Value("${inactive}")
    private int inactive;
    @Value("${lessActive}")
    private int lessActive;
    @Value("${relativelyActive}")
    private int relativelyActive;


    private RestInterfaceManager restInterfaceManager;
    private RepoMeasureMapper repoMeasureMapper;
    private GitUtil gitUtil;


    public MeasureDeveloperServiceImpl(RestInterfaceManager restInterfaceManager, RepoMeasureMapper repoMeasureMapper, GitUtil gitUtil) {
        this.restInterfaceManager = restInterfaceManager;
        this.repoMeasureMapper = repoMeasureMapper;
        this.gitUtil = gitUtil;
    }

    @Override
    public int getCommitCountsByDuration(String repo_id, String since, String until) {
        if (since.compareTo(until)>0 || since.length()>10 || until.length()>10){
            throw new RuntimeException("please input correct date");
        }
        String sinceday = dateFormatChange(since);
        String untilday = dateFormatChange(until);

        return repoMeasureMapper.getCommitCountsByDuration(repo_id, sinceday, untilday,null);
    }

    //把日期格式从“2010.10.10转化为2010-10-10”
    private String dateFormatChange(String dateStr){
        return dateStr.replace('.','-');
    }

    @Override
    public String getActivityByRepoId(String repo_id) {
        String active = null;
        LocalDate nowDate = LocalDate.now();
        int nowYear = nowDate.getYear();
        int nowMonth = nowDate.getMonthValue();
        int nowDay = nowDate.getDayOfMonth();
        LocalDate startDate = nowDate.minusDays(90);
        int startYear = startDate.getYear();
        int startMonth = startDate.getMonthValue();
        int startDay = startDate.getDayOfMonth();
        String startMonthStr = String.valueOf(startMonth);
        String startDayStr = String.valueOf(startDay);
        String nowMonthStr = String.valueOf(nowMonth);
        String nowDayStr = String.valueOf(nowDay);
        if (startMonth<10){
            startMonthStr = "0" + startMonth;
        }
        if (startDay<10){
            startDayStr = "0" + startDay;
        }
        if (nowMonth<10){
            nowMonthStr = "0" + nowMonth;
        }
        if (nowDay<10){
            nowDayStr = "0" + nowDay;
        }

        String start = startYear+"."+startMonthStr+"."+startDayStr;
        String now = nowYear+"."+nowMonthStr+"."+nowDayStr;
//        System.out.println(start);
//        System.out.println(now);
        int commitCount = getCommitCountsByDuration(repo_id,start,now);
        if(commitCount != -1){
            if(commitCount <= inactive){
                active = "inactive";
            }else if(commitCount <= lessActive){
                active = "lessActive";
            }else if(commitCount <= relativelyActive){
                active = "relativelyActive";
            }else{
                active = "active";
            }
        }
        return active;
    }

    @Override
    public Object getCodeChangesByDurationAndDeveloperName(String developer_name, String since, String until, String token, String category,String repoId) {
        CommitBase commitBase = new CommitBase();
        int lineAdds = 0;
        int lineDels = 0;
        if(repoId == null || repoId.isEmpty()){
            if(category == null || category.isEmpty()){
                return " The category should not be null when  repo id is null";
            }
            List<JSONObject> projectList = restInterfaceManager.getProjectListByCategory(token,category);
            for(Object project:projectList){
                JSONObject protectJson = (JSONObject)project;
                if(protectJson.get("download_status").toString().equals("Downloading")){
                    continue;
                }
                String eachRepoId = protectJson.get("repo_id").toString();
                List<RepoMeasure> repoMeasures = repoMeasureMapper.getRepoMeasureByDeveloperAndRepoId(eachRepoId,developer_name,0,since,until);
                for(RepoMeasure repoMeasure : repoMeasures){
                    lineAdds += repoMeasure.getAdd_lines();
                    lineDels += repoMeasure.getDel_lines();
                }

//                try{
//                    repoPath=restInterfaceManager.getRepoPath(repo_id,"");
//                    if(repoPath!=null){
//
//                        //获取repo一段时间内行数变化值
//                        lineChanges = gitUtil.getRepoLineChanges(repoPath,since,until,developer_name);
//                        lineAdds += lineChanges[0];
//                        lineDels += lineChanges[1];
//
//                    }
//                }finally {
//                    if(repoPath!=null) {
//                        restInterfaceManager.freeRepoPath(repo_id,repoPath);
//                    }
//                }

            }
        }else{
            List<RepoMeasure> repoMeasures = repoMeasureMapper.getRepoMeasureByDeveloperAndRepoId(repoId,developer_name,0,since,until);
            for(RepoMeasure repoMeasure : repoMeasures){
                lineAdds += repoMeasure.getAdd_lines();
                lineDels += repoMeasure.getDel_lines();
            }
        }

        commitBase.setAddLines(lineAdds);
        commitBase.setDelLines(lineDels);

        return commitBase;
    }

    @Override
    public int getCommitCountByDurationAndDeveloperName(String developerName, String since, String until, String token, String category,String repoId) {
        int commitTotalCount = 0;
        if(repoId != null && !repoId.isEmpty()){
            return repoMeasureMapper.getCommitCountsByDuration(repoId,since,until,developerName);
        }else{
            List<JSONObject> projectList = restInterfaceManager.getProjectListByCategory(token,category);
            for(Object project:projectList){
                JSONObject protectJson = (JSONObject)project;
                if(protectJson.get("download_status").toString().equals("Downloading")){
                    continue;
                }
                String repo_id = protectJson.get("repo_id").toString();

                int commitCount = repoMeasureMapper.getCommitCountsByDuration(repo_id,since,until,developerName);
                commitTotalCount += commitCount;

            }
        }

        return commitTotalCount;
    }

    @Override
    public Object getRepoListByDeveloperName(String developer_name, String token, String category) {
        String repoPath=null;
        // 这里配合脚本的判断分支，将since以及until设置为值为null的字符串；
        String since = "null";
        String until = "null";
        int commitCount ;
        Map<String,String> result = new HashMap<>();
        List<JSONObject> projectList = restInterfaceManager.getProjectListByCategory(token,category);
        if(projectList != null){
            for(Object project:projectList){
                JSONObject protectJson = (JSONObject)project;
                if(protectJson.get("download_status").toString().equals("Downloading")){
                    continue;
                }
                String repo_id = protectJson.get("repo_id").toString();
                String repo_name = protectJson.get("name").toString();
                try{
                    repoPath=restInterfaceManager.getRepoPath(repo_id,"");
                    if(repoPath!=null){
                        commitCount = gitUtil.getCommitCount(repoPath,since,until,developer_name);
                        if(commitCount != 0){
                            String active = getActivityByRepoId(repo_id);
                            result.put(repo_name,active);
                        }
                    }
                }finally {
                    if(repoPath!=null) {
                        restInterfaceManager.freeRepoPath(repo_id,repoPath);
                    }
                }
            }
        }

        return result;
    }

    @Override
    public Object getQualityChangesByDeveloperName(String developer_name, String token, String category, int counts, String project_name) {
        String spaceType= "project";
        //判断count是否合法
        if(counts<0){
            throw new RuntimeException("counts can not be  negative");
        }

        List<RepoMeasure> repoMeasures;
        //如果projectName的名字为null则表示用户在所有project列表中最近30次commit的代码质量信息
        if(project_name==null){
            repoMeasures = repoMeasureMapper.getRepoMeasureByDeveloperAndRepoId(null,developer_name,counts,null,null);
        }else{
            JSONObject result = restInterfaceManager.getProjectListByCondition(token,category,project_name,null);
            int code =result.getIntValue("code");
            if(code != 200){
                logger.error("request project api failed  ---> {}",project_name);
                throw new RuntimeException("request project api failed  ---> "+ project_name);
            }
            JSONArray projects = result.getJSONArray("data");
            if(projects.size() == 0){
                logger.info("do not have this project --> {}",project_name );
                throw new RuntimeException("do not have this project --> "+project_name);
            }
            if (projects.size() > 1){
                // 这一段的方法待优化 可以在多个project中取最近counts 次数的commits的度量
                logger.info("more than one project named --> {}",project_name );
                throw new RuntimeException("more than one project named --> "+project_name);
            }
            JSONObject project =  projects.getJSONObject(0);
            String repoId = project.getString("repo_id");
            repoMeasures = repoMeasureMapper.getRepoMeasureByDeveloperAndRepoId(repoId,developer_name,counts,null,null);

        }


        List<CodeQuality> queries = new ArrayList<>();

        CodeQuality codeQuality;
        String projectName = null;
        for(RepoMeasure repoMeasure :repoMeasures){
            int newIssueCounts = restInterfaceManager.getNumberOfNewIssueByCommit(repoMeasure.getRepo_id(),repoMeasure.getCommit_id(),category,spaceType,token);
            codeQuality = new CodeQuality(repoMeasure.getAdd_lines(),repoMeasure.getDel_lines(),newIssueCounts,repoMeasure.getCommit_time());
            JSONArray projects = restInterfaceManager.getProjectsOfRepo(repoMeasure.getRepo_id());
            if(projects.size()==0){
                logger.error("can not find project by repo_id --->{}",repoMeasure.getRepo_id());
            }else {
                projectName = projects.getJSONObject(0).getString("name");
            }
            codeQuality.setCommitId(repoMeasure.getCommit_id());
            codeQuality.setProjectName(projectName);
            if(newIssueCounts == -1){
                logger.info("this commit --> {} can not be compiled",repoMeasure.getCommit_id());
                codeQuality.setExpression("this commit can not be compiled!");
            }

            queries.add(codeQuality);
        }

        return queries;
    }

    @Override
    public Object getDeveloperActivenessByDuration(String repo_id, String since, String until, String developer_name) {
        since = dateFormatChange(since);
        until = dateFormatChange(until);
        List<Map<String, Object>> result = new ArrayList<>();

        LocalDate indexDay = LocalDate.parse(since,DateTimeUtil.Y_M_D_formatter);
        LocalDate untilDay = LocalDate.parse(until,DateTimeUtil.Y_M_D_formatter);
        while(untilDay.isAfter(indexDay) || untilDay.isEqual(indexDay)){
            Map<String, Object> map = new HashMap<>();

            List<CommitInfoDeveloper> CommitInfoDeveloper = repoMeasureMapper.getCommitInfoDeveloperListByDuration(repo_id, indexDay.toString(), indexDay.toString(), developer_name);
            if (CommitInfoDeveloper.size()==1){
                int add = CommitInfoDeveloper.get(0).getAdd();
                int del = CommitInfoDeveloper.get(0).getDel();
                //还差缺陷数量，计算E/L N/L
//                int newIssues = 0;
//                int elliminateIssues = 0;

//                double newIssuesQuality = newIssues*100.0 / (add+del);
//                double elliminateIssuesQuality = elliminateIssues*100.0 / (add+del);

                map.put("commit_date", indexDay.toString());
                map.put("add", add);
                map.put("del", del);
//                map.put("E/L", newIssuesQuality);
                result.add(map);
            }

            indexDay = indexDay.plusDays(1);
        }
        return result;

    }

    @Override
    public Object getDeveloperActivenessByGranularity(String repo_id, String granularity, String developer_name) {
        //获取查询时的日期 也就是今天的日期,也作为查询时间段中的截止日期
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> result = new ArrayList<>();


        //此为“week”的情况
        LocalDate sinceDay = today.minusWeeks(1);
        String since = sinceDay.toString();
        String until = today.toString();

        switch (granularity){
            case "week":
                return getDeveloperActivenessByDuration(repo_id, since, until, developer_name);
            case "month":
                sinceDay = today.minusMonths(1);
                since = sinceDay.toString();
                return getDeveloperActivenessByDuration(repo_id, since, until, developer_name);
            case "year":
                sinceDay = today.minusYears(1);
                LocalDate indexDay = sinceDay;
                while(today.isAfter(indexDay)){
                    Map<String, Object> map = new HashMap<>();
                    List<CommitInfoDeveloper> list = repoMeasureMapper.getCommitInfoDeveloperListByDuration(repo_id, indexDay.toString(), indexDay.plusMonths(1).toString(), developer_name);

                    if (list.size()>0){
                        int add = list.get(0).getAdd();
                        int del = list.get(0).getDel();
                        map.put("commit_date", indexDay.toString().substring(0,7));
                        map.put("add", add);
                        map.put("del", del);
                        result.add(map);
                    }

                    //indexDay 变成indexDay这天的下个月的这一天
                    indexDay = indexDay.plusMonths(1);
                }
                break;
            default:
                throw new RuntimeException("please input correct granularity type: week or month or year");
        }
        return result;

    }

    @Override
    public Object getLOCByCondition(String repoId, String developer, String beginDate, String endDate, String type) {
        int totalLOC = repoMeasureMapper.getLOCByCondition(repoId,developer,beginDate,endDate);
        if (type.equals("total")){
            return totalLOC;
        }
        if (type.equals("dayAverage")){
            List<Map<String, Object>> commitDays = repoMeasureMapper.getCommitDays(repoId,developer,beginDate,endDate);
            int days = commitDays.size();
            return totalLOC/days;
        }
        return ("please input correct type: total or dayAverage !");
    }


    @Override
    public DeveloperMetrics getPortrait(String repoId, String developer, String beginDate, String endDate, String token, String tool) throws ParseException {
        if ("".equals(beginDate) || beginDate == null){
            beginDate = repoMeasureMapper.getFirstCommitDateByCondition(repoId,null);
        }
        if ("".equals(endDate) || endDate == null){
            LocalDate today = LocalDate.now();
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            endDate = df.format(today);
        }
        List<Map<String, Object>> developerList = repoMeasureMapper.getDeveloperListByRepoId(repoId);
        int developerNumber = developerList.size();
        JSONArray projects = restInterfaceManager.getProjectsOfRepo(repoId);
        String branch = projects.getJSONObject(0).getString("branch");
        String repoName = projects.getJSONObject(0).getString("name");

        //获取程序员在本项目中第一次提交commit的日期
        LocalDateTime firstCommitDateTime;
        LocalDate firstCommitDate = null;
        JSONObject firstCommitDateData = restInterfaceManager.getFirstCommitDate(developer);
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

        //----------------------------------开发效率相关指标-------------------------------------
        Efficiency efficiency = getDeveloperEfficiency(repoId, beginDate, endDate, developer, branch, developerNumber);
        int totalLOC = efficiency.getTotalLOC();
        int developerAddStatement = efficiency.getDeveloperAddStatement();
        int totalAddStatement = efficiency.getTotalAddStatement();
        int developerValidLine = efficiency.getDeveloperValidLine();
        int totalValidLine = efficiency.getTotalValidLine();
        int developerLOC = efficiency.getDeveloperLOC();
        int developerCommitCount = efficiency.getDeveloperCommitCount();

        //----------------------------------代码质量相关指标-------------------------------------
        Quality quality = getDeveloperQuality(repoId,developer,beginDate,endDate,tool,token,developerNumber,totalLOC);

        //----------------------------------开发能力相关指标-------------------------------------
        Competence competence = getDeveloperCompetence(repoId,beginDate,endDate,developer,developerNumber,developerAddStatement,totalAddStatement,developerValidLine,totalValidLine);

        return new DeveloperMetrics(firstCommitDate, developerLOC, developerCommitCount, repoName, developer, efficiency, quality, competence);
    }

    private Efficiency getDeveloperEfficiency(String repoId,String beginDate, String endDate, String developer, String branch,
                                              int developerNumber){
        //提交频率指标
        int totalCommitCount = repoMeasureMapper.getCommitCountsByDuration(repoId, beginDate, endDate,null);
        int developerCommitCount = repoMeasureMapper.getCommitCountsByDuration(repoId, beginDate, endDate, developer);
        //代码量指标
        int developerLOC = repoMeasureMapper.getRepoLOCByDuration(repoId, beginDate, endDate, developer);
        int totalLOC = repoMeasureMapper.getRepoLOCByDuration(repoId, beginDate, endDate, "");
        //获取代码新增、删除逻辑行数数据
        JSONObject statements = restInterfaceManager.getStatements(repoId, beginDate, endDate, branch);
        int developerAddStatement = 0;
        int totalAddStatement = 0;
        int developerDelStatement = 0;
        int totalDelStatement = 0;
        for(String str:statements.keySet()){
            if (str.equals(developer)){
                developerAddStatement = statements.getJSONObject(str).getIntValue("ADD");
                developerDelStatement = statements.getJSONObject(str).getIntValue("DELETE");
            }
            totalAddStatement += statements.getJSONObject(str).getIntValue("ADD");
            totalDelStatement += statements.getJSONObject(str).getIntValue("DELETE");
        }
        JSONObject validLines = restInterfaceManager.getValidLine(repoId, beginDate, endDate, branch);
        int developerValidLine = 0;
        int totalValidLine = 0;
        for(String key:validLines.keySet()){
            if (key.equals(developer)){
                developerValidLine = validLines.getIntValue(key);
            }
            totalValidLine += validLines.getIntValue(key);
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
        for (int i = 0; i < issueList.size(); i++){
            JSONObject each = issueList.getJSONObject(i);
            String developerName = each.getString("developer");
            int newIssueCount = each.getIntValue("newIssueCount");
            if (developer.equals(developerName)){
                developerNewIssueCount = newIssueCount;
            }
            totalNewIssueCount += newIssueCount;
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
        JSONObject focusMeasure = restInterfaceManager.getFocusFilesCount(repoId, beginDate, endDate);
        int totalChangedFile = 0;
        int developerFocusFile = 0;
        if (focusMeasure != null){
            totalChangedFile = focusMeasure.getIntValue("total");
            developerFocusFile = focusMeasure.getJSONObject("developer").getIntValue(developer);
        }

        JSONObject changedCodeInfo = restInterfaceManager.getChangedCodeAge(repoId, beginDate, endDate, developer);
        double changedCodeAVGAge = 0;
        int changedCodeMAXAge = 0;
        if (changedCodeInfo != null){
            changedCodeAVGAge = changedCodeInfo.getDoubleValue("average");
            changedCodeMAXAge = changedCodeInfo.getIntValue("max");
        }

        JSONObject deletedCodeInfo = restInterfaceManager.getDeletedCodeAge(repoId, beginDate, endDate, developer);
        double deletedCodeAVGAge = 0;
        int deletedCodeMAXAge = 0;
        if (deletedCodeInfo != null){
            deletedCodeAVGAge = deletedCodeInfo.getDoubleValue("average");
            deletedCodeMAXAge = deletedCodeInfo.getIntValue("max");
        }
        int repoAge = restInterfaceManager.getRepoAge(repoId, endDate);

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

    @Override
    public Object getPortraitLevel(String developer, String token) throws ParseException {
        //获取developerMetricsList
        List<String> repoList = repoMeasureMapper.getRepoListByDeveloper(developer);
        List<DeveloperMetrics> developerMetricsList = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String endDate = df.format(today);
        for (String repoId : repoList) {
            JSONArray projects = restInterfaceManager.getProjectsOfRepo(repoId);
            for (int i = 0; i < projects.size(); i++){
                String tool = projects.getJSONObject(i).getString("type");
                String repoName = projects.getJSONObject(i).getString("name");
                logger.info("Current repo is : " + repoName + ", the issue_scan_type is " + tool);
                //只添加被sonarqube扫描过的项目，findbugs之后会逐渐被废弃
                if ("sonarqube".equals(tool)){
                    String beginDate = repoMeasureMapper.getFirstCommitDateByCondition(repoId,null);
//                    String endDate = repoMeasureMapper.getLastCommitDateOfOneRepo(repoId);
                    logger.info("Start to get portrait of " + developer + " in repo : " + repoName);
                    DeveloperMetrics metrics = getPortrait(repoId, developer, beginDate, endDate, token, tool);
                    developerMetricsList.add(metrics);
                    logger.info("Successfully get portrait of " + developer + " in repo : " + repoName);
                }
            }
        }
        logger.info("Get portrait of " + developer + " complete!" );
        //获取第一次提交commit的日期
        LocalDateTime firstCommitDateTime;
        LocalDate firstCommitDate = null;
        JSONObject firstCommitDateData = restInterfaceManager.getFirstCommitDate(developer);
        JSONObject repoDateList = firstCommitDateData.getJSONObject("repos_summary");
        String dateString = repoDateList.getString("first_commit_time_summary");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        firstCommitDateTime = LocalDateTime.parse(dateString, fmt);
        firstCommitDateTime = firstCommitDateTime.plusHours(8);
        firstCommitDate = firstCommitDateTime.toLocalDate();

        int totalCommitCount = repoMeasureMapper.getCommitCountsByDuration(null, null, null, developer);
        int totalLOC = repoMeasureMapper.getLOCByCondition(null,developer,null,null);
        List<Map<String, Object>> commitDays = repoMeasureMapper.getCommitDays(null,developer,null,null);
        int days = commitDays.size();
        int dayAverageLOC = totalLOC/days;
        //todo 日后需要添加程序员类型接口 目前统一认为是java后端工程师
        String developerType = "Java后端工程师";
        return new DeveloperPortrait(firstCommitDate,totalLOC,dayAverageLOC,totalCommitCount,developer,developerType,developerMetricsList);
    }

    @Override
    public Object getCommitMsgByCondition(String repoId, String developer, String beginDate, String endDate) {
        List<Map<String, Object>> commitMsgList = repoMeasureMapper.getCommitMsgByCondition(repoId, developer, beginDate, endDate);
        for (Map<String, Object> map : commitMsgList) {
            //将数据库中timeStamp/dateTime类型转换成指定格式的字符串 map.get("commit_time") 这个就是数据库中dateTime类型
            String commit_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(map.get("commit_time"));
            map.put("commit_time", commit_time);
        }
        return commitMsgList;
    }

    @Override
    public Object getJiraMeasureInfo(String repoId, String developer, String beginDate, String endDate) {
        if ("".equals(beginDate) || beginDate == null){
            beginDate = repoMeasureMapper.getFirstCommitDateByCondition(repoId,developer);
        }
        if ("".equals(endDate) || endDate == null){
//            LocalDate today = LocalDate.now();
//            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//            endDate = df.format(today);
            endDate = repoMeasureMapper.getLastCommitDateOfOneRepo(repoId,developer);
        }
        List<String> repoList = new ArrayList<>();
        if ("".equals(repoId) || repoId == null){
            repoList = repoMeasureMapper.getRepoListByDeveloper(developer);
        }else {
            repoList.add(repoId);
        }

        // 遍历所有repo，进行jira度量统计
        for (String repo : repoList){
            // 0. 获取开发者完成的jira任务列表 即jira的ID列表

            // 1. 获取完成jira任务需要的commit数量

            // 2. 获取这些commit的工作日天数

        }


        return null;
    }

    @Override
    public Object getDeveloperRecentNews(String repoId, String developer, String beginDate, String endDate) {
        List<Map<String, Object>> commitMsgList = repoMeasureMapper.getCommitMsgByCondition(repoId, developer, beginDate, endDate);
        for (Map<String, Object> map : commitMsgList) {
            //将数据库中timeStamp/dateTime类型转换成指定格式的字符串 map.get("commit_time") 这个就是数据库中dateTime类型
            String commit_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(map.get("commit_time"));
            map.put("commit_time", commit_time);
            //以下操作是为了获取jira信息
            String commitMessage = map.get("commit_message").toString();
            String jiraID = getJiraIDFromCommitMsg(commitMessage);
            JSONArray jiraResponse = restInterfaceManager.getJiraInfoByKey("key",jiraID);
            if (jiraResponse == null || jiraResponse.isEmpty()){
                map.put("jira_info", "本次commit不含jira单号");
            }else {
                map.put("jira_info", jiraResponse.get(0));
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
            logger.info("jira ID is : {}",matcher.group());
            return matcher.group();
        }
        return "noJiraID" ;
    }

}
