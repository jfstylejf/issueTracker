package cn.edu.fudan.issueservice.core.process;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.core.analyzer.BaseAnalyzer;
import cn.edu.fudan.issueservice.core.analyzer.EsLintBaseAnalyzer;
import cn.edu.fudan.issueservice.dao.*;
import cn.edu.fudan.issueservice.domain.dbo.*;
import cn.edu.fudan.issueservice.domain.enums.IgnoreTypeEnum;
import cn.edu.fudan.issueservice.domain.enums.ToolEnum;
import cn.edu.fudan.issueservice.util.AstParserUtil;
import cn.edu.fudan.issueservice.util.JGitHelper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 单一职责  该类只计算匹配上后的新增 消除 剩余  ignore等其他操作放在其他类中
 * <p>
 * 计算原则  在任一 匹配对中的 currentRawIssue中 只要有任一个匹配上了就算匹配上了
 * 新增： currentRawIssue中 在所有匹配对中的没有任何一个匹配上就算没有匹配上
 * 消除： 原有的oldIssue 中没有匹配上的 （需要扣除已经解决了的数量）
 * 剩余： 当前rawIssue的总数 （从 sonar 中拿到的数量）
 * <p>
 * todo 归总后基于ignore和误报的信息再次统计
 *
 * @author fancying
 */
@Slf4j
@Component
@Getter
@Setter
@NoArgsConstructor
public class IssueStatistics {

    private static IssueDao issueDao;
    private static IssueScanDao issueScanDao;
    private static IssueTypeDao issueTypeDao;
    private static IssueIgnoreDao issueIgnoreDao;
    private static CommitDao commitDao;
    private static RestInterfaceManager restInterfaceManager;

    private JGitHelper jGitHelper;
    private String commitId;
    private IssueMatcher issueMatcher;
    private ScanResult scanResult;
    private Date currentCommitDate;
    private BaseAnalyzer analyzer;

    /**
     * 当前 raw issues 中没匹配上的
     */
    private int newIssueCount = 0;

    private List<Issue> newIssues;

    /**
     * pre issues 中没匹配上的 目前被解决的数量
     */
    private int eliminate = 0;

    private List<Issue> solvedIssues;

    /**
     * remain
     */
    private int remaining = 0;

    private List<Issue> mappedIssues;

    /**
     * ignore 以及 misinformation 等需要忽略的信息
     **/
    private int ignore = 0;

    /**
     * 用于更新使用过的ignore Record
     */
    private List<String> usedIgnoreRecordsUuid = new ArrayList<>();

    private List<String> ignoreFiles = new ArrayList<>();

    /**
     * 根据issueMatch中的信息做数据统计
     **/
    public boolean doingStatisticalAnalysis(IssueMatcher issueMatcher, String repoUuid, String tool, String repoPath) {

        //0.get all data
        Map<String, Issue> newIssue = issueMatcher.getNewIssues();
        Map<String, Issue> solvedIssue = issueMatcher.getSolvedIssue();
        Map<String, Issue> mappedIssue = issueMatcher.getMappedIssues();
        List<RawIssue> curAllRawIssues = issueMatcher.getCurAllRawIssues();

        //1.get ignore issues' uuid
        Map<String, String> issueUuidToRecord = new HashMap<>(16);
        List<String> ignoredIssueUuids = ignoreMatch(curAllRawIssues, repoUuid, issueUuidToRecord);

        if (ToolEnum.ESLINT.getType().equals(tool)) {
            ignoreFiles.addAll(readEsLintIgnoreFile(repoPath));
        }

        //2.set ignore issues' manual status
        for (String ignoredIssueUuid : ignoredIssueUuids) {
            if (newIssue.containsKey(ignoredIssueUuid)) {
                newIssue.get(ignoredIssueUuid).setManualStatus(IgnoreTypeEnum.IGNORE.getName());
                usedIgnoreRecordsUuid.add(issueUuidToRecord.get(ignoredIssueUuid));
            } else if (mappedIssue.containsKey(ignoredIssueUuid)) {
                mappedIssue.get(ignoredIssueUuid).setManualStatus(IgnoreTypeEnum.IGNORE.getName());
                usedIgnoreRecordsUuid.add(issueUuidToRecord.get(ignoredIssueUuid));
            } else if (solvedIssue.containsKey(ignoredIssueUuid)) {
                solvedIssue.get(ignoredIssueUuid).setManualStatus(IgnoreTypeEnum.IGNORE.getName());
                usedIgnoreRecordsUuid.add(issueUuidToRecord.get(ignoredIssueUuid));
            }
        }

        String developer = commitDao.getDeveloperByCommitId(commitId);
        if (developer == null) {
            developer = jGitHelper.getAuthorName(commitId);
        }
        //3.get new issues,solved issues and mapped issues
        newIssues = new ArrayList<>(newIssue.values());
        for (Issue issue : newIssues) {
            issue.setStartCommitDate(currentCommitDate);
            issue.setEndCommitDate(currentCommitDate);
            issue.setProducer(developer);
        }
        newIssueCount = (int) newIssues.stream()
                .filter(issue -> !issue.getManualStatus().equals(IgnoreTypeEnum.IGNORE.getName()))
                .count();

        solvedIssues = new ArrayList<>(solvedIssue.values());
        for (Issue issue : solvedIssues) {
            issue.setSolveCommit(commitId);
            issue.setSolveCommitDate(currentCommitDate);
            issue.setSolver(developer);
        }
        eliminate = (int) Stream.concat(solvedIssue.values().stream(),
                mappedIssue.values().stream().filter(issue -> issue.getManualStatus().equals(IgnoreTypeEnum.IGNORE.getName())))
                .count();

        mappedIssues = new ArrayList<>(mappedIssue.values());
        for (Issue issue : mappedIssues) {
            issue.setSolver(null);
            issue.setSolveCommit(null);
            issue.setSolveCommitDate(null);
            issue.setEndCommitDate(currentCommitDate);
        }
        remaining = issueDao.getRemainingIssueCount(repoUuid) + newIssueCount - eliminate;

        //4.get the scan result
        scanResult = new ScanResult(tool, repoUuid, new Date(), commitId, currentCommitDate, jGitHelper.getAuthorName(commitId), newIssueCount, eliminate, remaining, ignoredIssueUuids.size());

        //5.set issue matcher for next step persist data
        this.issueMatcher = issueMatcher;

        return true;
    }

