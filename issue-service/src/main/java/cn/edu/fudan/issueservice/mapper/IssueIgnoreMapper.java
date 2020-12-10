package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.dbo.IgnoreRecord;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Beethoven
 */
@Repository
public interface IssueIgnoreMapper {

    /**
     * 插入issueIgnore记录
     * @param ignoreRecords
     */
    void insertIssueIgnoreRecords(List<IgnoreRecord> ignoreRecords);
}
