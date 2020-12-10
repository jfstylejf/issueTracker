package cn.edu.fudan.issueservice.service;

import cn.edu.fudan.issueservice.dao.IssueDao;
import cn.edu.fudan.issueservice.dao.IssueRepoDao;
import cn.edu.fudan.issueservice.service.impl.IssueIgnoreServiceImpl;
import cn.edu.fudan.issueservice.service.impl.IssueScanServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class IssueIgnoreServiceImplTest {
    @InjectMocks //用来注解被测类IssueIgnoreServiceImpl的实例对象，对其注入mock
    private IssueIgnoreServiceImpl issueIgnoreServiceImpl;

    @Mock //用来模拟 被测类中所依赖的IssueDao类对象
    private IssueDao issueDao;

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
