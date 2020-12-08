package cn.edu.fudan.measureservice.dao;

import cn.edu.fudan.measureservice.domain.dto.Query;
import cn.edu.fudan.measureservice.mapper.MeasureMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wjzho
 */
@Repository
public class MeasureDao {
    private MeasureMapper measureMapper;

    /**
     * 获取开发者个人新增物理行
     * @Param query 查询条件
     * @return int developerAddLine
     */
    public int getDeveloperAddLine (Query query) {
        return measureMapper.getAddLinesByDuration(query.getRepoUuidList().get(0),query.getSince(),query.getUntil(),query.getDeveloper());
    }

    /**
     * 获取相应的物理行数据
     * @param query 查询条件
     * @return Map<String,Object> developerLoc,totalLoc
     */
    public Map<String,Object> getLocByCondition(Query query) {
        Map<String,Object> map = new HashMap<>(10);
        int developerLoc = measureMapper.getLocByCondition(query.getRepoUuidList().get(0),query.getDeveloper(),query.getSince(),query.getUntil());
        int totalLoc =measureMapper.getLocByCondition(query.getRepoUuidList().get(0),null,query.getSince(),query.getUntil());
        map.put("developerLoc",developerLoc);
        map.put("totalLoc",totalLoc);
        return map;
    }

    /**
     * 获取相应的commit提交次数数据
     * @param query 查询条件
     * @return Map<String,Object> developerCommitCount,totalCommitCount
     */
    public Map<String,Object> getCommitCountsByDuration(Query query) {
        Map<String,Object> map = new HashMap<>(10);
        int developerCommitCount = measureMapper.getCommitCountsByDuration(query.getRepoUuidList().get(0),query.getSince(),query.getUntil(),query.getDeveloper());
        int totalCommitCount = measureMapper.getCommitCountsByDuration(query.getRepoUuidList().get(0),query.getSince(),query.getUntil(),null);
        map.put("developerCommitCount",developerCommitCount);
        map.put("totalCommitCount",totalCommitCount);
        return map;
    }


    @Autowired
    public void setMeasureMapper(MeasureMapper measureMapper){
        this.measureMapper = measureMapper;
    }
}
