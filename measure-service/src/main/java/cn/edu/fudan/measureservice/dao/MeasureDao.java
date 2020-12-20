package cn.edu.fudan.measureservice.dao;

import cn.edu.fudan.measureservice.domain.dto.Query;
import cn.edu.fudan.measureservice.mapper.MeasureMapper;
import cn.edu.fudan.measureservice.mapper.ProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wjzho
 */
@Repository
public class MeasureDao {

    private MeasureMapper measureMapper;
    private ProjectMapper projectMapper;

    /**
     * 获取开发者个人新增物理行
     * @Param query 查询条件
     * @return int developerAddLine
     */
    public int getDeveloperAddLine (Query query) {
        return measureMapper.getDeveloperAddLines(query.getRepoUuidList().get(0),query.getSince(),query.getUntil(),query.getDeveloper());
    }

    /**
     * 获取相应的物理行数据
     * @param query 查询条件
     * @return int Loc
     */
    public int getLocByCondition(Query query) {
        return measureMapper.getLocByCondition(query.getRepoUuidList(),query.getDeveloper(),query.getSince(),query.getUntil());
    }

    /**
     * 获取MeasureInfo中开发者物理行数
     * @param query 查询条件
     * @return Map<String,Object> key : developerLoc, totalLoc
     */
    public Map<String,Object> getDeveloperLocByCondition(Query query) {
        Map<String,Object> map = new HashMap<>(4);
        int developerLoc = measureMapper.getLocByCondition(query.getRepoUuidList(),query.getDeveloper(),query.getSince(),query.getUntil());
        int totalLoc = measureMapper.getLocByCondition(query.getRepoUuidList(),null,query.getSince(),query.getUntil());
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
        int developerCommitCount = projectMapper.getDeveloperCommitCountsByDuration(query.getRepoUuidList(),query.getSince(),query.getUntil(),query.getDeveloper());
        int totalCommitCount = projectMapper.getDeveloperCommitCountsByDuration(query.getRepoUuidList(),query.getSince(),query.getUntil(),null);
        map.put("developerCommitCount",developerCommitCount);
        map.put("totalCommitCount",totalCommitCount);
        return map;
    }

    /**
     * 获取所查询库列表中前3名增加代码物理行数的开发者
     * @param query 查询条件
     * @return key : developerName , developerLoc
     */
    public List<Map<String,Object>> getDeveloperRankByLoc(Query query) {
        return measureMapper.getDeveloperRankByLoc(query.getRepoUuidList(),query.getSince(),query.getUntil());
    }

    /**
     * 返回所查询库列表下的信息条数
     * @param query 查询条件
     * @return int countNum
     */
    public int getMsgNumByRepo(Query query) {
        return measureMapper.getMsgNumByRepo(query.getRepoUuidList());
    }

    @Autowired
    public void setMeasureMapper(MeasureMapper measureMapper){
        this.measureMapper = measureMapper;
    }

    @Autowired
    public void setProjectMapper(ProjectMapper projectMapper) {this.projectMapper = projectMapper;}

}
