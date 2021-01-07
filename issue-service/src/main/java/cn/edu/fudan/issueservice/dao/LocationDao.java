package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.domain.dbo.Location;
import cn.edu.fudan.issueservice.mapper.LocationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author WZY
 * @version 1.0
 **/
@Repository
public class LocationDao {

    private LocationMapper locationMapper;

    @Autowired
    public void setLocationMapper(LocationMapper locationMapper) {
        this.locationMapper = locationMapper;
    }

    public void insertLocationList(List<Location> list) {
        locationMapper.insertLocationList(list);
    }

    public void deleteLocationByRawIssueIds(List<String> rawIssueIds) {
        if(rawIssueIds == null || rawIssueIds.isEmpty ()){
            return;
        }
        locationMapper.deleteLocationByRawIssueIds(rawIssueIds);
    }

    public List<Location> getLocations(String rawIssueId) {
        return locationMapper.getLocations(rawIssueId);
    }

    public List<Map<String, Object>> getLocationsByRawIssueUuid(String uuid) {
        return locationMapper.getLocationsByRawIssueUuid(uuid);
    }

    public List<String> getRawIssueUuidsByMethodName(String methodName, String filePath) {
        return locationMapper.getRawIssueUuidsByMethodName(methodName, filePath);
    }
}
