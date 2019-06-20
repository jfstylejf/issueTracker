package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.domain.IssueCountDeveloper;
import cn.edu.fudan.issueservice.domain.IssueCountPo;
import cn.edu.fudan.issueservice.domain.ScanResult;
import cn.edu.fudan.issueservice.mapper.ScanResultMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author WZY
 * @version 1.0
 **/
@Slf4j
@Repository
public class ScanResultDao {

    private ScanResultMapper scanResultMapper;

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
           log.error(e.getMessage());
        }
    }

    public List<IssueCountPo> getScanResultsGroupByDay(List<String> repoIds, String category, String start, String end){
        try {
            return scanResultMapper.getScanResultsGroupByDay(repoIds, category, start, end);
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    public IssueCountPo getMergedScanResult(List<String> repoIds, String category, String start, String end){
        try{
            return scanResultMapper.getMergedScanResult(repoIds, category, start, end);
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    public List<IssueCountPo> getScanResultsEachCommit(String repo_id,String category, String start,String end){
        try{
            return scanResultMapper.getScanResultsEachCommit(repo_id, category, start, end);
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    public List<IssueCountDeveloper> getScanResultsEachDeveloper(String repo_id, String category, String start, String end){
        try{
            return scanResultMapper.getScanResultsEachDeveloper(repo_id, category, start, end);
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }
}
