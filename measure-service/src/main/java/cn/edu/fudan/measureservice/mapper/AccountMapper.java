package cn.edu.fudan.measureservice.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.Min;
import java.util.List;

@Repository
public interface AccountMapper {

    /**
     * 获取开发者的唯一名称
     * @param gitName    git name
     * @return     account name
     */
    String getAccountName(@Param("gitName") String gitName);

    /**
     * 获取开发者聚合后名列表
     * @param accountGitNameList 待查询开发者的gitName列表
     * @return List<account_Name>
     */
    List<String> getAccountNameList(@Param("accountGitNameList") List<String> accountGitNameList);

    /**
     * 获取指定开发者的 gitName
     * @param accountName 聚合后名
     * @return account_gitName
     */
    String getAccountGitName(@Param("accountName") String accountName);

    /**
     * 获取多个开发者的 gitName
     * @param accountNameList 开发者聚合后名列表
     * @return List<account_gitName>
     */
    List<String> getAccountGitNameList(@Param("accountNameList") List<String> accountNameList);

}
