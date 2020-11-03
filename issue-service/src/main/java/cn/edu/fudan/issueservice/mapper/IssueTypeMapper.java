package cn.edu.fudan.issueservice.mapper;

import cn.edu.fudan.issueservice.domain.dbo.IssueType;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueTypeMapper {

    IssueType getIssueTypeByTypeName(@Param("type") String type);

    /**
     *  插入 issue type
     * @param list
     */
    void insertIssueTypeList(List<IssueType> list);
}
