package cn.edu.fudan.scanservice.dao;

import cn.edu.fudan.scanservice.domain.dbo.Scan;
import cn.edu.fudan.scanservice.mapper.ScanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class ScanDao {


    private ScanMapper scanMapper;

    @Autowired
    public void setScanMapper(ScanMapper scanMapper) {
        this.scanMapper = scanMapper;
    }


    public void deleteScanByRepoId(String repoId){
        scanMapper.deleteScanByRepoId (repoId);
    }

    public void insertOneScan(Scan scan){
        scanMapper.insertOneScan (scan);
    }

    public void updateOneScan(Scan scan){
        scanMapper.updateOneScan (scan);
    }

    public Scan getScanByRepoId(String repoId){
        return scanMapper.getScanByRepoId (repoId);
    }
}
