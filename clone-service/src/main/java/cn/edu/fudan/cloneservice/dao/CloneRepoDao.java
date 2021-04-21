package cn.edu.fudan.cloneservice.dao;

import cn.edu.fudan.cloneservice.domain.clone.CloneRepo;
import cn.edu.fudan.cloneservice.mapper.CloneRepoMapper;
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
        cloneRepoMapper.updateCloneRepo(cloneRepo);
    }

    public void deleteCloneRepo(String repoId){cloneRepoMapper.deleteRepoByRepoId(repoId);}

    public CloneRepo getLatestCloneRepo(String repoId){
        return cloneRepoMapper.getLatestCloneRepo(repoId);
    }

    public Integer getScanCount(String repoId){
        return cloneRepoMapper.getScanCount(repoId);
    }
}
