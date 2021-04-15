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

    void insertFileMeasureList(@Param("fileMeasureList")List<FileMeasure> fileMeasureList);

    int sameFileMeasureOfOneCommit(@Param("repo_id")String repoUuid,@Param("commit_id")String commitId);
}