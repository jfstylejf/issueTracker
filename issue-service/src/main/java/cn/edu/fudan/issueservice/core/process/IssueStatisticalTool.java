package cn.edu.fudan.issueservice.core.process;

import cn.edu.fudan.issueservice.dao.IssueDao;
import cn.edu.fudan.issueservice.dao.IssueScanDao;
import cn.edu.fudan.issueservice.dao.IssueTypeDao;
import cn.edu.fudan.issueservice.domain.dbo.*;
import cn.edu.fudan.issueservice.domain.ScanResult;
import cn.edu.fudan.issueservice.domain.enums.IssueStatusEnum;
import cn.edu.fudan.issueservice.domain.enums.ScanStatusEnum;
import cn.edu.fudan.issueservice.util.DateTimeUtil;
import cn.edu.fudan.issueservice.util.JGitHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author lsw
 */
@Slf4j
public class IssueStatisticalTool {


    private String commitId;

    private int newIssueCount = 0;
    private int solvedDuplicateElimination = 0;
    private int openDuplicateElimination = 0;
    private int normalElimination = 0;
    private int reopenIssueCount = 0;
    private int normalMatchedIssueCount = 0;
    private int remainIssueChangedCount = 0;

    private int currentDisplayId = 1;

    private volatile boolean  isDefaultDisplayId = true;

    private boolean isMerge = false;
    private boolean isMergeConflict = false;
    private boolean isFirstScanCommit = false;
    private Date currentCommitDate;
    private Date addDate;
    private ScanResult scanResultGlobal;

    public IssueStatisticalTool(IssueDao issueDao, IssueScanDao issueScanDao, IssueTypeDao issueTypeDao){
        this.issueDao = issueDao;
        this.issueScanDao = issueScanDao;
        this.issueTypeDao = issueTypeDao;
    }

    private IssueDao issueDao;
    private IssueScanDao issueScanDao;
    private IssueTypeDao issueTypeDao;

    private Map<String, List<RawIssue>> ignoreParentRawIssuesResult ;
    private List<RawIssue> ignoreCurrentRawIssuesResult;

    private Map<String, List<RawIssue>> normalParentRawIssuesResult ;
    private List<RawIssue> normalCurrentRawIssuesResult;

    private List<Issue> allInsertIssues = new ArrayList<> ();
    private List<Issue> allEliminatedIssues = new ArrayList<> ();
    private List<Issue> allMatchedIssues = new ArrayList<> ();


    public boolean doingStatisticalAnalysis(String repoId, String commitId, JGitHelper jGitInvoker,
                                            List<RawIssue> currentRawIssuesResult,
                                            Map<String, List<RawIssue>> parentRawIssuesResult,
                                            BaseAnalyzer analyzer){
        this.commitId = commitId;

        try{
            init(jGitInvoker);
            /*
            1. 先根据ignore 规则，过滤RawIssue.
            Ignore 类型的 rawIssue匹配的Issue只执行issue的状态更新操作,不计入缺陷更改数据统计中
             */
            filterIgnore(currentRawIssuesResult, parentRawIssuesResult);

            //2. 开始匹配更新issue状态，以及缺陷统计数据。
            startStatistics(ignoreParentRawIssuesResult, ignoreCurrentRawIssuesResult,
                    true, analyzer);
            startStatistics(normalParentRawIssuesResult, normalCurrentRawIssuesResult,
                    false, analyzer);

            //3. 整理统计结果
            sortOutStatisticalResult(jGitInvoker,repoId,analyzer.getToolName ());


        }catch(Exception e){
            e.printStackTrace ();
            return false;
        }
        return true;
    }


