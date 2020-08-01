package cn.edu.fudan.cloneservice.mapper;

import cn.edu.fudan.cloneservice.domain.clone.CloneLocation;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author zyh
 * @date 2020/5/25
 */
@Repository
public interface CloneLocationMapper {

    /**
     * 批量插入clone检测结果
     * @param cloneLocations 检测结果列表
     */
    void insertCloneLocationList(List<CloneLocation> cloneLocations);

    /**
     * 获取对应commit的所有clone location
     * @param repoId repo id
     * @param commitId commit id
     * @return location list
     */
    List<CloneLocation> getCloneLocations(@Param("repo_id") String repoId,
                                          @Param("commit_id")String commitId);

    /**
     * 删除对应repo所有的clone location
     * @param repoId repo id
     */
    void deleteCloneLocations(@Param("repo_id") String repoId);


}
