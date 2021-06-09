package cn.edu.fudan.accountservice.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * description:
 *
 * @author fancying
 * create: 2020-11-12 11:17
 **/
@Repository
public interface CommitViewMapper {

    /**
     *
     * @param repoUuids 参与的库
     * @param since 起始时间
     * @param until 结束时间
     * @param developers 名字搜索
     * @return
     */
    List<Map<String, Object>> getDevelopers(@Param("repoUuids") List<String> repoUuids, @Param("since") String since, @Param("until") String until, @Param("developers") String developers);

}
