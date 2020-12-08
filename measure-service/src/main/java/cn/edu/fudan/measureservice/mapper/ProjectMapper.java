package cn.edu.fudan.measureservice.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author wjzho
 */
@Repository
public interface ProjectMapper {

    /**
     * 返回具体情况下的developerList
     * @param repoUuidList 查询库列表
     * @param since 查询起止时间
     * @param until 查询结束时间
     * @return List<String> 开发者列表信息
     */
    List<String> getDeveloperList(@Param("repoUuidList") List<String> repoUuidList, @Param("since")String since, @Param("until")String until);

    /**
     *
     * 根据开发者的名字得到其参加过的项目信息
     * @param developer 开发者名字
     * @return Map key repo_id module
     */
    List<Map<String, String>> getProjectInfo(@Param("developer") String developer) ;

}
