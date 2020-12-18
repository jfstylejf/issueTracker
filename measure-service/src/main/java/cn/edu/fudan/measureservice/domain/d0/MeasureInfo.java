package cn.edu.fudan.measureservice.domain.d0;

import cn.edu.fudan.measureservice.dao.MeasureDao;
import cn.edu.fudan.measureservice.domain.dto.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author wjzho
 */
@Slf4j
public class MeasureInfo extends BaseData {


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
        super(query);
    }

    @Override
    public void dataInjection() {
        log.debug("{} : in repo : {} start to get MeasureInfo",query.getDeveloper(),query.getRepoUuidList());
        /**
         *  key : getDeveloperAddLine
         *  value {@link MeasureDao}
         */
        developerAddLine = measureDao.getDeveloperAddLine(query);
        /**
         *  key : getLocByCondition
         *  value {@link MeasureDao}
         */
        Map<String,Object> locMap = measureDao.getDeveloperLocByCondition(query);
        developerLoc = (int) locMap.get("developerLoc");
        totalLoc = (int) locMap.get("totalLoc");
        /**
         *  key : getCommitCountsByDuration
         *  value {@link MeasureDao}
         */
        Map<String,Object> commitCountMap = measureDao.getCommitCountsByDuration(query);
        developerCommitCount = (int) commitCountMap.get("developerCommitCount");
        totalCommitCount = (int) commitCountMap.get("totalCommitCount");

        log.debug("{} in repo : {} get MeasureInfo success",query.getDeveloper(),query.getRepoUuidList());
    }

    @Autowired
    public void setMeasureDao(MeasureDao measureDao) {
        this.measureDao = measureDao;
    }

}
