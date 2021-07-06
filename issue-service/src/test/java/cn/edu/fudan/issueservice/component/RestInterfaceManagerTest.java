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
        JSONObject sonarIssueResults = restInterfaceManager.getSonarIssueResults("4202370e-346e-11eb-8dca-4dbb5f7a5f33_12f9f424f007dec1f65f388ae8fb702163395bfe", null, 100, false, 0);
        Assert.assertNotNull(sonarIssueResults);
    }
}
