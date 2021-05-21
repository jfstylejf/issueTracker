package cn.edu.fudan.issueservice;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@SpringBootTest(classes = IssueServiceApplication.class)
@ActiveProfiles("test-fancying")
@PowerMockIgnore({"javax.crypto.*", "javax.management.*", "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "com.sun.org.apache.xalan.internal.xsltc.trax.*"})
public class IssueServiceApplicationTest {

    private final static String SH_PATH = System.getProperty("user.dir") + "/src/test/dependency/sh/";
    private final static String TEST_REPO_PATH = System.getProperty("user.dir") + "/src/test/dependency/repo";

    @Before
    public void beforeTest() throws IOException, InterruptedException {
        System.out.println(".................begin test.................");

        System.out.println("downloading test repo");

        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        Runtime rt = Runtime.getRuntime();

        String command = SH_PATH + (!windows ? "downloadTestProject.sh " : "downloadTestProject.bat ") + TEST_REPO_PATH;
        System.out.println(command);

        rt.exec("chmod 755 " + (!windows ? "downloadTestProject.sh" : "downloadTestProject.bat"));
        Process process = rt.exec(command);

        boolean timeOut = process.waitFor(30, TimeUnit.SECONDS);
        if (!timeOut || !new File(TEST_REPO_PATH + "/forTest").exists()) {
            System.out.println("download test repo failed");
            System.exit(1);
        } else {
            System.out.println("download test repo success");
        }
    }

    @Test
    public void init() {

    }

    @After
    public void afterTest() {
        System.out.println(".................test finish.................");
    }

}
