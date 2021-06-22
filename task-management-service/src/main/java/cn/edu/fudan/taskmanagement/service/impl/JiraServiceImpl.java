package cn.edu.fudan.taskmanagement.service.impl;

import cn.edu.fudan.taskmanagement.component.JiraAPI;
import cn.edu.fudan.taskmanagement.domain.*;
import cn.edu.fudan.taskmanagement.domain.taskinfo.Fields;
import cn.edu.fudan.taskmanagement.mapper.JiraMapper;
import cn.edu.fudan.taskmanagement.service.JiraService;
import cn.edu.fudan.taskmanagement.util.JiraUtil;
import cn.edu.fudan.taskmanagement.component.JiraAPI;
import cn.edu.fudan.taskmanagement.domain.Task;
import cn.edu.fudan.taskmanagement.mapper.JiraMapper;
import cn.edu.fudan.taskmanagement.util.JiraUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class JiraServiceImpl implements JiraService {

    @Autowired
    private JiraAPI jiraAPI;
    @Autowired
    private JiraMapper jiraMapper;
    @Autowired
    private JiraUtil jiraUtil;

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
        if (commitMsgList.isEmpty()) return jiraBasicMsgList;

        for (Map<String, String> commitMsgMap : commitMsgList) {

            String commitMessage = commitMsgMap.get("message");
            String jiraUuid = jiraUtil.getJiraIdFromCommitMsg(commitMessage);

            if (!"noJiraUuid".equals(jiraUuid) && !jiraUuidDistinct.contains(jiraUuid)) {

                jiraUuidDistinct.add(jiraUuid);

                //这里没有判空，因为一定会有返回值，以后需求改变再优化
                JiraBasicMsg jiraBasicMsg = JiraBasicMsg.builder()
                        .commitTime(commitMsgMap.get("commitTime"))
                        .commitUuid(commitMsgMap.get("commitUuid"))
                        .developer(commitMsgMap.get("developer"))
                        .repoUuid(commitMsgMap.get("repoUuid"))
                        .jiraUuid(jiraUuid)
                        .build();

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
            if (jiraTaskInfo.isEmpty() || jiraTaskInfo.get(0) == null || jiraTaskInfo.get(0).getFields() == null)
                return jiraEmptyMsg;
            else fields = jiraTaskInfo.get(0).getFields();

            if (fields.getSummary() != null)
                summary = fields.getSummary();

            if (fields.getIssuetype().getName() != null)
                issueType = fields.getIssuetype().getName();

            String uniqueName = jiraMapper.getUniqueName(developer);

            String str = fields.getCreated();
            if (str != null) {
                String[] stringResult = str.split("[T.]");
                createdTime = stringResult[0] + " " + stringResult[1];
            }

            if (fields.getStatus() != null) {
                status = fields
                        .getStatus()
                        .getName();
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

            JiraMsg jiraMsg = JiraMsg.builder()
                    .developer(developer)
                    .assignee(assignee)
                    .uniqueName(uniqueName)
                    .createdTime(createdTime)
                    .summary(summary)
                    .issueType(issueType)
                    .currentStatus(status)
                    .status(status)
                    .commitTime(jiraBasicMsg.getCommitTime())
                    .commitUuid(jiraBasicMsg.getCommitUuid())
                    .repoUuid(jiraBasicMsg.getRepoUuid())
                    .dueDate(dueDate)
                    .workLoad(workLoad)
                    .priority(priority)
                    .build();

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
        if (jiraMapper.getJiraMsgFromCurrentDatabase(jiraUuid)==null) return toDoJiraMsg;

        JiraMsg jiraCurrentDetail = jiraMapper.getJiraMsgFromCurrentDatabase(jiraUuid);
        List<JiraDetail> jiraDetailList = jiraMapper.getJiraDetailFromHistoryDatabase(jiraUuid);

        toDoJiraMsg.setJiraUuid(jiraUuid);
        toDoJiraMsg.setCreatedTime(jiraCurrentDetail.getCreatedTime());
        toDoJiraMsg.setSummary(jiraCurrentDetail.getSummary());
        toDoJiraMsg.setIssueType(jiraCurrentDetail.getIssueType());
        toDoJiraMsg.setJiraDetails(jiraDetailList);
        return toDoJiraMsg;
    }

//    Map<String, Object> getJiraMeasure(String developer, List<String> jiraUuidList) throws ParseException {
//        int bugSum = 0;
//        int taskSum = 0;
//        int commitSum = 0;
//        int jiraSoloSum = 0;
//        double timeSpanCreatedSum = 0;
//        double timeSpanCommittedSum = 0;
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        for (String jiraUuid : jiraUuidList) {
//            JiraMsg jiraMsg = jiraMapper.getJiraMsgFromCurrentDatabase(jiraUuid);
//
//            //Is empty?
//            if (jiraMsg == null) {
//                continue;
//            }
//
//            //获取总bug数或feature数
//            if ("bug".equals(jiraMsg.getIssueType()))
//                bugSum++;
//            else if ("task".equals(jiraMsg.getIssueType()))
//                taskSum++;
//
//            // 获取完成jira任务需要的commit数量,以免数据不准，当一项任务完全由这个人完成时，我们才加入度量
//            if (jiraMapper.getDeveloperJiraCommitFromDatabase(developer, jiraUuid) != jiraMapper.getDeveloperJiraCommitFromDatabase(null, jiraUuid))
//                continue;
//            commitSum += jiraMapper.getDeveloperJiraCommitFromDatabase(developer, jiraUuid);
//            jiraSoloSum++;
//
//            //分别按照创建时间和第一次提交的时间获取时间跨度timeSpan
//            Date today = new Date();
//            double timeSpanCreated = (today.getTime() - sdf.parse(jiraMsg.getCreatedTime()).getTime()) / (1000 * 60 * 60 * 24);
//            double timeSpanCommitted = (today.getTime() - sdf.parse(jiraMapper.getFirstCommitDate(jiraUuid, developer)).getTime()) / (1000 * 60 * 60 * 24);
//            timeSpanCreatedSum += timeSpanCreated;
//            timeSpanCommittedSum += timeSpanCommitted;
//        }
//
//        //fix me
//        Map<String, Object> result = new HashMap<>();
//        result.put("bugSum", bugSum);
//        result.put("taskSum", taskSum);
//        result.put("commitSum", commitSum);
//        result.put("jiraSoloSum", jiraSoloSum);
//        result.put("timeSpanCreatedSum", timeSpanCreatedSum);
//        result.put("timeSpanCommittedSum", timeSpanCommittedSum);
//
//        return result;
//    }

    @Override
    public Map<String, Object> getDeveloperMsg(String repoUuidPara, String developer, String since, String until) throws ParseException {

//        System.out.println(System.currentTimeMillis());
        long time = System.currentTimeMillis();
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
//        System.out.println(System.currentTimeMillis()-time);

        int teamBugSum = 0;
        for (String jiraUuid : teamJiraUuidList) {
            TeamJiraMsg jiraMsg = jiraMapper.getTeamJiraMsgFromCurrentDatabase(jiraUuid);
//            System.out.println(System.currentTimeMillis()-time);

            //Is empty?
            if (jiraMsg == null) {
                continue;
            }
//            System.out.println(System.currentTimeMillis()-time);

            if ("done".equals(jiraMsg.getCurrentStatus())) {
                finishedTeamJiraUuidList.add(jiraUuid);
            }
            if ("bug".equals(jiraMsg.getIssueType())){
                teamBugSum++;
            }
//            System.out.println(System.currentTimeMillis()-time);

        }

//        System.out.println(System.currentTimeMillis()-time);
        JiraMeasure toDoJiraMeasure = new JiraMeasure(developer, unfinishedJiraUuidList, true, jiraMapper);

        JiraMeasure finishedJiraMeasure = new JiraMeasure(developer, finishedJiraUuidList, false, jiraMapper);
//        System.out.println(System.currentTimeMillis()-time);

        Map<String, Object> commitPerJira = new HashMap<>();
        double rate = 0;
        if (finishedJiraMeasure.getJiraSoloSum() != 0)
            rate = finishedJiraMeasure.getCommitSum() * 1.0 / finishedJiraMeasure.getJiraSoloSum();
        commitPerJira.put("finishedJiraSum", finishedJiraMeasure.getJiraSoloSum());
        commitPerJira.put("commitSum", finishedJiraMeasure.getCommitSum());
        commitPerJira.put("rate", rate);

        Map<String, Object> completionRate = new HashMap<>();
        rate = 0;
        if (!jiraUuidList.isEmpty())
            rate = finishedJiraUuidList.size() * 1.0 / jiraUuidList.size();

        completionRate.put("completedJiraSum", finishedJiraUuidList.size());
        completionRate.put("jiraSoloSum", jiraUuidList.size());
        completionRate.put("completionRate", rate);

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
        double completedBugSumPerDay = 0;
        double completedTaskSumPerDay = 0;
        if (duration != 0) completedBugSumPerDay = finishedJiraMeasure.getBugSum() * 1.0 / duration;
        if (duration != 0) completedTaskSumPerDay = finishedJiraMeasure.getTaskSum() * 1.0 / duration;

        Map<String, Object> differentTypeSum = new HashMap<>();
        differentTypeSum.put("totalBugSum", finishedJiraMeasure.getBugSum() * 1.0 + toDoJiraMeasure.getBugSum() * 1.0);
        differentTypeSum.put("totalTaskSum", finishedJiraMeasure.getTaskSum() * 1.0 + toDoJiraMeasure.getTaskSum() * 1.0);
        differentTypeSum.put("completedBugSum", finishedJiraMeasure.getBugSum());
        differentTypeSum.put("completedTaskSum", finishedJiraMeasure.getTaskSum());
        differentTypeSum.put("duration", duration);
        differentTypeSum.put("completedBugSumPerDay", completedBugSumPerDay);
        differentTypeSum.put("completedTaskSumPerDay", completedTaskSumPerDay);

        rate = 0;
        if (teamBugSum != 0)
            rate = ( finishedJiraMeasure.getBugSum() * 1.0 + toDoJiraMeasure.getBugSum() * 1.0) / teamBugSum;
        Map<String, Object> defectRate = new HashMap<>();
        defectRate.put("individualBugSum", finishedJiraMeasure.getBugSum() + toDoJiraMeasure.getBugSum());
        defectRate.put("teamBugSum", teamBugSum);
        defectRate.put("rate", rate);


        double individualRate = 0;
        if (!finishedTeamJiraUuidList.isEmpty()) individualRate = jiraUuidList.size() * 1.0 / teamJiraUuidList.size();
        double solvedRate = 0;
        if (!finishedTeamJiraUuidList.isEmpty())
            solvedRate = finishedJiraUuidList.size() * 1.0 / finishedTeamJiraUuidList.size();
        Map<String, Object> assignedJiraRate = new HashMap<>();
        assignedJiraRate.put("individualJiraSum", jiraUuidList.size());
        assignedJiraRate.put("teamJiraSum", teamJiraUuidList.size());
        assignedJiraRate.put("solvedIndividualJiraSum", finishedJiraUuidList.size());
        assignedJiraRate.put("solvedTeamJiraSum", finishedTeamJiraUuidList.size());
        assignedJiraRate.put("individualRate", individualRate);
        assignedJiraRate.put("solvedJiraRate", solvedRate);

        List<ToDoJiraMsg> toDoJiraMsgs = new ArrayList<>();
        if (!unfinishedJiraUuidList.isEmpty()) {
            for (String jiraUuid : unfinishedJiraUuidList) {
                toDoJiraMsgs.add(getToDoJiraMsg(jiraUuid));
            }
        }

        //result
        Map<String, Object> result = new HashMap<>();
        result.put("commitPerJira", commitPerJira);
        result.put("completionRate", completionRate);
        result.put("timeSpan", timeSpan);
        result.put("differentTypeSum", differentTypeSum);
        result.put("defectRate", defectRate);
        result.put("assignedJiraRate", assignedJiraRate);
        result.put("toDoJiraMessage", toDoJiraMsgs);
        System.out.println(System.currentTimeMillis()-time);

        return result;
    }

}