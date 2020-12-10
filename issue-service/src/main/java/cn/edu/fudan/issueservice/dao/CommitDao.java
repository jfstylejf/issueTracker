package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.domain.dbo.Commit;
import cn.edu.fudan.issueservice.mapper.CommitViewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.Map;

@Repository
public class CommitDao {

    private CommitViewMapper commitViewMapper;

    @Autowired
    public void setCommitViewMapper(CommitViewMapper commitViewMapper) {
        this.commitViewMapper = commitViewMapper;
    }

    public LinkedList<Commit> getCommits(String repoId, String startCommitTime) {
        return commitViewMapper.getCommits(repoId, startCommitTime);
    }

    public Map<String, Object> getCommitViewInfoByCommitId(String repoId, String commitId) {
        return commitViewMapper.getCommitViewInfoByCommitId(repoId, commitId);
    }

    public Commit getCommitByCommitId(String repoUuid, String startCommit) {
        return commitViewMapper.getCommitByCommitId(repoUuid, startCommit);
    }
}
