package cn.edu.fudan.issueservice;

import cn.edu.fudan.issueservice.controller.IssueScanController;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@SpringBootTest(classes = IssueServiceApplication.class)
@ActiveProfiles("test-fancying")
@PowerMockIgnore({"javax.crypto.*","javax.management.*","com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*",  "com.sun.org.apache.xalan.internal.xsltc.trax.*"})
public class IssueServiceApplicationTest {

    @Autowired
    IssueScanController scanController;

    @Before
    public void beforeTest() {
        System.out.println("开始测试..................................");
    }

    @Test
    public void init() {
        System.out.println("su");
    }

    @After
    public void afterTest() {
        System.out.println("结束测试..................................");
    }

}
