package cn.edu.fudan.scanservice.mapper;

import cn.edu.fudan.scanservice.domain.dbo.Tool;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * description:
 *
 * @author fancying
 * create: 2020-03-10 20:35
 **/
@Repository
public interface ToolMapper {

    /**
     * get all installed tools
     *
     * @return list Tool
     */
    List<Tool> getAllTools();

    /**
     * modify tool enabled status
     *
     * @param id tool id
     * @param enabled  0 for turn off , 1 for turn on
     * @return is success
     */
    @Update("UPDATE tool SET is_enabled = #{is_enabled} WHERE id = #{id};")
    Integer modifyToolEnabled(@Param("id") int id,@Param("is_enabled") int enabled);

}