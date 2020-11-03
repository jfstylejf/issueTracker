package cn.edu.fudan.scanservice.mapper;

import cn.edu.fudan.scanservice.domain.dbo.Scan;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ScanMapper {

    void insertOneScan(Scan scan);

    void deleteScanByRepoId(@Param("repo_id") String repoId);

    void updateOneScan(Scan scan);

    Scan getScanByRepoId(@Param("repo_id") String repoId);
}
