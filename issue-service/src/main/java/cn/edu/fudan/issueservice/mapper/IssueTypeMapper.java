package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.dbo.IssueType;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author Beethoven
 */
@Repository
public interface IssueTypeMapper {

    /**
     * 获取issueType
     * @param type type
     * @return issueType
     */
    IssueType getIssueTypeByTypeName(@Param("type") String type);
}
