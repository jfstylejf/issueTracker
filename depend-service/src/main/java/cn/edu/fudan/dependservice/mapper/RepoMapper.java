package cn.edu.fudan.dependservice.mapper;

import cn.edu.fudan.dependservice.domain.ProjectIdsInfo;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepoMapper {
    @Select("SELECT distinct() FROM  " +
            "WHERE name in  = #{repo_uuid};")
    List<String> getScannedCommitList(String repoUuid);
    List<ProjectIdsInfo> getAllProjectIds();



}
