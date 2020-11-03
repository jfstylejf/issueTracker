package cn.edu.fudan.issueservice.core.strategy;

import cn.edu.fudan.issueservice.util.JGitHelper;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public interface ScanStrategy {

     List<String> getScanCommitList(String repoId, JGitHelper jGitHelper, String branch, String beginCommit);

     ConcurrentLinkedDeque<String> getScanCommitLinkedDeque(String repoId, JGitHelper jGitHelper, String branch, String beginCommit);
}
