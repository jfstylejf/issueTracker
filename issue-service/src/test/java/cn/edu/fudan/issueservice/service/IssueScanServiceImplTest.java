package cn.edu.fudan.issueservice.service;

import cn.edu.fudan.issueservice.controller.IssueScanController;
import cn.edu.fudan.issueservice.dao.IssueRepoDao;
import cn.edu.fudan.issueservice.service.impl.IssueScanServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(JUnit4.class)
public class IssueScanServiceImplTest {
    @InjectMocks //用来注解被测类IssueScanController的实例对象，对其注入mock
    private IssueScanServiceImpl issueScanServiceImpl;

    @Mock //用来模拟 被测类中所依赖的IssueScanService类对象
    private IssueRepoDao issueRepoDao;

    //该方法用来在当前的测试类内测试方法执行测试之前，对mock测试进行初始化
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    // 测试方法
    @Test
    public void testGetScanStatus() throws Exception {

    }

}
