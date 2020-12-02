package cn.edu.fudan.issueservice.core.process;

import cn.edu.fudan.issueservice.core.strategy.MatchStrategy;
import cn.edu.fudan.issueservice.dao.IssueDao;
import cn.edu.fudan.issueservice.dao.IssueScanDao;
import cn.edu.fudan.issueservice.dao.RawIssueDao;
import cn.edu.fudan.issueservice.domain.dbo.Issue;
import cn.edu.fudan.issueservice.domain.dbo.IssueScan;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.domain.dto.MatcherCommitInfo;
import cn.edu.fudan.issueservice.domain.dto.RawIssueMatchResult;
import cn.edu.fudan.issueservice.domain.enums.ScanStatusEnum;
import cn.edu.fudan.issueservice.util.JGitHelper;
import cn.edu.fudan.issueservice.util.SearchUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * description:
 *
 * @author fancying
 * create: 2020-05-20 16:56
 **/
@Slf4j
public class IssueMatcher {

    //TODO 关于RawIssue的status 状态更改还没做

    private IssueScanDao issueScanDao;
    private RawIssueDao rawIssueDao;
    private IssueDao issueDao;
    private MatchStrategy matchStrategy;

    public IssueMatcher(IssueDao issueDao, RawIssueDao rawIssueDao, IssueScanDao issueScanDao, MatchStrategy matchStrategy){
        this.issueDao = issueDao;
        this.rawIssueDao = rawIssueDao;
        this.issueScanDao = issueScanDao;
        this.matchStrategy = matchStrategy;
    }

    private List<RawIssue> currentRawIssuesResult;
    private Map<String, List<RawIssue>> parentRawIssuesResult;
    private List<String> bestMappedPreRawIssueIds = new ArrayList<> ();


    public List<RawIssue> getCurrentRawIssuesResult() {
        return currentRawIssuesResult;
    }

    public Map<String, List<RawIssue>> getParentRawIssuesResult() {
        return parentRawIssuesResult;
    }

    public void emptyMatchResult(){
        currentRawIssuesResult = null;
        parentRawIssuesResult = null;
        bestMappedPreRawIssueIds = new ArrayList<> ();
    }

    public boolean matchProcess(String repoId, String commitId, JGitHelper jGitHelper, String toolName, List<RawIssue> analyzeRawIssues){
        try{

            //判断当前commit是否是第一个扫描的commit
            List<String> parentCommits =  getPreScanSuccessfullyCommit(repoId,commitId,jGitHelper,toolName);

            //todo 测试bug，测试过后删除
            for(String parentCommit : parentCommits){
                log.info ( "{} --> pre scan success commit --> {} ", commitId, parentCommit);
            }

            if(parentCommits.isEmpty ()){
                //如果当前的commit没有任何parent commit扫描过，则认为该commit是第一次扫描
                firstMatch(analyzeRawIssues);
                return true;
            }

            if(parentCommits.size () == 1){
                normalMatch(repoId, toolName, commitId, parentCommits.get (0), analyzeRawIssues);
                return true;
            }

            mergeMatch(repoId, toolName, commitId, parentCommits, analyzeRawIssues);
            return true;
        }catch(Exception e){
            e.printStackTrace ();
            return false;
        }

    }


    private void firstMatch(List<RawIssue> analyzeRawIssues){

        log.info ("start first matching  ...");
        List<RawIssue> rawIssues = analyzeRawIssues;
        currentRawIssuesResult = new ArrayList<> ();
        for(RawIssue rawIssue : rawIssues){
            rawIssue.setMapped (false);
            rawIssue.setMatchResultDTOIndex (-1);
        }
        currentRawIssuesResult = rawIssues;

    }


