package cn.edu.fudan.dependservice.codetrackermapper;


import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;


@Repository
public interface FileMapperInCT {
    /**
     * track_file
     * */
    @Select("SELECT meta_file_uuid FROM raw_file " +
            "WHERE repo_uuid = #{repoUuid}" +
            " and commitid = #{commitId}" +
            " and file_name = #{filePath} limit 1;")
    String getLastedScannedCommit(String filePath,String repoUuid,String commitId);
    @Select("SELECT meta_file_uuid FROM raw_file " +
            "WHERE file_name = #{filePath} limit 1;")
    String getMetaFileUUid(String filePath,String repoUuid,String commitId);
}
