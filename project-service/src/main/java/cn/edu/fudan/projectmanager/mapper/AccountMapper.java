package cn.edu.fudan.projectmanager.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * description:
 *
 * @author fancying
 * create: 2020-09-23 16:58
 **/
@Repository
public interface AccountMapper {

    @Select("SELECT * FROM issueTracker.account WHERE uuid = #{uuid};")
    Integer getAccountRightByAccountUuid(@Param("uuid") String accountId);

}
