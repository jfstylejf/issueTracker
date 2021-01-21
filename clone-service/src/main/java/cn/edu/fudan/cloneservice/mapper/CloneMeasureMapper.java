package cn.edu.fudan.cloneservice.mapper;

import cn.edu.fudan.cloneservice.domain.CloneMeasure;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author zyh
 * @date 2020/5/28
 */
@Repository
public interface CloneMeasureMapper {

    /**
     * 获取对应commit的clone度量信息
     * @param repoId repo id
     * @param commitId commit id
     * @return
     */
    CloneMeasure getCloneMeasure(@Param("repo_id") String repoId,
                                 @Param("commit_id") String commitId);

    /**
     * 插入clone度量信息
     * @param cloneMeasure clone measure
     */
    void insertCloneMeasure(CloneMeasure cloneMeasure);

    /**
     * 获取对应repo的所有clone度量信息
     * @param repoId repo id
     * @return measure list
     */
    List<CloneMeasure> getCloneMeasures(@Param("repo_id") String repoId);

    /**
     * 删除项目clone度量信息
     * @param repoId repo id
     */
    void deleteCloneMeasureByRepoId(@Param("repo_id") String repoId);

    /**
     * 获取对应的clone measure次数，用于检测对应repoId， commitId是否扫描过了
     * @param repoId repo id
     * @param commitId commit id
     * @return 1 or 0
     */
    Integer getMeasureCountByCommitId(@Param("repo_id") String repoId,
                                      @Param("commit_id")String commitId);

    /**
     * 获取最新一次commit的clone行数
     * @param repoIds repo ids
     * @return
     */
    CloneMeasure getLatestCloneLines(@Param("repo_ids") List<String> repoIds);

    /**
     * description
     * @param repositoryId repoId
     * @return list
     */
    List<String> getAllDeveloper(@Param("repositoryId") String repositoryId, @Param("start") String start, @Param("end") String end);

}
