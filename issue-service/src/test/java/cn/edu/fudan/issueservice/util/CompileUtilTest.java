package cn.edu.fudan.issueservice.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;

public class CompileUtilTest {

    @InjectMocks //用来注解被测类IssueScanController的实例对象，对其注入mock
    private CompileUtil compileUtil;

    private MockMvc mockMvc;

    //该方法用来在当前测试类内测试方法执行测试之前，对mock测试进行初始化
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

}
