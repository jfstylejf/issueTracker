package cn.edu.fudan.issueservice.service;

import cn.edu.fudan.issueservice.domain.dbo.Location;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;

import java.util.List;
import java.util.Map;

/**
 * @author WZY
 * @version 1.0
 **/
public interface RawIssueService {

    void deleteRawIssueByRepoIdAndTool(String repoId,String tool);

    List<RawIssue> getRawIssueByIssueId(String issueId);

    Map<String, Object> getCode(String project_id, String commit_id, String file_path);

    Object getRawIssueList(String issue_id,Integer page,Integer size,String status);
}
