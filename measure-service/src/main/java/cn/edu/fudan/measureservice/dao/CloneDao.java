package cn.edu.fudan.measureservice.dao;

import cn.edu.fudan.measureservice.component.RestInterfaceManager;
import cn.edu.fudan.measureservice.domain.dto.Query;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


/**
 * @author wjzho
 */
@Repository
public class CloneDao {


    private RestInterfaceManager restInterface;

    /**
     * 获取开发者自克隆行数
     * @param query 查询条件
     * @return selfCloneLines
     */
    public int getSelfCloneLines(Query query) {
        JSONObject cloneMeasure = restInterface.getCloneMeasure(query.getRepoUuidList().get(0),query.getDeveloper(),query.getSince(), query.getUntil());
        int selfCloneLines = 0;
        if (cloneMeasure != null){
            selfCloneLines = Integer.parseInt(cloneMeasure.getString("increasedCloneLines"));
        }
        return selfCloneLines;
    }

    @Autowired
    public void setRestInterface(RestInterfaceManager restInterface) {this.restInterface=restInterface;}

}
