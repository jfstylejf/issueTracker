package cn.edu.fudan.projectmanager.mapper;

import cn.edu.fudan.projectmanager.domain.SubRepository;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * description:
 *
 * @author Richy
 **/
@Repository
public interface ProjectMapper {

    /**
     * 插入repo信息
     * @param  accountUuid 当前登录人
     * @param  projectName 项目名
     */
    void insertOneProject (@Param("accountUuid")String accountUuid,@Param("projectName") String projectName);

    /**
     * 更新项目名
     * @param  accountUuid 当前登录人
     * @param  oldProjectName 旧项目名
     * @param  newProjectName 新项目名
     */
    void updateProjectNameP(@Param("accountUuid") String accountUuid, @Param("oldProjectName")String oldProjectName, @Param("newProjectName")String newProjectName);

    /**
     * 获取所有项目
     */
    List<Map<String, Object>> getProjectP();
}
