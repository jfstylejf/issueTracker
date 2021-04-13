package cn.edu.fudan.dependservice.mapper;


import cn.edu.fudan.dependservice.domain.DependencyInfo;
import cn.edu.fudan.dependservice.domain.ProjectIdsInfo;
import cn.edu.fudan.dependservice.domain.RelationShip;
import cn.edu.fudan.dependservice.domain.RepoUuidsInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface LocationMapper {

//    /**
//     * 获取指定路径指定方法的最新rawIssue个数
//     *
//     * @param list
//     * @return list
//     */
//    List<NodeDetail> getRawIssueCount(@Param("list") List<NodeDetail> list);

    String getProjectName(@Param("projectId") String projectId);
    List<RelationShip> getDependencyInfo(@Param("repoUuid") String repoUuid);

    List<RepoUuidsInfo> getRepoUuids(@Param("projectName") String projectName);

    /**
     * 获取所有projectId
     *
     * @return String
     */
    List<ProjectIdsInfo> getAllProjectIds();

}
