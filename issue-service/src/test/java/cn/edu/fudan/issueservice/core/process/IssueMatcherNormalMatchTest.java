package cn.edu.fudan.issueservice.core.process;

import cn.edu.fudan.issueservice.IssueServiceApplicationTest;
import cn.edu.fudan.issueservice.core.analyzer.SonarQubeBaseAnalyzer;
import cn.edu.fudan.issueservice.domain.dbo.Location;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.domain.dbo.RawIssueMatchInfo;
import cn.edu.fudan.issueservice.domain.dto.RawIssueMatchResult;
import cn.edu.fudan.issueservice.util.AstParserUtil;
import cn.edu.fudan.common.jgit.JGitHelper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * description:
 *
 * @author fancying
 * create: 4/20/2021
 **/
@PrepareForTest({AstParserUtil.class})
public class IssueMatcherNormalMatchTest extends IssueServiceApplicationTest {

    @Autowired
    private IssueMatcher issueMatcher;

    private final String REPO_PATH = System.getProperty("user.dir") + "/src/test/dependency/repo/forTest";

    private JGitHelper jGitHelper;

    @Before
    public void setUp() {
        jGitHelper = new JGitHelper(REPO_PATH);

        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(AstParserUtil.class);
    }

    /**
     * 针对parent1有issue,文件未改动,parent2无issue,文件改动。并且应该正常追溯，不应出现add issue。
     *
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @Test
    public void normalMatch() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //init issueMatcher
        String curAllRawIssuesStr = "[{\"codeLines\":0,\"commitId\":\"494b3e766c911edfff28b6c6d6f04ff95e86ff93\",\"detail\":\"Define a constant instead of duplicating this literal \\\"normal\\\" 4 times.---MINOR\",\"developerName\":\"支乌\",\"fileName\":\"src/main/java/application/issue/Issue2.java\",\"issueId\":\"b9e6f4da-a92d-4f17-9d29-df294eac1752\",\"locations\":[{\"bugLines\":\"9-9\",\"code\":\"    public String volee = \\\"normal\\\";\\n\",\"endLine\":9,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[{\"location\":{\"bugLines\":\"9-9\",\"code\":\"    public String volee = \\\"normal\\\";\\n\",\"endLine\":9,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[{\"location\":{\"$ref\":\"$[0].locations[0]\"},\"matchedLocationId\":\"fa3f1280-67a9-484c-beaf-77d5c82c457d\",\"matchingDegree\":1.0}],\"matched\":true,\"matchedIndex\":-1,\"methodName\":\"volee \",\"offset\":1,\"rawIssueId\":\"65f8d210-301c-4515-b79a-b1cf2873fecd\",\"startLine\":9,\"startToken\":0,\"tokens\":[-50,-35,-50,-60],\"uuid\":\"4f0e2122-d374-4429-af6b-3a3fba7f97fd\"},\"matchedLocationId\":\"4f0e2122-d374-4429-af6b-3a3fba7f97fd\",\"matchingDegree\":1.0}],\"matched\":true,\"matchedIndex\":-1,\"methodName\":\"volee \",\"offset\":1,\"rawIssueId\":\"33439011-005a-4704-be41-15393c219e6e\",\"startLine\":9,\"startToken\":0,\"tokens\":[-50,-35,-50,-60],\"uuid\":\"fa3f1280-67a9-484c-beaf-77d5c82c457d\"},{\"bugLines\":\"11-11\",\"code\":\"    public String dosome = \\\"normal\\\";\\n\",\"endLine\":11,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[{\"location\":{\"bugLines\":\"11-11\",\"code\":\"    public String dosome = \\\"normal\\\";\\n\",\"endLine\":11,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[{\"location\":{\"$ref\":\"$[0].locations[1]\"},\"matchedLocationId\":\"eebbac32-1ed6-44b4-87da-f883f362ed4a\",\"matchingDegree\":1.0}],\"matched\":true,\"matchedIndex\":-1,\"methodName\":\"dosome \",\"offset\":1,\"rawIssueId\":\"65f8d210-301c-4515-b79a-b1cf2873fecd\",\"startLine\":11,\"startToken\":0,\"tokens\":[-50,-35,-42,-60],\"uuid\":\"a86902d5-25bd-4565-84c6-511a7b46dc4f\"},\"matchedLocationId\":\"a86902d5-25bd-4565-84c6-511a7b46dc4f\",\"matchingDegree\":1.0}],\"matched\":true,\"matchedIndex\":-1,\"methodName\":\"dosome \",\"offset\":1,\"rawIssueId\":\"33439011-005a-4704-be41-15393c219e6e\",\"startLine\":11,\"startToken\":0,\"tokens\":[-50,-35,-42,-60],\"uuid\":\"eebbac32-1ed6-44b4-87da-f883f362ed4a\"},{\"bugLines\":\"13-13\",\"code\":\"    public String test = \\\"normal\\\";\\n\",\"endLine\":13,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[{\"location\":{\"bugLines\":\"13-13\",\"code\":\"    public String test = \\\"normal\\\";\\n\",\"endLine\":13,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[{\"location\":{\"$ref\":\"$[0].locations[2]\"},\"matchedLocationId\":\"e74b90d3-e98a-43fb-9b6e-86730510399a\",\"matchingDegree\":1.0}],\"matched\":true,\"matchedIndex\":-1,\"methodName\":\"test \",\"offset\":1,\"rawIssueId\":\"65f8d210-301c-4515-b79a-b1cf2873fecd\",\"startLine\":13,\"startToken\":0,\"tokens\":[-50,-35,-30,-60],\"uuid\":\"8b3eee2a-43a4-44a1-baf6-39b623274421\"},\"matchedLocationId\":\"8b3eee2a-43a4-44a1-baf6-39b623274421\",\"matchingDegree\":1.0}],\"matched\":true,\"matchedIndex\":-1,\"methodName\":\"test \",\"offset\":1,\"rawIssueId\":\"33439011-005a-4704-be41-15393c219e6e\",\"startLine\":13,\"startToken\":0,\"tokens\":[-50,-35,-30,-60],\"uuid\":\"e74b90d3-e98a-43fb-9b6e-86730510399a\"},{\"bugLines\":\"15-15\",\"code\":\"    public String test2 = \\\"normal\\\";\\n\",\"endLine\":15,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[{\"location\":{\"bugLines\":\"15-15\",\"code\":\"    public String test2 = \\\"normal\\\";\\n\",\"endLine\":15,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[{\"location\":{\"$ref\":\"$[0].locations[3]\"},\"matchedLocationId\":\"9dca3829-7e96-4dbf-abd0-46ef1df1758f\",\"matchingDegree\":1.0}],\"matched\":true,\"matchedIndex\":-1,\"methodName\":\"test2 \",\"offset\":1,\"rawIssueId\":\"65f8d210-301c-4515-b79a-b1cf2873fecd\",\"startLine\":15,\"startToken\":0,\"tokens\":[-50,-35,-19,-60],\"uuid\":\"786d2752-5f6e-4b99-93ff-959f4be33ac6\"},\"matchedLocationId\":\"786d2752-5f6e-4b99-93ff-959f4be33ac6\",\"matchingDegree\":1.0}],\"matched\":true,\"matchedIndex\":-1,\"methodName\":\"test2 \",\"offset\":1,\"rawIssueId\":\"33439011-005a-4704-be41-15393c219e6e\",\"startLine\":15,\"startToken\":0,\"tokens\":[-50,-35,-19,-60],\"uuid\":\"9dca3829-7e96-4dbf-abd0-46ef1df1758f\"}],\"mapped\":false,\"matchDegree\":0.0,\"matchInfos\":[{\"curCommitId\":\"494b3e766c911edfff28b6c6d6f04ff95e86ff93\",\"curRawIssueUuid\":\"33439011-005a-4704-be41-15393c219e6e\",\"issueUuid\":\"b9e6f4da-a92d-4f17-9d29-df294eac1752\",\"matchDegree\":1.0,\"preCommitId\":\"38b5cfd2322bd533aa11b960ba9aa1e589fe5197\",\"preRawIssueUuid\":\"65f8d210-301c-4515-b79a-b1cf2873fecd\",\"status\":\"changed\",\"uuid\":\"ca52f394-68bb-40a6-b3e1-13dda80913a8\"}],\"matchResultDTOIndex\":-1,\"notChange\":true,\"onceMapped\":true,\"priority\":3,\"rawIssueMatchResults\":[],\"realEliminate\":false,\"repoId\":\"3b1f56e8-69c9-11eb-b829-432f1e48d2fb\",\"scanId\":\"sonarqube\",\"status\":\"add\",\"tool\":\"sonarqube\",\"type\":\"String literals should not be duplicated\",\"uuid\":\"33439011-005a-4704-be41-15393c219e6e\",\"version\":2}]";
        initIssueMatcher("38b5cfd2322bd533aa11b960ba9aa1e589fe5197", curAllRawIssuesStr);

        //get class
        Class<? extends IssueMatcher> issueMatcherClass = issueMatcher.getClass();
        //get method through reflect
        Method normalMatch = issueMatcherClass.getDeclaredMethod("normalMatch", String.class, String.class, String.class, Map.class);

        String curRawIssuesMatchResultStr = "{\"a711d433786a9045eb91e662142e3d983789faed\":[{\"codeLines\":0,\"commitId\":\"494b3e766c911edfff28b6c6d6f04ff95e86ff93\",\"detail\":\"Define a constant instead of duplicating this literal \\\"normal\\\" 4 times.---MINOR\",\"developerName\":\"支乌\",\"fileName\":\"src/main/java/application/issue/Issue2.java\",\"issueId\":\"38b9c6ef-331d-4c5c-b4ec-ac338aa10906\",\"locations\":[{\"bugLines\":\"9-9\",\"code\":\"    public String volee = \\\"normal\\\";\\n\",\"endLine\":9,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[{\"location\":{\"bugLines\":\"9-9\",\"code\":\"    public String volee = \\\"normal\\\";\\n\",\"endLine\":9,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[{\"location\":{\"$ref\":\"$.a711d433786a9045eb91e662142e3d983789faed[0].locations[0]\"},\"matchedLocationId\":\"d75696e8-6172-4dc6-a540-3f576c0430b5\",\"matchingDegree\":1.0}],\"matched\":true,\"matchedIndex\":-1,\"methodName\":\"volee \",\"offset\":1,\"rawIssueId\":\"304327b2-56fe-4bf6-a2e4-39ac1b9230bc\",\"startLine\":9,\"startToken\":0,\"tokens\":[-50,-35,-50,-60],\"uuid\":\"3a92a42a-f1fb-4740-9aa4-b6ecdfa89e28\"},\"matchedLocationId\":\"3a92a42a-f1fb-4740-9aa4-b6ecdfa89e28\",\"matchingDegree\":1.0}],\"matched\":true,\"matchedIndex\":-1,\"methodName\":\"volee \",\"offset\":1,\"rawIssueId\":\"ddd413a1-f5de-4893-90ff-6c1f3fbf0e9b\",\"startLine\":9,\"startToken\":0,\"tokens\":[-50,-35,-50,-60],\"uuid\":\"d75696e8-6172-4dc6-a540-3f576c0430b5\"},{\"bugLines\":\"11-11\",\"code\":\"    public String dosome = \\\"normal\\\";\\n\",\"endLine\":11,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[{\"location\":{\"bugLines\":\"11-11\",\"code\":\"    public String dosome = \\\"normal\\\";\\n\",\"endLine\":11,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[{\"location\":{\"$ref\":\"$.a711d433786a9045eb91e662142e3d983789faed[0].locations[1]\"},\"matchedLocationId\":\"233d9b42-6249-46ba-9a9b-d1fa7137eb16\",\"matchingDegree\":1.0}],\"matched\":true,\"matchedIndex\":-1,\"methodName\":\"dosome \",\"offset\":1,\"rawIssueId\":\"304327b2-56fe-4bf6-a2e4-39ac1b9230bc\",\"startLine\":11,\"startToken\":0,\"tokens\":[-50,-35,-42,-60],\"uuid\":\"6fadaba0-1db6-4058-ba52-4d50879e1898\"},\"matchedLocationId\":\"6fadaba0-1db6-4058-ba52-4d50879e1898\",\"matchingDegree\":1.0}],\"matched\":true,\"matchedIndex\":-1,\"methodName\":\"dosome \",\"offset\":1,\"rawIssueId\":\"ddd413a1-f5de-4893-90ff-6c1f3fbf0e9b\",\"startLine\":11,\"startToken\":0,\"tokens\":[-50,-35,-42,-60],\"uuid\":\"233d9b42-6249-46ba-9a9b-d1fa7137eb16\"},{\"bugLines\":\"13-13\",\"code\":\"    public String test = \\\"normal\\\";\\n\",\"endLine\":13,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[{\"location\":{\"bugLines\":\"13-13\",\"code\":\"    public String test = \\\"normal\\\";\\n\",\"endLine\":13,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[{\"location\":{\"$ref\":\"$.a711d433786a9045eb91e662142e3d983789faed[0].locations[2]\"},\"matchedLocationId\":\"d3a17029-dca6-423e-bec1-74d1a4ff570e\",\"matchingDegree\":1.0}],\"matched\":true,\"matchedIndex\":-1,\"methodName\":\"test \",\"offset\":1,\"rawIssueId\":\"304327b2-56fe-4bf6-a2e4-39ac1b9230bc\",\"startLine\":13,\"startToken\":0,\"tokens\":[-50,-35,-30,-60],\"uuid\":\"c6eb0cf2-3209-46d1-8e1b-f5516722cd52\"},\"matchedLocationId\":\"c6eb0cf2-3209-46d1-8e1b-f5516722cd52\",\"matchingDegree\":1.0}],\"matched\":true,\"matchedIndex\":-1,\"methodName\":\"test \",\"offset\":1,\"rawIssueId\":\"ddd413a1-f5de-4893-90ff-6c1f3fbf0e9b\",\"startLine\":13,\"startToken\":0,\"tokens\":[-50,-35,-30,-60],\"uuid\":\"d3a17029-dca6-423e-bec1-74d1a4ff570e\"},{\"bugLines\":\"15-15\",\"code\":\"    public String test2 = \\\"normal\\\";\\n\",\"endLine\":15,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[{\"location\":{\"bugLines\":\"15-15\",\"code\":\"    public String test2 = \\\"normal\\\";\\n\",\"endLine\":15,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[{\"location\":{\"$ref\":\"$.a711d433786a9045eb91e662142e3d983789faed[0].locations[3]\"},\"matchedLocationId\":\"79a362d1-a43a-4220-8454-b5def989b9aa\",\"matchingDegree\":1.0}],\"matched\":true,\"matchedIndex\":-1,\"methodName\":\"test2 \",\"offset\":1,\"rawIssueId\":\"304327b2-56fe-4bf6-a2e4-39ac1b9230bc\",\"startLine\":15,\"startToken\":0,\"tokens\":[-50,-35,-19,-60],\"uuid\":\"98a1f25d-8539-451e-a745-9b1335770df8\"},\"matchedLocationId\":\"98a1f25d-8539-451e-a745-9b1335770df8\",\"matchingDegree\":1.0}],\"matched\":true,\"matchedIndex\":-1,\"methodName\":\"test2 \",\"offset\":1,\"rawIssueId\":\"ddd413a1-f5de-4893-90ff-6c1f3fbf0e9b\",\"startLine\":15,\"startToken\":0,\"tokens\":[-50,-35,-19,-60],\"uuid\":\"79a362d1-a43a-4220-8454-b5def989b9aa\"}],\"mapped\":false,\"matchDegree\":0.0,\"matchInfos\":[{\"curCommitId\":\"494b3e766c911edfff28b6c6d6f04ff95e86ff93\",\"curRawIssueUuid\":\"ddd413a1-f5de-4893-90ff-6c1f3fbf0e9b\",\"issueUuid\":\"38b9c6ef-331d-4c5c-b4ec-ac338aa10906\",\"matchDegree\":1.0,\"preCommitId\":\"38b5cfd2322bd533aa11b960ba9aa1e589fe5197\",\"preRawIssueUuid\":\"304327b2-56fe-4bf6-a2e4-39ac1b9230bc\",\"status\":\"changed\",\"uuid\":\"69035dd4-c405-48ec-9849-e9b650949441\"}],\"matchResultDTOIndex\":-1,\"notChange\":false,\"onceMapped\":true,\"priority\":3,\"rawIssueMatchResults\":[],\"realEliminate\":false,\"repoId\":\"3b1f56e8-69c9-11eb-b829-432f1e48d2fb\",\"scanId\":\"sonarqube\",\"status\":\"add\",\"tool\":\"sonarqube\",\"type\":\"String literals should not be duplicated\",\"uuid\":\"ddd413a1-f5de-4893-90ff-6c1f3fbf0e9b\",\"version\":2}]}";
        Map<String, List<RawIssue>> curRawIssuesMatchResult = getCurRawIssuesMatchResult(curRawIssuesMatchResultStr);

        normalMatch.setAccessible(true);

        Mockito.when(AstParserUtil.getAllMethodAndFieldName(REPO_PATH + "/src/main/java/application/issue/Issue2.java"))
                .thenReturn(
                        new HashSet<>() {{
                            add("volee");
                            add("dosome");
                            add("test");
                            add("test2");
                        }}
                );

//        Mockito.when(jGitHelper.getDiffFilePair(preCommit, curCommit, preFileToCurFile, curFileToPreFile))
//                .thenReturn();

        normalMatch.invoke(issueMatcher, "3b1f56e8-69c9-11eb-b829-432f1e48d2fb", "sonarqube", "38b5cfd2322bd533aa11b960ba9aa1e589fe5197", new HashMap<>());
        normalMatch.invoke(issueMatcher, "3b1f56e8-69c9-11eb-b829-432f1e48d2fb", "sonarqube", "a711d433786a9045eb91e662142e3d983789faed", new HashMap<>());

        //assert issue is changed, not add.
        Assert.assertEquals("changed", issueMatcher.getCurAllRawIssues().get(0).getMatchInfos().get(0).getStatus());

    }

    private void initIssueMatcher(String commit, String curAllRawIssuesStr) {
        List<RawIssue> curAllRawIssues = JSONArray.parseArray(curAllRawIssuesStr, RawIssue.class);
        issueMatcher.setAnalyzer(new SonarQubeBaseAnalyzer());
        issueMatcher.setNewIssues(new HashMap<>());
        issueMatcher.setMappedIssues(new HashMap<>());
        issueMatcher.setSolvedIssue(new HashMap<>());
        issueMatcher.setJGitHelper(jGitHelper);
        issueMatcher.setCurCommit(commit);
        issueMatcher.setCurAllRawIssues(new ArrayList<>());
        issueMatcher.setCurAllRawIssues(curAllRawIssues);
    }

    private Map<String, List<RawIssue>> getCurRawIssuesMatchResult(String curRawIssuesMatchResultStr) {
        Map<String, List<RawIssue>> result = new HashMap<>(4);
        JSONObject parse = JSONObject.parseObject(curRawIssuesMatchResultStr);
        //get rawIssues
        Set<String> set = parse.keySet();
        for (String s : set) {
            result.put(s, new ArrayList<>());
            JSONArray temp = parse.getJSONArray(s);
            for (int i = 0; i < temp.size(); i++) {
                JSONObject o = temp.getJSONObject(i);

                RawIssue rawIssue = new RawIssue();
                rawIssue.setRepoId(o.getString("repoId"));
                rawIssue.setFileName(o.getString("fileName"));
                rawIssue.setIssueId(o.getString("issueId"));
                rawIssue.setScanId(o.getString("scanId"));
                rawIssue.setRealEliminate(o.getBoolean("realEliminate"));
                rawIssue.setCommitId(o.getString("commitId"));
                rawIssue.setPriority(o.getInteger("priority"));
                rawIssue.setType(o.getString("type"));
                rawIssue.setMatchDegree(o.getDouble("matchDegree"));
                rawIssue.setUuid(o.getString("uuid"));
                rawIssue.setVersion(o.getIntValue("version"));
                rawIssue.setOnceMapped(o.getBoolean("onceMapped"));
                rawIssue.setTool(o.getString("tool"));
                rawIssue.setCodeLines(o.getIntValue("codeLines"));
                rawIssue.setMatchResultDTOIndex(o.getInteger("matchResultDTOIndex"));

                JSONArray matchInfos = o.getJSONArray("matchInfos");
                List<RawIssueMatchInfo> matchInfoList = new ArrayList<>();
                for (int j = 0; j < matchInfos.size(); j++) {
                    RawIssueMatchInfo rawIssueMatchInfo = JSONObject.parseObject(JSON.toJSONString(matchInfos.getJSONObject(i)), RawIssueMatchInfo.class);
                    matchInfoList.add(rawIssueMatchInfo);
                }
                rawIssue.setMatchInfos(matchInfoList);


                rawIssue.setMapped(o.getBoolean("mapped"));

                JSONArray locations = o.getJSONArray("locations");
                List<Location> locationList = new ArrayList<>();
                for (int j = 0; j < locations.size(); j++) {
                    Location location = JSONObject.parseObject(JSON.toJSONString(locations.getJSONObject(i)), Location.class);
                    locationList.add(location);
                }
                rawIssue.setLocations(locationList);

                rawIssue.setNotChange(o.getBoolean("notChange"));
                rawIssue.setDetail(o.getString("detail"));

                JSONArray rawIssueMatchResults = o.getJSONArray("rawIssueMatchResults");
                List<RawIssueMatchResult> rawIssueMatchResultList = new ArrayList<>();
                for (int j = 0; j < rawIssueMatchResults.size(); j++) {
                    RawIssueMatchResult rawIssueMatchResult = JSONObject.parseObject(JSON.toJSONString(rawIssueMatchResults.getJSONObject(i)), RawIssueMatchResult.class);
                    rawIssueMatchResultList.add(rawIssueMatchResult);
                }
                rawIssue.setRawIssueMatchResults(rawIssueMatchResultList);

                rawIssue.setDeveloperName(o.getString("developerName"));
                rawIssue.setStatus(o.getString("status"));
            }
        }

        return result;
    }

}
