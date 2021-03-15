package cn.edu.fudan.dependservice.mapper;

import cn.edu.fudan.dependservice.domain.Group;
import cn.edu.fudan.dependservice.domain.RelationShip;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMapper extends RelationshipMapper {
    public int add(Group group);

    @Select("SELECT distinct(commit_id) FROM dependency " +
            "WHERE repo_uuid = #{repo_uuid};")
    List<String> getScannedCommitList(String repoUuid);
    // todo the seletct
    @Select("SELECT commit_id FROM dependency " +
            "WHERE repo_uuid = #{repo_uuid};")
    String getLastedScannedCommit(String repoUuid);
}
