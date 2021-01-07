package cn.edu.fudan.issueservice.service;

import com.alibaba.fastjson.JSONObject;

/**
 * @author Beethoven
 */
public interface LocationService {

    /**
     * 方法追溯页面追溯链
     * @param metaUuid metaUuid
     * @return 方法追溯页面追溯链
     */
    JSONObject getMethodTraceHistory(String metaUuid);
}
