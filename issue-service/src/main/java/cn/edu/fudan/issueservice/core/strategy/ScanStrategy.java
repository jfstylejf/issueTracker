package cn.edu.fudan.issueservice.core.strategy;

import cn.edu.fudan.issueservice.util.JGitHelper;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author Beethoven
 */
public interface ScanStrategy {

    /**
     * 根据扫描策略获取commit list
     *
     * @param repoId         repoUuid
     * @param jGitHelper     jGit
     * @param branch         branch
     * @param beginCommit    begin commit
     * @param scannedCommits scannedCommits
     * @return commit list
     */

    ConcurrentLinkedDeque<String> getScanCommitLinkedQueue(String repoId, JGitHelper jGitHelper, String branch, String beginCommit, List<String> scannedCommits);
}
