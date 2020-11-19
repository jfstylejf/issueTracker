package cn.edu.fudan.measureservice.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountMapper {

    /**
     * 获取开发者的唯一名称
     * @param name    git accountName
     * @return     account accountName
     */
    String getAccountName(@Param("accountName") String name);

}
