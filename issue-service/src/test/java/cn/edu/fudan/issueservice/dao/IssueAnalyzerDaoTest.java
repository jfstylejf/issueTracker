package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.IssueServiceApplicationTest;
import cn.edu.fudan.issueservice.domain.dbo.IssueAnalyzer;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.mapper.IssueAnalyzerMapper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author beethoven
 * @date 2021-05-17 13:21:07
 */
public class IssueAnalyzerDaoTest extends IssueServiceApplicationTest {


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
        JSONObject obj = new JSONObject();
        obj.put("result", "11");
        obj.put("result2", "1221");
        IssueAnalyzer issueAnalyzer = IssueAnalyzer.builder()
                .analyzeResult(obj)
                .uuid(UUID.randomUUID().toString())
                .repoUuid("11122")
                .commitId("11122333")
                .tool("sonarqube")
                .invokeResult(IssueAnalyzer.InvokeResult.SUCCESS.getStatus())
                .build();
        issueAnalyzerMapper.insertIssueAnalyzerRecords(issueAnalyzer);
        JSONObject sonarqube = issueAnalyzerDao.getAnalyzeResultByRepoUuidCommitIdTool("11122", "11122333", "sonarqube");
        Assert.assertNotNull(sonarqube);
    }

}
