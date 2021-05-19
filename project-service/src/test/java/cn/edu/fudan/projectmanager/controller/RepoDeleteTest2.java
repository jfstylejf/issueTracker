package cn.edu.fudan.projectmanager.controller;

import cn.edu.fudan.projectmanager.component.RestInterfaceManager;
import cn.edu.fudan.projectmanager.dao.SubRepositoryDao;
import cn.edu.fudan.projectmanager.domain.Account;
import cn.edu.fudan.projectmanager.service.impl.ProjectControlServiceImpl;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * description: the test of delete repo
 *
 * @author Richy
 * create: 2021-04-22 21:16
 **/

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
//配置文件，连接测试库
@TestPropertySource(value = {"classpath:application-test-richy.properties"})
//按指定顺序运行测试方法
@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)

@Transactional
@Rollback() // 事务自动回滚，默认是true
public class RepoDeleteTest2 {

    /**
     * 2.测试某服务未回调时的删除库功能
     *   （1）将待删库放入回收站
     *   （2）调用总删库接口（各个服务硬删除，project服务软删除）
     *   （3）各个服务完成硬删除并检查确认没有脏数据后，调用project服务回调接口，修改该服务删除状态
     *   （4）project服务判断各服务删除状态，均为1后硬删除project服务repo数据
     *   （5）恢复数据
     *
     *   输入:repoUuid (DEPENDENCY,CLONE服务未回调)
     *   预期结果：test_03，test_04失败
     */

    final String token = "ec15d79e36e14dd258cfff3d48b73d35";
    //测试库
    final String repoUuid = "fb74406a-346e-11eb-8dca-4dbb5f7a5f33";

    @Autowired
    ProjectControlServiceImpl projectControlService;

    @Autowired
    SubRepositoryDao subRepositoryDao;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;
    private MockHttpSession session;

    @Mock
    RestInterfaceManager restInterfaceManager;

    @Before
    public void setupMockMvc() {
        //初始化MockMvc对象
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        session = new MockHttpSession();
        Account account = new Account("admin", "1");
        session.setAttribute("user", account); //拦截器那边会判断用户是否登录，所以这里注入一个用户

    }

    @Test
    @Rollback(value = false)
    public void test_01_Recycled() throws Exception {

        Integer recycled = 0;

        //修改回收站状态接口
        MvcResult setRecycled = mockMvc.perform(MockMvcRequestBuilders
                .put("/repository/recycle")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .param("repo_uuid", repoUuid)
                .param("recycled", String.valueOf(recycled))
                .header("token", token)
                .session(session)
        )
                .andExpect(MockMvcResultMatchers.status().is(200))
                //.andExpect(MockMvcResultMatchers.)
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        //将返回值转换成json，拿到recycled的值，并判断是否在回收站中
        JSONObject jsonObject = JSONObject.parseObject(setRecycled.getResponse().getContentAsString());
        Assert.assertEquals(10000000,jsonObject.getIntValue("data"));

    }


