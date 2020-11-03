package cn.edu.fudan.issueservice.service.impl;

import cn.edu.fudan.issueservice.dao.IssueTypeDao;
import cn.edu.fudan.issueservice.domain.dbo.IssueType;
import cn.edu.fudan.issueservice.service.IssueTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class IssueTypeServiceImpl implements IssueTypeService {


    private IssueTypeDao issueTypeDao;

    @Autowired
    public void setIssueTypeDao(IssueTypeDao issueTypeDao) {
        this.issueTypeDao = issueTypeDao;
    }

    @Override
    public void insertIssueTypeList(List<IssueType> list) {
        issueTypeDao.insertIssueTypeList(list);
    }

    @Override
    public IssueType getIssueTypeByTypeName(String type) {
        return issueTypeDao.getIssueTypeByTypeName(type);
    }
}
