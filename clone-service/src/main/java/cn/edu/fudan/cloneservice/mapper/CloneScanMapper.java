package cn.edu.fudan.cloneservice.mapper;

import cn.edu.fudan.cloneservice.domain.clone.CloneScan;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author zyh
 * @date 2020/5/25
 */
@Repository
public interface CloneScanMapper {
    /**
     * 获取对应的clone scan次数，用于检测对应repoId， commitId， type是否扫描过了
     * @param repoId repo id
     * @param commitId commit id
     * @param type method or snippet
     * @return
     */
    Integer getScanCountByCommitIdAndType(@Param("repo_id") String repoId,
                                          @Param("commit_id")String commitId,
                                          @Param("type")String type);

    /**
     * 插入一个clone scan的结果
     * @param cloneScan clone扫描状态
     */
    void insertOneScan(CloneScan cloneScan);

    /**
     * 删除clone cn.edu.fudan.common.scan， 删除整个项目时用到
     * @param repoId repo id
     */
    void deleteScanByRepoId(@Param("repo_id") String repoId);

    /**
     * 更新clone cn.edu.fudan.common.scan
     * @param cloneScan clone扫描状态
     */
    void updateOneScan(CloneScan cloneScan);
}
