package cn.edu.fudan.issueservice.mapper;


import cn.edu.fudan.issueservice.domain.Location;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface LocationMapper {

    void insertLocationList(List<Location> list);

    void deleteLocationByRepoId(@Param("repo_id") String repo_id);

    List<Location> getLocations(@Param("uuid") String rawIssueId);

}
