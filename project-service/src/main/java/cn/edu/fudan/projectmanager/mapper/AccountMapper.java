package cn.edu.fudan.projectmanager.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * description:
 *
 * @author fancying
 * create: 2020-09-23 16:58
 **/
@Repository
public interface AccountMapper {

    @Select("SELECT account_right FROM issueTracker.account WHERE uuid = #{uuid};")
    Integer getAccountRightByAccountUuid(@Param("uuid") String accountId);

    @Select("select p.account_name,account_right,p.account_role,p.project_name" +
            " from account as a ,project_relation as p" +
            " where a.account_name = p.account_name and a.account_name = #{account_name}")
    List<Map<String, Object>> getProjectInfoByAccountName(String accountName);

}
