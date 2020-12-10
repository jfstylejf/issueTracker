package cn.edu.fudan.issueservice.service;

import cn.edu.fudan.issueservice.domain.dbo.IssueRepo;
import cn.edu.fudan.issueservice.domain.dto.RepoResourceDTO;

import java.util.Map;

public interface IssueScanService {

    String prepareForScan(RepoResourceDTO repoResourceDTO, String branch, String beginCommit, String toolName);

    void stopScan(String  repoId, String toolName);

    IssueRepo getScanStatus(String  repoId, String toolName) throws Exception;

    Map<String, Object> getCommitsCount(String repoUuid, String tool);

    Map<String, Object> getCommits(String repoUuid, Integer page, Integer size, Boolean isWhole, String tool);
}
