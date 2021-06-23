package cn.edu.fudan.cloneservice.mapper;

import cn.edu.fudan.cloneservice.domain.RepoTagMetric;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoMetricMapper {

    RepoTagMetric getRepoCloneLineMetric(@Param("repoUuid") String repoId);

}