    public void emptyStatisticalResult(){
        commitId = null;
        newIssueCount = 0;
        solvedDuplicateElimination = 0;
        openDuplicateElimination = 0;
        normalElimination = 0;
        reopenIssueCount = 0;
        normalMatchedIssueCount = 0;
        remainIssueChangedCount = 0;

        currentDisplayId = 1;
        isDefaultDisplayId = true;

        ignoreParentRawIssuesResult = null;
        ignoreCurrentRawIssuesResult = null;
        normalParentRawIssuesResult = null;
        normalCurrentRawIssuesResult = null;

        allInsertIssues = new ArrayList<> ();
        allEliminatedIssues = new ArrayList<> ();
        allMatchedIssues = new ArrayList<> ();

        isMerge = false;
        isMergeConflict = false;
        isFirstScanCommit = false;
        currentCommitDate = null;
        addDate = null;
        scanResultGlobal = null;
    }

    public ScanResult getStatisticalResult(){
        return scanResultGlobal;
    }

    public List<Issue> getAllInsertIssues(){
        return allInsertIssues;
    }

    public List<Issue> getAllEliminatedIssues(){
        return allEliminatedIssues;
    }

    public List<Issue> getAllMatchedIssues(){
        return allMatchedIssues;
    }


    private void sortOutStatisticalResult(JGitHelper jGitInvoker,
                                          String repoId,
                                          String toolName){

        String currentCommitter = jGitInvoker.getAuthorName (commitId);
        String issueCommitter;
        int newIssueCountResult = 0;
        int eliminateIssueCountResult = 0;
        int remainIssueChangedCountResult;

        // todo 目前暂不考虑merge存在冲突的情况。  后面改成switch case
        if(isFirstScanCommit){
            issueCommitter = currentCommitter;
            remainIssueChangedCountResult = newIssueCount;
        }else{
            if(!isMerge){
                issueCommitter = currentCommitter;
                newIssueCountResult = newIssueCount;
                eliminateIssueCountResult = solvedDuplicateElimination + normalElimination;
                remainIssueChangedCountResult = newIssueCount - normalElimination + reopenIssueCount;

            }else{
                String preScanFailedCommitId = getPreScannedFailedCommit(jGitInvoker,repoId,toolName,commitId);
                if(preScanFailedCommitId != null){
                    issueCommitter = jGitInvoker.getAuthorName (preScanFailedCommitId);
                }else{
                    issueCommitter = currentCommitter;
                }
                newIssueCountResult = newIssueCount;
                eliminateIssueCountResult = normalElimination;
                // todo 如果存在冲突的话 该值可能有改变
                remainIssueChangedCountResult = newIssueCount - normalElimination + reopenIssueCount - openDuplicateElimination;

            }
        }

        remainIssueChangedCount = remainIssueChangedCountResult;

        List<String> statusList = new ArrayList<> ();
        statusList.add (IssueStatusEnum.IGNORE.getName ());
        statusList.add (IssueStatusEnum.MISINFORMATION.getName ());
        statusList.add (IssueStatusEnum.SOLVED.getName ());

        //fixme 剩余缺陷数量统计方式 有待确认
        List<Issue> issue = issueDao.getIssueByRepoIdAndToolAndStatusListAndTypeList(repoId,toolName,statusList);
        int remainIssueCount = issue.size () + remainIssueChangedCount;

        scanResultGlobal = new ScanResult (toolName, repoId, addDate, commitId, currentCommitDate,
                issueCommitter,newIssueCountResult,eliminateIssueCountResult,remainIssueCount);
    }

    private void init(JGitHelper jGitInvoker){
        String[] parentCommits = jGitInvoker.getCommitParents (commitId);
        isMerge = parentCommits.length > 1  ;
        if(isMerge){
            int isConflict =  jGitInvoker.mergeJudgment(commitId);
            isMergeConflict = isConflict == JGitHelper.getConflictValue ();
        }

        currentCommitDate = getCommitDate(commitId,jGitInvoker);
        addDate = new Date ();
    }

