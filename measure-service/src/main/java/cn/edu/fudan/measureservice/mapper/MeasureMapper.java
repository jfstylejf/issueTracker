package cn.edu.fudan.measureservice.mapper;


import org.springframework.data.repository.query.Param;
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
    List<Map<String, Object>> getWorkLoadByCondition(@Param("repoUuidList")List<String> repoUuidList, @Param("developer_name")String developerName, @Param("since")String since, @Param("until")String until);

    /**
     * 获取所查询库列表中前3名增加代码物理行数的开发者
     * @param repoUuidList 查询库列表
     * @param since 查询起时时间
     * @param until 查询终止时间
     * @return key : developerName , developerLoc
     */
    List<Map<String, Object>> getDeveloperRankByLoc(@Param("repoUuidList")List<String> repoUuidList, @Param("since") String since, @Param("until") String until);

    /**
     * 返回所查询库列表下的信息条数
     * @param repoUuidList 查询库列表
     * @return int countNum
     */
    int getMsgNumByRepo(@Param("repoUuidList") List<String> repoUuidList);


    /**
     * 根据情况获取开发者库下的新增物理行数
     * @param repoUuid 查询库
     * @param since 查询起时时间
     * @param until 查询结束时间
     * @param developer 开发者
     * @return int developerAddLine
     */
    int getDeveloperAddLines(@Param("repoUuid")String repoUuid,@Param("since")String since,@Param("until")String until,@Param("developer")String developer);

    /**
     * 根据情况获取在查询库下的物理行数
     * @param repoUuidList 查询库列表
     * @param developer 开发者姓名
     * @param since 查询起时时间
     * @param until 查询结束时间
     * @return int Loc
     */
    int getLocByCondition(@Param("repoUuidList")List<String> repoUuidList,@Param("developer")String developer,@Param("since")String since,@Param("until")String until);


}
