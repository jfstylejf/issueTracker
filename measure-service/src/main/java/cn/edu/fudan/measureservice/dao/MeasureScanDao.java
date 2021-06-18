package cn.edu.fudan.measureservice.dao;


import cn.edu.fudan.measureservice.mapper.RepoMeasureMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author wjzho
 */
@Repository
public class MeasureScanDao {

    private RepoMeasureMapper repoMeasureMapper;

    /**
     * 获取查询库的起始扫描 commit
     * @param repoUuid 查询库
     * @return
     */
    public String getRepoStartCommit(String repoUuid) {
        Objects.requireNonNull(repoUuid);
        return repoMeasureMapper.getRepoStartCommit(repoUuid);
    }

    @Autowired
    public void setRepoMeasureMapper(RepoMeasureMapper repoMeasureMapper) {
        this.repoMeasureMapper = repoMeasureMapper;
    }
}
