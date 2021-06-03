package cn.edu.fudan.accountservice.mapper;

import cn.edu.fudan.accountservice.domain.AccountAuthor;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * description:
 *
 * @author fancying
 * create: 2020-11-12 11:17
 **/
@Repository
public interface AccountAuthorMapper {

    /**
     * batch insert
     * @param accountAuthors list
     */
    void batchInsertAccountAuthor(@Param("accountAuthors") List<AccountAuthor> accountAuthors);

    /**
     * 修改被合并人关联信息
     *
     * @param subAccountName 被合并人姓名
     * @param majorAccountName 主合并人姓名
     * @param majorAccountUuid 主合并人uuid
     */
    void resetSubAccount(@Param("subAccountName") String subAccountName, @Param("majorAccountName") String majorAccountName, @Param("majorAccountUuid")String majorAccountUuid);

}
