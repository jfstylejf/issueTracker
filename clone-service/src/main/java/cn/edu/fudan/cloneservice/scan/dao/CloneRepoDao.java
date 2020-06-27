package cn.edu.fudan.cloneservice.scan.dao;

import cn.edu.fudan.cloneservice.scan.domain.CloneRepo;
import cn.edu.fudan.cloneservice.scan.mapper.CloneRepoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author zyh
 * @date 2020/6/22
 */
@Repository
public class CloneRepoDao {

    private CloneRepoMapper cloneRepoMapper;

    @Autowired
    public void setCloneRepoMapper(CloneRepoMapper cloneRepoMapper) {
        this.cloneRepoMapper = cloneRepoMapper;
    }

    public void insertCloneRepo(CloneRepo cloneRepo){
        cloneRepoMapper.insertCloneRepo(cloneRepo);
    }

    public void updateScan(CloneRepo cloneRepo){
        cloneRepoMapper.updateOneScan(cloneRepo);
    }

    public CloneRepo getLatestCloneRepo(String repoId){
        return cloneRepoMapper.getLatestCloneRepo(repoId);
    }

    public Integer getScanCount(String repoId){
        return cloneRepoMapper.getScanCount(repoId);
    }
}
