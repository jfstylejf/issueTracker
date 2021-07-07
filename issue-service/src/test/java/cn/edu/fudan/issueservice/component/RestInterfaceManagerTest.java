package cn.edu.fudan.issueservice.component;

import cn.edu.fudan.issueservice.IssueServiceApplicationTest;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author beethoven
 * @date 2021-07-05 14:44:13
 */
public class RestInterfaceManagerTest extends IssueServiceApplicationTest {

    private RestInterfaceManager restInterfaceManager;

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    @Test
    public void getSonarIssueResultTest() {
        JSONObject sonarIssueResults = restInterfaceManager.getSonarIssueResults("18a2aeb0-db40-11eb-9db6-0242c0a83002_0375eb649670a27738c5551f8506777d16a56617", null, 100, false, 0);
        Assert.assertNotNull(sonarIssueResults);
    }

    @Test
    public void getRuleInfoTest() {
        JSONObject ruleInfo = restInterfaceManager.getRuleInfo("common-java:DuplicatedBlocks", null, null);
        Assert.assertNotNull(ruleInfo);
    }
}
