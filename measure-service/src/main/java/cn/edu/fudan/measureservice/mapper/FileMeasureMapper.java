package cn.edu.fudan.measureservice.mapper;

import cn.edu.fudan.measureservice.domain.core.FileMeasure;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * description:
 *
 * @author fancying
 * create: 2020-06-10 23:43
 **/
@Repository
public interface FileMeasureMapper {

    void insertOneFileMeasure(FileMeasure fileMeasure);

    List<Map<String, Object>> getDevHistoryCommitInfo(@Param("repo_id")String repo_id, @Param("since")String since, @Param("until")String until);

    List<Map<String, Object>> getDevHistoryFileInfo(@Param("commit_id")String commit_id);

    Integer getCcnByCommitIdAndFilePath(@Param("commit_id")String commit_id,@Param("file_path")String file_path);

    /**
     * 插入数据
     * @param fileMeasureList 待插入数据列
     */
    void insertFileMeasureList(@Param("fileMeasureList")List<FileMeasure> fileMeasureList);

    /**
     * 返回当前查询条件下， file_measure表 入库数据数量
     * @param repoUuid 查询库
     * @param commitId 查询 commit_id
     * @return 符合条件的查询个数
     */
    int sameFileMeasureOfOneCommit(@Param("repo_id")String repoUuid,@Param("commit_id")String commitId);

    /**
     * 查询库下所有大文件最新信息
     * @param repoUuidList 查询库列表
     * @return key: file_path, total_lines, currentModifyTime, repo_id
     */
    List<Map<String,Object>> getCurrentFileInfoByRepoUuidList(@Param("repoUuidList") List<String> repoUuidList,@Param("until") String until);
}