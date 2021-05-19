package cn.edu.fudan.projectmanager;

import cn.edu.fudan.projectmanager.controller.ProjectController;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * description:
 *
 * @author Richy
 * create: 2021-05-18 15:09
 **/

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@SpringBootTest(classes = ProjectManagerApplication.class)
@ActiveProfiles("test-richy")
@PowerMockIgnore({"javax.crypto.*", "javax.management.*", "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "com.sun.org.apache.xalan.internal.xsltc.trax.*"})
public class ProjectServiceApplicationTest {
    @Autowired
    ProjectController projectController;

    @Before
    public void beforeTest() {
        System.out.println("---------------------------开始测试---------------------------");
    }

    @Test
    public void init() {
        System.out.println("test");
    }

    @After
    public void afterTest() {
        System.out.println("---------------------------结束测试---------------------------");
    }
}