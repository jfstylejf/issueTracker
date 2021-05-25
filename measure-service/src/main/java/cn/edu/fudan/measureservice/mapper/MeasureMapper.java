package cn.edu.fudan.measureservice.mapper;


import cn.edu.fudan.measureservice.domain.bo.DeveloperWorkLoad;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author wjzho
 */
@Repository
public interface MeasureMapper {


    /**
     * 根据情况获取项目下的开发者工作量数据
     * @param repoUuidList
     * @param developerName
     * @param since
     * @param until
     * @return
     */
    DeveloperWorkLoad getDeveloperWorkLoad(@Param("repoUuidList")List<String> repoUuidList, @Param("developer_name")String developerName, @Param("since")String since, @Param("until")String until);

    /**
     * 获取所查询库列表中前3名增加代码物理行数的开发者
     * @param repoUuidList 查询库列表
     * @param since 查询起时时间
     * @param until 查询终止时间
     * @return key : developerName , developerLoc
     */
    List<Map<String, Object>> getDeveloperRankByLoc(@Param("repoUuidList")List<String> repoUuidList, @Param("since") String since, @Param("until") String until);


}
