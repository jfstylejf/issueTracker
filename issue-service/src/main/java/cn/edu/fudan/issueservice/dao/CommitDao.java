package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.domain.dbo.Commit;
import cn.edu.fudan.issueservice.mapper.CommitViewMapper;
import cn.edu.fudan.issueservice.util.JGitHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author beethoven
 */
@Repository
public class CommitDao {

    private CommitViewMapper commitViewMapper;

    @Autowired
    public void setCommitViewMapper(CommitViewMapper commitViewMapper) {
        this.commitViewMapper = commitViewMapper;
    }

    public List<Commit> getCommits(String repoId, String startCommitTime) {
        return commitViewMapper.getCommits(repoId, startCommitTime);
    }

    public Map<String, Object> getCommitViewInfoByCommitId(String repoId, String commitId) {
        return commitViewMapper.getCommitViewInfoByCommitId(repoId, commitId);
    }

    public Commit getCommitByCommitId(String repoUuid, String startCommit) {
        return commitViewMapper.getCommitByCommitId(repoUuid, startCommit);
    }

    public String getCommitTimeByCommitId(String curCommitId, String repoUuid) {
        return commitViewMapper.getCommitTimeByCommitId(curCommitId, repoUuid);
    }

    public List<String> getParentCommits(String repoUuid, String commit, JGitHelper jGitHelper) {
        List<String> parentCommits = new ArrayList<>();

        String commitTime = commitViewMapper.getCommitTimeByCommitId(commit, repoUuid);
        List<Map<String, Object>> commits = commitViewMapper.getParentCommits(commitTime, repoUuid);
        for (Map<String, Object> commitInfo : commits) {
            String commitId = commitInfo.get("commit_id").toString();
            if (!commitInfo.get("commit_time").toString().equals(commitTime)) {
                parentCommits.add(commitId);
            } else {
                boolean flag = true;
                String[] commitParents = jGitHelper.getCommitParents(commitId);
                for (String commitParent : commitParents) {
                    if (commitParent.equals(commit)) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    parentCommits.add(commitId);
                }
            }
        }

        return parentCommits;
    }

    public String getDeveloperByCommitId(String commitId) {
        return commitViewMapper.getDeveloperByCommitId(commitId);
    }

    public String getCommitMessageByCommitIdAndRepoUuid(String commitId, String repoUuid) {
        return commitViewMapper.getCommitMessageByCommitIdAndRepoUuid(commitId, repoUuid);
    }
}
