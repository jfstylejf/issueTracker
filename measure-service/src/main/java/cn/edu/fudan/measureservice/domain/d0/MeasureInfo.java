package cn.edu.fudan.measureservice.domain.d0;

import cn.edu.fudan.measureservice.dao.MeasureDao;
import cn.edu.fudan.measureservice.domain.dto.Query;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author wjzho
 */
@Slf4j
public class MeasureInfo extends BaseData {

    private Query query;

    private MeasureDao measureDao;

    /**
     *  开发者个人新增物理行
     */
    private int developerAddLine;
    /**
     *  个人总物理行（addLines+delLines）
     */
    private int developerLoc;
    /**
     *  团队总物理行
     */
    private int totalLoc;
    /**
     *  开发者个人commit提交次数
     */
    private int developerCommitCount;
    /**
     *  团队commit提交次数
     */
    private int totalCommitCount;

    public MeasureInfo(Query query) {
        this.query = query;
    }

    @Override
    public void dataInjection() {
        log.debug("{} in repo : {} start to get MeasureInfo",query.getDeveloper(),query.getRepoUuidList());
        /**
         *  key : getDeveloperAddLine
         *  value {@link MeasureDao}
         */
        developerAddLine = measureDao.getDeveloperAddLine(query);
        /**
         *  key : getLocByCondition
         *  value {@link MeasureDao}
         */
        Map<String,Object> LocMap = measureDao.getLocByCondition(query);
        developerLoc = (int) LocMap.get("developerLoc");
        totalLoc = (int) LocMap.get("totalLoc");
        /**
         *  key : getCommitCountsByDuration
         *  value {@link MeasureDao}
         */
        Map<String,Object> commitCountMap = measureDao.getCommitCountsByDuration(query);
        developerCommitCount = (int) commitCountMap.get("developerCommitCount");
        totalCommitCount = (int) commitCountMap.get("totalCommitCount");

        log.debug("{} in repo : {} get MeasureInfo success",query.getDeveloper(),query.getRepoUuidList());
    }

    public void setMeasureDao(MeasureDao measureDao) {
        this.measureDao = measureDao;
    }

}