    private void startStatistics(Map<String, List<RawIssue>> parentRawIssuesResult,
                                 List<RawIssue> currentRawIssuesResult,
                                 boolean isIgnore,
                                 BaseAnalyzer analyzer){

        /*
            分为三种匹配统计结果：
            第一种：新增issue
            第二种：匹配上，更新issue状态
            第三种：关闭issue
         */

        List<Issue> insertIssues = new ArrayList<> ();
        List<Issue> eliminatedIssues = new ArrayList<> ();
        List<Issue> matchedIssues = new ArrayList<> ();

        //新增issue
        for(RawIssue rawIssue : currentRawIssuesResult){
            if (rawIssue.isMapped()) {
                String matchedIssueId = rawIssue.getRawIssueMatchResults().get (rawIssue.getMatchResultDTOIndex ()).getMatchedIssueId ();
                rawIssue.setIssue_id (matchedIssueId);
                continue;
            }

            Issue issue = generateOneNewIssue (rawIssue, currentCommitDate, addDate, analyzer, isIgnore);
            //resetProducer(rawIssue, issue, jGitInvoker);
            insertIssues.add (issue);
            rawIssue.setIssue_id (issue.getUuid ());
            if(!isIgnore){
                newIssueCount++;
            }
        }

        String solver = currentRawIssuesResult.size() == 0 ? null : currentRawIssuesResult.get(0).getDeveloperName();

        //匹配与消除
        for(Map.Entry<String, List<RawIssue>> entry : parentRawIssuesResult.entrySet ()){
            List<RawIssue> parentRawIssues = entry.getValue ();
            for(RawIssue parentRawIssue : parentRawIssues){
                if(!parentRawIssue.isMapped ()){
                    //此时认为是消除缺陷
                    Issue eliminatedIssue = eliminateIssue(parentRawIssue, isIgnore);
                    eliminatedIssue.setSolver(solver);
                    parentRawIssue.setIssue (eliminatedIssue);
                    eliminatedIssues.add (eliminatedIssue);
                }else{
                    //更新issue的状态
                    Issue matchedIssue = matchedIssue(parentRawIssue, isIgnore, currentCommitDate, addDate);
                    matchedIssue.setSolver(null);
                    matchedIssues.add (matchedIssue);
                }
            }

        }


        allInsertIssues.addAll (insertIssues);
        allEliminatedIssues.addAll (eliminatedIssues);
        allMatchedIssues.addAll (matchedIssues);

    }

    private Issue matchedIssue(RawIssue parentRawIssue, boolean isIgnore, Date currentCommitDate, Date addDate){
        Issue issue = issueDao.getIssueByID(parentRawIssue.getIssue_id());
        //如果issue的最近状态为solved，则更新为open，且reopen次数加1
        if(IssueStatusEnum.SOLVED.getName().equals(issue.getStatus ())){
            if(issue.getManual_status() != null){
                issue.setStatus(issue.getManual_status());
            }else{
                issue.setStatus(IssueStatusEnum.OPEN.getName());
            }
            if(!isIgnore){
                reopenIssueCount++;
            }
            String resolution = issue.getResolution();
            if(resolution == null){
                issue.setResolution(String.valueOf(1));
            }else{
                issue.setResolution(String.valueOf(Integer.parseInt(resolution) + 1));
            }

        }else{
            if(!isIgnore){
                normalMatchedIssueCount++;
            }
        }
        issue.setEnd_commit(commitId);
        issue.setEnd_commit_date(currentCommitDate);
        issue.setUpdate_time(addDate);
        return issue;
    }

    private Issue eliminateIssue(RawIssue parentRawIssue, boolean isIgnore){
        Issue issue = issueDao.getIssueByID(parentRawIssue.getIssue_id());
        String resolution = issue.getResolution();
        if(IssueStatusEnum.SOLVED.getName().equals(issue.getStatus())){
            if(!isIgnore){
                if(!isMerge){
                    if(resolution == null){
                        issue.setResolution(String.valueOf(1));
                    }else{
                        issue.setResolution(String.valueOf(Integer.parseInt(resolution) + 1));
                    }
                    parentRawIssue.setRealEliminate (true);
                    solvedDuplicateElimination++;
                }else{
                    //todo 暂不考虑merge冲突
                    if(!(resolution == null ||  Integer.parseInt(resolution) == 0)){
                        issue.setResolution (String.valueOf(Integer.parseInt(resolution) - 1));
                    }
                }

            }

        }else{
            issue.setStatus(IssueStatusEnum.SOLVED.getName());
            if(!isIgnore){
                //todo 如果存在冲突 后面结合diff 再做分类分析 暂不考虑在merge点有冲突的情况，是不是人为消除了缺陷
                if(isMerge){
                    if(resolution == null ||  Integer.parseInt(resolution) == 0){
                        parentRawIssue.setRealEliminate (true);
                        normalElimination++;
                    }else{
                        issue.setResolution (String.valueOf(Integer.parseInt(resolution) - 1));
                        openDuplicateElimination++;
                    }
                }else{
                    normalElimination++;
                    parentRawIssue.setRealEliminate (true);
                }
            }
        }
        return issue;
    }


