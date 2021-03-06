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
                                          @Param("group_id") String groupId,
                                          @Param("commit_id")String commitId);

    /**
     * 获取对应commit的所有clone location(测试用)
     * @param repoId repo id
     * @param commitId commit id
     * @return location list
     */
    List<CloneLocation> getCloneLocationsTest(@Param("repo_id") String repoId,
                                          @Param("commit_id")String commitId);


    /**
     * 获取最近一次commit信息
     * @param repoUuid
     * @param since
     * @param until
     * @return 最新的commitId
     */
    String getLatestCommitId(@Param("repo_id") String repoUuid,
                              @Param("since") String since,
                              @Param("until") String until);
    /**
     * 删除对应repo所有的clone location
     * @param repoId repo id
     */
    void deleteCloneLocations(@Param("repo_id") String repoId);

    /**
     * 删除对应commit所有的clone location
     * @param commitId commit id
     */
    void deleteCloneLocationByCommitId(@Param("commit_id") String commitId);

    /**
     * 根据commitId获得组数
     * @param latestCommitId
     * @return clone组数
     */
    int getGroupCount(@Param("commit_id") String latestCommitId);

    int getCaseCount(@Param("commit_id") String latestCommitId, @Param("group_id")String groupId);

    int getFileCount(@Param("commit_id") String latestCommitId, @Param("group_id")String groupId);

    List<String> getCloneNum(@Param("commit_id") String latestCommitId, @Param("group_id")String groupId);

    List<String> getGroupIds(@Param("commit_id") String latestCommitId);

    String getRepoIdByCommitId(@Param("commit_id") String latestCommitId);
}
