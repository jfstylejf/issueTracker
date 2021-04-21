package cn.edu.fudan.issueservice.core.process;

import cn.edu.fudan.issueservice.IssueServiceApplicationTest;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.util.JGitHelper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author beethoven
 * @date 2021-03-02 16:33:39
 */
public class IssueMatcherTest extends IssueServiceApplicationTest {

    @Autowired
    private IssueMatcher issueMatcher;

    @Value("${test.repo.java.path}")
    private String REPO_PATH;

    private JGitHelper jGitHelper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        jGitHelper = new JGitHelper(REPO_PATH);
    }

    /**
     * case1: merge commit (single location method name change -> add, mapped, solved)
     * test commit 7b129b587d9a4b2bbae7bec3368ab1a96bf370de and f9b9f42f47782989b07ca5e5473fbb4b5f4d56da
     * <p>
     * son commit 7b129b587d9a4b2bbae7bec3368ab1a96bf370de has rawIssues a and m
     * parent commit f9b9f42f47782989b07ca5e5473fbb4b5f4d56da has rawIssues s and m
     * <p>
     * a and s should be regard as mapped and m should be regard as mapped
     * a,m,s have same code,but method name is different
     *
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Test
    public void mapRawIssuesTest1() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //get class
        Class<? extends IssueMatcher> issueMatcherClass = issueMatcher.getClass();
        //get method through reflect
        Method mapRawIssuesMethod = issueMatcherClass.getDeclaredMethod("mapRawIssues", List.class, List.class, String.class, Map.class, Map.class);
        //mock data
        String preRawIssueStr = "[{\"codeLines\":0,\"commitId\":\"f9b9f42f47782989b07ca5e5473fbb4b5f4d56da\",\"detail\":\"Return an empty collection instead of null.---CRITICAL\",\"fileName\":\"src/main/java/application/issue/merge/Merge.java\",\"issueId\":\"d93b4079-6a16-452f-baa4-eb14f671d950\",\"locations\":[{\"bugLines\":\"17\",\"code\":\"        return null;\\n\",\"endLine\":17,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/merge/Merge.java\",\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"methodName\":\"mergeSolved()\",\"offset\":1,\"rawIssueId\":\"3690e8c7-cf04-4d3a-9489-487fbfbe8c3f\",\"startLine\":17,\"startToken\":0,\"tokens\":[-49,-55],\"uuid\":\"9f23dc84-8957-48f3-91dc-9e51a7f00a5f\"}],\"mapped\":false,\"matchDegree\":0.0,\"matchInfos\":[],\"matchResultDTOIndex\":-1,\"notChange\":false,\"onceMapped\":false,\"priority\":0,\"rawIssueMatchResults\":[],\"realEliminate\":false,\"repoId\":\"3b1f56e8-69c9-11eb-b829-432f1e48d2fb\",\"scanId\":\"sonarqube\",\"status\":\"add\",\"tool\":\"sonarqube\",\"type\":\"Empty arrays and collections should be returned instead of null\",\"uuid\":\"3690e8c7-cf04-4d3a-9489-487fbfbe8c3f\",\"version\":3},{\"codeLines\":0,\"commitId\":\"f9b9f42f47782989b07ca5e5473fbb4b5f4d56da\",\"detail\":\"Return an empty collection instead of null.---CRITICAL\",\"fileName\":\"src/main/java/application/issue/merge/Merge.java\",\"issueId\":\"de5b126e-c811-4f06-af70-b3536ae45711\",\"locations\":[{\"bugLines\":\"26\",\"code\":\"        return null;\\n\",\"endLine\":26,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/merge/Merge.java\",\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"methodName\":\"mergeMapped()\",\"offset\":1,\"rawIssueId\":\"4bd9e99b-ab7d-420b-a422-31ff5de1025b\",\"startLine\":26,\"startToken\":0,\"tokens\":[-49,-55],\"uuid\":\"6b29719d-5bf8-4442-9b6b-8798b4a5fc2a\"}],\"mapped\":false,\"matchDegree\":0.0,\"matchInfos\":[],\"matchResultDTOIndex\":-1,\"notChange\":false,\"onceMapped\":false,\"priority\":0,\"rawIssueMatchResults\":[],\"realEliminate\":false,\"repoId\":\"3b1f56e8-69c9-11eb-b829-432f1e48d2fb\",\"scanId\":\"sonarqube\",\"status\":\"add\",\"tool\":\"sonarqube\",\"type\":\"Empty arrays and collections should be returned instead of null\",\"uuid\":\"4bd9e99b-ab7d-420b-a422-31ff5de1025b\",\"version\":3}]";
        String curRawIssueStr = "[{\"codeLines\":0,\"commitId\":\"7b129b587d9a4b2bbae7bec3368ab1a96bf370de\",\"detail\":\"Return an empty collection instead of null.---CRITICAL\",\"developerName\":\"shaoxi\",\"fileName\":\"src/main/java/application/issue/merge/Merge.java\",\"locations\":[{\"bugLines\":\"19\",\"code\":\"        return null;\\n\",\"endLine\":19,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/merge/Merge.java\",\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"methodName\":\"mergeMapped()\",\"offset\":1,\"rawIssueId\":\"9fb92ca4-ef3c-4205-bc4f-66c2275609fe\",\"startLine\":19,\"startToken\":0,\"tokens\":[-49,-55],\"uuid\":\"2ae0a2d0-ad46-4cf4-9723-c92fec785b61\"}],\"mapped\":false,\"matchDegree\":0.0,\"matchInfos\":[],\"matchResultDTOIndex\":-1,\"notChange\":false,\"onceMapped\":false,\"priority\":1,\"rawIssueMatchResults\":[],\"realEliminate\":false,\"repoId\":\"3b1f56e8-69c9-11eb-b829-432f1e48d2fb\",\"scanId\":\"sonarqube\",\"status\":\"default\",\"tool\":\"sonarqube\",\"type\":\"Empty arrays and collections should be returned instead of null\",\"uuid\":\"9fb92ca4-ef3c-4205-bc4f-66c2275609fe\",\"version\":1},{\"codeLines\":0,\"commitId\":\"7b129b587d9a4b2bbae7bec3368ab1a96bf370de\",\"detail\":\"Return an empty collection instead of null.---CRITICAL\",\"developerName\":\"shaoxi\",\"fileName\":\"src/main/java/application/issue/merge/Merge.java\",\"locations\":[{\"bugLines\":\"28\",\"code\":\"        return null;\\n\",\"endLine\":28,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/merge/Merge.java\",\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"methodName\":\"mergeAdd()\",\"offset\":1,\"rawIssueId\":\"a762a9e6-c8c4-455d-8dcf-52490faf3ef4\",\"startLine\":28,\"startToken\":0,\"tokens\":[-49,-55],\"uuid\":\"aa5d1320-f4af-4d65-a313-d1210899aa9a\"}],\"mapped\":false,\"matchDegree\":0.0,\"matchInfos\":[],\"matchResultDTOIndex\":-1,\"notChange\":false,\"onceMapped\":false,\"priority\":1,\"rawIssueMatchResults\":[],\"realEliminate\":false,\"repoId\":\"3b1f56e8-69c9-11eb-b829-432f1e48d2fb\",\"scanId\":\"sonarqube\",\"status\":\"default\",\"tool\":\"sonarqube\",\"type\":\"Empty arrays and collections should be returned instead of null\",\"uuid\":\"a762a9e6-c8c4-455d-8dcf-52490faf3ef4\",\"version\":1}]";
        List<RawIssue> preRawIssues = JSONArray.parseArray(preRawIssueStr, RawIssue.class);
        List<RawIssue> curRawIssues = JSONArray.parseArray(curRawIssueStr, RawIssue.class);
        //checkout to the commit
        jGitHelper.checkout("7b129b587d9a4b2bbae7bec3368ab1a96bf370de");
        //violence reflected
        mapRawIssuesMethod.setAccessible(true);
        mapRawIssuesMethod.invoke(issueMatcher, preRawIssues, curRawIssues, REPO_PATH, new HashMap<>(), new HashMap<>());
        //assert rawIssue mapped
        Assert.assertEquals(curRawIssues.get(1).getRawIssueMatchResults().get(0).getRawIssue(), preRawIssues.get(0));
        Assert.assertEquals(preRawIssues.get(1).getRawIssueMatchResults().get(0).getRawIssue(), curRawIssues.get(0));
    }

    /**
     * case2: normal commit (single location add line and del line -> mapped)
     * test commit e8b019d92b4aabbb4ff75c301408316fad8ce8ca and 6f796192d84c075e473f6b4db537cbfa1a96ae20
     * <p>
     * son commit 6f796192d84c075e473f6b4db537cbfa1a96ae20 has rawIssues a and d
     * parent commit e8b019d92b4aabbb4ff75c301408316fad8ce8ca has rawIssues a and d
     * <p>
     * rawIssue a and d should be regard as mapped
     * a, d have different codeLines
     *
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Test
    public void mapRawIssuesTest2() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //get class
        Class<? extends IssueMatcher> issueMatcherClass = issueMatcher.getClass();
        //get method through reflect
        Method mapRawIssuesMethod = issueMatcherClass.getDeclaredMethod("mapRawIssues", List.class, List.class, String.class, Map.class, Map.class);
        //mock data
        String preRawIssueStr = "[{\"codeLines\":0,\"commitId\":\"e8b019d92b4aabbb4ff75c301408316fad8ce8ca\",\"detail\":\"Return an empty collection instead of null.---CRITICAL\",\"fileName\":\"src/main/java/application/issue/single/location/AddLine.java\",\"issueId\":\"731c37ba-7081-4f87-9d5e-15e65ff011ad\",\"locations\":[{\"bugLines\":\"15\",\"code\":\"        return null;\\n\",\"endLine\":15,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/single/location/AddLine.java\",\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"methodName\":\"addLineTest()\",\"offset\":1,\"rawIssueId\":\"a5a1c761-7c18-4343-81d7-ca8edc0510c7\",\"startLine\":15,\"startToken\":0,\"tokens\":[-49,-55],\"uuid\":\"a842fae8-e533-4815-ab69-98c7d6b8c864\"}],\"mapped\":false,\"matchDegree\":0.0,\"matchInfos\":[],\"matchResultDTOIndex\":-1,\"notChange\":false,\"onceMapped\":false,\"priority\":0,\"rawIssueMatchResults\":[],\"realEliminate\":false,\"repoId\":\"3b1f56e8-69c9-11eb-b829-432f1e48d2fb\",\"scanId\":\"sonarqube\",\"status\":\"add\",\"tool\":\"sonarqube\",\"type\":\"Empty arrays and collections should be returned instead of null\",\"uuid\":\"a5a1c761-7c18-4343-81d7-ca8edc0510c7\",\"version\":1},{\"codeLines\":0,\"commitId\":\"e8b019d92b4aabbb4ff75c301408316fad8ce8ca\",\"detail\":\"Return an empty collection instead of null.---CRITICAL\",\"fileName\":\"src/main/java/application/issue/single/location/DelLine.java\",\"issueId\":\"d3afc5d4-6115-467b-b558-c5ec8a0662f3\",\"locations\":[{\"bugLines\":\"14\",\"code\":\"        return null;\\n\",\"endLine\":14,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/single/location/DelLine.java\",\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"methodName\":\"deleteLineTest()\",\"offset\":1,\"rawIssueId\":\"b3d08949-ed74-4a47-9512-ca03d5a34b1a\",\"startLine\":14,\"startToken\":0,\"tokens\":[-49,-55],\"uuid\":\"cc931fd4-d2bf-433e-9146-ef31f7c936fd\"}],\"mapped\":false,\"matchDegree\":0.0,\"matchInfos\":[],\"matchResultDTOIndex\":-1,\"notChange\":false,\"onceMapped\":false,\"priority\":0,\"rawIssueMatchResults\":[],\"realEliminate\":false,\"repoId\":\"3b1f56e8-69c9-11eb-b829-432f1e48d2fb\",\"scanId\":\"sonarqube\",\"status\":\"add\",\"tool\":\"sonarqube\",\"type\":\"Empty arrays and collections should be returned instead of null\",\"uuid\":\"b3d08949-ed74-4a47-9512-ca03d5a34b1a\",\"version\":1}]";
        String curRawIssueStr = "[{\"codeLines\":0,\"commitId\":\"6f796192d84c075e473f6b4db537cbfa1a96ae20\",\"detail\":\"Return an empty collection instead of null.---CRITICAL\",\"developerName\":\"支乌\",\"fileName\":\"src/main/java/application/issue/single/location/AddLine.java\",\"locations\":[{\"bugLines\":\"16\",\"code\":\"        return null;\\n\",\"endLine\":16,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/single/location/AddLine.java\",\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"methodName\":\"addLineTest()\",\"offset\":1,\"rawIssueId\":\"220590be-d8cf-4ad3-a469-70aa090cbcae\",\"startLine\":16,\"startToken\":0,\"tokens\":[-49,-55],\"uuid\":\"f0cf5dd3-ee48-4b27-b9af-a16584fb9d0c\"}],\"mapped\":false,\"matchDegree\":0.0,\"matchInfos\":[],\"matchResultDTOIndex\":-1,\"notChange\":false,\"onceMapped\":false,\"priority\":1,\"rawIssueMatchResults\":[],\"realEliminate\":false,\"repoId\":\"3b1f56e8-69c9-11eb-b829-432f1e48d2fb\",\"scanId\":\"sonarqube\",\"status\":\"default\",\"tool\":\"sonarqube\",\"type\":\"Empty arrays and collections should be returned instead of null\",\"uuid\":\"220590be-d8cf-4ad3-a469-70aa090cbcae\",\"version\":1},{\"codeLines\":0,\"commitId\":\"6f796192d84c075e473f6b4db537cbfa1a96ae20\",\"detail\":\"Return an empty collection instead of null.---CRITICAL\",\"developerName\":\"支乌\",\"fileName\":\"src/main/java/application/issue/single/location/DelLine.java\",\"locations\":[{\"bugLines\":\"13\",\"code\":\"        return null;\\n\",\"endLine\":13,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/single/location/DelLine.java\",\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"methodName\":\"deleteLineTest()\",\"offset\":1,\"rawIssueId\":\"f5867fb1-8053-4b70-ac47-5214b82a5c8f\",\"startLine\":13,\"startToken\":0,\"tokens\":[-49,-55],\"uuid\":\"716dd7c7-a9a7-41f7-98d4-81a8257f891d\"}],\"mapped\":false,\"matchDegree\":0.0,\"matchInfos\":[],\"matchResultDTOIndex\":-1,\"notChange\":false,\"onceMapped\":false,\"priority\":1,\"rawIssueMatchResults\":[],\"realEliminate\":false,\"repoId\":\"3b1f56e8-69c9-11eb-b829-432f1e48d2fb\",\"scanId\":\"sonarqube\",\"status\":\"default\",\"tool\":\"sonarqube\",\"type\":\"Empty arrays and collections should be returned instead of null\",\"uuid\":\"f5867fb1-8053-4b70-ac47-5214b82a5c8f\",\"version\":1}]";
        List<RawIssue> preRawIssues = JSONArray.parseArray(preRawIssueStr, RawIssue.class);
        List<RawIssue> curRawIssues = JSONArray.parseArray(curRawIssueStr, RawIssue.class);
        //checkout to the commit
        jGitHelper.checkout("7b129b587d9a4b2bbae7bec3368ab1a96bf370de");
        //violence reflected
        mapRawIssuesMethod.setAccessible(true);
        mapRawIssuesMethod.invoke(issueMatcher, preRawIssues, curRawIssues, REPO_PATH, new HashMap<>(), new HashMap<>());
        //assert mapped rawIssue1
        Assert.assertEquals(1, curRawIssues.get(0).getRawIssueMatchResults().size());
        Assert.assertEquals(curRawIssues.get(0).getRawIssueMatchResults().get(0).getRawIssue(), preRawIssues.get(0));
        //assert mapped rawIssue2
        Assert.assertEquals(curRawIssues.get(1).getRawIssueMatchResults().get(0).getRawIssue(), preRawIssues.get(1));
    }

    /**
     * case3: normal commit (single location fileName.methodName.code.codeLine.codeOffset)
     * test commit d7c60b219ce98178bb2240c347a0bc9da6903192 and 68e75e3ed7e78fff2abdbe8917b93604262558b0
     * <p>
     * son commit 68e75e3ed7e78fff2abdbe8917b93604262558b0 has rawIssues a
     * parent commit d7c60b219ce98178bb2240c347a0bc9da6903192 has rawIssues b
     * <p>
     * a and b should be regard as mapped
     * a and b have different fileName, methodName, codeLines, codeLine offset, code.
     *
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Test
    public void mapRawIssuesTest3() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //get class
        Class<? extends IssueMatcher> issueMatcherClass = issueMatcher.getClass();
        //get method through reflect
        Method mapRawIssuesMethod = issueMatcherClass.getDeclaredMethod("mapRawIssues", List.class, List.class, String.class, Map.class, Map.class);
        //mock map
        Map<String, String> preToCur = new HashMap<>();
        preToCur.put("src/main/java/application/issue/Issue.java", "src/main/java/application/issue/IssueSecond.java");
        Map<String, String> curToPre = new HashMap<>();
        preToCur.put("src/main/java/application/issue/IssueSecond.java", "src/main/java/application/issue/Issue.java");
        //mock rawIssues
        String preRawIssueStr = "[{\"codeLines\":0,\"commitId\":\"d7c60b219ce98178bb2240c347a0bc9da6903192\",\"detail\":\"Use try-with-resources or close this \\\"FileInputStream\\\" in a \\\"finally\\\" clause.---CRITICAL\",\"fileName\":\"src/main/java/application/issue/Issue.java\",\"issueId\":\"3b628473-04e0-4c74-b73a-7dcb7459009a\",\"locations\":[{\"bugLines\":\"14\",\"code\":\"            FileInputStream fileInputStream = new FileInputStream(\\\"test\\\");\\n\",\"endLine\":14,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue.java\",\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"methodName\":\"issueName(int, int)\",\"offset\":1,\"rawIssueId\":\"fa61b10a-ce92-449a-8869-48b8b8fdf19c\",\"startLine\":14,\"startToken\":0,\"tokens\":[-62,-62,-14,-62,-30],\"uuid\":\"1ef16306-f240-4135-a3eb-203f4193f13d\"}],\"mapped\":false,\"matchDegree\":0.0,\"matchInfos\":[],\"matchResultDTOIndex\":-1,\"notChange\":false,\"onceMapped\":false,\"priority\":0,\"rawIssueMatchResults\":[],\"realEliminate\":false,\"repoId\":\"3b1f56e8-69c9-11eb-b829-432f1e48d2fb\",\"scanId\":\"sonarqube\",\"status\":\"add\",\"tool\":\"sonarqube\",\"type\":\"Resources should be closed\",\"uuid\":\"fa61b10a-ce92-449a-8869-48b8b8fdf19c\",\"version\":1}]";
        String curRawIssueStr = "[{\"codeLines\":0,\"commitId\":\"68e75e3ed7e78fff2abdbe8917b93604262558b0\",\"detail\":\"Use try-with-resources or close this \\\"FileInputStream\\\" in a \\\"finally\\\" clause.---CRITICAL\",\"developerName\":\"支乌\",\"fileName\":\"src/main/java/application/issue/IssueSecond.java\",\"locations\":[{\"bugLines\":\"15\",\"code\":\"            FileInputStream fileInputStream = new FileInputStream(\\\"test/test2\\\");\\n\",\"endLine\":15,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/IssueSecond.java\",\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"methodName\":\"issueName2(int, int)\",\"offset\":1,\"rawIssueId\":\"a706fb9f-0ea9-4c69-82c9-6c03ee6923b8\",\"startLine\":15,\"startToken\":0,\"tokens\":[-62,-62,-14,-62,-30,-19],\"uuid\":\"23c19c17-58f5-4185-9d38-59063e2218b0\"}],\"mapped\":false,\"matchDegree\":0.0,\"matchInfos\":[],\"matchResultDTOIndex\":-1,\"notChange\":false,\"onceMapped\":false,\"priority\":1,\"rawIssueMatchResults\":[],\"realEliminate\":false,\"repoId\":\"3b1f56e8-69c9-11eb-b829-432f1e48d2fb\",\"scanId\":\"sonarqube\",\"status\":\"default\",\"tool\":\"sonarqube\",\"type\":\"Resources should be closed\",\"uuid\":\"a706fb9f-0ea9-4c69-82c9-6c03ee6923b8\",\"version\":1}]";
        List<RawIssue> preRawIssues = JSONArray.parseArray(preRawIssueStr, RawIssue.class);
        List<RawIssue> curRawIssues = JSONArray.parseArray(curRawIssueStr, RawIssue.class);
        //checkout to the commit
        jGitHelper.checkout("68e75e3ed7e78fff2abdbe8917b93604262558b0");
        //violence reflected
        mapRawIssuesMethod.setAccessible(true);
        mapRawIssuesMethod.invoke(issueMatcher, preRawIssues, curRawIssues, REPO_PATH, preToCur, curToPre);
        //this rawIssue should mapped
        Assert.assertFalse(preRawIssues.get(0).getRawIssueMatchResults().isEmpty());
        Assert.assertEquals(preRawIssues.get(0).getRawIssueMatchResults().get(0).getRawIssue(), curRawIssues.get(0));
    }

    /**
     * case4: normal commit (multiple location fileName.methodName.code.codeLine.codeOffset)
     * test commit e7a9bd48221570f548a1592c1a83f762c165b695 and 291832a0c93ce06d85871a7d211909d5eb84dafd
     * <p>
     * son commit 291832a0c93ce06d85871a7d211909d5eb84dafd has rawIssues a
     * parent commit e7a9bd48221570f548a1592c1a83f762c165b695 has rawIssues b
     * <p>
     * a and b should be regard as mapped
     * a and b have different fileName, methodName, codeLines, codeLine offset, code.
     *
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @Test
    public void mapRawIssuesTest4() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //get class
        Class<? extends IssueMatcher> issueMatcherClass = issueMatcher.getClass();
        //get method through reflect
        Method mapRawIssuesMethod = issueMatcherClass.getDeclaredMethod("mapRawIssues", List.class, List.class, String.class, Map.class, Map.class);
        //mock map
        Map<String, String> preToCur = new HashMap<>();
        preToCur.put("src/main/java/application/issue/Issue.java", "src/main/java/application/issue/Issue2.java");
        Map<String, String> curToPre = new HashMap<>();
        preToCur.put("src/main/java/application/issue/Issue2.java", "src/main/java/application/issue/Issue.java");
        //mock rawIssues
        String preRawIssueStr = "[{\"codeLines\":0,\"commitId\":\"e7a9bd48221570f548a1592c1a83f762c165b695\",\"detail\":\"Define a constant instead of duplicating this literal \\\"test locations\\\" 3 times.---MINOR\",\"fileName\":\"src/main/java/application/issue/Issue.java\",\"issueId\":\"9a8d16d7-e946-4a0f-835d-6407716c3891\",\"locations\":[{\"bugLines\":\"11\",\"code\":\"        System.out.println(\\\"test locations\\\");\\n\",\"endLine\":11,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue.java\",\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"methodName\":\"a()\",\"offset\":1,\"rawIssueId\":\"552230e8-a416-455a-bf0a-61c676b52473\",\"startLine\":11,\"startToken\":0,\"tokens\":[-66,-32,-51,-30,-11],\"uuid\":\"989955dc-0e7f-4b67-9b7c-a03cd83c7494\"},{\"bugLines\":\"15\",\"code\":\"        System.out.println(\\\"test locations\\\");\\n\",\"endLine\":15,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue.java\",\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"methodName\":\"b()\",\"offset\":1,\"rawIssueId\":\"552230e8-a416-455a-bf0a-61c676b52473\",\"startLine\":15,\"startToken\":0,\"tokens\":[-66,-32,-51,-30,-11],\"uuid\":\"a5885541-aaa8-4962-82b2-d88c927753a6\"},{\"bugLines\":\"19\",\"code\":\"        System.out.println(\\\"test locations\\\");\\n\",\"endLine\":19,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue.java\",\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"methodName\":\"c()\",\"offset\":1,\"rawIssueId\":\"552230e8-a416-455a-bf0a-61c676b52473\",\"startLine\":19,\"startToken\":0,\"tokens\":[-66,-32,-51,-30,-11],\"uuid\":\"b01ba7f0-0339-4f5a-b375-ca28b1228299\"}],\"mapped\":false,\"matchDegree\":0.0,\"matchInfos\":[],\"matchResultDTOIndex\":-1,\"notChange\":false,\"onceMapped\":false,\"priority\":0,\"rawIssueMatchResults\":[],\"realEliminate\":false,\"repoId\":\"3b1f56e8-69c9-11eb-b829-432f1e48d2fb\",\"scanId\":\"sonarqube\",\"status\":\"add\",\"tool\":\"sonarqube\",\"type\":\"String literals should not be duplicated\",\"uuid\":\"552230e8-a416-455a-bf0a-61c676b52473\",\"version\":1}]";
        String curRawIssueStr = "[{\"codeLines\":0,\"commitId\":\"291832a0c93ce06d85871a7d211909d5eb84dafd\",\"detail\":\"Define a constant instead of duplicating this literal \\\"test locations 2\\\" 3 times.---MINOR\",\"developerName\":\"支乌\",\"fileName\":\"src/main/java/application/issue/Issue2.java\",\"locations\":[{\"bugLines\":\"12\",\"code\":\"        System.out.println(\\\"test locations 2\\\");\\n\",\"endLine\":12,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"methodName\":\"a2()\",\"offset\":1,\"rawIssueId\":\"d98db024-11d8-4a99-bad7-94b69066560e\",\"startLine\":12,\"startToken\":0,\"tokens\":[-66,-32,-51,-30,-11,-103],\"uuid\":\"88cbdce4-25f0-4d93-a0e2-b02231d4949b\"},{\"bugLines\":\"17\",\"code\":\"        System.out.println(\\\"test locations 2\\\");\\n\",\"endLine\":17,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"methodName\":\"b2()\",\"offset\":1,\"rawIssueId\":\"d98db024-11d8-4a99-bad7-94b69066560e\",\"startLine\":17,\"startToken\":0,\"tokens\":[-66,-32,-51,-30,-11,-103],\"uuid\":\"3e354170-b07f-4fd1-84e2-c1042e863651\"},{\"bugLines\":\"22\",\"code\":\"        System.out.println(\\\"test locations 2\\\");\\n\",\"endLine\":22,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"methodName\":\"c2()\",\"offset\":1,\"rawIssueId\":\"d98db024-11d8-4a99-bad7-94b69066560e\",\"startLine\":22,\"startToken\":0,\"tokens\":[-66,-32,-51,-30,-11,-103],\"uuid\":\"b32d4562-223b-4257-bb37-db1cd14b5e90\"}],\"mapped\":false,\"matchDegree\":0.0,\"matchInfos\":[],\"matchResultDTOIndex\":-1,\"notChange\":false,\"onceMapped\":false,\"priority\":3,\"rawIssueMatchResults\":[],\"realEliminate\":false,\"repoId\":\"3b1f56e8-69c9-11eb-b829-432f1e48d2fb\",\"scanId\":\"sonarqube\",\"status\":\"default\",\"tool\":\"sonarqube\",\"type\":\"String literals should not be duplicated\",\"uuid\":\"d98db024-11d8-4a99-bad7-94b69066560e\",\"version\":1}]";
        List<RawIssue> preRawIssues = JSONArray.parseArray(preRawIssueStr, RawIssue.class);
        List<RawIssue> curRawIssues = JSONArray.parseArray(curRawIssueStr, RawIssue.class);
        //checkout to the commit
        jGitHelper.checkout("291832a0c93ce06d85871a7d211909d5eb84dafd");
        //violence reflected
        mapRawIssuesMethod.setAccessible(true);
        mapRawIssuesMethod.invoke(issueMatcher, preRawIssues, curRawIssues, REPO_PATH, preToCur, curToPre);
        //this rawIssue should mapped
        Assert.assertEquals(1, curRawIssues.get(0).getMatchInfos().size());
    }

    /**
     * case5: normal commit (single location solved)
     * test commit 8bfe2d10ae521dc5e694aa9714b5c0efb5e0ac54 and 046d4960f4906fadb9156a2100dc348abf49666e
     * <p>
     * son commit 046d4960f4906fadb9156a2100dc348abf49666e has not rawIssues a
     * parent commit 8bfe2d10ae521dc5e694aa9714b5c0efb5e0ac54 has rawIssues a
     * <p>
     * a should be regard as solved
     *
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Test
    public void mapRawIssuesTest5() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<? extends IssueMatcher> issueMatcherClass = issueMatcher.getClass();
        Method mapRawIssues = issueMatcherClass.getDeclaredMethod("mapRawIssues", List.class, List.class, String.class, Map.class, Map.class);
        //mock data
        String preRawIssues = "[{\"codeLines\":0,\"commitId\":\"8bfe2d10ae521dc5e694aa9714b5c0efb5e0ac54\",\"detail\":\"Return an empty collection instead of null.---CRITICAL\",\"fileName\":\"src/main/java/application/issue/Issue2.java\",\"issueId\":\"4417da17-7c9a-4a00-a8e4-0fccfd467994\",\"locations\":[{\"bugLines\":\"12\",\"code\":\"        return null;\\n\",\"endLine\":12,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"methodName\":\"a()\",\"offset\":1,\"rawIssueId\":\"1568c964-f15e-42d7-9c5b-b7555603dd3b\",\"startLine\":12,\"startToken\":0,\"tokens\":[-49,-55],\"uuid\":\"34dcd83f-a217-435e-9b07-9f004a11a9d2\"}],\"mapped\":false,\"matchDegree\":0.0,\"matchInfos\":[],\"matchResultDTOIndex\":-1,\"notChange\":false,\"onceMapped\":false,\"priority\":0,\"rawIssueMatchResults\":[],\"realEliminate\":false,\"repoId\":\"3b1f56e8-69c9-11eb-b829-432f1e48d2fb\",\"scanId\":\"sonarqube\",\"status\":\"add\",\"tool\":\"sonarqube\",\"type\":\"Empty arrays and collections should be returned instead of null\",\"uuid\":\"1568c964-f15e-42d7-9c5b-b7555603dd3b\",\"version\":1}]";
        String curRawIssues = "[]";
        List<RawIssue> rawIssues = JSONArray.parseArray(preRawIssues, RawIssue.class);
        List<RawIssue> rawIssues1 = JSONArray.parseArray(curRawIssues, RawIssue.class);

        jGitHelper.checkout("046d4960f4906fadb9156a2100dc348abf49666e");
        mapRawIssues.setAccessible(true);
        mapRawIssues.invoke(issueMatcher, rawIssues, rawIssues1, REPO_PATH, new HashMap<>(), new HashMap<>());

        Assert.assertEquals(0, rawIssues.get(0).getRawIssueMatchResults().size());
    }

    /**
     * 三个locations只匹配上2个
     */
    @Test
    public void mapRawIssuesTest7() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<? extends IssueMatcher> issueMatcherClass = issueMatcher.getClass();
        Method mapRawIssues = issueMatcherClass.getDeclaredMethod("mapRawIssues", List.class, List.class, String.class, Map.class, Map.class);
        //mock data
        String preRawIssues = "[{\"codeLines\":0,\"commitId\":\"757c997225ee58815cae008a6915536b903f67de\",\"detail\":\"Define a constant instead of duplicating this literal \\\"normal\\\" 3 times.---MINOR\",\"fileName\":\"src/main/java/application/issue/Issue2.java\",\"issueId\":\"a1afd0f9-1c55-43f9-bb56-4d3a984b26e8\",\"locations\":[{\"bugLines\":\"10-10\",\"code\":\"    public String dosome = \\\"normal\\\";\\n\",\"endLine\":10,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"methodName\":\"dosome \",\"offset\":1,\"rawIssueId\":\"6e9fe53c-70f1-4b55-bcb2-24e9733be7c8\",\"startLine\":10,\"startToken\":0,\"tokens\":[-50,-35,-42,-60],\"uuid\":\"345533c5-02d5-4470-a252-f5599ed6e826\"},{\"bugLines\":\"12-12\",\"code\":\"    public String test = \\\"normal\\\";\\n\",\"endLine\":12,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"methodName\":\"test \",\"offset\":1,\"rawIssueId\":\"6e9fe53c-70f1-4b55-bcb2-24e9733be7c8\",\"startLine\":12,\"startToken\":0,\"tokens\":[-50,-35,-30,-60],\"uuid\":\"3fe97477-a977-43e4-b10f-e4685a7b5300\"},{\"bugLines\":\"8-8\",\"code\":\"    public String volumn = \\\"normal\\\";\\n\",\"endLine\":8,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"methodName\":\"volumn \",\"offset\":1,\"rawIssueId\":\"6e9fe53c-70f1-4b55-bcb2-24e9733be7c8\",\"startLine\":8,\"startToken\":0,\"tokens\":[-50,-35,-52,-60],\"uuid\":\"4fd185ff-9d98-41b0-be7b-80ec16433e3a\"}],\"mapped\":false,\"matchDegree\":0.0,\"matchInfos\":[],\"matchResultDTOIndex\":-1,\"notChange\":false,\"onceMapped\":false,\"priority\":0,\"rawIssueMatchResults\":[],\"realEliminate\":false,\"repoId\":\"3b1f56e8-69c9-11eb-b829-432f1e48d2fb\",\"scanId\":\"sonarqube\",\"status\":\"add\",\"tool\":\"sonarqube\",\"type\":\"String literals should not be duplicated\",\"uuid\":\"6e9fe53c-70f1-4b55-bcb2-24e9733be7c8\",\"version\":1}]";
        String curRawIssues = "[{\"codeLines\":0,\"commitId\":\"f846f08b35f6ee3fd532557ef9c88d0342e475ed\",\"detail\":\"Define a constant instead of duplicating this literal \\\"normal\\\" 3 times.---MINOR\",\"developerName\":\"支乌\",\"fileName\":\"src/main/java/application/issue/Issue2.java\",\"locations\":[{\"bugLines\":\"8-8\",\"code\":\"    public String volee = \\\"normal\\\";\\n\",\"endLine\":8,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"methodName\":\"volee \",\"offset\":1,\"rawIssueId\":\"04eb7d47-2fee-47d5-b112-57c42eb01b5e\",\"startLine\":8,\"startToken\":0,\"tokens\":[-50,-35,-50,-60],\"uuid\":\"7ab1ffd8-28f2-4fdd-b0a3-e99456dda7f7\"},{\"bugLines\":\"10-10\",\"code\":\"    public String dosome = \\\"normal\\\";\\n\",\"endLine\":10,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"methodName\":\"dosome \",\"offset\":1,\"rawIssueId\":\"04eb7d47-2fee-47d5-b112-57c42eb01b5e\",\"startLine\":10,\"startToken\":0,\"tokens\":[-50,-35,-42,-60],\"uuid\":\"663e3f07-6a14-4dfd-9a91-da63929012ef\"},{\"bugLines\":\"12-12\",\"code\":\"    public String test = \\\"normal\\\";\\n\",\"endLine\":12,\"endToken\":0,\"filePath\":\"src/main/java/application/issue/Issue2.java\",\"locationMatchResults\":[],\"matched\":false,\"matchedIndex\":-1,\"methodName\":\"test \",\"offset\":1,\"rawIssueId\":\"04eb7d47-2fee-47d5-b112-57c42eb01b5e\",\"startLine\":12,\"startToken\":0,\"tokens\":[-50,-35,-30,-60],\"uuid\":\"aac62785-1426-4486-a671-254bab1bc2d6\"}],\"mapped\":false,\"matchDegree\":0.0,\"matchInfos\":[],\"matchResultDTOIndex\":-1,\"notChange\":false,\"onceMapped\":false,\"priority\":3,\"rawIssueMatchResults\":[],\"realEliminate\":false,\"repoId\":\"3b1f56e8-69c9-11eb-b829-432f1e48d2fb\",\"scanId\":\"sonarqube\",\"status\":\"default\",\"tool\":\"sonarqube\",\"type\":\"String literals should not be duplicated\",\"uuid\":\"04eb7d47-2fee-47d5-b112-57c42eb01b5e\",\"version\":1}]";
        List<RawIssue> preRawIssues1 = JSONArray.parseArray(preRawIssues, RawIssue.class);
        List<RawIssue> curRawIssues1 = JSONArray.parseArray(curRawIssues, RawIssue.class);

        String curLocations = "[\n" +
                "  {\n" +
                "    \"bugLines\": \"8-8\",\n" +
                "    \"code\": \"    public String volee = \\\"normal\\\";\\n\",\n" +
                "    \"endLine\": 8,\n" +
                "    \"endToken\": 0,\n" +
                "    \"filePath\": \"src/main/java/application/issue/Issue2.java\",\n" +
                "    \"locationMatchResults\": [],\n" +
                "    \"matched\": false,\n" +
                "    \"matchedIndex\": -1,\n" +
                "    \"methodName\": \"volee \",\n" +
                "    \"offset\": 1,\n" +
                "    \"rawIssueId\": \"04eb7d47-2fee-47d5-b112-57c42eb01b5e\",\n" +
                "    \"startLine\": 8,\n" +
                "    \"startToken\": 0,\n" +
                "    \"tokens\": [\n" +
                "      -50,\n" +
                "      -35,\n" +
                "      -50,\n" +
                "      -60\n" +
                "    ],\n" +
                "    \"uuid\": \"7ab1ffd8-28f2-4fdd-b0a3-e99456dda7f7\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"bugLines\": \"10-10\",\n" +
                "    \"code\": \"    public String dosome = \\\"normal\\\";\\n\",\n" +
                "    \"endLine\": 10,\n" +
                "    \"endToken\": 0,\n" +
                "    \"filePath\": \"src/main/java/application/issue/Issue2.java\",\n" +
                "    \"locationMatchResults\": [],\n" +
                "    \"matched\": false,\n" +
                "    \"matchedIndex\": -1,\n" +
                "    \"methodName\": \"dosome \",\n" +
                "    \"offset\": 1,\n" +
                "    \"rawIssueId\": \"04eb7d47-2fee-47d5-b112-57c42eb01b5e\",\n" +
                "    \"startLine\": 10,\n" +
                "    \"startToken\": 0,\n" +
                "    \"tokens\": [\n" +
                "      -50,\n" +
                "      -35,\n" +
                "      -42,\n" +
                "      -60\n" +
                "    ],\n" +
                "    \"uuid\": \"663e3f07-6a14-4dfd-9a91-da63929012ef\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"bugLines\": \"12-12\",\n" +
                "    \"code\": \"    public String test = \\\"normal\\\";\\n\",\n" +
                "    \"endLine\": 12,\n" +
                "    \"endToken\": 0,\n" +
                "    \"filePath\": \"src/main/java/application/issue/Issue2.java\",\n" +
                "    \"locationMatchResults\": [],\n" +
                "    \"matched\": false,\n" +
                "    \"matchedIndex\": -1,\n" +
                "    \"methodName\": \"test \",\n" +
                "    \"offset\": 1,\n" +
                "    \"rawIssueId\": \"04eb7d47-2fee-47d5-b112-57c42eb01b5e\",\n" +
                "    \"startLine\": 12,\n" +
                "    \"startToken\": 0,\n" +
                "    \"tokens\": [\n" +
                "      -50,\n" +
                "      -35,\n" +
                "      -30,\n" +
                "      -60\n" +
                "    ],\n" +
                "    \"uuid\": \"aac62785-1426-4486-a671-254bab1bc2d6\"\n" +
                "  }\n" +
                "]";
        String preLocations = "[\n" +
                "  {\n" +
                "    \"bugLines\": \"10-10\",\n" +
                "    \"code\": \"    public String dosome = \\\"normal\\\";\\n\",\n" +
                "    \"endLine\": 10,\n" +
                "    \"endToken\": 0,\n" +
                "    \"filePath\": \"src/main/java/application/issue/Issue2.java\",\n" +
                "    \"locationMatchResults\": [],\n" +
                "    \"matched\": false,\n" +
                "    \"matchedIndex\": -1,\n" +
                "    \"methodName\": \"dosome \",\n" +
                "    \"offset\": 1,\n" +
                "    \"rawIssueId\": \"6e9fe53c-70f1-4b55-bcb2-24e9733be7c8\",\n" +
                "    \"startLine\": 10,\n" +
                "    \"startToken\": 0,\n" +
                "    \"tokens\": [\n" +
                "      -50,\n" +
                "      -35,\n" +
                "      -42,\n" +
                "      -60\n" +
                "    ],\n" +
                "    \"uuid\": \"345533c5-02d5-4470-a252-f5599ed6e826\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"bugLines\": \"12-12\",\n" +
                "    \"code\": \"    public String test = \\\"normal\\\";\\n\",\n" +
                "    \"endLine\": 12,\n" +
                "    \"endToken\": 0,\n" +
                "    \"filePath\": \"src/main/java/application/issue/Issue2.java\",\n" +
                "    \"locationMatchResults\": [],\n" +
                "    \"matched\": false,\n" +
                "    \"matchedIndex\": -1,\n" +
                "    \"methodName\": \"test \",\n" +
                "    \"offset\": 1,\n" +
                "    \"rawIssueId\": \"6e9fe53c-70f1-4b55-bcb2-24e9733be7c8\",\n" +
                "    \"startLine\": 12,\n" +
                "    \"startToken\": 0,\n" +
                "    \"tokens\": [\n" +
                "      -50,\n" +
                "      -35,\n" +
                "      -30,\n" +
                "      -60\n" +
                "    ],\n" +
                "    \"uuid\": \"3fe97477-a977-43e4-b10f-e4685a7b5300\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"bugLines\": \"8-8\",\n" +
                "    \"code\": \"    public String volumn = \\\"normal\\\";\\n\",\n" +
                "    \"endLine\": 8,\n" +
                "    \"endToken\": 0,\n" +
                "    \"filePath\": \"src/main/java/application/issue/Issue2.java\",\n" +
                "    \"locationMatchResults\": [],\n" +
                "    \"matched\": false,\n" +
                "    \"matchedIndex\": -1,\n" +
                "    \"methodName\": \"volumn \",\n" +
                "    \"offset\": 1,\n" +
                "    \"rawIssueId\": \"6e9fe53c-70f1-4b55-bcb2-24e9733be7c8\",\n" +
                "    \"startLine\": 8,\n" +
                "    \"startToken\": 0,\n" +
                "    \"tokens\": [\n" +
                "      -50,\n" +
                "      -35,\n" +
                "      -52,\n" +
                "      -60\n" +
                "    ],\n" +
                "    \"uuid\": \"4fd185ff-9d98-41b0-be7b-80ec16433e3a\"\n" +
                "  }\n" +
                "]";

        System.out.println(JSON.toJSONString(preRawIssues1.get(0).getLocations()));
        jGitHelper.checkout("f846f08b35f6ee3fd532557ef9c88d0342e475ed");
        mapRawIssues.setAccessible(true);
        mapRawIssues.invoke(issueMatcher, preRawIssues1, curRawIssues1, REPO_PATH, new HashMap<>(), new HashMap<>());
        Assert.assertEquals(preRawIssues1.get(0).getRawIssueMatchResults().get(0).getRawIssue(), curRawIssues1.get(0));
    }
}
