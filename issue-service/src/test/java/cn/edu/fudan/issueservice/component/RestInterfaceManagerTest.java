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
        JSONObject sonarIssueResults = restInterfaceManager.getSonarIssueResults("18a2aeb0-db40-11eb-9db6-0242c0a83002_77315628fbe635ec417bc45c237527577092e742", null, 100, false, 1);
        Assert.assertNotNull(sonarIssueResults);
    }

    @Test
    public void getRuleInfoTest() {
        JSONObject ruleInfo = restInterfaceManager.getRuleInfo("common-java:DuplicatedBlocks", null, null);
        Assert.assertNotNull(ruleInfo);
    }
}
