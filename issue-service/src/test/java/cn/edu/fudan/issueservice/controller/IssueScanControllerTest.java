package cn.edu.fudan.issueservice.controller;

import cn.edu.fudan.issueservice.domain.ResponseBean;
import cn.edu.fudan.issueservice.domain.dbo.IssueRepo;
import cn.edu.fudan.issueservice.service.IssueScanService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.alibaba.fastjson.*;

import java.util.Date;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@RunWith(JUnit4.class)
public class IssueScanControllerTest {

    @InjectMocks //用来注解被测类IssueScanController的实例对象，对其注入mock
    private IssueScanController issueScanController;

    @Mock //用来模拟 被测类中所依赖的IssueScanService类对象
    private IssueScanService issueScanService;
    // 声明一个MockMvc类的实例，用于mock测试
    private MockMvc mockMvc;

    //该方法用来在当前测试类内测试方法执行测试之前，对mock测试进行初始化
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        // 对mockMvc实例进行初始化
        mockMvc = MockMvcBuilders.standaloneSetup(issueScanController).build();
    }

    // 测试方法
    @Test
    public void testGetScanStatus() throws Exception {

        IssueRepo res = new IssueRepo("uuid", "repoid", "branch", "tool", "startCommit",
                "endCommit", 1, 1, 1, "status", "nature", new Date(), new Date());
        System.out.println(res.toString());

        // 使用 Mockito.when( 被mock对象.有返回值方法 ).thenReturn( 给定的返回值 ); 在执行到getScanStatus()方法时，会直接返回给定值
        // 使用Mockito.any...() 或 Mockito.any( XXX.class )方法来代替对应类型的参数
        Mockito.when(issueScanService.getScanStatus(Mockito.anyString(), Mockito.anyString())).thenReturn(res);

        // 利用mockMvc实例模拟用户的url访问，获取返回的字符串结果
        String result = mockMvc.perform(MockMvcRequestBuilders.get("/issue/sonar/scan-status").param("repo_uuid", "repoIdValue"))
                .andReturn().getResponse().getContentAsString();

        ResponseBean<IssueRepo> expected = new ResponseBean<>(200, "success!", res);
        String expectedString = JSON.toJSONString(expected);
        JSONObject jsonObjectExpected = JSON.parseObject(expectedString);
        JSONObject jsonObjectResult = JSON.parseObject(result);
        Assert.assertEquals(jsonObjectExpected, jsonObjectResult);
    }

}
