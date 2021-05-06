package cn.edu.fudan.dependservice.mapper;

import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author shaoxi
 * @version 0.1.0
 * @create 2021-05-06 16:15
 **/
@Repository
public interface RepoMapper {
    @Select("SELECT language FROM issueTracker.sub_repository " +
            "WHERE repo_uuid = #{repo_uuid};")
    public String getLanguage(String repoUid);
}
