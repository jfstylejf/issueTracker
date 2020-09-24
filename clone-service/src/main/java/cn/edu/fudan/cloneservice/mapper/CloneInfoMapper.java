package cn.edu.fudan.cloneservice.mapper;

import cn.edu.fudan.cloneservice.domain.CloneInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author zyh
 * @date 2020/5/28
 */
@Repository
public interface CloneInfoMapper {

//    List<String> getGitName(@Param("name")String name);

    /**
     * 批量插入clone info
     * @param cloneInfoList clone info list
     */
    void insertCloneInfo(List<CloneInfo> cloneInfoList);

    /**
     * 删除clone info
     * @param repoId repo id
     */
    void deleteCloneInfo(@Param("repo_id") String repoId);
}
