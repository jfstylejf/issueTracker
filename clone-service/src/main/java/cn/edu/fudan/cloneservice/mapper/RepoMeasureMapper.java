package cn.edu.fudan.cloneservice.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepoMeasureMapper {

    int getDeveloperAddLines(@Param("repoUuidList") List<String> repoUuidList, @Param("developer_name")String developerName, @Param("since")String since, @Param("until")String until);
}
