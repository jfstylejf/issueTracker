package cn.edu.fudan.issueservice.core.process;

import cn.edu.fudan.issueservice.dao.*;
import cn.edu.fudan.issueservice.domain.dbo.Issue;
import cn.edu.fudan.issueservice.domain.dbo.Location;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.domain.dbo.RawIssueMatchInfo;
import cn.edu.fudan.issueservice.domain.enums.RawIssueStatus;
import cn.edu.fudan.common.jgit.JGitHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 需要入库的内容包括
 * <p>
 * 将数据持久化
 *
 * @author fancying
 */
@Slf4j
@Component
@Scope("prototype")
public class IssuePersistenceManager {

    private RawIssueDao rawIssueDao;
    private LocationDao locationDao;
    private IssueDao issueDao;
    private ScanResultDao scanResultDao;
    private RawIssueMatchInfoDao rawIssueMatchInfoDao;
    private IssueIgnoreDao issueIgnoreDao;

    @Transactional(rollbackFor = Exception.class)
    public void persistScanData(IssueStatistics issueStatistics, String repoUuid) {

        //0.get the issues infos
        IssueMatcher issueMatcher = issueStatistics.getIssueMatcher();
        List<Issue> newIssues = issueStatistics.getNewIssues();
        List<Issue> mappedIssues = issueStatistics.getMappedIssues();
        List<Issue> solvedIssues = issueStatistics.getSolvedIssues();

        //1.handle issues and persist
        solvedIssues.forEach(issue -> issue.setResolution(String.valueOf(Integer.parseInt(issue.getResolution()) + 1)));
        issueDao.batchUpdateIssue(Stream.concat(solvedIssues.stream(), mappedIssues.stream()).collect(Collectors.toList()));
        issueDao.insertIssueList(newIssues);

        //2.rawIssue persist
        List<RawIssue> curAllRawIssues = issueMatcher.getCurAllRawIssues();
        //2.1 get the new rawIssues' stream list for step2 and step3
        List<RawIssue> newRawIssuesStreamList = curAllRawIssues.stream()
                .filter(rawIssue -> newIssues.stream()
                        .map(Issue::getUuid)
                        .collect(Collectors.toList())
                        .contains(rawIssue.getIssueId()))
                .collect(Collectors.toList());
        //2.2 get the mapped rawIssues' stream list for step2 and step3
        List<RawIssue> mappedRawIssuesStreamList = curAllRawIssues.stream()
                .filter(rawIssue -> mappedIssues.stream()
                        .map(Issue::getUuid)
                        .collect(Collectors.toList())
                        .contains(rawIssue.getIssueId()))
                .collect(Collectors.toList());
        //2.3 concat two streams
        List<RawIssue> insertRawIssueList = Stream.concat(newRawIssuesStreamList.stream(), mappedRawIssuesStreamList.stream()).collect(Collectors.toList());
        rawIssueDao.insertRawIssueList(insertRawIssueList);

        //3.rawIssueMatchInfo persist
        List<RawIssueMatchInfo> rawIssueMatchInfos = new ArrayList<>();
        //3.1 get new issues' rawIssueMatchInfo
        newRawIssuesStreamList.forEach(rawIssue -> rawIssueMatchInfos.addAll(rawIssue.getMatchInfos()));
        //3.2 get mappedIssues' rawIssueMatchInfo
        mappedRawIssuesStreamList.forEach(rawIssue -> rawIssueMatchInfos.addAll(rawIssue.getMatchInfos()));
        //3.2 new solvedIssues' rawIssueMatchInfo
        handleSolvedRawIssueMatchInfo(rawIssueMatchInfos, issueStatistics.getCommitId(), issueMatcher.getParentRawIssuesResult(), solvedIssues.stream().map(Issue::getUuid).collect(Collectors.toList()), issueStatistics.getJGitHelper());
        rawIssueMatchInfoDao.insertRawIssueMatchInfoList(rawIssueMatchInfos);

        //4.update issue ignore records
        issueIgnoreDao.updateIssueIgnoreRecords(issueStatistics.getUsedIgnoreRecordsUuid());

        //5.location persist
        List<Location> locations = new ArrayList<>();
        insertRawIssueList.forEach(rawIssue -> locations.addAll(rawIssue.getLocations()));
        locationDao.insertLocationList(locations);

        //6.handle eslint ignore file
        issueDao.updateIssuesForIgnore(issueStatistics.getIgnoreFiles(), repoUuid);

        //7.scanResult persist
        scanResultDao.addOneScanResult(issueStatistics.getScanResult());
    }

