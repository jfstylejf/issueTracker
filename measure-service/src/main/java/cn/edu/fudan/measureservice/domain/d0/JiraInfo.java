package cn.edu.fudan.measureservice.domain.d0;

import cn.edu.fudan.measureservice.dao.JiraDao;
import cn.edu.fudan.measureservice.domain.dto.Query;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author wjzho
 */
public class JiraInfo extends BaseData {

    private JiraDao jiraDao;

    /**
     * 个人完成bug数
     */
    private int developerJiraBugCount;
    /**
     * 团队完成bug数
     */
    private int totalJiraBugCount;
    /**
     * 个人完成feature数
     */
    private int developerJiraFeatureCount;
    /**
     * 解决jira任务提交的commit次数
     */
    private int jiraCommitNum;
    /**
     * 完成的jira任务数量
     */
    private int completedJiraNum;
    /**
     * 开发者包含有jira单号的commit个数
     */
    private int developerJiraCommitCount;

    /**
     * 开发者分配的jira任务数
     */
    private int developerAssignedJiraCount;
    /**
     * 团队分配jira任务数
     */
    private int totalAssignedJiraCount;
    /**
     * 个人解决jira任务数（和completedJiraNum相同）
     */
    private int developerSolvedJiraCount;
    /**
     * 团队解决jira任务
     */
    private int totalSolvedJiraCount;


    public JiraInfo(Query query) {
        super(query);
    }

    @Override
    public void dataInjection() {

        this.developerJiraCommitCount = jiraDao.getDeveloperJiraCommitCount(query);
    }

    @Autowired
    public void setJiraDao(JiraDao jiraDao) {this.jiraDao = jiraDao;}

}


