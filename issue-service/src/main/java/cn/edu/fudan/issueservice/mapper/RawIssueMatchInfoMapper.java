package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.dbo.RawIssueMatchInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author beethoven
 * @date 2021-01-19 16:04:34
 */
@Repository
public interface RawIssueMatchInfoMapper {
    /**
     * insert rawIssueMatchInfoList
     *
     * @param list list
     */
    void insertRawIssueMatchInfoList(List<RawIssueMatchInfo> list);

    /**
     * delete rawIssueMatchInfo
     *
     * @param partOfRawIssueIds
     */
    void deleteRawIssueMatchInfo(@Param("partOfRawIssueIds") List<String> partOfRawIssueIds);

    /**
     * pre Issues
     *
     * @param preCommitParents preCommitParents
     * @return pre Issues
     */
    List<String> getIssueByPreCommits(@Param("preCommitParents") List<String> preCommitParents);

    /**
     * get rawIssueMatchInfo list
     *
     * @param issueUuid issueUuid
     * @return rawIssueMatchInfo list
     */
    List<Map<String, String>> getMatchInfoByIssueUuid(String issueUuid);
}
