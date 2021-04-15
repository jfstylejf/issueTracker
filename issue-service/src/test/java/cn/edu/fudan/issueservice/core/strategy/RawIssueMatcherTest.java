package cn.edu.fudan.issueservice.core.strategy;

import cn.edu.fudan.issueservice.core.process.RawIssueMatcher;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.util.AstParserUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Set;

@RunWith(JUnit4.class)
public class RawIssueMatcherTest {
    @InjectMocks
    private RawIssueMatcher rawIssueMatcher;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * 测试单行issue增加行号匹配
     *
     * @throws Exception Exception
     */
    @Test
    public void testSingleLocationAddLineMatch() throws Exception {

        String pre = "[{\"file_name\":\"AddLine.java\",\"realEliminate\":false,\"type\":\"Empty arrays and collections should be returned instead of null\",\"uuid\":\"a80475fd-c62b-461b-8e96-768c978002e5\",\"code_lines\":0,\"matchResultDTOIndex\":-1,\"mapped\":false,\"locations\":[{\"start_token\":0,\"code\":\"        return null;\",\"offset\":1,\"bug_lines\":\"12\",\"locationMatchResults\":[],\"start_line\":12,\"end_line\":12,\"method_name\":\"addLineTest()\",\"end_token\":0,\"matched\":false,\"tokens\":[-49,-55],\"matchedIndex\":-1}],\"detail\":\"Return an empty collection instead of null.\",\"rawIssueMatchResults\":[],\"scan_id\":\"tempScan_id\",\"commit_id\":\"tempCommit_id\"}]";
        String cur = "[{\"file_name\":\"AddLine.java\",\"realEliminate\":false,\"type\":\"Empty arrays and collections should be returned instead of null\",\"uuid\":\"9185bd48-52d8-4340-b9d1-eed7fba887c0\",\"code_lines\":0,\"matchResultDTOIndex\":-1,\"mapped\":false,\"locations\":[{\"start_token\":0,\"code\":\"        return null;\",\"offset\":1,\"bug_lines\":\"14\",\"locationMatchResults\":[],\"start_line\":14,\"end_line\":14,\"method_name\":\"addLineTest()\",\"end_token\":0,\"matched\":false,\"tokens\":[-49,-55],\"matchedIndex\":-1}],\"detail\":\"Return an empty collection instead of null.\",\"rawIssueMatchResults\":[],\"scan_id\":\"tempScan_id\",\"commit_id\":\"tempCommit_id\"}]";

        List<RawIssue> preRawIssueList = JSONObject.parseArray(JSONArray.parseArray(pre).toJSONString(), RawIssue.class);
        List<RawIssue> curRawIssueList = JSONObject.parseArray(JSONArray.parseArray(cur).toJSONString(), RawIssue.class);

        String filePath = "/Users/beethoven/Desktop/saic/forTest/src/main/java/issue/singleLocation/AddLine.java";

        Set<String> curName = AstParserUtil.getAllMethodAndFieldName(filePath);

        rawIssueMatcher.match(preRawIssueList, curRawIssueList, curName);
        for (RawIssue curRawIssue : curRawIssueList) {
            Assert.assertEquals(curRawIssue.getMappedRawIssue().getDetail(), curRawIssue.getDetail());
        }
    }

    /**
     * 测试单行issue减少行号匹配
     *
     * @throws Exception Exception
     */
    @Test
    public void testSingleLocationDelLineMatch() throws Exception {

        String pre = "[{\"file_name\":\"AddLine.java\",\"realEliminate\":false,\"type\":\"Empty arrays and collections should be returned instead of null\",\"uuid\":\"9185bd48-52d8-4340-b9d1-eed7fba887c0\",\"code_lines\":0,\"matchResultDTOIndex\":-1,\"mapped\":false,\"locations\":[{\"start_token\":0,\"code\":\"        return null;\",\"offset\":1,\"bug_lines\":\"14\",\"locationMatchResults\":[],\"start_line\":14,\"end_line\":14,\"method_name\":\"addLineTest()\",\"end_token\":0,\"matched\":false,\"tokens\":[-49,-55],\"matchedIndex\":-1}],\"detail\":\"Return an empty collection instead of null.\",\"rawIssueMatchResults\":[],\"scan_id\":\"tempScan_id\",\"commit_id\":\"tempCommit_id\"}]";
        String cur = "[{\"file_name\":\"AddLine.java\",\"realEliminate\":false,\"type\":\"Empty arrays and collections should be returned instead of null\",\"uuid\":\"a80475fd-c62b-461b-8e96-768c978002e5\",\"code_lines\":0,\"matchResultDTOIndex\":-1,\"mapped\":false,\"locations\":[{\"start_token\":0,\"code\":\"        return null;\",\"offset\":1,\"bug_lines\":\"12\",\"locationMatchResults\":[],\"start_line\":12,\"end_line\":12,\"method_name\":\"addLineTest()\",\"end_token\":0,\"matched\":false,\"tokens\":[-49,-55],\"matchedIndex\":-1}],\"detail\":\"Return an empty collection instead of null.\",\"rawIssueMatchResults\":[],\"scan_id\":\"tempScan_id\",\"commit_id\":\"tempCommit_id\"}]";

        List<RawIssue> preRawIssueList = JSONObject.parseArray(JSONArray.parseArray(pre).toJSONString(), RawIssue.class);
        List<RawIssue> curRawIssueList = JSONObject.parseArray(JSONArray.parseArray(cur).toJSONString(), RawIssue.class);

        String filePath = "/Users/beethoven/Desktop/saic/forTest/src/main/java/issue/singleLocation/AddLine.java";

        Set<String> curName = AstParserUtil.getAllMethodAndFieldName(filePath);

        rawIssueMatcher.match(preRawIssueList, curRawIssueList, curName);
        for (RawIssue curRawIssue : curRawIssueList) {
            Assert.assertEquals(curRawIssue.getMappedRawIssue().getDetail(), curRawIssue.getDetail());
        }
    }

