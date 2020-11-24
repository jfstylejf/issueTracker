package cn.edu.fudan.projectmanager.mapper;

import cn.edu.fudan.projectmanager.domain.SubRepository;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

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
     * @param  newProject
     * @return integer 返回影响行数n（n为0时实际为插入失败）
     */
   // @Insert("INSERT INTO`project` SET `project_name` = #{projectName},'import_account_uuid' = #{accountUuid};")
    Integer insertOneProject (Map<String,Integer> newProject);
}
