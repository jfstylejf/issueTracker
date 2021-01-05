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
     * 个人完成jiraBug数
     */
    private int developerCompletedJiraBugCount;
    /**
     * 个人总jiraBug数
     */
    private int developerJiraBugCount;
    /**
     * 团队jiraBug数
     */
    private int totalJiraBugCount;
    /**
     * 个人完成feature数
     */
    private int developerCompletedJiraFeatureCount;
    /**
     * 个人总feature数
     */
    private int developerJiraFeatureCount;
    /**
     * 开发者包含有jira单号的commit个数
     */
    private int developerJiraCommitNum;
    /**
     * 完成的jira任务数量
     */
    private int developerCompletedJiraNum;
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
        /**
         *  key : getJiraMsgInfo
         *  value {@link JiraDao}
         */
        Map<String,Object> jiraMap = jiraDao.getJiraMsgInfo(query);
        developerCompletedJiraNum = (int) jiraMap.get("developerCompletedJiraNum");
        developerJiraCommitNum = (int) jiraMap.get("developerJiraCommitNum");
        developerJiraBugCount = (int) jiraMap.get("developerJiraBugCount");
        developerCompletedJiraBugCount = (int) jiraMap.get("developerCompletedJiraBugCount");
        developerJiraFeatureCount = (int) jiraMap.get("developerJiraFeatureCount");
        developerCompletedJiraFeatureCount = (int) jiraMap.get("developerCompletedJiraFeatureCount");
        totalJiraBugCount = (int) jiraMap.get("totalJiraBugCount");
        developerAssignedJiraCount = (int) jiraMap.get("developerAssignedJiraCount");
        totalAssignedJiraCount = (int) jiraMap.get("totalAssignedJiraCount");
        developerSolvedJiraCount = (int) jiraMap.get("developerSolvedJiraCount");
        totalSolvedJiraCount = (int) jiraMap.get("totalSolvedJiraCount");
    }

    @Autowired
    public void setJiraDao(JiraDao jiraDao) {this.jiraDao = jiraDao;}

}


