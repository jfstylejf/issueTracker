package cn.edu.fudan.issueservice.core.strategy;

import cn.edu.fudan.issueservice.util.JGitHelper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author lsw
 */
@Component("PSSS")
public class PositiveSequenceScanStrategy implements ScanStrategy {

    @Override
    public ConcurrentLinkedDeque<String> getScanCommitLinkedQueue(String repoId, JGitHelper jGitHelper, String branch, String beginCommit, List<String> scannedCommits) {
        List<String> commitList =  jGitHelper.getScanCommitListByBranchAndBeginCommit(branch, beginCommit, scannedCommits);
        return new ConcurrentLinkedDeque<>(commitList);
    }

}