    private void filterIgnore(List<RawIssue> currentRawIssuesResult,
                              Map<String, List<RawIssue>> parentRawIssuesResult){
        /*  TODO
            分两部分筛选：
            第一部分是根据ignore record表的统一管理进行筛选
            第二部分是根据issue单独进行标记为 ignore misinformation等进行筛选
         */
        if(parentRawIssuesResult == null){
            isFirstScanCommit = true;
            normalParentRawIssuesResult = new HashMap<> (64);
        }else {
            normalParentRawIssuesResult = parentRawIssuesResult;
        }
        if(currentRawIssuesResult == null){
            normalCurrentRawIssuesResult = new ArrayList<> ();
        }else{
            normalCurrentRawIssuesResult = currentRawIssuesResult;
        }


        ignoreParentRawIssuesResult = new HashMap<> ();
        ignoreCurrentRawIssuesResult = new ArrayList<> ();

    }


    private Issue generateOneNewIssue(RawIssue rawIssue, Date commitDate, Date addTime, BaseAnalyzer analyzer, boolean isIgnore){
        String repoId = rawIssue.getRepo_id ();
        String commitId = rawIssue.getCommit_id ();
        String toolName = rawIssue.getTool ();
        String newIssueId = UUID.randomUUID().toString();
        Integer priority = analyzer.getPriorityByRawIssue (rawIssue);

        rawIssue.setIssue_id(newIssueId);
        String targetFiles = rawIssue.getFile_name();
        boolean hasDisplayId = issueDao.getMaxIssueDisplayId(repoId) != null;
        if (isDefaultDisplayId){
            currentDisplayId = hasDisplayId ? issueDao.getMaxIssueDisplayId(repoId) : 0;
            isDefaultDisplayId = false;
        }

        Issue issue = new Issue(newIssueId, rawIssue.getType(), toolName, commitId,
                commitDate, commitId, commitDate, repoId, targetFiles, addTime, addTime, ++currentDisplayId,
                priority, rawIssue.getDeveloperName(), null);
        IssueType issueType = issueTypeDao.getIssueTypeByTypeName (rawIssue.getType ());
        if(issueType != null){
            issue.setIssueCategory (issueType.getCategory ());
        }else{
            log.error ("this type --> {} has bot been recorded in db!", rawIssue.getType ());
        }

        if(isIgnore){
            issue.setStatus(IssueStatusEnum.IGNORE.getName());
        }else{
            issue.setStatus(IssueStatusEnum.OPEN.getName());
        }

        return issue;
    }

    /**
     *  获取commit 时间 ，可能还存在bug
     * @param commitId commitId
     * @param jGitInvoker jGit
     * @return commit time
     */
    private Date getCommitDate(String commitId, JGitHelper jGitInvoker){
        return DateTimeUtil.localToUTC(jGitInvoker.getCommitTime(commitId));
    }

    private String getPreScannedFailedCommit(JGitHelper jGitHelper, String repoId, String tool, String commitId) {
        String[] parents = jGitHelper.getCommitParents(commitId);
        for(String parent : parents){
            IssueScan  issueScan = issueScanDao.getIssueScanByRepoIdAndCommitIdAndTool(repoId,parent,tool);
            if(issueScan == null || !ScanStatusEnum.DONE.getType ().equals(issueScan.getStatus())){
                return parent;
            }
        }
        return null;
    }

}
