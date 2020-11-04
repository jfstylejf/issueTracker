package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.domain.dbo.Commit;
import cn.edu.fudan.issueservice.mapper.CommitViewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Repository
public class CommitDao {

    private CommitViewMapper commitViewMapper;

    @Autowired
    public void setCommitViewMapper(CommitViewMapper commitViewMapper) {
        this.commitViewMapper = commitViewMapper;
    }

    public Integer getCommitCount(String repoId, String startCommitTime) {
        return commitViewMapper.getCommitCount(repoId, startCommitTime);
    }

    public List<Commit> getCommits(String repoId, String startCommitTime) {
        return commitViewMapper.getCommits(repoId, startCommitTime);
    }

    public List<String> getDevelopersByRepoIdList(List<String> repoIdList) {
        return commitViewMapper.getDevelopersByRepoIdList(repoIdList);
    }

    public Map<String, Object> getCommitViewInfoByCommitId(String repoId, String commitId) {
        return commitViewMapper.getCommitViewInfoByCommitId(repoId, commitId);
    }
}
