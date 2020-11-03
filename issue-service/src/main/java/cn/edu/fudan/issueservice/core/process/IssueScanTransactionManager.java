package cn.edu.fudan.issueservice.core.process;

import cn.edu.fudan.issueservice.dao.*;
import cn.edu.fudan.issueservice.domain.ScanResult;
import cn.edu.fudan.issueservice.domain.dbo.Issue;
import cn.edu.fudan.issueservice.domain.dbo.IssueScan;
import cn.edu.fudan.issueservice.domain.dbo.Location;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.domain.dto.RawIssueMatchResult;
import cn.edu.fudan.issueservice.domain.enums.RawIssueStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 将数据持久化
 */
@Slf4j
@Component("DataPersistManager")
@Scope("prototype")
public class IssueScanTransactionManager {

    @Autowired
    private CommitDao commitDao;

    private RawIssueDao rawIssueDao;
    private LocationDao locationDao;
    private IssueDao issueDao;
    private ScanResultDao scanResultDao;

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


    @Transactional(rollbackFor = Exception.class)
    public void persistScanData(List<RawIssue> currentRawIssues,
                                   Map<String, List<RawIssue>> parentRawIssuesResult,
                                   IssueScan issueScan,
                                   IssueStatisticalTool issueStatisticalTool){

        String commit = issueScan.getCommitId ();

        ScanResult scanResult = issueStatisticalTool.getStatisticalResult ();
        List<Issue> insertIssues =  issueStatisticalTool.getAllInsertIssues();
        List<Issue> eliminateIssues = issueStatisticalTool.getAllEliminatedIssues ();
        List<Issue> updateIssues = issueStatisticalTool.getAllMatchedIssues ();



        /*
        1. 更新current raw issue 的信息，具体包括 issue id 以及 status
        */
        rawIssueUpdateStatusAndPersistence(currentRawIssues, parentRawIssuesResult, commit, issueScan.getUuid ());
        log.info ("update raw issue and persist success ! " );

        //2. 缺陷统计结果入库
        // 更改入库时的开发者姓名，改为聚合后的唯一姓名
        String repoId = scanResult.getRepo_id();
        String commitId = scanResult.getCommit_id();
        Map<String, Object> commitViewInfo = commitDao.getCommitViewInfoByCommitId(repoId, commitId);
        String developerUniqueName = (String) commitViewInfo.get("developer_unique_name");
        if(developerUniqueName == null || developerUniqueName.length() == 0){
            developerUniqueName = (String) commitViewInfo.get("developer");
        }
        scanResult.setDeveloper(developerUniqueName);
        statisticalPersistence(scanResult);
        log.info ("persist statistical result  success ! " );

        //TODO Mark后续考虑是否加入

        //3. 缺陷更新以及缺陷
        issuePersistence(insertIssues, eliminateIssues, updateIssues);
        log.info ("persist issues  success ! " );

    }



    private void rawIssueUpdateStatusAndPersistence(List<RawIssue> currentRawIssues,
                                                       Map<String, List<RawIssue>> parentRawIssuesResult,
                                                       String commitId, String scanId){

        //已有的 current raw issue 进行更新
        for(RawIssue rawIssue : currentRawIssues){
            boolean isMatched = rawIssue.isMapped ();
            if(isMatched){
                List<RawIssueMatchResult> rawIssueMatchResults =  rawIssue.getRawIssueMatchResults();
                RawIssueMatchResult rawIssueMatchResult =  rawIssueMatchResults.get (rawIssue.getMatchResultDTOIndex ());
                boolean isBestMatch = rawIssueMatchResult.isBestMatch ();
                if(!isBestMatch){
                    rawIssue.setStatus (RawIssueStatus.CHANGED.getType ());
                }else{
                    rawIssue.setStatus (RawIssueStatus.DEFAULT.getType ());
                }
            }else{
                rawIssue.setStatus (RawIssueStatus.ADD.getType ());
            }
            rawIssue.setScan_id (scanId);
        }

        if (!currentRawIssues.isEmpty()) {
            //插入所有的rawIssue
            List<Location> locations = new ArrayList<> ();
            for (RawIssue rawIssue : currentRawIssues) {
                locations.addAll(rawIssue.getLocations());
            }
            rawIssueDao.insertRawIssueList(currentRawIssues);
            locationDao.insertLocationList(locations);
        }


        if(parentRawIssuesResult == null){
            return ;
        }
        //对 已经解决的issue，同时也插入一条raw issue 中。
        List<RawIssue> insertSolvedRawIssues = new ArrayList<> ();
        for(Map.Entry<String, List<RawIssue>> parentRawIssuesEntry : parentRawIssuesResult.entrySet ()){
            List<RawIssue> preRawIssues = parentRawIssuesEntry.getValue ();
            for(RawIssue rawIssue : preRawIssues){
                if(!rawIssue.isMapped ()){
                    Issue issue = rawIssue.getIssue ();
                    assert(issue != null);
                    RawIssue rawIssueSolved = new RawIssue ();
                    rawIssueSolved.setUuid (UUID.randomUUID ().toString ());
                    rawIssueSolved.setType (rawIssue.getType ());
                    rawIssueSolved.setDetail (rawIssue.getDetail ());
                    rawIssueSolved.setIssue_id (rawIssue.getIssue_id ());
                    rawIssueSolved.setTool (rawIssue.getTool ());
                    rawIssueSolved.setDetail (rawIssue.getDetail ());
                    rawIssueSolved.setScan_id (rawIssue.getScan_id ());
                    rawIssueSolved.setFile_name (rawIssue.getFile_name ());
                    rawIssueSolved.setCommit_id (commitId);
                    rawIssueSolved.setRepo_id (rawIssue.getRepo_id ());
                    rawIssueSolved.setCode_lines (rawIssue.getCode_lines());
                    //set聚合后的唯一姓名
                    Map<String, Object> commitViewInfo = commitDao.getCommitViewInfoByCommitId(rawIssue.getRepo_id (), commitId);
                    String developerUniqueName = (String) commitViewInfo.get("developer_unique_name");
                    rawIssueSolved.setDeveloperName(developerUniqueName);
                    if(rawIssue.isRealEliminate ()){
                        rawIssueSolved.setStatus (RawIssueStatus.SOLVED.getType ());
                    }else{
                        rawIssueSolved.setStatus (RawIssueStatus.MERGE_SOLVED.getType ());
                    }

                    rawIssueSolved.setLocations (rawIssue.getLocations ());
                    insertSolvedRawIssues.add(rawIssueSolved);
                }

            }

        }

        if(!insertSolvedRawIssues.isEmpty ()){
            rawIssueDao.insertRawIssueList (insertSolvedRawIssues);
        }

    }


    private void issuePersistence(List<Issue> insertIssues, List<Issue> eliminateIssues, List<Issue> updateIssues){

        //插入新增的issue

        if(insertIssues != null && !insertIssues.isEmpty ()){
            issueDao.insertIssueList (insertIssues);
        }


        //更新 issue 信息
        if(eliminateIssues != null && !eliminateIssues.isEmpty ()){
            issueDao.batchUpdateIssue (eliminateIssues);
        }

        //更新 issue 信息
        if(updateIssues != null && !updateIssues.isEmpty ()){
            issueDao.batchUpdateIssue (updateIssues);
        }
    }

    private void statisticalPersistence(ScanResult scanResult){

        scanResultDao.addOneScanResult (scanResult);

    }

}
