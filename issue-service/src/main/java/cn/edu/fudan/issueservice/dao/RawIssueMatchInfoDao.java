package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.domain.dbo.RawIssueMatchInfo;
import cn.edu.fudan.issueservice.mapper.RawIssueMatchInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author beethoven
 * @date 2021-01-19 16:04:34
 */
@Repository
public class RawIssueMatchInfoDao {

    private RawIssueMatchInfoMapper rawIssueMatchInfoMapper;

    @Autowired
    public void setRawIssueMatchInfoMapper(RawIssueMatchInfoMapper rawIssueMatchInfoMapper) {
        this.rawIssueMatchInfoMapper = rawIssueMatchInfoMapper;
    }

    public void insertRawIssueMatchInfoList(List<RawIssueMatchInfo> rawIssueMatchInfos) {
        if (rawIssueMatchInfos.isEmpty()) {
            return;
        }
        rawIssueMatchInfoMapper.insertRawIssueMatchInfoList(rawIssueMatchInfos);
    }

    public void deleteRawIssueMatchInfo(List<String> partOfRawIssueIds) {
        rawIssueMatchInfoMapper.deleteRawIssueMatchInfo(partOfRawIssueIds);
    }

    public List<Map<String, String>> getMatchInfoByIssueUuid(String issueUuid) {
        return rawIssueMatchInfoMapper.getMatchInfoByIssueUuid(issueUuid);
    }

    public List<String> getIssueUuidsByCommits(List<String> parentCommits) {
        return rawIssueMatchInfoMapper.getIssueByPreCommits(parentCommits);
    }
}
