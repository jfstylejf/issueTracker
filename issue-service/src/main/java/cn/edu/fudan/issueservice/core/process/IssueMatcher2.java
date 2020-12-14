package cn.edu.fudan.issueservice.core.process;

import cn.edu.fudan.issueservice.core.strategy.MatchStrategy;
import cn.edu.fudan.issueservice.core.strategy.RawIssueMatcher;
import cn.edu.fudan.issueservice.dao.IssueDao;
import cn.edu.fudan.issueservice.dao.IssueScanDao;
import cn.edu.fudan.issueservice.dao.RawIssueDao;
import cn.edu.fudan.issueservice.domain.dbo.Issue;
import cn.edu.fudan.issueservice.domain.dbo.IssueScan;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.domain.dto.MatcherCommitInfo;
import cn.edu.fudan.issueservice.domain.dto.RawIssueMatchResult;
import cn.edu.fudan.issueservice.domain.enums.ScanStatusEnum;
import cn.edu.fudan.issueservice.util.AstParserUtil;
import cn.edu.fudan.issueservice.util.JGitHelper;
import cn.edu.fudan.issueservice.util.SearchUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * description:
 *
 * @author fancying
 * create: 2020-05-20 16:56
 **/
@Slf4j
@SuppressWarnings("Duplicated")
public class IssueMatcher2 {
//
//    //TODO 关于RawIssue的status 状态更改还没做
//
//    private IssueScanDao issueScanDao;
//    private RawIssueDao rawIssueDao;
//    private IssueDao issueDao;
//    private MatchStrategy matchStrategy;
//
//    public IssueMatcher2(IssueDao issueDao, RawIssueDao rawIssueDao, IssueScanDao issueScanDao, MatchStrategy matchStrategy){
//        this.issueDao = issueDao;
//        this.rawIssueDao = rawIssueDao;
//        this.issueScanDao = issueScanDao;
//        this.matchStrategy = matchStrategy;
//    }
//
//    private List<RawIssue> currentRawIssuesResult;
//    private Map<String, List<RawIssue>> parentRawIssuesResult;
//    private List<String> bestMappedPreRawIssueIds = new ArrayList<> ();
//
//
//    public List<RawIssue> getCurrentRawIssuesResult() {
//        return currentRawIssuesResult;
//    }
//
//    public Map<String, List<RawIssue>> getParentRawIssuesResult() {
//        return parentRawIssuesResult;
//    }
//
//    public void emptyMatchResult(){
//        currentRawIssuesResult = null;
//        parentRawIssuesResult = null;
//        bestMappedPreRawIssueIds = new ArrayList<> ();
//    }
//
//    public IssueMatcher2(){
//    }
//
//
//    public boolean matchProcess(String repoUuid, String commitId, JGitHelper jGitHelper, String toolName, List<RawIssue> analyzeRawIssues){
//        try{
//            jGitHelper.checkout(commitId);
//            //判断当前commit是否是第一个扫描的commit
//            List<String> parentCommits =  getPreScanSuccessfullyCommit(repoUuid, commitId, jGitHelper, toolName);
//            parentCommits.forEach(c -> log.debug ( "{} --> pre cn.edu.fudan.common.scan success commit --> {} ",commitId, c));
//
//            if (analyzeRawIssues == null || analyzeRawIssues.isEmpty()) {
//                log.warn("all issues were solved or raw issue insert error , commit id -->  {}", commitId);
//            }
//
//            //如果当前的commit没有任何parent commit扫描过，则认为该commit是第一次扫描
//            if(parentCommits.size () == 1){
//                normalMatch(repoUuid, toolName, commitId, parentCommits.get (0), analyzeRawIssues, jGitHelper);
//                return true;
//            }
//
//            // 第一次匹配 不做任何事情
//            if(parentCommits.isEmpty ()){
//                log.info ("start first matching  ...");
//                currentRawIssuesResult = analyzeRawIssues;
//                return true;
//            }
//
//            mergeMatch(repoUuid, toolName, commitId, parentCommits, analyzeRawIssues);
//            return true;
//        }catch(Exception e){
//            e.printStackTrace ();
//            return false;
//        }
//
//    }
//
//
//    private void normalMatch(String repoId, String toolName, String commitId, String preCommitId, List<RawIssue> currentRawIssues, JGitHelper jGitHelper){
//        log.info ("start  matching commit id --> {} ...", commitId);
//        currentRawIssuesResult = new ArrayList<> ();
//        parentRawIssuesResult = new HashMap<> (2);
//
//        // todo 根据preCommitId 以及 commitId 得到两个commit文件之间的diff
//        //  根据修改的文件来获取需要匹配的raw Issue key add delete change value
//        //  add : a, delete: ,a   change a,a   英文逗号 ， 区分 add delete change
//        List<String> diffFiles = jGitHelper.getDiffFilePair(preCommitId, commitId);
//        String delimiter = ",";
//        List<String> preFiles = diffFiles.stream().filter(d -> !d.startsWith(delimiter)).map(r -> Arrays.asList(r.split(delimiter)).get(0)).collect(Collectors.toList());
//
//        // pre commit 中变化部分存在的所有 raw issue
//        List<RawIssue> preRawIssues = rawIssueDao.getRawIssueByRepoIdFileNameTool(repoId, preFiles, toolName);
//        /// List<RawIssue> preRawIssues = rawIssueDao.getRawIssueByCommitIDAndTool(repoId, toolName, preCommitId);
//
//
//        // 匹配两个rawIssue集合（parent的rawIssue集合，当前的rawIssue集合）
//        mapRawIssues(preRawIssues, currentRawIssues, jGitHelper.getRepoPath());
//
//        parentRawIssuesResult.put (preCommitId, preRawIssues);
//        currentRawIssuesResult = currentRawIssues;
//    }
//
//
//    private void mergeMatch(String repoId, String toolName, String commitId, List<String> parentCommits, List<RawIssue> currentRawIssues){
//        log.info ("start merge matching commit id --> {} ...", commitId);
//        if (currentRawIssues == null || currentRawIssues.isEmpty()) {
//            log.warn("all issues were solved or raw issue insert error , commit id -->  {}", commitId);
//        }
//        // key parentCommitId value parentRawIssues
//        Map<String, List<RawIssue>> matchPreRawIssueLists = new HashMap<> (4);
//
//        // key parentCommitId value [k parentRawIssueId v parentRawIssue ]
//        Map<String, Map<String, RawIssue>> matchPreRawIssueMaps = new HashMap<> (4);
//        int parentCommitsSize = parentCommits.size();
//
//        // key curRawIssueId value 匹配上的 preRawIssue
//        Map<String,List<RawIssueMappingSort>> currentRawIssueMatchedPreRawIssues = new HashMap<> ();
//
//        //用于全匹配的issue列表 parentCommits 包含的所有 issue
//        List<Issue>  allIssues = new ArrayList<>();
//
//
//
//        for(int j = 0; j < parentCommitsSize ; j++) {
//            List<RawIssue> parentRawIssues = rawIssueDao.getRawIssueByCommitIDAndTool (repoId, toolName, parentCommits.get (j));
//            if (parentRawIssues.size() == 0) {
//                continue;
//            }
//
//            //filterRawIssue这个方法会更新allIssues，把所有parent的不同的issue存到allIssues，
//            // 并且返回当前parent里的与之前parent里的不重复的rawIssue列表，返回给parentRawIssues
//            parentRawIssues = filterRawIssue (parentRawIssues, allIssues);
//
//            // key 是 parent raw issue 的 uuid , value 是对应的 parent raw issue
//            Map<String, RawIssue> parentRawIssuesMap = new HashMap<> ();
//            parentRawIssues.forEach(r -> parentRawIssuesMap.put(r.getUuid(), r));
//
//            matchPreRawIssueMaps.put (parentCommits.get (j), parentRawIssuesMap);
//            matchPreRawIssueLists.put (parentCommits.get (j), parentRawIssues);
//
//
//            mapRawIssues(parentRawIssues, currentRawIssues, repoPath);
//
//            for(RawIssue rawIssue : currentRawIssues){
//                int index = rawIssue.getMatchResultDTOIndex ();
//                if(index == -1){
//                    continue;
//                }
//                RawIssueMatchResult matchResultDTO = rawIssue.getRawIssueMatchResults().get (index);
//
//                RawIssueMappingSort rawIssueMappingSort = new RawIssueMappingSort ();
//                rawIssueMappingSort.setCommitId (parentCommits.get (j));
//                rawIssueMappingSort.setRawIssueId (matchResultDTO.getMatchedRawIssueId ());
//                rawIssueMappingSort.setMappingSort (matchResultDTO.getMatchingDegree ());
//                rawIssueMappingSort.setMatchedIssueId (matchResultDTO.getMatchedIssueId ());
//                rawIssueMappingSort.setBestMatch (matchResultDTO.isBestMatch ());
//
//                List<RawIssueMappingSort> rawIssueMappingSorts = currentRawIssueMatchedPreRawIssues.get (rawIssue.getUuid ());
//                if(rawIssueMappingSorts == null){
//                    rawIssueMappingSorts = new ArrayList<> ();
//                }
//                rawIssueMappingSorts.add (rawIssueMappingSort);
//                currentRawIssueMatchedPreRawIssues.put (rawIssue.getUuid (), rawIssueMappingSorts);
//
//            }
//
//            //把currentRawIssues的匹配状态全部初始化，准备与下一个parent 的rawIssue做匹配
//            for(RawIssue rawIssue : currentRawIssues){
//
//                rawIssue.setRawIssueMatchResults(new ArrayList<> ());
//                rawIssue.setMapped (false);
//                rawIssue.setMatchResultDTOIndex (-1);
//            }
//
//        }
//
//        for(RawIssue rawIssue : currentRawIssues){
//            List<RawIssueMappingSort> rawIssueMappingSorts = currentRawIssueMatchedPreRawIssues.get (rawIssue.getUuid ());
//            if(rawIssueMappingSorts == null){
//                continue;
//            }
//            String matchRawIssueId = null;
//            double maxScore = 0;
//            String matchedIssueId = null;
//            boolean isBestMatch = false;
//            String matchedCommitId = null;
//            for(RawIssueMappingSort rawIssueMappingSort : rawIssueMappingSorts){
//                if(rawIssueMappingSort.getMappingSort () > maxScore){
//                    maxScore = rawIssueMappingSort.getMappingSort ();
//
//                    if(matchRawIssueId != null){
//                        RawIssue preMatchedRawIssue = matchPreRawIssueMaps.get (matchedCommitId).get (matchRawIssueId);
//                        preMatchedRawIssue.setMapped (false);
//                        preMatchedRawIssue.setMatchResultDTOIndex (-1);
//                    }
//                    matchRawIssueId = rawIssueMappingSort.getRawIssueId ();
//                    matchedIssueId = rawIssueMappingSort.getMatchedIssueId ();
//                    isBestMatch = rawIssueMappingSort.isBestMatch ();
//                    matchedCommitId = rawIssueMappingSort.getCommitId ();
//
//                }else{
//                    //todo 当前面有多次编译失败时，报空指针异常
//                    Map<String, RawIssue> map =matchPreRawIssueMaps.get (rawIssueMappingSort.getCommitId ());
//                    if(map == null){
//                        log.error ("can not get pre commit raw issues, pre commit id ---> {} ,and matched raw issue --> {}" ,
//                                rawIssueMappingSort.getCommitId (),
//                                matchRawIssueId);
//                    }
//                    RawIssue preMatchedRawIssue = matchPreRawIssueMaps.get (rawIssueMappingSort.getCommitId ()).get (rawIssueMappingSort.getRawIssueId ());
//                    preMatchedRawIssue.setMapped (false);
//                    preMatchedRawIssue.setMatchResultDTOIndex (-1);
//                }
//            }
//
//            if (matchRawIssueId != null ) {
//                RawIssueMatchResult rawIssueMatchResult = new RawIssueMatchResult();
//                rawIssueMatchResult.setMatchedRawIssueId (matchRawIssueId);
//                rawIssueMatchResult.setMatchingDegree (maxScore);
//                rawIssueMatchResult.setMatchedIssueId (matchedIssueId);
//                rawIssueMatchResult.setBestMatch (isBestMatch);
//                rawIssue.getRawIssueMatchResults().add (rawIssueMatchResult);
//                rawIssue.setMapped (true);
//                rawIssue.setMatchResultDTOIndex (0);
//            }
//
//        }
//
//        parentRawIssuesResult = matchPreRawIssueLists ;
//        currentRawIssuesResult = currentRawIssues;
//    }
//
//    private void mapRawIssues(List<RawIssue> preRawIssues, List<RawIssue> curRawIssues, String repoPath){
//        // key fileName
//        Map<String, List<RawIssue>> preRawIssueMap = preRawIssues.stream().collect(Collectors.groupingBy(RawIssue::getFile_name));
//        Map<String, List<RawIssue>> curRawIssueMap = curRawIssues.stream().collect(Collectors.groupingBy(RawIssue::getFile_name));
//
//        preRawIssueMap.entrySet().stream()
//                .filter(e -> curRawIssueMap.containsKey(e.getKey()))
//                .forEach(pre -> RawIssueMatcher.match(pre.getValue(), curRawIssueMap.get(pre.getKey()),
//                        AstParserUtil.getAllMethodAndFieldName(repoPath + pre.getKey())));
//    }
//
//    private List<String> getPreScanSuccessfullyCommit(String repoId, String commitId,JGitHelper jGitHelper, String tool){
//        List<String > scannedParents = new ArrayList<> ();
//
//        List<IssueScan> scanList = issueScanDao.getIssueScanByRepoIdAndStatusAndTool(repoId,null,tool);
//        if(scanList == null || scanList.isEmpty ()){
//            return scannedParents;
//        }
//        int scannedCommitListSize = scanList.size ();
//        String[] scannedCommitIds =  new String[scannedCommitListSize];
//
//        for(int i = 0 ; i < scannedCommitListSize ; i++){
//            scannedCommitIds[i] = scanList.get (i).getCommitId ();
//        }
//
//        List<MatcherCommitInfo> parentCommits = new ArrayList<> ();
//        MatcherCommitInfo commitInfoFirst = new MatcherCommitInfo ();
//        commitInfoFirst.setRepoId (repoId);
//        commitInfoFirst.setCommitId (commitId);
//        commitInfoFirst.setCommitTime (jGitHelper.getCommitTime (commitId));
//
//        parentCommits.add(commitInfoFirst);
//        while (!parentCommits.isEmpty()){
//            MatcherCommitInfo matcherCommitInfo = parentCommits.remove (0);
//            String[] parents = jGitHelper.getCommitParents(matcherCommitInfo.getCommitId ());
//            for(String parent : parents){
//                int index = SearchUtil.dichotomy (scannedCommitIds,parent);
//                if( index == -1){
//                    continue;
//                }
//
//                if(ScanStatusEnum.DONE.getType ().equals (scanList.get (index).getStatus ())){
//                    scannedParents.add(parent);
//                }else{
//                    MatcherCommitInfo commitInfoNew = new MatcherCommitInfo ();
//                    commitInfoNew.setRepoId (repoId);
//                    commitInfoNew.setCommitId (parent);
//                    commitInfoNew.setCommitTime (jGitHelper.getCommitTime (commitId));
//                    parentCommits.add(commitInfoNew);
//                    parentCommits = parentCommits.stream ().distinct ().sorted (Comparator.comparing (MatcherCommitInfo:: getCommitTime).reversed ()).collect(Collectors.toList());
//                }
//
//            }
//        }
//        scannedParents = scannedParents.stream ().distinct ().collect (Collectors.toList ());
//        return scannedParents;
//    }
//
//    /**
//     * 筛选出不重复的raw issue 列表
//     */
//    private List<RawIssue> filterRawIssue(List<RawIssue> originalRawIssues, List<Issue> allIssues){
//
//        List<String> issuesUuid = new ArrayList<> ();
//
//        //key  issue id ，value 为 raw issue
//        Map<String, RawIssue> issueRawIssueMap = new HashMap<> (originalRawIssues.size() << 1);
//
//        originalRawIssues.forEach(r -> {
//            issuesUuid.add (r.getIssue_id ());
//            issueRawIssueMap.put (r.getIssue_id (), r);
//        });
//        List<Issue> originalIssues = issueDao.getIssuesByUuids(issuesUuid);
//
//        if(allIssues.isEmpty ()){
//            allIssues.addAll (originalIssues);
//            return originalRawIssues;
//        }
//
//        //key 为issue id ，value 为 issue
//        Map<String, Issue> issueMap = new HashMap<> (allIssues.size() << 1);
//        allIssues.forEach(i ->  issueMap.put (i.getUuid (), i));
//
//        List<RawIssue>  results = new ArrayList<>();
//        for(Issue originalIssue : originalIssues){
//            if(issueMap.get (originalIssue.getUuid ()) == null){
//                allIssues.add (originalIssue);
//                results.add (issueRawIssueMap.get (originalIssue.getUuid ()));
//            }
//        }
//        originalIssues.stream().filter(i -> issueMap.get (i.getUuid ()) == null).
//                forEach(i -> );
//
//        return results;
//
//    }
//
//
//    @Data
//    class RawIssueMappingSort {
//        private String rawIssueId;
//        private double mappingSort;
//        private String commitId;
//        private String matchedIssueId;
//        private boolean bestMatch;
//    }
//
////    private void findBestMatching(Map<String, RawIssue> preRawIssuesMap,
////                                  Map<String, RawIssue> currentRawIssuesMap,
////                                  RawIssue currentRawIssue){
////
////        List<RawIssueMatchResult> currentRawIssueMatchResults = currentRawIssue.getRawIssueMatchResults();
////        int size = currentRawIssueMatchResults.size ();
////        for(int i = 0 ; i < size ; i++ ){
////            String matchRawIssueId = currentRawIssueMatchResults.get (i).getMatchedRawIssueId ();
////
////            RawIssue preRawIssue = preRawIssuesMap.get (matchRawIssueId);
////            int index = preRawIssue.getMatchResultDTOIndex ();
////            int matchIndex = getIndexOfRawIssueMatchResultDtoByRawIssueId(preRawIssue,currentRawIssue.getUuid ());
////            //如果pre raw issue 列表没有匹配过，则为-1，则将当前raw issue 与之匹配
////            if(index == -1){
////                preRawIssue.setMatchResultDTOIndex (matchIndex);
////                preRawIssue.setMapped (true);
////                currentRawIssue.setMapped (true);
////                currentRawIssue.setMatchResultDTOIndex (i);
////                return;
////            }
////            ///如果pre raw issue 列表已经匹配过，但是此时的raw issue 匹配度更高，则进行替换
////            if(matchIndex < index){
////                preRawIssue.setMatchResultDTOIndex (matchIndex);
////                String lowMatchDegreeRawIssueId = preRawIssue.getRawIssueMatchResults().get (index).getMatchedRawIssueId ();
////                RawIssue lowMatchDegreeRawIssue = currentRawIssuesMap.get (lowMatchDegreeRawIssueId);
////                findBestMatching(preRawIssuesMap,currentRawIssuesMap,lowMatchDegreeRawIssue);
////                currentRawIssue.setMapped (true);
////                currentRawIssue.setMatchResultDTOIndex (i);
////                return;
////            }
////        }
////
////        currentRawIssue.setMatchResultDTOIndex (-1);
////        currentRawIssue.setMapped (false);
////
////    }
////
////
////    private void findSimilarRawIssues(RawIssue currentRawIssue, List<RawIssue> preList) {
////
////        boolean isFindBestMatch = false;
////        for (RawIssue rawIssue : preList) {
////            if(bestMappedPreRawIssueIds.contains (rawIssue.getUuid ())){
////                continue;
////            }
////
////            //对两个rawIssue进行匹配
////            isFindBestMatch = matchStrategy.match (currentRawIssue, rawIssue);
////            if(isFindBestMatch){
////                bestMappedPreRawIssueIds.add (rawIssue.getUuid ());
////                break;
////            }
////
////        }
////        List<RawIssueMatchResult> rawIssueMatchResults = currentRawIssue.getRawIssueMatchResults().stream().sorted(Comparator.comparing(RawIssueMatchResult:: getMatchingDegree).reversed()).collect(Collectors.toList());
////        //为了debug 。应该不需要重新赋值。
////        currentRawIssue.setRawIssueMatchResults(rawIssueMatchResults);
////
////    }
//
////    private Integer getIndexOfRawIssueMatchResultDtoByRawIssueId(RawIssue rawIssue , String rawIssueId){
////        Integer result = -1;
////        if(rawIssueId == null){
////            return result;
////        }
////        List<RawIssueMatchResult> rawIssueMatchResults = rawIssue.getRawIssueMatchResults();
////        int rawIssueMatchResultDTOSize = rawIssueMatchResults.size ();
////        for(int i = 0; i < rawIssueMatchResultDTOSize; i++){
////            if(rawIssueId.equals (rawIssueMatchResults.get (i).getMatchedRawIssueId ())){
////                result = i;
////                break;
////            }
////        }
////
////        return result;
////    }

}