    private void handleSolvedRawIssueMatchInfo(List<RawIssueMatchInfo> rawIssueMatchInfos, String curCommitId, Map<String, List<RawIssue>> parentRawIssuesResult, List<String> solvedIssuesUuid, JGitHelper jGitHelper) {
        if (parentRawIssuesResult.size() <= 1) {
            //handle single parent rawIssues result
            parentRawIssuesResult.values()
                    .forEach(rawIssues ->
                            rawIssues.stream().filter(rawIssue -> !rawIssue.isMapped() && solvedIssuesUuid.contains(rawIssue.getIssueId()))
                                    .forEach(rawIssue ->
                                            rawIssueMatchInfos.add(
                                                    RawIssueMatchInfo.builder()
                                                            .uuid(UUID.randomUUID().toString())
                                                            .curRawIssueUuid(RawIssueMatchInfo.EMPTY)
                                                            .curCommitId(curCommitId)
                                                            .preRawIssueUuid(rawIssue.getUuid())
                                                            .preCommitId(rawIssue.getCommitId())
                                                            .issueUuid(rawIssue.getIssueId())
                                                            .status(RawIssueStatus.SOLVED.getType())
                                                            .build()
                                            )
                                    )
                    );
        } else {
            //handle multiple parents rawIssues result
            handleMultipleParentRawIssues(rawIssueMatchInfos, parentRawIssuesResult, jGitHelper, curCommitId, solvedIssuesUuid);
        }
    }

    private void handleMultipleParentRawIssues(List<RawIssueMatchInfo> rawIssueMatchInfos, Map<String, List<RawIssue>> parentRawIssuesResult, JGitHelper jGitHelper, String curCommitId, List<String> solvedIssuesUuid) {
        //commit time, author, rawIssues
        List<CommitInfo> list = new ArrayList<>();
        //get the commit info list
        String author = jGitHelper.getAuthorName(curCommitId);
        //author equal -> 2 else -> 1
        parentRawIssuesResult.forEach((commit, rawIssues) ->
                list.add(new CommitInfo(jGitHelper.getLongCommitTime(curCommitId) - jGitHelper.getLongCommitTime(commit), jGitHelper.getAuthorName(commit).equals(author) ? 2 : 1, rawIssues)));
        //sort commit info
        list.sort((o1, o2) -> o1.author == o2.author ? (int) (o1.commitTime - o2.commitTime) : o2.author - o1.author);
        //new hashset for used issuesUuid
        Set<String> set = new HashSet<>();
        //new rawIssueMatchInfo
        for (CommitInfo commitInfo : list) {
            commitInfo.rawIssues.stream()
                    .filter(rawIssue -> !rawIssue.isMapped() && solvedIssuesUuid.contains(rawIssue.getIssueId()))
                    .forEach(rawIssue -> {
                        rawIssueMatchInfos.add(
                                RawIssueMatchInfo.builder()
                                        .uuid(UUID.randomUUID().toString())
                                        .curRawIssueUuid(RawIssueMatchInfo.EMPTY)
                                        .curCommitId(curCommitId)
                                        .preRawIssueUuid(rawIssue.getUuid())
                                        .preCommitId(rawIssue.getCommitId())
                                        .issueUuid(rawIssue.getIssueId())
                                        .status(set.contains(rawIssue.getIssueId()) ? RawIssueStatus.MERGE_SOLVED.getType() : RawIssueStatus.SOLVED.getType())
                                        .build());
                        set.add(rawIssue.getIssueId());
                    });
        }
    }

    @AllArgsConstructor
    private static class CommitInfo {
        private final long commitTime;
        private final int author;
        private final List<RawIssue> rawIssues;
    }

    @Autowired
    public void setRawIssueDao(RawIssueDao rawIssueDao) {
        this.rawIssueDao = rawIssueDao;
    }

    @Autowired
    public void setLocationDao(LocationDao locationDao) {
        this.locationDao = locationDao;
    }

    @Autowired
    public void setScanResultDao(ScanResultDao scanResultDao) {
        this.scanResultDao = scanResultDao;
    }

    @Autowired
    public void setIssueDao(IssueDao issueDao) {
        this.issueDao = issueDao;
    }

    @Autowired
    public void setRawIssueMatchInfoDao(RawIssueMatchInfoDao rawIssueMatchInfoDao) {
        this.rawIssueMatchInfoDao = rawIssueMatchInfoDao;
    }

    @Autowired
    public void setIssueIgnoreDao(IssueIgnoreDao issueIgnoreDao) {
        this.issueIgnoreDao = issueIgnoreDao;
    }
}
