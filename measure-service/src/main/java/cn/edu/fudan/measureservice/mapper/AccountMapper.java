package cn.edu.fudan.measureservice.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountMapper {

    /**
     * 获取开发者的唯一名称
     * @param name    git name
     * @return     account name
     */
    String getAccountName(@Param("name") String name);

}
