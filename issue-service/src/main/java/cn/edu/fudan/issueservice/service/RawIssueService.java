package cn.edu.fudan.issueservice.service;

import java.util.List;
import java.util.Map;

/**
 * @author WZY
 * @version 1.0
 **/
public interface RawIssueService {

    /**
     * 根据issueId返回rawIssueList
     *
     * @param issueId issue_uuid
     * @return rawIssueList
     */
    List<Map<String, Object>> getRawIssueByIssueUuid(String issueId);
}
