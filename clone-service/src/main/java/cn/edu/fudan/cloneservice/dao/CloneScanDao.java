package cn.edu.fudan.cloneservice.dao;

import cn.edu.fudan.cloneservice.domain.clone.CloneScan;
import cn.edu.fudan.cloneservice.mapper.CloneScanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author zyh
 * @date 2020/5/26
 */
@Repository
public class CloneScanDao {

    private CloneScanMapper cloneScanMapper;

    @Autowired
    private void setCloneScanMapper(CloneScanMapper cloneScanMapper){
        this.cloneScanMapper = cloneScanMapper;
    }

    public void insertCloneScan(CloneScan cloneScan){
        cloneScanMapper.insertOneScan(cloneScan);
    }

    public boolean isScanned(String repoId, String commitId, String type){
        Integer count = cloneScanMapper.getScanCountByCommitIdAndType(repoId, commitId, type);
        return count != null && count > 0;
    }

    public void updateCloneScan(CloneScan cloneScan){
        cloneScanMapper.updateOneScan(cloneScan);
    }

    public void deleteCloneScan(String repoId){
        cloneScanMapper.deleteScanByRepoId(repoId);
    }

}
