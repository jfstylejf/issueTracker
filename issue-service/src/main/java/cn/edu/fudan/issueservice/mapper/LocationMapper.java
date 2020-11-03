package cn.edu.fudan.issueservice.mapper;


import cn.edu.fudan.issueservice.domain.dbo.Location;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface LocationMapper {

    void insertLocationList(List<Location> list);

    void deleteLocationByRepoIdAndTool(@Param("repo_id") String repo_id, @Param("tool")String tool);

    List<Location> getLocations(@Param("uuid") String rawIssueId);

    void deleteLocationByRawIssueIds(@Param("list")List<String> list);

}
