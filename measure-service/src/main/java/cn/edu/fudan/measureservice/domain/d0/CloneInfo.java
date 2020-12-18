package cn.edu.fudan.measureservice.domain.d0;

import cn.edu.fudan.measureservice.dao.CloneDao;
import cn.edu.fudan.measureservice.domain.dto.Query;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author wjzho
 */
public class CloneInfo extends BaseData {

    private CloneDao cloneDao;

    /**
     *  自克隆行数
     */
    private int selfCloneLines;

    public CloneInfo(Query query) {
        super(query);
    }

    @Override
    public void dataInjection() {
        /**
         *  key : getSelfCloneLines
         *  value {@link CloneDao}
         */
        selfCloneLines = cloneDao.getSelfCloneLines(query);
    }

    @Autowired
    public void setCloneDao(CloneDao cloneDao) {this.cloneDao = cloneDao;}

}