    private List<String> readEsLintIgnoreFile(String repoPath) {

        List<String> fileList = new ArrayList<>();
        File file = new File(repoPath + "/.eslintignore");

        try (LineNumberReader reader = new LineNumberReader(new FileReader(file))) {
            String s;
            while ((s = reader.readLine()) != null) {
                s = s.replaceAll("\\*", "%");
                String temp = "\"%" + s + "%\"";
                fileList.add(temp);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        return fileList;
    }

    private List<String> ignoreMatch(List<RawIssue> curAllRawIssues, String repoUuid, Map<String, String> issueUuidToRecord) {

        List<String> ignoreIssueUuids = new ArrayList<>();

        //1.get repo url and pre commits for select ignore record
        String repoUrl = restInterfaceManager.getRepoUrl(repoUuid);
        List<String> preCommits = jGitHelper.getAllCommitParents(commitId);

        //2.get not used ignore record in this repo
        List<Map<String, Object>> ignoreRecords = issueIgnoreDao.getAllIgnoreRecord(repoUrl, preCommits);

        //3.handle the ignore records and get the ignore rawIssue list
        List<RawIssue> ignoreRawIssues = new ArrayList<>();
        Map<String, String> rawIssueUuidToIgnoreRecord = new HashMap<>(16);
        String scanId = UUID.randomUUID().toString();
        for (Map<String, Object> ignoreRecord : ignoreRecords) {
            //analyze rawIssues from ignore records
            RawIssue rawIssue = RawIssue.valueOf((String) ignoreRecord.get("rawIssue"));
            rawIssueUuidToIgnoreRecord.put(rawIssue.getUuid(), (String) ignoreRecord.get("uuid"));
            //set file path
            String filePath = ignoreRecord.get("filePath").toString();
            rawIssue.setScanId(scanId);
            rawIssue.setFileName(filePath);
            rawIssue.getLocations().forEach(location -> {
                location.setFilePath(filePath);
                location.setRawIssueId(rawIssue.getUuid());
            });
            ignoreRawIssues.add(rawIssue);
        }

        //4.copy current rawIssues
        List<RawIssue> curAllRawIssuesCopy = new ArrayList<>();
        curAllRawIssues.forEach(rawIssue -> curAllRawIssuesCopy.add(RawIssue.copyOf(rawIssue)));

        //5.filter two rawIssue list by file path for ignore match
        Map<String, List<RawIssue>> curAllRawIssuesCopyFilter =
                curAllRawIssuesCopy.parallelStream()
                        .collect(Collectors.groupingBy(RawIssue::getFileName));

        Map<String, List<RawIssue>> ignoreRawIssuesFilter =
                ignoreRawIssues.parallelStream()
                        .collect(Collectors.groupingBy(RawIssue::getFileName));

        //6.map current RawIssues and ignore rawIssues
        curAllRawIssuesCopyFilter.forEach((fileName, rawIssueList) ->
                RawIssueMatcher.match(rawIssueList, ignoreRawIssuesFilter.getOrDefault(fileName, new ArrayList<>()),
                        analyzer instanceof EsLintBaseAnalyzer ? analyzer.getMethodsAndFieldsInFile().getOrDefault(fileName, new HashSet<>()) :
                                AstParserUtil.getAllMethodAndFieldName(jGitHelper.getRepoPath() + "/" + fileName)));

        //7.handle the match result and get the ignore issue uuids
        curAllRawIssuesCopyFilter.forEach((fileName, rawIssueList) ->
                ignoreIssueUuids.addAll(
                        rawIssueList.stream()
                                .filter(RawIssue::isMapped)
                                .map(RawIssue::getIssueId)
                                .collect(Collectors.toList())
                )
        );

        ignoreRawIssuesFilter.values().forEach(rawIssues ->
                rawIssues.stream()
                        .filter(RawIssue::isMapped)
                        .forEach(rawIssue -> issueUuidToRecord.put(rawIssue.getMappedRawIssue().getIssueId(), rawIssueUuidToIgnoreRecord.get(rawIssue.getUuid())))
        );

        return ignoreIssueUuids;
    }

    @Autowired
    public IssueStatistics(IssueDao issueDao, CommitDao commitDao, IssueScanDao issueScanDao, IssueTypeDao issueTypeDao, IssueIgnoreDao issueIgnoreDao, RestInterfaceManager restInterfaceManager) {
        IssueStatistics.issueDao = issueDao;
        IssueStatistics.commitDao = commitDao;
        IssueStatistics.issueScanDao = issueScanDao;
        IssueStatistics.issueTypeDao = issueTypeDao;
        IssueStatistics.issueIgnoreDao = issueIgnoreDao;
        IssueStatistics.restInterfaceManager = restInterfaceManager;
    }
}
