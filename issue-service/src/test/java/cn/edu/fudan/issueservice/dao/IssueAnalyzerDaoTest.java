package cn.edu.fudan.issueservice.dao;

import cn.edu.fudan.issueservice.IssueServiceApplicationTest;
import cn.edu.fudan.issueservice.domain.dbo.IssueAnalyzer;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.mapper.IssueAnalyzerMapper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

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
        IssueAnalyzer issueAnalyzer = new IssueAnalyzer();
        JSONObject obj = new JSONObject();
        obj.put("result", "11");
        obj.put("result2", "1221");
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
        JSONObject issueAnalyzer = issueAnalyzerDao.getAnalyzeResultByRepoUuidCommitIdTool("dafeb164-40fb-11eb-b6ff-f9c372bb0fcb", "76b821de0d80e755001d326b1840d417e02f4e42", "ESLint");
        // 第一步：先获取jsonArray数组
        JSONArray resArr = issueAnalyzer.getJSONArray("result");
        // 第二步：将数组转换成字符串
        String js = JSONObject.toJSONString(resArr, SerializerFeature.WriteClassName);
        // 第三步：将字符串转换成List集合
        List<RawIssue> analyzeRawIssues = JSONArray.parseArray(resArr.toJSONString(), RawIssue.class);
        System.out.println(1 + 1);
    }

}