    private void normalMatch(String repoId, String toolName, String commitId, String preCommitId, List<RawIssue> currentRawIssues){
        currentRawIssuesResult = new ArrayList<> ();
        parentRawIssuesResult = new HashMap<> (64);
        log.info ("start  matching commit id --> {} ...", commitId);
        List<RawIssue> preRawIssues = rawIssueDao.getRawIssueByCommitIDAndTool(repoId, toolName, preCommitId);

        Map<String, List<RawIssue>> preMap = new HashMap<>(64);
        Map<String,  List<RawIssue>> curMap = new HashMap<>(64);

        for(RawIssue rawIssue : preRawIssues){
            List<RawIssue> rawIssueList = preMap.getOrDefault(rawIssue.getFile_name(), new ArrayList<>());
            rawIssueList.add(rawIssue);
            preMap.put(rawIssue.getFile_name(),rawIssueList);
        }

        for(RawIssue rawIssue : currentRawIssues){
            List<RawIssue> rawIssueList = curMap.getOrDefault(rawIssue.getFile_name(), new ArrayList<>());
            rawIssueList.add(rawIssue);
            curMap.put(rawIssue.getFile_name(),rawIssueList);
        }

        String fileName="./raw.txt";
        try {
            BufferedWriter out=new BufferedWriter(new FileWriter(fileName));
            for(Map.Entry<String, List<RawIssue>> entry : preMap.entrySet()){
                JSONArray pre= JSONArray.parseArray(JSON.toJSONString(entry.getValue()));
                out.write("\n\n-----------------"+entry.getKey()+"----------------\n\n");
                out.write(String.valueOf(pre));
            }
            out.write("\n\n-----------------cur----------------\n\n");
            for(Map.Entry<String, List<RawIssue>> entry : curMap.entrySet()){
                JSONArray cur= JSONArray.parseArray(JSON.toJSONString(entry.getValue()));
                out.write("\n\n-----------------"+entry.getKey()+"----------------\n\n");
                out.write(String.valueOf(cur));
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //匹配两个rawIssue集合（parent的rawIssue集合，当前的rawIssue集合）
        mappingTwoRawIssueList(preRawIssues, currentRawIssues);
        parentRawIssuesResult.put (preCommitId, preRawIssues);
        currentRawIssuesResult = currentRawIssues;
    }


    private void mergeMatch(String repoId, String toolName, String commitId, List<String> parentCommits, List<RawIssue> currentRawIssues){
        log.info ("start merge matching commit id --> {} ...", commitId);

        if (currentRawIssues == null || currentRawIssues.isEmpty()) {
            log.info("all issues were solved or raw issue insert error , commit id -->  {}", commitId);
        }

        // key parentCommitId value parentRawIssues
        Map<String, List<RawIssue>> matchPreRawIssueLists = new HashMap<> (4);

        // key parentCommitId value [k parentRawIssueId v parentRawIssue ]
        Map<String, Map<String, RawIssue>> matchPreRawIssueMaps = new HashMap<> (4);

        // key curRawIssueId value 匹配上的 preRawIssue
        Map<String,List<RawIssueMappingSort>> currentRawIssueMatchedPreRawIssues = new HashMap<> ();

        //用于全匹配的issue列表 parentCommits 包含的所有 issue
        List<Issue>  allIssues = new ArrayList<>();


        for (String parentCommit : parentCommits) {
            List<RawIssue> parentRawIssues = rawIssueDao.getRawIssueByCommitIDAndTool(repoId, toolName, parentCommit);
            if (parentRawIssues.size() == 0) {
                continue;
            }

            //filterRawIssue这个方法会更新allIssues，把所有parent的不同的issue存到allIssues，并且返回当前parent里的与之前parent里的不重复的rawIssue列表，返回给parentRawIssues
            parentRawIssues = filterRawIssue(parentRawIssues, allIssues);

            // key 是 parent raw issue 的 uuid , value 是对应的 parent raw issue
            Map<String, RawIssue> parentRawIssuesMap = new HashMap<>();
            for (RawIssue rawIssue : parentRawIssues) {
                parentRawIssuesMap.put(rawIssue.getUuid(), rawIssue);
            }
            matchPreRawIssueMaps.put(parentCommit, parentRawIssuesMap);
            matchPreRawIssueLists.put(parentCommit, parentRawIssues);


            mappingTwoRawIssueList(parentRawIssues, currentRawIssues);

            for (RawIssue rawIssue : currentRawIssues) {
                int index = rawIssue.getMatchResultDTOIndex();
                if (index == -1) {
                    continue;
                }
                RawIssueMatchResult matchResultDTO = rawIssue.getRawIssueMatchResults().get(index);

                RawIssueMappingSort rawIssueMappingSort = new RawIssueMappingSort();
                rawIssueMappingSort.setCommitId(parentCommit);
                rawIssueMappingSort.setRawIssueId(matchResultDTO.getMatchedRawIssueId());
                rawIssueMappingSort.setMappingSort(matchResultDTO.getMatchingDegree());
                rawIssueMappingSort.setMatchedIssueId(matchResultDTO.getMatchedIssueId());
                rawIssueMappingSort.setBestMatch(matchResultDTO.isBestMatch());

                List<RawIssueMappingSort> rawIssueMappingSorts = currentRawIssueMatchedPreRawIssues.get(rawIssue.getUuid());
                if (rawIssueMappingSorts == null) {
                    rawIssueMappingSorts = new ArrayList<>();
                }
                rawIssueMappingSorts.add(rawIssueMappingSort);
                currentRawIssueMatchedPreRawIssues.put(rawIssue.getUuid(), rawIssueMappingSorts);

            }

            //把currentRawIssues的匹配状态全部初始化，准备与下一个parent 的rawIssue做匹配
            for (RawIssue rawIssue : currentRawIssues) {
                rawIssue.setRawIssueMatchResults(new ArrayList<>());
                rawIssue.setMapped(false);
                rawIssue.setMatchResultDTOIndex(-1);
            }
        }

        for(RawIssue rawIssue : currentRawIssues){
            List<RawIssueMappingSort> rawIssueMappingSorts = currentRawIssueMatchedPreRawIssues.get (rawIssue.getUuid ());
            if(rawIssueMappingSorts == null){
                continue;
            }
            String matchRawIssueId = null;
            double maxScore = 0;
            String matchedIssueId = null;
            boolean isBestMatch = false;
            String matchedCommitId = null;
            for(RawIssueMappingSort rawIssueMappingSort : rawIssueMappingSorts){
                if(rawIssueMappingSort.getMappingSort () > maxScore){
                    maxScore = rawIssueMappingSort.getMappingSort ();

                    if(matchRawIssueId != null){
                        RawIssue preMatchedRawIssue = matchPreRawIssueMaps.get (matchedCommitId).get (matchRawIssueId);
                        preMatchedRawIssue.setMapped (false);
                        preMatchedRawIssue.setMatchResultDTOIndex (-1);
                    }
                    matchRawIssueId = rawIssueMappingSort.getRawIssueId ();
                    matchedIssueId = rawIssueMappingSort.getMatchedIssueId ();
                    isBestMatch = rawIssueMappingSort.isBestMatch ();
                    matchedCommitId = rawIssueMappingSort.getCommitId ();

                }else{
                    //todo 当前面有多次编译失败时，报空指针异常
                    Map<String, RawIssue> map =matchPreRawIssueMaps.get (rawIssueMappingSort.getCommitId ());
                    if(map == null){
                        log.error ("can not get pre commit raw issues, pre commit id ---> {} ,and matched raw issue --> {}" ,
                                rawIssueMappingSort.getCommitId (),
                                matchRawIssueId);
                    }
                    RawIssue preMatchedRawIssue = matchPreRawIssueMaps.get (rawIssueMappingSort.getCommitId ()).get (rawIssueMappingSort.getRawIssueId ());
                    preMatchedRawIssue.setMapped (false);
                    preMatchedRawIssue.setMatchResultDTOIndex (-1);
                }
            }

            if (matchRawIssueId != null ) {
                RawIssueMatchResult rawIssueMatchResult = new RawIssueMatchResult();
                rawIssueMatchResult.setMatchedRawIssueId (matchRawIssueId);
                rawIssueMatchResult.setMatchingDegree (maxScore);
                rawIssueMatchResult.setMatchedIssueId (matchedIssueId);
                rawIssueMatchResult.setBestMatch (isBestMatch);
                rawIssue.getRawIssueMatchResults().add (rawIssueMatchResult);
                rawIssue.setMapped (true);
                rawIssue.setMatchResultDTOIndex (0);
            }

        }

        parentRawIssuesResult = matchPreRawIssueLists ;
        currentRawIssuesResult = currentRawIssues;
    }


    /**
     * 更改中，比对两个raw issue列表
     * @param preRawIssues parent rawIssue list
     * @param currentRawIssues current rawIssue list
     */
    private void mappingTwoRawIssueList(List<RawIssue> preRawIssues,
                                        List<RawIssue> currentRawIssues){
        Map<String, List<RawIssue>> curRawIssueMap = classifyRawIssue(currentRawIssues);
        Map<String, List<RawIssue>> preRawIssueMap = classifyRawIssue(preRawIssues);


        //for循环中每一个entry就是一个key（fileName+issueType）+value（这个类别下的rawIssue集合）
        for (Map.Entry<String, List<RawIssue>> entry : curRawIssueMap.entrySet()) {
            //判断parent中是否有相同类别（fileName+issueType）的rawIssue集合
            if (preRawIssueMap.containsKey(entry.getKey())) {
                //如果有相同类别，则取出对应的rawIssue集合
                List<RawIssue> preList = preRawIssueMap.get(entry.getKey());
                for (RawIssue currentRawIssue : entry.getValue()) {
                    //当前的每一个rawIssue，和preList匹配
                    findSimilarRawIssues(currentRawIssue,preList);
                }
            }
        }

        for(RawIssue preRawIssue : preRawIssues){
            List<RawIssueMatchResult> rawIssueMatchResults = preRawIssue.getRawIssueMatchResults().stream().sorted(Comparator.comparing(RawIssueMatchResult:: getMatchingDegree).reversed()).collect(Collectors.toList());
            //为了debug 。应该不需要重新赋值。
            preRawIssue.setRawIssueMatchResults(rawIssueMatchResults);
        }

        Map<String, RawIssue> preRawIssuesMap = new HashMap<> ();
        Map<String, RawIssue> currentRawIssuesMap = new HashMap<> ();


        for(RawIssue preRawIssue : preRawIssues){
            preRawIssuesMap.put(preRawIssue.getUuid (),preRawIssue);
            preRawIssue.setMatchResultDTOIndex (-1);
        }

        for(RawIssue currentRawIssue : currentRawIssues){
            currentRawIssuesMap.put(currentRawIssue.getUuid (),currentRawIssue);
            currentRawIssue.setMatchResultDTOIndex (-1);

        }


        for (RawIssue currentRawIssue : currentRawIssues) {
            findBestMatching(preRawIssuesMap,currentRawIssuesMap,currentRawIssue);
        }



    }

    private void findBestMatching(Map<String, RawIssue> preRawIssuesMap,
                                  Map<String, RawIssue> currentRawIssuesMap,
                                  RawIssue currentRawIssue){

        List<RawIssueMatchResult> currentRawIssueMatchResults = currentRawIssue.getRawIssueMatchResults();
        int size = currentRawIssueMatchResults.size ();
        for(int i = 0 ; i < size ; i++ ){
            String matchRawIssueId = currentRawIssueMatchResults.get (i).getMatchedRawIssueId ();

            RawIssue preRawIssue = preRawIssuesMap.get (matchRawIssueId);
            int index = preRawIssue.getMatchResultDTOIndex ();
            int matchIndex = getIndexOfRawIssueMatchResultDtoByRawIssueId(preRawIssue,currentRawIssue.getUuid ());
            //如果pre raw issue 列表没有匹配过，则为-1，则将当前raw issue 与之匹配
            if(index == -1){
                preRawIssue.setMatchResultDTOIndex (matchIndex);
                preRawIssue.setMapped (true);
                currentRawIssue.setMapped (true);
                currentRawIssue.setMatchResultDTOIndex (i);
                return;
            }
            ///如果pre raw issue 列表已经匹配过，但是此时的raw issue 匹配度更高，则进行替换
            if(matchIndex < index){
                preRawIssue.setMatchResultDTOIndex (matchIndex);
                String lowMatchDegreeRawIssueId = preRawIssue.getRawIssueMatchResults().get (index).getMatchedRawIssueId ();
                RawIssue lowMatchDegreeRawIssue = currentRawIssuesMap.get (lowMatchDegreeRawIssueId);
                findBestMatching(preRawIssuesMap,currentRawIssuesMap,lowMatchDegreeRawIssue);
                currentRawIssue.setMapped (true);
                currentRawIssue.setMatchResultDTOIndex (i);
                return;
            }
        }

        currentRawIssue.setMatchResultDTOIndex (-1);
        currentRawIssue.setMapped (false);

    }


    private void findSimilarRawIssues(RawIssue currentRawIssue, List<RawIssue> preList) {

        boolean isFindBestMatch;
        for (RawIssue rawIssue : preList) {
            if(bestMappedPreRawIssueIds.contains (rawIssue.getUuid ())){
                continue;
            }

            //对两个rawIssue进行匹配
            isFindBestMatch = matchStrategy.match (currentRawIssue, rawIssue);
            if(isFindBestMatch){
                bestMappedPreRawIssueIds.add (rawIssue.getUuid ());
                break;
            }

        }
        List<RawIssueMatchResult> rawIssueMatchResults = currentRawIssue.getRawIssueMatchResults().stream().sorted(Comparator.comparing(RawIssueMatchResult:: getMatchingDegree).reversed()).collect(Collectors.toList());
        //为了debug 。应该不需要重新赋值。
        currentRawIssue.setRawIssueMatchResults(rawIssueMatchResults);

    }

    /**
     * 筛选出不重复的raw issue 列表
     * @return
     */
    private List<RawIssue> filterRawIssue(List<RawIssue> originalRawIssues, List<Issue> allIssues){
        List<RawIssue>  rawIssueList = new ArrayList<>();
        List<String> issueIds = new ArrayList<> ();

        //key  issue id ，value 为 raw issue
        Map<String, RawIssue> rawIssueMap= new HashMap<> ();

        for(RawIssue rawIssue : originalRawIssues){
            issueIds.add (rawIssue.getIssue_id ());
            rawIssueMap.put (rawIssue.getIssue_id (),rawIssue);
        }

        List<Issue> originalIssues = issueDao.getIssuesByUuids(issueIds);

        if(allIssues.isEmpty ()){
            allIssues.addAll (originalIssues);
            rawIssueList = originalRawIssues;
        }else{
            //key 为issue id ，value 为 issue
            Map<String, Issue> issueMap= new HashMap<> ();

            for(Issue issue : allIssues){
                issueMap.put (issue.getUuid (), issue);
            }


            for(Issue originalIssue : originalIssues){
                if(issueMap.get (originalIssue.getUuid ()) == null){
                    allIssues.add (originalIssue);
                    rawIssueList.add (rawIssueMap.get (originalIssue.getUuid ()));
                }

            }
        }


        return rawIssueList;

    }


    private Integer getIndexOfRawIssueMatchResultDtoByRawIssueId(RawIssue rawIssue , String rawIssueId){
        Integer result = -1;
        if(rawIssueId == null){
            return result;
        }
        List<RawIssueMatchResult> rawIssueMatchResults = rawIssue.getRawIssueMatchResults();
        int rawIssueMatchResultDTOSize = rawIssueMatchResults.size ();
        for(int i = 0; i < rawIssueMatchResultDTOSize; i++){
            if(rawIssueId.equals (rawIssueMatchResults.get (i).getMatchedRawIssueId ())){
                result = i;
                break;
            }
        }

        return result;
    }

    /**
     * 根据issue所在的文件名以及issue类型  对issue进行分类
     * @param rawIssues rawIssues
     * @return Map<String, List<RawIssue>> key:rawIssue.getFile_name () + "-" + rawIssue.getType () value:在该分类下的rawIssue列表
     */
    private Map<String, List<RawIssue>> classifyRawIssue(List<RawIssue> rawIssues) {
        Map<String, List<RawIssue>> map = new HashMap<>(2);
        for (RawIssue rawIssue : rawIssues) {
            String key = rawIssue.getFile_name () + "-" + rawIssue.getType ();
            if (map.containsKey(key)) {
                map.get(key).add(rawIssue);
            } else {
                List<RawIssue> list = new ArrayList<>();
                list.add(rawIssue);
                map.put(key, list);
            }
        }
        return map;
    }



    private List<String> getPreScanSuccessfullyCommit(String repoId, String commitId,JGitHelper jGitHelper, String tool){
        List<String > scannedParents = new ArrayList<> ();

        List<IssueScan> scanList = issueScanDao.getIssueScanByRepoIdAndStatusAndTool(repoId,null,tool);
        if(scanList == null || scanList.isEmpty ()){
            return scannedParents;
        }
        int scannedCommitListSize = scanList.size ();
        String[] scannedCommitIds =  new String[scannedCommitListSize];

        for(int i = 0 ; i < scannedCommitListSize ; i++){
            scannedCommitIds[i] = scanList.get (i).getCommitId ();
        }

        List<MatcherCommitInfo> parentCommits = new ArrayList<> ();
        MatcherCommitInfo commitInfoFirst = new MatcherCommitInfo ();
        commitInfoFirst.setRepoId (repoId);
        commitInfoFirst.setCommitId (commitId);
        commitInfoFirst.setCommitTime (jGitHelper.getCommitTime (commitId));

        parentCommits.add(commitInfoFirst);
        while (!parentCommits.isEmpty()){
            MatcherCommitInfo matcherCommitInfo = parentCommits.remove (0);
            String[] parents = jGitHelper.getCommitParents(matcherCommitInfo.getCommitId ());
            for(String parent : parents){
                int index = SearchUtil.dichotomy (scannedCommitIds,parent);
                if( index == -1){
                    continue;
                }

                if(ScanStatusEnum.DONE.getType ().equals (scanList.get (index).getStatus ())){
                    scannedParents.add(parent);
                }else{
                    MatcherCommitInfo commitInfoNew = new MatcherCommitInfo ();
                    commitInfoNew.setRepoId (repoId);
                    commitInfoNew.setCommitId (parent);
                    commitInfoNew.setCommitTime (jGitHelper.getCommitTime (commitId));
                    parentCommits.add(commitInfoNew);
                    parentCommits = parentCommits.stream ().distinct ().sorted (Comparator.comparing (MatcherCommitInfo:: getCommitTime).reversed ()).collect(Collectors.toList());
                }

            }
        }
        scannedParents = scannedParents.stream ().distinct ().collect (Collectors.toList ());
        return scannedParents;
    }



    @Data
    class RawIssueMappingSort {
        private String rawIssueId;
        private double mappingSort;
        private String commitId;
        private String matchedIssueId;
        private boolean bestMatch;
    }
}
