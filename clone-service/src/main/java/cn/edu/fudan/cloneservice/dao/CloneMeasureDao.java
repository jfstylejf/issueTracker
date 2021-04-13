
package cn.edu.fudan.cloneservice.dao;

import cn.edu.fudan.cloneservice.domain.CloneMeasure;
import cn.edu.fudan.cloneservice.mapper.CloneMeasureMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zyh
 * @date 2020/4/29
 */
@Repository
public class CloneMeasureDao {

    private static Map<String, List<CloneMeasure>> cloneMeasureMap = new ConcurrentHashMap<>(26);

    private CloneMeasureMapper cloneMeasureMapper;

    public List<CloneMeasure> getCloneMeasures(String repoId){
        if (cloneMeasureMap.keySet().contains(repoId)) {
            return cloneMeasureMap.get(repoId);
        }
        List<CloneMeasure> cloneMeasures = cloneMeasureMapper.getCloneMeasures(repoId);
        cloneMeasureMap.put(repoId, cloneMeasures);
        return cloneMeasureMap.get(repoId);
    }

    @Autowired
    public void setCloneMeasureMapper(CloneMeasureMapper cloneMeasureMapper){
        this.cloneMeasureMapper = cloneMeasureMapper;
    }

    public CloneMeasure getCloneMeasureTest(String repoId, String commitId){
        CloneMeasure cloneMeasure = cloneMeasureMapper.getCloneMeasureTest(repoId, commitId);
        return cloneMeasure;
    }

    public CloneMeasure getCloneMeasure(String repoId, String commitId){
        CloneMeasure cloneMeasure = cloneMeasureMapper.getCloneMeasure(repoId, commitId);
        return cloneMeasure;
    }

    public void insertCloneMeasure(CloneMeasure cloneMeasure){
        cloneMeasureMapper.insertCloneMeasure(cloneMeasure);
    }

    public void deleteCloneMeasureByRepoId(String repoId){
        cloneMeasureMapper.deleteCloneMeasureByRepoId(repoId);
    }

    public void deleteCloneMeasureByRepoIdAndCommitId(String repoId, String commitId){
        cloneMeasureMapper.deleteCloneMeasureByRepoIdAndCommitId(repoId, commitId);
    }

    public int getCloneMeasureCount(String repoId, String commitId){
        return cloneMeasureMapper.getMeasureCountByCommitId(repoId, commitId);
    }

    public CloneMeasure getLatestCloneLines(List<String> repoIds){
        return cloneMeasureMapper.getLatestCloneLines(repoIds);
    }
}