    /*
    删除库测试:case 1:正常删除接口并回调
     */
    @Test
    public void test_02_DeleteRepo() throws Exception{

        //如果不在回收站中则failure
        if(subRepositoryDao.getRecycledStatus(repoUuid) != 10000000) {
           Assert.fail("recycled status is wrong!");
        }

        //删除库接口
        MvcResult deleteRepo = mockMvc.perform(MockMvcRequestBuilders
                .delete("/repo")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .param("repo_uuid", repoUuid)
                .header("token", token)
                .session(session)
        )
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    @Rollback(value = false)
    public void test_03_DeleteCallBack() throws Exception {

        //回调接口 mock JIRA服务回调  1
        String[] serviceName = { "JIRA", "DEPENDENCY", "CLONE", "MEASURE", "CODETRACKER", "ISSUE", "SCAN"};
        MvcResult recallResult1 = mockMvc.perform(MockMvcRequestBuilders
                .put("/repo")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .param("repo_uuid", repoUuid)
                .param("service_name", serviceName[0])
                .header("token", token)
                .session(session)
        )
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        //将返回值转换成json，拿到data中的recycled的值
        JSONObject jsonObject1 = JSONObject.parseObject(recallResult1.getResponse().getContentAsString());

        Integer recycledStatus1 = jsonObject1.getIntValue("data");
        char[] deleteStatus1 = String.valueOf(recycledStatus1).toCharArray();
        Assert.assertEquals('1',deleteStatus1[0]);


        //回调接口 mock MEASURE服务回调  4
        MvcResult recallResult4 = mockMvc.perform(MockMvcRequestBuilders
                .put("/repo")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .param("repo_uuid", repoUuid)
                .param("service_name", serviceName[3])
                .header("token", token)
                .session(session)
        )
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        //将返回值转换成json，拿到data中的recycled的值
        JSONObject jsonObject4 = JSONObject.parseObject(recallResult4.getResponse().getContentAsString());

        Integer recycledStatus4 = jsonObject4.getIntValue("data");
        char[] deleteStatus4 = String.valueOf(recycledStatus4).toCharArray();
        Assert.assertEquals('1',deleteStatus4[3]);


        //回调接口 mock CODETRACKER服务回调  5
        MvcResult recallResult5 = mockMvc.perform(MockMvcRequestBuilders
                .put("/repo")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .param("repo_uuid", repoUuid)
                .param("service_name", serviceName[4])
                .header("token", token)
                .session(session)
        )
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        //将返回值转换成json，拿到data中的recycled的值
        JSONObject jsonObject5 = JSONObject.parseObject(recallResult5.getResponse().getContentAsString());

        Integer recycledStatus5 = jsonObject5.getIntValue("data");
        char[] deleteStatus5 = String.valueOf(recycledStatus5).toCharArray();
        Assert.assertEquals('1',deleteStatus5[4]);


        //回调接口 mock ISSUE服务回调  6
        MvcResult recallResult6 = mockMvc.perform(MockMvcRequestBuilders
                .put("/repo")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .param("repo_uuid", repoUuid)
                .param("service_name", serviceName[5])
                .header("token", token)
                .session(session)
        )
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        //将返回值转换成json，拿到data中的recycled的值
        JSONObject jsonObject6 = JSONObject.parseObject(recallResult6.getResponse().getContentAsString());

        Integer recycledStatus6 = jsonObject6.getIntValue("data");
        char[] deleteStatus6 = String.valueOf(recycledStatus6).toCharArray();
        Assert.assertEquals('1',deleteStatus6[5]);


        //回调接口 mock SCAN服务回调  7
        MvcResult recallResult7 = mockMvc.perform(MockMvcRequestBuilders
                .put("/repo")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .param("repo_uuid", repoUuid)
                .param("service_name", serviceName[6])
                .header("token", token)
                .session(session)
        )
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        //将返回值转换成json，拿到data中的recycled的值
        JSONObject jsonObject7 = JSONObject.parseObject(recallResult7.getResponse().getContentAsString());

        Integer recycledStatus7 = jsonObject7.getIntValue("data");
        char[] deleteStatus7 = String.valueOf(recycledStatus7).toCharArray();
        Assert.assertEquals('1',deleteStatus7[6]);


    }

    @Test
    public void test_04_deleteProjectRepo() throws Exception {

        MvcResult deleteProjectRepo = mockMvc.perform(MockMvcRequestBuilders
                .delete("/repo/project")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .param("repo_uuid", repoUuid)
                .header("token", token)
                .session(session)
        )
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        //将返回值转换成json，拿到status的值
        JSONObject jsonObject = JSONObject.parseObject(deleteProjectRepo.getResponse().getContentAsString());
        Assert.assertEquals(200,jsonObject.getIntValue("code"));
    }


    @Test
    @Rollback(value = false)
    public void test_05_DataRecover() throws Exception {

        Integer recycled = 10000000;

        //修改回收站状态接口
        MvcResult setRecycled = mockMvc.perform(MockMvcRequestBuilders
                .put("/repository/recycle")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .param("repo_uuid", repoUuid)
                .param("recycled", String.valueOf(recycled))
                .header("token", token)
                .session(session)
        )
                .andExpect(MockMvcResultMatchers.status().is(200))
                //.andExpect(MockMvcResultMatchers.)
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        //将返回值转换成json，拿到recycled的值，并判断是否在回收站中
        JSONObject jsonObject = JSONObject.parseObject(setRecycled.getResponse().getContentAsString());
        Assert.assertEquals(0,jsonObject.getIntValue("data"));

    }


}