package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.dbo.IgnoreRecord;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author Beethoven
 */
@Repository
public interface IssueIgnoreMapper {

    /**
     * 插入issueIgnore记录
     *
     * @param ignoreRecords ignoreRecords
     */
    void insertIssueIgnoreRecords(List<IgnoreRecord> ignoreRecords);


    /**
     * 更新使用过的ignore records
     *
     * @param usedIgnoreRecordsUuid usedIgnoreRecordsUuid
     */
    void updateIssueIgnoreRecords(@Param("usedIgnoreRecordsUuid") List<String> usedIgnoreRecordsUuid);

    /**
     * 根据url commit获取忽略记录
     *
     * @param repoUrl    repoUrl
     * @param preCommits preCommits
     * @return 忽略记录
     */
    List<Map<String, Object>> getAllIgnoreRecord(String repoUrl, List<String> preCommits);
}
