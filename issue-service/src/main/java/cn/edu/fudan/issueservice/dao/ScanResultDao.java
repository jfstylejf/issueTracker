package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.domain.IssueCountDeveloper;
import cn.edu.fudan.issueservice.domain.dto.IssueCountPo;
import cn.edu.fudan.issueservice.domain.ScanResult;
import cn.edu.fudan.issueservice.mapper.ScanResultMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author WZY
 * @version 1.0
 **/
@Slf4j
@Repository
public class ScanResultDao {

    private ScanResultMapper scanResultMapper;
    //jeff replace log with logger
    private Logger logger = LoggerFactory.getLogger(ScanResultDao.class);

    @Autowired
    public void setScanResultMapper(ScanResultMapper scanResultMapper) {
        this.scanResultMapper = scanResultMapper;
    }

    public void addOneScanResult(ScanResult scanResult){
        scanResultMapper.addOneScanResult(scanResult);
    }

    public void deleteScanResultsByRepoIdAndCategory(String repo_id,String category){
        try{
            scanResultMapper.deleteScanResultsByRepoIdAndCategory(repo_id, category);
        }catch (Exception e){
            logger.error(e.getMessage());
        }
    }

    public List<IssueCountPo> getScanResultsGroupByDay(List<String> repoIds, String category, String start, String end){
        try {
            return scanResultMapper.getScanResultsGroupByDay(repoIds, category, start, end);
        }catch (Exception e){
            logger.error(e.getMessage());
            return null;
        }
    }

    public IssueCountPo getMergedScanResult(List<String> repoIds, String category, String start, String end){
        try{
            return scanResultMapper.getMergedScanResult(repoIds, category, start, end);
        }catch (Exception e){
            logger.error(e.getMessage());
            return null;
        }
    }

    public List<IssueCountPo> getScanResultsEachCommit(String repo_id,String category, String start,String end){
        try{
            return scanResultMapper.getScanResultsEachCommit(repo_id, category, start, end);
        }catch (Exception e){
            logger.error(e.getMessage());
            return null;
        }
    }

    public List<IssueCountDeveloper> getScanResultsEachDeveloper(String repo_id, String category, String start, String end){
        try{
            return scanResultMapper.getScanResultsEachDeveloper(repo_id, category, start, end);
        }catch (Exception e){
            logger.error(e.getMessage());
            return null;
        }
    }


    public int getNumberOfNewIssueByCommit(String repo_id, String commit_id,String category){
        try{
            return scanResultMapper.getNewIssueCountByCommit(repo_id, commit_id,category);
        }catch (Exception e){
            e.printStackTrace();
            logger.error(e.getMessage());
            return -1;
        }
    }

    public int getNumberOfEliminateIssueByCommit(String repo_id, String commit_id,String category){
        try{
            return scanResultMapper.getEliminateIssueCountByCommit(repo_id, commit_id,category);
        }catch (Exception e){
            logger.error(e.getMessage());
            return -1;
        }
    }

    public List<ScanResult> getScanResultByCondition( String repoId, String since, String until, String category, String developer){
        try{
            return scanResultMapper.getScanResultByCondition(repoId, since,until,category,developer);
        }catch (Exception e){
            logger.error(e.getMessage());
            return new ArrayList<>(0);
        }
    }

    public List<Map<String, Object>> getRepoIssueCounts(String repoId, String since, String until, String category, String developer){
        try{
            return scanResultMapper.getRepoIssueCounts(repoId, since, until, category, developer);
        }catch (Exception e){
            logger.error(e.getMessage());
            return null;
        }
    }

}
