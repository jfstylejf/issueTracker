package cn.edu.fudan.measureservice.domain.d0;

import cn.edu.fudan.measureservice.domain.dto.Query;
import cn.edu.fudan.measureservice.service.DataProcess;

/**
 * @author wjzho
 */
public abstract class BaseData implements DataProcess {

    protected Query query;

    public BaseData(Query query){
        this.query = query;
    }

}
