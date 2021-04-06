package cn.edu.fudan.issueservice;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = IssueServiceApplication.class)
@TestPropertySource("classpath:application.properties")
public class IssueServiceApplicationTests {

    @BeforeClass
    public static void beforeTest() {
        System.out.println("开始测试..................................");
    }

    @Test
    public void init() {
    }

    @AfterClass
    public static void afterTest() {
        System.out.println("结束测试..................................");
    }


}
