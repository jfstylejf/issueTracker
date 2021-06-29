package cn.edu.fudan.taskmanagement.domain;

import cn.edu.fudan.taskmanagement.mapper.JiraMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class JiraMeasure {
    private final int bugSum;
    private final int taskSum;
    private final int commitSum;
    private final int jiraSoloSum;
    private final double timeSpanCreatedSum;
    private final double timeSpanCommittedSum;

    public JiraMeasure(String developer, List<String> jiraUuidList, Boolean isUnfinished, JiraMapper jiraMapper) throws ParseException {
        int bugSum = 0;
        int taskSum = 0;
        int commitSum = 0;
        int jiraSoloSum = 0;
        double timeSpanCreatedSum = 0;
        double timeSpanCommittedSum = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (String jiraUuid : jiraUuidList) {
            JiraMsg jiraMsg = jiraMapper.getJiraMsgFromCurrentDatabase(jiraUuid);

            //Is empty?
            if (jiraMsg == null) {
                continue;
            }

            //获取总bug数或feature数
            if ("bug".equals(jiraMsg.getIssueType()))
                bugSum++;
            else if ("task".equals(jiraMsg.getIssueType()))
                taskSum++;

            // 获取完成jira任务需要的commit数量,以免数据不准，当一项任务完全由这个人完成时，我们才加入度量
            if (jiraMapper.getDeveloperJiraCommitFromDatabase(developer, jiraUuid) == jiraMapper.getDeveloperJiraCommitFromDatabase(null, jiraUuid)) {
                commitSum += jiraMapper.getDeveloperJiraCommitFromDatabase(developer, jiraUuid);
                jiraSoloSum++;
            }


            if (Boolean.TRUE.equals(isUnfinished)) {
                //分别按照创建时间和第一次提交的时间获取时间跨度timeSpan
                Date today = new Date();
                double timeSpanCreated = (today.getTime() - sdf.parse(jiraMsg.getCreatedTime()).getTime()) * 1.0 / (1000 * 60 * 60 * 24);
                double timeSpanCommitted = (today.getTime() - sdf.parse(jiraMapper.getFirstCommitDate(jiraUuid, developer)).getTime()) * 1.0 / (1000 * 60 * 60 * 24);
                timeSpanCreatedSum += timeSpanCreated;
                timeSpanCommittedSum += timeSpanCommitted;
            }
        }
        this.bugSum = bugSum;
        this.taskSum = taskSum;
        this.commitSum = commitSum;
        this.jiraSoloSum = jiraSoloSum;
        this.timeSpanCommittedSum = timeSpanCommittedSum;
        this.timeSpanCreatedSum = timeSpanCreatedSum;
    }
}
