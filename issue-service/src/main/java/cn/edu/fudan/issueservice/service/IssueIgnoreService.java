package cn.edu.fudan.issueservice.service;

import cn.edu.fudan.issueservice.domain.dbo.IgnoreRecord;

import java.util.List;

/**
 * @author Beethoven
 */
public interface IssueIgnoreService {
    /**
     * 插入IssueIgnore记录
     *
     * @param list ignore record list
     * @return String
     */
    String insertIssueIgnoreRecords(List<IgnoreRecord> list);
}
