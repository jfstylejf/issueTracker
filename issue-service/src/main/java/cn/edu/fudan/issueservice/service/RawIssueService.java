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

    /**
     * 根据issueId返回rawIssueList
     * @param issueId issue_uuid
     * @return rawIssueList
     */
    List<Map<String, Object>> getRawIssueByIssueId(String issueId);

    /**
     * 根据条件返回rawIssueList
     * @param issueId issue_uuid
     * @param page 页号
     * @param size 页大小
     * @param status 状态
     * @return rawIssueList
     */
    Object getRawIssueList(String issueId, Integer page, Integer size, String status);
}
