package cn.edu.fudan.issueservice.core.process;

import cn.edu.fudan.common.jgit.JGitHelper;
import cn.edu.fudan.issueservice.core.analyzer.BaseAnalyzer;
import cn.edu.fudan.issueservice.core.analyzer.EsLintBaseAnalyzer;
import cn.edu.fudan.issueservice.core.analyzer.SonarQubeBaseAnalyzer;
import cn.edu.fudan.issueservice.dao.*;
import cn.edu.fudan.issueservice.domain.dbo.*;
import cn.edu.fudan.issueservice.domain.dto.MatcherCommitInfo;
import cn.edu.fudan.issueservice.domain.enums.IssueStatusEnum;
import cn.edu.fudan.issueservice.domain.enums.JavaScriptIssuePriorityEnum;
import cn.edu.fudan.issueservice.domain.enums.RawIssueStatus;
import cn.edu.fudan.issueservice.domain.enums.ScanStatusEnum;
import cn.edu.fudan.issueservice.util.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author fancying
 * create: 2020-05-20 16:56
 **/
@Slf4j
@Data
@Component
public class IssueMatcher {

    private final ThreadLocal<List<String>> parentCommitsThreadLocal = new ThreadLocal<>();

    /**
     * fixme dao与匹配策略后续可以改成静态的 不需要每次 new 的时候注入
     **/
    private static IssueScanDao issueScanDao;
    private static RawIssueDao rawIssueDao;
    private static IssueDao issueDao;
    private static IssueTypeDao issueTypeDao;
    private static LocationDao locationDao;
    private JGitHelper jGitHelper;
    private String curCommit;
    private BaseAnalyzer analyzer;

    /**
     * des: @key parentCommitId   @value 与curCommit 映射后
     * 有 RawIssueMatchInfo 的需要入库
     * 入库 solved情况
     * matchInfoResult RawIssue 中不空都需要入库
     **/
    @Getter
    private Map<String, List<RawIssue>> parentRawIssuesResult = new HashMap<>(4);

    /**
     * 需要在rawIssue location matchInfoResult
     * rawIssue中新增的是 1 所有matchInfo中status都为 change 的情况 2 rawIssue 有改变的情况 （ {@link RawIssue#isNotChange}  为 false）
     * 【todo 目前只要文件变了就算rawIssue变了】
     * matchInfoResult RawIssue 中不空都需要入库 【入库 add change mergeSolved 情况】
     **/
    @Getter
    private List<RawIssue> curAllRawIssues;

    /**
     * des: @key issue uuid   @value issue 存放 mapped issue
     * <p>
     * 用于更新issue表
     **/
    @Getter
    private Map<String, Issue> mappedIssues;

    /**
     * des: @key issue uuid   @value issue 存放新增的issue列表
     * 需要在issue 表新增记录
     **/
    @Getter
    private Map<String, Issue> newIssues = new HashMap<>(0);

    /**
     * des: @key issue uuid   @value issue 存放新增的issue列表
     * 需要在issue 表 更新记录
     **/
    @Getter
    private Map<String, Issue> solvedIssue;

