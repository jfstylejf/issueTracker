package cn.edu.fudan.measureservice.mapper;

import cn.edu.fudan.measureservice.domain.Package;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author zjf
 */
@Repository
public interface PackageMeasureMapper {

    /**
     * 入库 包级别的度量信息
     * @param list  PackageMeasureList
     */
    void insertPackageMeasureDataList(List<Package> list);

    /**
     *
     */
    int samePackageMeasureExist(@Param("repo_id")String repoUuid,@Param("commit_id")String commitId,@Param("name")String name);

    List<Package> getPackageMeasureByrepoUuidAndCommit(@Param("repo_id")String repo_id,@Param("commit_id")String commit_id);
}
