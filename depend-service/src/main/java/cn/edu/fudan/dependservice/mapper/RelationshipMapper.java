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
    public List<RelationView> getRelationBydate(String date);
    public int add(RelationShip relationship);
    @Select("SELECT  count(*) FROM issueTracker.dependency_detail " +
            "WHERE repo_uuid = #{repoUuid};")
    int getCountByRepoUuid(String repoUuid);

    void deleteByRepoUuidLimit100(@Param("repoUuid") String repoUuid);

}
