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
     * 根据情况获取开发者库下的新增物理行数
     * @param repoUuid
     * @param since
     * @param until
     * @param developerName
     * @return
     */
    int getAddLinesByDuration(@Param("repoUuid")String repoUuid,@Param("since")String since,@Param("until")String until,@Param("developer_name")String developerName);

    /**
     * 根据情况获取开发者在库下的物理行数
     * @param repoUuid
     * @param developerName
     * @param beginDate
     * @param endDate
     * @return
     */
    int getLocByCondition(@Param("repoUuid")String repoUuid,@Param("developer_name")String developerName,@Param("since")String beginDate,@Param("until")String endDate);

    /**
     * 获取开发者该库下的总提交次数
     * @param repoUuid
     * @param since
     * @param until
     * @param developerName
     * @return
     */
    int getCommitCountsByDuration(@Param("repoUuid")String repoUuid,@Param("since")String since,@Param("until")String until,@Param("developer_name")String developerName);

}
