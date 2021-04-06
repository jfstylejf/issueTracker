package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.dbo.Location;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author beethoven
 */
@Repository
public interface LocationMapper {

    /**
     * insert locations
     *
     * @param list locations
     */
    void insertLocationList(List<Location> list);

    /**
     * 获取locations
     *
     * @param rawIssueId rawIssueUuid
     * @return locations
     */
    List<Location> getLocations(@Param("uuid") String rawIssueId);

    /**
     * 删除location
     *
     * @param list rawIssueUuid list
     */
    void deleteLocationByRawIssueIds(@Param("list") List<String> list);

    /**
     * 获取locations
     *
     * @param uuid rawIssueUuid
     * @return locations
     */
    List<Map<String, Object>> getLocationsByRawIssueUuid(String uuid);

    /**
     * 获取某个方法的rawIssueUuids
     *
     * @param methodName methodName
     * @param filePath   filePath
     * @return 某个方法的rawIssueUuids
     */
    List<String> getRawIssueUuidsByMethodName(String methodName, String filePath);

    /**
     * get issue filter detail
     *
     * @param rawIssueUuids rawIssueUuids
     * @return issue filter detail
     */
    List<Map<String, Object>> getIssueFilterDetailList(@Param("rawIssueUuids") List<String> rawIssueUuids);
}