    public boolean matchProcess(String repoUuid, String curCommit, JGitHelper jGitHelper, String toolName, List<RawIssue> currentAllRawIssues) {
        this.curAllRawIssues = currentAllRawIssues;
        this.jGitHelper = jGitHelper;
        this.curCommit = curCommit;
        parentCommitsThreadLocal.remove();
        solvedIssue = new HashMap<>(currentAllRawIssues.size() >> 2);
        mappedIssues = new HashMap<>(currentAllRawIssues.size() << 1);
        try {
            jGitHelper.checkout(curCommit);
            // 获取curCommit的所有 在之前的扫描过程中成功扫描过的 祖先commit 不一定是直接的parents
            List<String> parentCommits = getPreScanSuccessfullyCommit(repoUuid, curCommit, jGitHelper, toolName);
            parentCommitsThreadLocal.set(parentCommits);
            parentCommits.forEach(c -> log.debug("{} --> pre scan success commit --> {} ", curCommit, c));
            if (currentAllRawIssues.isEmpty()) {
                log.warn("all issues were solved or raw issue insert error , commit id -->  {}", curCommit);
            }

            Map<String, List<RawIssue>> curRawIssuesMatchResult = new HashMap<>(4);
            // 只有一个parent 进行normal match
            if (parentCommits.size() == 1) {
                normalMatch(repoUuid, toolName, parentCommits.get(0), curRawIssuesMatchResult);
            } else if (parentCommits.isEmpty()) {
                // 第一次匹配 会产生新的issue 以及 rawIssue 以及对应的关系
                // todo 如果前面扫描过了 可以拿前面扫描过的diff来做匹配
                log.info("start first matching  ...");
                curAllRawIssues.forEach(currentAllRawIssue -> currentAllRawIssue.setStatus(RawIssueStatus.ADD.getType()));
                newIssues = curAllRawIssues.stream().map(this::generateOneIssue).collect(Collectors.toMap(Issue::getUuid, Function.identity()));
                return true;
            } else {
                mergeMatch(repoUuid, toolName, parentCommits, curRawIssuesMatchResult);
            }
            // 全部匹配完成后设置 rawIssues 的匹配信息
            sumUpRawIssueMappedInfo(curRawIssuesMatchResult);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 根据rawIssue产生新的issue 并为rawIssue设置匹配信息
     **/
    private Issue generateOneIssue(RawIssue rawIssue) {
        Issue issue = Issue.valueOf(rawIssue);
        IssueType issueType = issueTypeDao.getIssueTypeByTypeName(rawIssue.getType());
        //fixme js and java category should in issue_type table
        issue.setIssueCategory(issueType == null ? JavaScriptIssuePriorityEnum.getPriorityByRank(rawIssue.getPriority()) : issueType.getCategory());
        // TODO: 2021/1/7 产生一些issue之后再次设置状态 @何越

        rawIssue.setIssue(issue);
        rawIssue.setIssueId(issue.getUuid());
        rawIssue.getMatchInfos().clear();
        rawIssue.getMatchInfos().add(rawIssue.generateRawIssueMatchInfo(null));
        return issue;
    }

    private void sumUpRawIssueMappedInfo(Map<String, List<RawIssue>> curRawIssuesMatchResult) {
        // 新产生的issue以及没有进行过匹配的rawIssue  关系表只存一份关系 其中parentCommitId 为匹配最近的commit
        // 1： 进行过匹配且有过匹配上  rawIssue uuid @key 匹配上的rawIssue的uuid  @value 对应的issue uuid
        Map<String, String> mappedRawIssues = curAllRawIssues.stream()
                .filter(RawIssue::isOnceMapped)
                .collect(Collectors.toMap(RawIssue::getUuid, RawIssue::getIssueId));
        Set<String> mapAndMappedRawIssuesUuid = mappedRawIssues.keySet();
        // 2 ：进行过匹配且没匹配上的 rawIssue(用于产生新issue) @key uuid   （而且不输入没改变过的文件）
        Map<String, RawIssue> mapNotMappedRawIssues = curRawIssuesMatchResult.values().stream()
                .flatMap(Collection::stream)
                .filter(rawIssue -> !mapAndMappedRawIssuesUuid.contains(rawIssue.getUuid()) && !rawIssue.isNotChange())
                .collect(Collectors.toMap(RawIssue::getUuid, Function.identity(), (existing, replacement) -> existing));

        newIssues = mapNotMappedRawIssues.values()
                .stream()
                .map(this::generateOneIssue)
                .collect(Collectors.toMap(Issue::getUuid, Function.identity()));

        mapNotMappedRawIssues.values().forEach(rawIssue -> {
            List<RawIssueMatchInfo> rawIssueMatchInfos = rawIssue.getMatchInfos();
            rawIssueMatchInfos.clear();
            List<String> parentCommits = parentCommitsThreadLocal.get();
            parentCommits.forEach(c -> {
                RawIssueMatchInfo rawIssueMatchInfo = rawIssue.generateRawIssueMatchInfo(c);
                rawIssueMatchInfo.setStatus(RawIssueStatus.ADD.getType());
                rawIssueMatchInfos.add(rawIssueMatchInfo);
            });
        });

        // 3： 没有进行过匹配的rawIssue 不做记录

    }

    private void normalMatch(String repoId, String toolName, String preCommit, Map<String, List<RawIssue>> curRawIssuesMatchResult) {
        log.info("start  matching commit id --> {} ...", curCommit);
        //  根据preCommitId以及commitId得到两个commit文件之间的diff
        //  根据修改的文件来获取需要匹配的raw Issue key add delete change value
        //  add : ,a delete: a,   change a,a   英文逗号 ， 区分 add delete change
        //根据preCommitId以及commitId得到两个commit处理rename后的文件之间的diff
        Map<String, String> preFileToCurFile = new HashMap<>(8);
        Map<String, String> curFileToPreFile = new HashMap<>(8);
        List<String> diffFiles = jGitHelper.getDiffFilePair(preCommit, curCommit, preFileToCurFile, curFileToPreFile);
        String delimiter = ",";
        List<String> preFiles = diffFiles.stream().filter(d -> !d.startsWith(delimiter)).map(f -> Arrays.asList(f.split(delimiter)).get(0)).collect(Collectors.toList());
        List<String> curFiles = diffFiles.stream().filter(d -> !d.endsWith(delimiter)).map(f -> Arrays.asList(f.split(delimiter)).get(1)).collect(Collectors.toList());
        curFiles = curFiles.stream().filter(file -> analyzer instanceof SonarQubeBaseAnalyzer ? !FileFilter.javaFilenameFilter(file) : !FileFilter.jsFileFilter(file)).collect(Collectors.toList());
        preFiles = preFiles.stream().filter(file -> analyzer instanceof SonarQubeBaseAnalyzer ? !FileFilter.javaFilenameFilter(file) : !FileFilter.jsFileFilter(file)).collect(Collectors.toList());

        // pre commit中变化部分存在的所有rawIssues
        List<String> issueUuids = issueDao.getIssuesByFilesToolAndRepo(Stream.concat(preFiles.stream(), curFiles.stream()).collect(Collectors.toList()), repoId, toolName);
        List<RawIssue> preRawIssues = rawIssueDao.getLastVersionRawIssues(issueUuids);
        // 由于这里二进制流是对preRawIssues的引用,对preRawIssuesMap set locations等于set preRawIssues locations
        Map<String, List<RawIssue>> preRawIssuesMap = preRawIssues.stream().collect(Collectors.groupingBy(RawIssue::getUuid));
        preRawIssuesMap.forEach((key, value) ->
                value.forEach(preRawIssue -> preRawIssue.setLocations(locationDao.getLocations(key))));
        List<String> finalCurFiles = curFiles;
        List<RawIssue> curRawIssues = curAllRawIssues.stream().filter(r -> finalCurFiles.contains(r.getFileName())).collect(Collectors.toList());

        // 对其余的rawIssue设置一个默认匹配上的标记
        curAllRawIssues.stream().filter(r -> !curRawIssues.contains(r)).forEach(rawIssue -> rawIssue.setNotChange(true));

        // 匹配两个rawIssue集合（parent的rawIssue集合，当前的rawIssue集合）
//        log.info("cur all rawIssues:" + JSON.toJSONString(curAllRawIssues));
//        log.info("pre rawIssues:" + JSON.toJSONString(preRawIssues));
//        log.info("cur rawIssues:" + JSON.toJSONString(curRawIssues));
        mapRawIssues(preRawIssues, curRawIssues, jGitHelper.getRepoPath(), preFileToCurFile, curFileToPreFile);

        // 归总结果集 更新issue 的 end_commit 以及 status
        List<String> oldIssuesUuid = preRawIssues.stream().map(RawIssue::getIssueId).collect(Collectors.toList());
        Map<String, Issue> oldIssuesMap = issueDao.getIssuesByUuid(oldIssuesUuid).stream()
                .collect(Collectors.toMap(Issue::getUuid, Function.identity()));

        // 记录curRawIssues的匹配状态
        curRawIssues.stream()
                .filter(rawIssue -> rawIssue.getMatchInfos().isEmpty())
                .forEach(curRawIssue -> curRawIssue.getMatchInfos().add(curRawIssue.generateRawIssueMatchInfo(preCommit)));
        unifyOldIssueStatus(preRawIssues, preCommit, oldIssuesMap);

        // 清空curRawIssues的匹配状态
        curRawIssues.stream().filter(RawIssue::isMapped).forEach(curRawIssue -> curRawIssue.setOnceMapped(true));
        curRawIssues.stream().filter(RawIssue::isMapped).forEach(RawIssue::resetMappedInfo);
        for (RawIssue preRawIssue : preRawIssues) {
            if (!preRawIssue.isMapped()) {
                List<RawIssueMatchInfo> rawIssueMatchInfos = preRawIssue.getMatchInfos();
                RawIssueMatchInfo matchInfo = RawIssueMatchInfo.builder()
                        .uuid(UUID.randomUUID().toString())
                        .curRawIssueUuid(null)
                        .curCommitId(curCommit)
                        .preCommitId(preCommit)
                        .preRawIssueUuid(preRawIssue.getUuid())
                        .status(preRawIssue.getStatus()).build();
                rawIssueMatchInfos.add(matchInfo);
            }
        }

        parentRawIssuesResult.put(preCommit, preRawIssues);
        curRawIssuesMatchResult.put(preCommit, curRawIssues);
    }

    // solved rawIssue 存储一下matchInfo
    private void unifyOldIssueStatus(List<RawIssue> preRawIssues, String preCommit, Map<String, Issue> oldIssuesMap) {
        Date curCommitDate = DateTimeUtil.localToUtc(jGitHelper.getCommitTime(curCommit));
        Date preCommitDate = DateTimeUtil.localToUtc(jGitHelper.getCommitTime(preCommit));

        for (RawIssue preRawIssue : preRawIssues) {
            Issue issue = oldIssuesMap.get(preRawIssue.getIssueId());
            if (issue == null) {
                log.error("issue  uuid:[{}] get failed ", preRawIssue.getIssueId());
                continue;
            }
            // 默认 没有匹配上的情况
            String curStatus = IssueStatusEnum.SOLVED.getName();
            String endCommit = preCommit;
            Date endCommitDate = preCommitDate;

            // 匹配上
            if (preRawIssue.isMapped()) {
                // 匹配上了要看是否之前匹配上过其他的issue——id 是的话 将该issue设置为solved，否则的话设置最新的commit
                boolean notMatchOtherIssue = notMapOtherIssue(preRawIssue.getMappedRawIssue(), issue.getUuid());
                if (notMatchOtherIssue) {
                    curStatus = IssueStatusEnum.OPEN.getName();
                    endCommit = curCommit;
                    endCommitDate = curCommitDate;
                } else {
                    // fixme 匹配上了其他的issue id的话 有一个肯定是mergeSolved 一个是匹配上了 [先默认当前是mergeSolved]
                    //  此时该rawIssue 算作没有匹配上
                    preRawIssue.setMapped(false);
                }
            } else if (IssueStatusEnum.SOLVED.getName().equals(issue.getStatus())) {
                // 没有匹配上 且以前就为solved
                continue;
            }

            boolean isCurCommitBeforePre = issue.getEndCommitDate() != null && endCommitDate.before(issue.getEndCommitDate());
            String preStatus = issue.getStatus();
            if (isCurCommitBeforePre) {
                endCommit = issue.getEndCommit();
                endCommitDate = issue.getEndCommitDate();
            }

            issue.setEndCommit(endCommit);
            issue.setEndCommitDate(endCommitDate);
            issue.setStatus(curStatus);

            if (IssueStatusEnum.SOLVED.getName().equals(curStatus)) {
                solvedIssue.put(issue.getUuid(), issue);
            } else {
                mappedIssues.put(issue.getUuid(), issue);
            }
            if (isCurCommitBeforePre) {
                issue.setStatus(preStatus);
            }
        }
    }

    private boolean notMapOtherIssue(RawIssue curRawIssue, String issueUuid) {
        List<RawIssueMatchInfo> matchInfos = curRawIssue.getMatchInfos();
        if (matchInfos == null || matchInfos.isEmpty()) {
            return true;
        }
        for (RawIssueMatchInfo matchInfo : matchInfos) {
            if (!matchInfo.getIssueUuid().equals(issueUuid)) {
                matchInfo.setStatus(RawIssueStatus.MERGE_SOLVED.getType());
                return false;
            }
        }
        return true;
    }

    // @review 存在 多分支的情况下匹配到不同的issueId  设置一个为merger solve
    private void mergeMatch(String repoUuid, String toolName, List<String> parentCommits, Map<String, List<RawIssue>> curRawIssuesMatchResult) {
        log.info("start merge matching commit id --> {} ...", curCommit);
        // key parentCommitId value parentRawIssues
        Map<String, List<RawIssue>> preRawIssuesMap = new LinkedHashMap<>(parentCommits.size() << 1);
        // 得到所有待匹配的组合 两两按照normalMatch匹配
        for (String parentCommit : parentCommits) {
//            log.info("curRawIssuesMatchResult:" + JSON.toJSONString(curRawIssuesMatchResult));
            normalMatch(repoUuid, toolName, parentCommit, curRawIssuesMatchResult);
            preRawIssuesMap.put(parentCommit, parentRawIssuesResult.get(parentCommit));
        }
        parentRawIssuesResult = preRawIssuesMap;
    }

    private void mapRawIssues(List<RawIssue> preRawIssues, List<RawIssue> curRawIssues, String repoPath, Map<String, String> preFileToCurFile, Map<String, String> curFileToPreFile) {

        preRenameHandle(preRawIssues, preFileToCurFile);

        // key fileName
        Map<String, List<RawIssue>> preRawIssueMap = preRawIssues.stream().collect(Collectors.groupingBy(RawIssue::getFileName));
        Map<String, List<RawIssue>> curRawIssueMap = curRawIssues.stream().collect(Collectors.groupingBy(RawIssue::getFileName));

        preRawIssueMap.entrySet().stream()
                .filter(e -> curRawIssueMap.containsKey(e.getKey()))
                .forEach(pre -> RawIssueMatcher.match(pre.getValue(), curRawIssueMap.get(pre.getKey()),
                        analyzer instanceof EsLintBaseAnalyzer ? analyzer.getMethodsAndFieldsInFile().getOrDefault(repoPath + "/" + pre.getKey(), new HashSet<>()) :
                                AstParserUtil.getAllMethodAndFieldName(repoPath + "/" + pre.getKey())));

        cleanUpRenameHandle(preRawIssues, curFileToPreFile);
    }

    private void cleanUpRenameHandle(List<RawIssue> preRawIssues, Map<String, String> curFileToPreFile) {
        preRawIssues.stream()
                .filter(r -> curFileToPreFile.containsKey(r.getFileName()))
                .forEach(rawIssue -> {
                    rawIssue.getLocations().forEach(location -> location.setFilePath(curFileToPreFile.get(rawIssue.getFileName())));
                    rawIssue.setFileName(curFileToPreFile.get(rawIssue.getFileName()));
                });
    }

    private void preRenameHandle(List<RawIssue> preRawIssues, Map<String, String> preFileToCurFile) {
        //pre file path and className change to cur
        preRawIssues.stream()
                .filter(r -> preFileToCurFile.containsKey(r.getFileName()))
                .forEach(rawIssue -> {
                    rawIssue.getLocations().forEach(location -> location.setFilePath(preFileToCurFile.get(rawIssue.getFileName())));
                    rawIssue.setFileName(preFileToCurFile.get(rawIssue.getFileName()));
                });
    }

    private List<String> getPreScanSuccessfullyCommit(String repoId, String commitId, JGitHelper jGitHelper, String tool) {

        List<IssueScan> scanList = issueScanDao.getIssueScanByRepoIdAndStatusAndTool(repoId, null, tool);
        if (scanList == null || scanList.isEmpty()) {
            return new ArrayList<>(0);
        }

        String[] scannedCommitIds = scanList.stream().map(IssueScan::getCommitId).toArray(String[]::new);

        List<MatcherCommitInfo> parentCommits = new ArrayList<>();
        parentCommits.add(new MatcherCommitInfo(repoId, commitId, jGitHelper.getCommitTime(commitId)));

        Set<String> scannedParents = new HashSet<>(8);
        while (!parentCommits.isEmpty()) {
            MatcherCommitInfo matcherCommitInfo = parentCommits.remove(0);
            String[] parents = jGitHelper.getCommitParents(matcherCommitInfo.getCommitId());

            for (String parent : parents) {
                int index = SearchUtil.dichotomy(scannedCommitIds, parent);
                if (index == -1) {
                    continue;
                }

                if (ScanStatusEnum.DONE.getType().equals(scanList.get(index).getStatus())) {
                    scannedParents.add(parent);
                    continue;
                }
                parentCommits.add(new MatcherCommitInfo(repoId, parent, jGitHelper.getCommitTime(commitId)));
                // parentCommits 根据时间由远及近排序 第一个是时间最小的
                parentCommits = parentCommits.stream().distinct()
                        .sorted(Comparator.comparing(MatcherCommitInfo::getCommitTime)).collect(Collectors.toList());
            }
        }
        return new ArrayList<>(scannedParents);
    }

    @Autowired
    public IssueMatcher(IssueScanDao issueScanDao, RawIssueDao rawIssueDao, IssueDao issueDao, IssueTypeDao issueTypeDao, LocationDao locationDao) {
        IssueMatcher.issueScanDao = issueScanDao;
        IssueMatcher.rawIssueDao = rawIssueDao;
        IssueMatcher.issueDao = issueDao;
        IssueMatcher.issueTypeDao = issueTypeDao;
        IssueMatcher.locationDao = locationDao;
    }
}
