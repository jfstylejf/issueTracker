package cn.edu.fudan.measureservice.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JiraMapper {
    /**
     * 返回开发者含有jira单号的commit次数
     * @param repoUuidList
     * @param since
     * @param until
     * @param developerName
     * @return developerJiraCount
     */
    int getJiraCountByCondition(@Param("repoUuidList") List<String> repoUuidList, @Param("since")String since, @Param("until")String until, @Param("developer_name")String developerName);
}
