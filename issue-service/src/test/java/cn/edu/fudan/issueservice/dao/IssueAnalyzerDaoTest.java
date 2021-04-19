package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.IssueServiceApplicationTests;
import cn.edu.fudan.issueservice.domain.dbo.IssueAnalyzer;
import cn.edu.fudan.issueservice.mapper.IssueAnalyzerMapper;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IssueAnalyzerDaoTest extends IssueServiceApplicationTests {


    private IssueAnalyzerMapper issueAnalyzerMapper;

    private IssueAnalyzerDao issueAnalyzerDao;

    @Autowired
    public void setIssueMapper(IssueAnalyzerMapper issueAnalyzerMapper) {
        this.issueAnalyzerMapper = issueAnalyzerMapper;
    }

    @Autowired
    public void setIssueAnalyzerDao(IssueAnalyzerDao issueAnalyzerDao) {
        this.issueAnalyzerDao = issueAnalyzerDao;
    }

    @Test
    public void insertTest() {
        IssueAnalyzer issueAnalyzer = new IssueAnalyzer();
        JSONObject obj = new JSONObject();
        obj.put("result","11");
        obj.put("result2","1221");
        issueAnalyzer.setAnalyzeResult(obj);
        issueAnalyzer.setUuid("111");
        issueAnalyzer.setRepoUuid("11122");
        issueAnalyzer.setCommitId("1112233");
        List<IssueAnalyzer> list = new ArrayList<>();
        list.add(issueAnalyzer);
        issueAnalyzerMapper.insertIssueAnalyzerRecords(list);
        System.out.println(1 + 1);
    }

    @Test
    public void selectTest() {
        JSONObject issueAnalyzer = issueAnalyzerDao.getAnalyzeResultByRepoUuidCommitIdTool("1", "2", "sonar");
        System.out.println(1 + 1);
    }

}
