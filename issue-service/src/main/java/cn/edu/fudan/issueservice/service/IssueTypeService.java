package cn.edu.fudan.issueservice.service;

import cn.edu.fudan.issueservice.domain.dbo.IssueType;

import java.util.List;

public interface IssueTypeService {

    void insertIssueTypeList(List<IssueType> list);

    IssueType getIssueTypeByTypeName(String type);
}
