package cn.edu.fudan.taskmanagement.domain;

import cn.edu.fudan.taskmanagement.mapper.JiraMapper;
import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.List;

@Getter
@Setter
public class TeamJiraMeasure {
    private final int bugSum;
    private final int taskSum;

    public TeamJiraMeasure(String developer, List<String> jiraUuidList, JiraMapper jiraMapper) {
        int bugSum = 0;
        int taskSum = 0;
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
        }

        this.bugSum = bugSum;
        this.taskSum = taskSum;
    }
}
