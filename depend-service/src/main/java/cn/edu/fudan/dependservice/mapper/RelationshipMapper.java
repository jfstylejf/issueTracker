package cn.edu.fudan.dependservice.mapper;

import cn.edu.fudan.dependservice.domain.RelationShip;
import cn.edu.fudan.dependservice.domain.RelationView;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelationshipMapper {
    // todo get certain commit  use where commit in
    // todo may one commit but many
    List<RelationView> getRelationBydate(String date);
    List<RelationView> getRelationBydateAndProjectIds(String date,String repoUuids);
    int add(RelationShip relationship);
    @Select("SELECT  count(*) FROM issueTracker.dependency_detail " +
            "WHERE repo_uuid = #{repoUuid};")
    int getCountByRepoUuid(String repoUuid);

    void deleteByRepoUuidLimit100(@Param("repoUuid") String repoUuid);
    void deleteByRepoUuidAndCommitId(@Param("repoUuid") String repoUuid,
                                     @Param("commitId") String commitId
                                     );
}
