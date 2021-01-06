package cn.edu.fudan.issueservice.core.process;

import cn.edu.fudan.issueservice.domain.dbo.RawIssue;

import java.util.ArrayList;
import java.util.List;

/**
 * description: 工具具体的执行流程
 *
 * @author fancying
 * create: 2020-05-20 15:53
 **/
public abstract class BaseAnalyzer {

    protected String binHome;

    protected List<RawIssue> resultRawIssues = new ArrayList<> ();

    public String getBinHome() {
        return binHome;
    }

    public void setBinHome(String binHome) {
        this.binHome = binHome;
    }

    /**
     * 调用工具扫描
     * @param repoUuid repoUuid
     * @param repoPath repoPath
     * @param commit commit
     * @return 调用工具是否成功
     */
    public abstract boolean invoke(String repoUuid, String repoPath, String commit);


    /**
     * 调用工具进行解析,如sonarqube结果解析成rawIssue
     * @param repoPath repoPath
     * @param repoUuid repoUuid
     * @param commit commitId
     * @return 解析是否成功
     */
    public abstract boolean analyze(String repoPath, String repoUuid, String commit);

    /**
     *  返回工具名
     * @return 工具名
     */
    public abstract String getToolName();

    /**
     * 返回该缺陷的优先级
     * @param rawIssue rawIssue
     * @return 缺陷优先级
     */
    public abstract Integer getPriorityByRawIssue(RawIssue rawIssue);

    public List<RawIssue> getResultRawIssues() {
        return resultRawIssues;
    }

    public void emptyAnalyzeRawIssues() {
        resultRawIssues.clear ();
    }
}
