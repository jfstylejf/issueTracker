package cn.edu.fudan.issueservice.service.impl;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.dao.LocationDao;
import cn.edu.fudan.issueservice.dao.RawIssueDao;
import cn.edu.fudan.issueservice.service.LocationService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
/**
 * @author Beethoven
 */
@Service
public class LocationServiceImpl implements LocationService {

    private LocationDao locationDao;

    private RawIssueDao rawIssueDao;

    private RestInterfaceManager restInterfaceManager;

    @Override
    public JSONObject getMethodTraceHistory(String metaUuid) {

        JSONObject methodTraceHistory = restInterfaceManager.getMethodTraceHistory(metaUuid);
        JSONArray commitInfoList = methodTraceHistory.getJSONArray("commitInfoList");

        Iterator<Object> iterator = commitInfoList.stream().iterator();
        while(iterator.hasNext()){
            JSONObject commitInfo = (JSONObject) iterator.next();
            List<String> rawIssueUuids = locationDao.getRawIssueUuidsByMethodName(commitInfo.getString("signature"), commitInfo.getString("filePath"));
            rawIssueUuids.removeIf(rawIssueUuid -> commitInfo.getString("commitId").equals(rawIssueDao.getCommitByRawIssueUuid(rawIssueUuid)));
            if(rawIssueUuids.size() == 0) {
                iterator.remove();
            }else{
                commitInfo.put("issueCountInMethod", rawIssueUuids.size());
            }
        }

        return methodTraceHistory;
    }


    @Autowired
    public void setRawIssueDao(RawIssueDao rawIssueDao) {
        this.rawIssueDao = rawIssueDao;
    }

    @Autowired
    public void setLocationDao(LocationDao locationDao) {
        this.locationDao = locationDao;
    }

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }
}
