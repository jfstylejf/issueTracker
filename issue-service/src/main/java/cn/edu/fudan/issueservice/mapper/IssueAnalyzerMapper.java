package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.dbo.IssueAnalyzer;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author Jeff
 */
@Repository
public interface IssueAnalyzerMapper {

    /**
     * 插入issueIgnore记录
     *
     * @param issueAnalyzer issueAnalyzer
     */
    void insertIssueAnalyzerRecords(List<IssueAnalyzer> issueAnalyzer);


    /**
     *
     * @param repoUuid repoUuid
     * @param commitId commitId
     * @param tool 工具名
     * @return IssueAnalyzer
     */
    IssueAnalyzer getIssueAnalyzeResultByRepoUuidCommitIdTool(@Param("repoUuid") String repoUuid,
                                                          @Param("commitId") String commitId,
                                                          @Param("tool") String tool);
}