    /**
     * 测试单行issue方法名改变匹配
     *
     * @throws Exception Exception
     */
    @Test
    public void testSingleLocationMethodNameMatch() throws Exception {

        String pre = "[{\"file_name\":\"MethodName.java\",\"realEliminate\":false,\"type\":\"Empty arrays and collections should be returned instead of null\",\"uuid\":\"385b70ba-f3fc-4f64-bd37-b2f3bb89d5b0\",\"code_lines\":0,\"matchResultDTOIndex\":-1,\"mapped\":false,\"locations\":[{\"start_token\":0,\"code\":\"        return null;\",\"offset\":1,\"bug_lines\":\"13\",\"locationMatchResults\":[],\"start_line\":13,\"end_line\":13,\"method_name\":\"MethodNameChange2()\",\"end_token\":0,\"matched\":false,\"tokens\":[-49,-55],\"matchedIndex\":-1}],\"detail\":\"Return an empty collection instead of null.\",\"rawIssueMatchResults\":[],\"scan_id\":\"tempScan_id\",\"commit_id\":\"tempCommit_id\"}, {\"file_name\":\"MethodName.java\",\"realEliminate\":false,\"type\":\"Empty arrays and collections should be returned instead of null\",\"uuid\":\"0e7c22b7-3e6e-4911-b1f9-07d2f9fffb53\",\"code_lines\":0,\"matchResultDTOIndex\":-1,\"mapped\":false,\"locations\":[{\"start_token\":0,\"code\":\"        return null;\",\"offset\":1,\"bug_lines\":\"17\",\"locationMatchResults\":[],\"start_line\":17,\"end_line\":17,\"method_name\":\"MethodNameChange2(int)\",\"end_token\":0,\"matched\":false,\"tokens\":[-49,-55],\"matchedIndex\":-1}],\"detail\":\"Return an empty collection instead of null.\",\"rawIssueMatchResults\":[],\"scan_id\":\"tempScan_id\",\"commit_id\":\"tempCommit_id\"}]";
        String cur = "[{\"file_name\":\"MethodName.java\",\"realEliminate\":false,\"type\":\"Empty arrays and collections should be returned instead of null\",\"uuid\":\"9e052c5f-feba-40e0-b54f-73970ece14dd\",\"code_lines\":0,\"matchResultDTOIndex\":-1,\"mapped\":false,\"locations\":[{\"start_token\":0,\"code\":\"        return null;\",\"offset\":1,\"bug_lines\":\"13\",\"locationMatchResults\":[],\"start_line\":13,\"end_line\":13,\"method_name\":\"MethodNameChange()\",\"end_token\":0,\"matched\":false,\"tokens\":[-49,-55],\"matchedIndex\":-1}],\"detail\":\"Return an empty collection instead of null.\",\"rawIssueMatchResults\":[],\"scan_id\":\"tempScan_id\",\"commit_id\":\"tempCommit_id\"}, {\"file_name\":\"MethodName.java\",\"realEliminate\":false,\"type\":\"Empty arrays and collections should be returned instead of null\",\"uuid\":\"22fd4379-227e-4845-89b6-f187938d15b4\",\"code_lines\":0,\"matchResultDTOIndex\":-1,\"mapped\":false,\"locations\":[{\"start_token\":0,\"code\":\"        return null;\",\"offset\":1,\"bug_lines\":\"17\",\"locationMatchResults\":[],\"start_line\":17,\"end_line\":17,\"method_name\":\"MethodNameChange2(int)\",\"end_token\":0,\"matched\":false,\"tokens\":[-49,-55],\"matchedIndex\":-1}],\"detail\":\"Return an empty collection instead of null.\",\"rawIssueMatchResults\":[],\"scan_id\":\"tempScan_id\",\"commit_id\":\"tempCommit_id\"}]";

        List<RawIssue> preRawIssueList = JSONObject.parseArray(JSONArray.parseArray(pre).toJSONString(), RawIssue.class);
        List<RawIssue> curRawIssueList = JSONObject.parseArray(JSONArray.parseArray(cur).toJSONString(), RawIssue.class);

        String filePath = "/Users/beethoven/Desktop/saic/forTest/src/main/java/issue/singleLocation/MethodName.java";

        Set<String> curName = AstParserUtil.getAllMethodAndFieldName(filePath);

        rawIssueMatcher.match(preRawIssueList, curRawIssueList, curName);
        for (RawIssue curRawIssue : curRawIssueList) {
            Assert.assertEquals(curRawIssue.getMappedRawIssue().getDetail(), curRawIssue.getDetail());
        }
    }
}
