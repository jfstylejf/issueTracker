package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.domain.ScanResult;
import cn.edu.fudan.issueservice.mapper.ScanResultMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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

    private final Logger logger = LoggerFactory.getLogger(ScanResultDao.class);

    @Autowired
    public void setScanResultMapper(ScanResultMapper scanResultMapper) {
        this.scanResultMapper = scanResultMapper;
    }

    public void addOneScanResult(ScanResult scanResult){
        scanResultMapper.addOneScanResult(scanResult);
    }

    public void deleteScanResultsByRepoIdAndCategory(String repoUuid, String category){
        try{
            scanResultMapper.deleteScanResultsByRepoIdAndCategory(repoUuid, category);
        }catch (Exception e){
            logger.error(e.getMessage());
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

    public String findFirstDateByRepo(String repoUuid) {
        return scanResultMapper.findFirstDateByRepo(repoUuid);
    }
}
