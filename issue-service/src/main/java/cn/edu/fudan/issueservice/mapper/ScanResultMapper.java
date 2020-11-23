package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.ScanResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author Beethoven
 */
@Repository
public interface ScanResultMapper {

    /**
     * 插入scanResult
     * @param scanResult scanResult
     */
    void addOneScanResult(ScanResult scanResult);

    /**
     * 删除scanResult
     * @param repoId repoUuid
     * @param category category
     */
    void deleteScanResultsByRepoIdAndCategory(@Param("repo_id")String repoId, @Param("category") String category);

    /**
     * 获取scanResult
     * @param repoId repoUuid
     * @param since since
     * @param until until
     * @param category category
     * @param developer developer
     * @return scanResult
     */
    List<Map<String, Object>> getRepoIssueCounts(@Param("repo_id")String repoId, @Param("since")String since, @Param("until")String until, @Param("category") String category, @Param("developer")String developer);

    /**
     *  获取firstDate
     * @param repoId repoUuid
     * @return firstDate
     */
    String findFirstDateByRepo(@Param("repo_id")String repoId);
}
