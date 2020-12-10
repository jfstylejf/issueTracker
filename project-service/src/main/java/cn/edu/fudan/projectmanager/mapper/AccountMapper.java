package cn.edu.fudan.projectmanager.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
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

    List<Map<String, Object>> getProjectInfoByAccountName(@Param("accountName") String accountName);

    @Select("SELECT account_right FROM account WHERE account_name = #{accountName}  LIMIT 1")
    Integer queryRightByAccountName(@Param("accountName") String accountName);

    void updateProjectNameAP(@Param("accountUuid") String accountUuid, @Param("oldProjectName")String oldProjectName, @Param("newProjectName")String newProjectName);
}
