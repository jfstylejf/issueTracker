package cn.edu.fudan.measureservice.domain.d0;

import cn.edu.fudan.measureservice.domain.dto.Query;

/**
 * @author wjzho
 */
public class IssueInfo extends BaseData {

    private Query query;

    public IssueInfo(Query query) {
        this.query = query;
    }

    @Override
    public void dataInjection() {

    }

}
