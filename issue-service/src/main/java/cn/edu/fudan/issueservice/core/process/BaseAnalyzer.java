package cn.edu.fudan.issueservice.core.process;

import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.util.JGitHelper;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * description: 工具具体的执行流程
 *
 * @author fancying
 * create: 2020-05-20 15:53
 **/
public abstract class BaseAnalyzer {

    IssueMatcher issueMatcher;
    private JGitHelper jGitHelper;

    protected String binHome;

    protected List<RawIssue> resultRawIssues = new ArrayList<> ();

    public String getBinHome() {
        return binHome;
    }

    public void setBinHome(String binHome) {
        this.binHome = binHome;
    }

    /**
     *  调用工具的流程
     */
    public abstract boolean invoke(String repoId, String repoPath, String commit);


    /**
     *  调用工具的流程
     */
    public abstract boolean analyze(String repoPath, String repoId, String commitId);

    /**
     *  返回工具名
     * @return
     */
    public abstract String getToolName();

    /**
     * 返回该缺陷的优先级
     * @param rawIssue
     * @return
     */
    public abstract Integer getPriorityByRawIssue(RawIssue rawIssue);

    public List<RawIssue> getResultRawIssues() {
        return resultRawIssues;
    }

    public void emptyAnalyzeRawIssues() {
        resultRawIssues.clear ();
    }


}
