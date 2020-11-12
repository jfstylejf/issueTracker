package cn.edu.fudan.accountservice.mapper;

import cn.edu.fudan.accountservice.domain.AccountAuthor;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
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
    void batchInsertAccountAuthor(List<AccountAuthor> accountAuthors);

}
