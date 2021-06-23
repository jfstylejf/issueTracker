package cn.edu.fudan.taskmanagement.service.impl;

import cn.edu.fudan.taskmanagement.JiraDao.JiraDao;
import cn.edu.fudan.taskmanagement.component.JiraAPI;
import cn.edu.fudan.taskmanagement.domain.*;
import cn.edu.fudan.taskmanagement.domain.taskinfo.Fields;
import cn.edu.fudan.taskmanagement.mapper.JiraMapper;
import cn.edu.fudan.taskmanagement.mapper.RepoCommitMapper;
import cn.edu.fudan.taskmanagement.service.JiraService;
import cn.edu.fudan.taskmanagement.util.JiraUtil;
import cn.edu.fudan.taskmanagement.domain.Task;
import cn.edu.fudan.taskmanagement.util.StringUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JiraServiceImpl implements JiraService {

    @Autowired
    private JiraDao jiraDao;
    @Autowired
    private JiraAPI jiraAPI;
    @Autowired
    private JiraMapper jiraMapper;
    @Autowired
    private JiraUtil jiraUtil;
    @Autowired
    private RepoCommitMapper repoCommitMapper;
    @Autowired
    private StringUtil stringUtil;
    // jira raw data transfer
    @Override
    public List<Task> getTaskInfoByJql(String type, String keyword) {
        String jql = type + "=" + keyword;
        JSONObject jsonObject = jiraAPI.getTaskByJql(jql);
        return jsonObject.getJSONArray("issues").toJavaList(Task.class);
    }

    //get commit message
    public List<Map<String, String>> getCommitMsgByCondition(String repoUuid, String developer, String since, String until) {
        List<Map<String, String>> commitMsgList = jiraMapper.getCommitMsgByCondition(repoUuid, developer, since, until);
        for (Map<String, String> map : commitMsgList) {
            //将数据库中timeStamp/dateTime类型转换成指定格式的字符串 map.get("commit_time") 这个就是数据库中dateTime类型
            String commitTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(map.get("commitTime"));
            map.put("commitTime", commitTime);
        }
        return commitMsgList;
    }

    //get Jira Basic message required by both jira_history and jira_current
    public List<JiraBasicMsg> initJiraBasicMsg(String repoUuid, String developer, String since, String until) {

        List<JiraBasicMsg> jiraBasicMsgList = new ArrayList<>();
        List<String> jiraUuidDistinct = new ArrayList<>();
        List<Map<String, String>> commitMsgList = getCommitMsgByCondition(repoUuid, developer, since, until);
        //avoid NullPointerException
        if (commitMsgList.isEmpty()){
            return jiraBasicMsgList;
        }
        for (Map<String, String> commitMsgMap : commitMsgList) {
            String commitMessage = commitMsgMap.get("message");
            String jiraUuid = jiraUtil.getJiraIdFromCommitMsg(commitMessage);
            if (!"noJiraUuid".equals(jiraUuid) && !jiraUuidDistinct.contains(jiraUuid)) {
                jiraUuidDistinct.add(jiraUuid);
                //这里没有判空，因为一定会有返回值，以后需求改变再优化
                JiraBasicMsg jiraBasicMsg = new JiraBasicMsg(jiraUuid, commitMsgMap.get("commitUuid"), commitMsgMap.get("repoUuid"), commitMsgMap.get("developer"), commitMsgMap.get("commitTime"));
                jiraBasicMsgList.add(jiraBasicMsg);
            }
        }
        return jiraBasicMsgList;
    }

    //规范吗？
    static final String DEFAULT_TYPE = "others";
    static final String DEFAULT_NAME = "";
    static final String DEFAULT_MEESAGE = "No Data";

    public List<JiraMsg> initJiraMsg(String repoUuid, String developer, String since, String until) {

        String assignee = DEFAULT_NAME;
        String summary = DEFAULT_NAME;
        String issueType = DEFAULT_TYPE;
        String status = DEFAULT_NAME;
        String createdTime = DEFAULT_MEESAGE;
        String dueDate = DEFAULT_MEESAGE;
        String workLoad = DEFAULT_MEESAGE;
        String priority = DEFAULT_MEESAGE;

        List<JiraMsg> result = new ArrayList<>();
        List<JiraMsg> jiraEmptyMsg = new ArrayList<>();
        List<JiraBasicMsg> jiraBasicMsgList = initJiraBasicMsg(repoUuid, developer, since, until);

        for (JiraBasicMsg jiraBasicMsg : jiraBasicMsgList) {
            List<Task> jiraTaskInfo = getTaskInfoByJql("key", jiraBasicMsg.getJiraUuid());

            Fields fields;
            //A test in case of avoiding nullPointerException, ugly...
            if (jiraTaskInfo.isEmpty() || jiraTaskInfo.get(0) == null || jiraTaskInfo.get(0).getFields() == null){
                return jiraEmptyMsg;
            }
            else{
                fields = jiraTaskInfo.get(0).getFields();
            }

            if (fields.getSummary() != null) {
                summary = fields.getSummary();
            }
            if (fields.getIssuetype().getName() != null) {
                issueType = fields.getIssuetype().getName();
            }
            String uniqueName = jiraMapper.getUniqueName(developer);

            String str = fields.getCreated();
            if (str != null) {
                String[] stringResult = str.split("[T.]");
                createdTime = stringResult[0] + " " + stringResult[1];
            }

            if (fields.getStatus() != null) {
                status = fields.getStatus().getName();
            }

            if (fields.getPriority() != null) {
                priority = fields.getPriority().getName();
            }

            if (fields.getDueDate() != null) {
                dueDate = fields.getDueDate();
            }

            if (fields.getWorkLoad() != null) {
                workLoad = fields.getWorkLoad();
            }

            if (fields.getAssignee() != null) {
                assignee = fields.getAssignee();
            }
            JiraMsg jiraMsg = new JiraMsg(developer, assignee, uniqueName, null, createdTime, summary, issueType, status, status, jiraBasicMsg.getCommitUuid(), jiraBasicMsg.getCommitTime(), jiraBasicMsg.getRepoUuid(), dueDate, workLoad, priority);
            result.add(jiraMsg);
        }
        return result;
    }

    @Override
    public void insertJiraMsg(String repoUuid, String developer, String since, String until) {

        List<JiraMsg> jiraMsg = initJiraMsg(repoUuid, developer, since, until);
        jiraMapper.insertCurrentJiraMsg(jiraMsg);
        jiraMapper.insertHistoryJiraMsg(jiraMsg);
    }


    //-----------------------------------------------------------------------------------------

    ToDoJiraMsg getToDoJiraMsg(String jiraUuid) {

        ToDoJiraMsg toDoJiraMsg = new ToDoJiraMsg();
        if (jiraMapper.getJiraMsgFromCurrentDatabase(jiraUuid)==null){
            return toDoJiraMsg;
        }

        JiraMsg jiraCurrentDetail = jiraMapper.getJiraMsgFromCurrentDatabase(jiraUuid);
        List<JiraDetail> jiraDetailList = jiraMapper.getJiraDetailFromHistoryDatabase(jiraUuid);

        toDoJiraMsg.setJiraUuid(jiraUuid);
        toDoJiraMsg.setCreatedTime(jiraCurrentDetail.getCreatedTime());
        toDoJiraMsg.setSummary(jiraCurrentDetail.getSummary());
        toDoJiraMsg.setIssueType(jiraCurrentDetail.getIssueType());
        toDoJiraMsg.setJiraDetails(jiraDetailList);
        return toDoJiraMsg;
    }

    @Override
    public Map<String, Object> getDeveloperMsg(String repoUuidPara, String developer, String since, String until) throws ParseException {

        //get jira uuid list from database
        List<String> jiraUuidList = jiraUtil.splitJiraUuid(jiraUtil.splitRepoUuid(repoUuidPara,developer), developer, since, until);
        List<String> teamJiraUuidList = jiraUtil.splitJiraUuid(jiraUtil.splitRepoUuid(repoUuidPara,developer), null, since, until);
        List<String> finishedJiraUuidList = new ArrayList<>();
        List<String> unfinishedJiraUuidList = new ArrayList<>();
        List<String> finishedTeamJiraUuidList = new ArrayList<>();

        for (String jiraUuid : jiraUuidList) {
            JiraMsg jiraMsg = jiraMapper.getJiraMsgFromCurrentDatabase(jiraUuid);

            //Is empty?
            if (jiraMsg == null) {
                continue;
            }

            if ("done".equals(jiraMsg.getCurrentStatus())) {
                finishedJiraUuidList.add(jiraUuid);
            } else {
                unfinishedJiraUuidList.add(jiraUuid);
            }
        }

        int teamBugSum = 0;
        for (String jiraUuid : teamJiraUuidList) {
            TeamJiraMsg jiraMsg = jiraMapper.getTeamJiraMsgFromCurrentDatabase(jiraUuid);

            //Is empty?
            if (jiraMsg == null) {
                continue;
            }

            if ("done".equals(jiraMsg.getCurrentStatus())) {
                finishedTeamJiraUuidList.add(jiraUuid);
            }
            if ("bug".equals(jiraMsg.getIssueType())){
                teamBugSum++;
            }
        }

        JiraMeasure toDoJiraMeasure = new JiraMeasure(developer, unfinishedJiraUuidList, true, jiraMapper);
        JiraMeasure finishedJiraMeasure = new JiraMeasure(developer, finishedJiraUuidList, false, jiraMapper);

        Map<String, Object> commitPerJira = getCommitPerJiraMap(finishedJiraMeasure);
        Map<String, Object> completionRate = getCompletionRateMap(jiraUuidList, finishedJiraUuidList);
        Map<String, Object> timeSpan = getTimeSpanMap(unfinishedJiraUuidList, toDoJiraMeasure);
        Map<String, Object> differentTypeSum = getDifferentTypeSumMap(developer, since, until, toDoJiraMeasure, finishedJiraMeasure);
        Map<String, Object> defectRate = getDefectRateMap(teamBugSum, toDoJiraMeasure, finishedJiraMeasure);
        Map<String, Object> assignedJiraRate = getAssignedJiraRateMap(jiraUuidList, teamJiraUuidList, finishedJiraUuidList, finishedTeamJiraUuidList);

        List<ToDoJiraMsg> toDoJiraMsgs = new ArrayList<>();
        if (!unfinishedJiraUuidList.isEmpty()) {
            for (String jiraUuid : unfinishedJiraUuidList) {
                toDoJiraMsgs.add(getToDoJiraMsg(jiraUuid));
            }
        }
        return getStringObjectMap(commitPerJira, completionRate, timeSpan, differentTypeSum, defectRate, assignedJiraRate, toDoJiraMsgs);
    }

    private Map<String, Object> getStringObjectMap(Map<String, Object> commitPerJira, Map<String, Object> completionRate, Map<String, Object> timeSpan, Map<String, Object> differentTypeSum, Map<String, Object> defectRate, Map<String, Object> assignedJiraRate, List<ToDoJiraMsg> toDoJiraMsgs) {
        //result
        Map<String, Object> result = new HashMap<>();
        result.put("commitPerJira", commitPerJira);
        result.put("completionRate", completionRate);
        result.put("timeSpan", timeSpan);
        result.put("differentTypeSum", differentTypeSum);
        result.put("defectRate", defectRate);
        result.put("assignedJiraRate", assignedJiraRate);
        result.put("toDoJiraMessage", toDoJiraMsgs);
        return result;
    }

    private Map<String, Object> getAssignedJiraRateMap(List<String> jiraUuidList, List<String> teamJiraUuidList, List<String> finishedJiraUuidList, List<String> finishedTeamJiraUuidList) {
        double individualRate = 0;
        if (!finishedTeamJiraUuidList.isEmpty()){
            individualRate = jiraUuidList.size() * 1.0 / teamJiraUuidList.size();
        }
        double solvedRate = 0;
        if (!finishedTeamJiraUuidList.isEmpty()) {
            solvedRate = finishedJiraUuidList.size() * 1.0 / finishedTeamJiraUuidList.size();
        }
        Map<String, Object> assignedJiraRate = new HashMap<>();
        assignedJiraRate.put("individualJiraSum", jiraUuidList.size());
        assignedJiraRate.put("teamJiraSum", teamJiraUuidList.size());
        assignedJiraRate.put("solvedIndividualJiraSum", finishedJiraUuidList.size());
        assignedJiraRate.put("solvedTeamJiraSum", finishedTeamJiraUuidList.size());
        assignedJiraRate.put("individualRate", individualRate);
        assignedJiraRate.put("solvedJiraRate", solvedRate);
        return assignedJiraRate;
    }

    private Map<String, Object> getDefectRateMap(int teamBugSum, JiraMeasure toDoJiraMeasure, JiraMeasure finishedJiraMeasure) {
        double rate;

        rate = 0;
        if (teamBugSum != 0) {
            rate = (finishedJiraMeasure.getBugSum() * 1.0 + toDoJiraMeasure.getBugSum() * 1.0) / teamBugSum;
        }
        Map<String, Object> defectRate = new HashMap<>();
        defectRate.put("individualBugSum", finishedJiraMeasure.getBugSum() + toDoJiraMeasure.getBugSum());
        defectRate.put("teamBugSum", teamBugSum);
        defectRate.put("rate", rate);
        return defectRate;
    }

    private Map<String, Object> getDifferentTypeSumMap(String developer, String since, String until, JiraMeasure toDoJiraMeasure, JiraMeasure finishedJiraMeasure) throws ParseException {

        double duration = getDuration(developer, since, until);
        double completedBugSumPerDay = 0;
        double completedTaskSumPerDay = 0;
        if (duration != 0){
            completedBugSumPerDay = finishedJiraMeasure.getBugSum() * 1.0 / duration;
        }
        if (duration != 0){
            completedTaskSumPerDay = finishedJiraMeasure.getTaskSum() * 1.0 / duration;
        }

        Map<String, Object> differentTypeSum = new HashMap<>();
        differentTypeSum.put("totalBugSum", finishedJiraMeasure.getBugSum() * 1.0 + toDoJiraMeasure.getBugSum() * 1.0);
        differentTypeSum.put("totalTaskSum", finishedJiraMeasure.getTaskSum() * 1.0 + toDoJiraMeasure.getTaskSum() * 1.0);
        differentTypeSum.put("completedBugSum", finishedJiraMeasure.getBugSum());
        differentTypeSum.put("completedTaskSum", finishedJiraMeasure.getTaskSum());
        differentTypeSum.put("duration", duration);
        differentTypeSum.put("completedBugSumPerDay", completedBugSumPerDay);
        differentTypeSum.put("completedTaskSumPerDay", completedTaskSumPerDay);
        return differentTypeSum;
    }

    private Map<String, Object> getTimeSpanMap(List<String> unfinishedJiraUuidList, JiraMeasure toDoJiraMeasure) {
        Map<String, Object> timeSpan = new HashMap<>();
        double averageTimeSpanCreatedSum = 0;
        double averageTimeSpanCommittedSum = 0;
        if (!unfinishedJiraUuidList.isEmpty()) {
            averageTimeSpanCreatedSum = toDoJiraMeasure.getTimeSpanCreatedSum() * 1.0 / unfinishedJiraUuidList.size();
            averageTimeSpanCommittedSum = toDoJiraMeasure.getTimeSpanCommittedSum() * 1.0 / unfinishedJiraUuidList.size();
        }
        timeSpan.put("unfinishedJiraSum", unfinishedJiraUuidList.size());
        timeSpan.put("timeSpanCreatedSum", toDoJiraMeasure.getTimeSpanCreatedSum());
        timeSpan.put("averageTimeSpanCreatedSum", averageTimeSpanCreatedSum);
        timeSpan.put("timeSpanCommittedSum", toDoJiraMeasure.getTimeSpanCommittedSum());
        timeSpan.put("averageTimeSpanCommittedSum", averageTimeSpanCommittedSum);
        return timeSpan;
    }

    private Map<String, Object> getCompletionRateMap(List<String> jiraUuidList, List<String> finishedJiraUuidList) {
        double rate;
        Map<String, Object> completionRate = new HashMap<>();
        rate = 0;
        if (!jiraUuidList.isEmpty()) {
            rate = finishedJiraUuidList.size() * 1.0 / jiraUuidList.size();
        }
        completionRate.put("completedJiraSum", finishedJiraUuidList.size());
        completionRate.put("jiraSoloSum", jiraUuidList.size());
        completionRate.put("completionRate", rate);
        return completionRate;
    }

    private Map<String, Object> getCommitPerJiraMap(JiraMeasure finishedJiraMeasure) {
        Map<String, Object> commitPerJira = new HashMap<>(8);
        double rate = 0;
        if (finishedJiraMeasure.getJiraSoloSum() != 0) {
            rate = finishedJiraMeasure.getCommitSum() * 1.0 / finishedJiraMeasure.getJiraSoloSum();
        }
        commitPerJira.put("finishedJiraSum", finishedJiraMeasure.getJiraSoloSum());
        commitPerJira.put("commitSum", finishedJiraMeasure.getCommitSum());
        commitPerJira.put("rate", rate);
        return commitPerJira;
    }

    private double getDuration(String developer, String since, String until) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = "2020-01-01";
        Date firstCommitCareer = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
        if(developer!=null) {
            firstCommitCareer = sdf.parse(jiraAPI.getFirstCommitDate(developer).getJSONObject("repos_summary").getString("first_commit_time_summary"));
        }
        Date today = new Date();
        double duration;
        if (since != null) {
            if (until != null) {
                duration = (sdf.parse(until).getTime() - sdf.parse(since).getTime()) * 1.0 / (1000 * 60 * 60 * 24) * 5 / 7;
            } else {
                duration = (today.getTime() - sdf2.parse(since).getTime()) * 1.0 / (1000 * 60 * 60 * 24) * 5 / 7;
            }
        } else {
            if (until != null) {
                duration = (sdf2.parse(until).getTime() - firstCommitCareer.getTime()) * 1.0 / (1000 * 60 * 60 * 24) * 5 / 7;
            } else {
                duration = (today.getTime() - firstCommitCareer.getTime()) * 1.0 / (1000 * 60 * 60 * 24) * 5 / 7;
            }
        }
        return duration;
    }

    @Override
    public List<JiraCount> getJiraCountList(String developers, String status, String projectId, String repoId, String start, String end, String token){
        List<String> projectList = getProjectIds(projectId, token);
        List<String> repoIdList = getRepoUuids(projectList, repoId);
        List<String> developerList = getDeveloperListByParam(developers);
        List<JiraCount> res = new ArrayList<>();
        for(String developer: developerList){
            int num = jiraDao.getJiraCount(developer, status, repoIdList, start, end);
            JiraCount jiraCount = new JiraCount(developer, num);
            res.add(jiraCount);
        }
        return res;
    }

    private List<String> getDeveloperListByParam(String developers) {
        List<String> developerList;
        if (!StringUtil.isEmpty(developers)) {
            developerList = split(developers);
        } else {
            developerList = repoCommitMapper.getDevelopers();
        }
        return developerList;
    }

    private List<String> getProjectIds(String projectId, String token) {
        List<String> projectIds = new ArrayList<>();
        if (StringUtils.isEmpty(projectId)) {
            List<Integer> temp = repoCommitMapper.getProjectIds();
            temp.forEach(a -> projectIds.add(a.toString()));
        } else {
            projectIds.addAll(Arrays.asList(projectId.split(",")));
        }
        List<Integer> projectsWithRightTemp = jiraUtil.getVisibleProjectByToken(token);
        List<String> projectsWithRight = new ArrayList<>();
        if (!StringUtils.isEmpty(projectsWithRightTemp)) {
            projectsWithRightTemp.forEach(a -> projectsWithRight.add(a.toString()));
        }
        return projectIds.stream().filter(projectsWithRight::contains).collect(Collectors.toList());
    }

    private List<String> getRepoUuids(List<String> projectIds, String repoUuid) {
        List<String> repoUuids = new ArrayList<>();
        if (projectIds.isEmpty()) {
            List<Integer> temp = repoCommitMapper.getProjectIds();
            temp.forEach(a -> projectIds.add(a.toString()));
        }
        for (String projectId : projectIds) {
            repoUuids.addAll(repoCommitMapper.getRepoIdByProjectId(projectId));
        }
        if (!StringUtils.isEmpty(repoUuid)) {
            repoUuids.retainAll(Arrays.asList(repoUuid.split(",")));
        }
        return repoUuids;
    }

    List<String> split(String repositoryId) {
        List<String> repoIds = new ArrayList<>();
        String trim = ",";
        String[] targetRepos = new String[0];

        if (repositoryId.contains(trim)) {
            targetRepos = repositoryId.split(trim);
            repositoryId = null;
        }
        repoIds.add(repositoryId);

        if (targetRepos.length != 0) {
            repoIds = Arrays.asList(targetRepos);
        }
        return repoIds;
    }
}