package cn.edu.fudan.projectmanager.controller;

import cn.edu.fudan.projectmanager.ProjectServiceApplicationTest;
import cn.edu.fudan.projectmanager.component.RestInterfaceManager;
import cn.edu.fudan.projectmanager.dao.SubRepositoryDao;
import cn.edu.fudan.projectmanager.domain.Account;
import cn.edu.fudan.projectmanager.service.impl.ProjectControlServiceImpl;
import cn.edu.fudan.projectmanager.util.JDBCUtil;
import com.alibaba.fastjson.JSONObject;
import org.junit.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;


import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * description: the test of delete repo
 *
 * @author Richy
 * create: 2021-04-22 21:16
 **/

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
//??????????????????????????????
@TestPropertySource(value = {"classpath:application-test-richy.properties"})
//?????????????????????????????????
@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)

@Transactional
@Rollback() // ??????????????????????????????true
public class RepoDeleteTest1 extends ProjectServiceApplicationTest {

    /**
     * 1.???????????????????????????
     * ???1??????????????????????????????
     * ???2???????????????????????????????????????????????????project??????????????????
     * ???3????????????????????????????????????????????????????????????????????????project????????????????????????????????????????????????
     * ???4???project??????????????????????????????????????????1????????????project??????repo??????
     * ???5???????????????
     * <p>
     * ?????????repoUuid
     */

    final String token = "ec15d79e36e14dd258cfff3d48b73d35";
    //?????????
    final String repoUuid = "042164ec-4534-11eb-b6ff-f9c372bb0fcb";

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
        //?????????MockMvc??????
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        session = new MockHttpSession();
        Account account = new Account("admin", "1");
        session.setAttribute("user", account); //???????????????????????????????????????????????????????????????????????????

    }

    @Test
    @Rollback(value = false)
    public void test_01_Recycled() throws Exception {

        Integer recycled = 0;

        //???????????????????????????
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

        //?????????????????????json?????????recycled???????????????????????????????????????
        JSONObject jsonObject = JSONObject.parseObject(setRecycled.getResponse().getContentAsString());
        Assert.assertEquals(100000000, jsonObject.getIntValue("data"));

    }


    /*
    ???????????????:case 1:???????????????????????????
     */
    @Test
    @Rollback(value = false)
    public void test_02_DeleteRepo() throws Exception {

        //???????????????????????????failure
        if (subRepositoryDao.getRecycledStatus(repoUuid) != 100000000) {
            Assert.fail("recycled status is wrong!");
        }

        //???????????????
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

        //???????????? mock JIRA????????????  1
        String[] serviceName = {"JIRA", "DEPENDENCY", "CLONE", "MEASURE", "CODETRACKER", "ISSUE", "SCAN", "REPOSITORY"};
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

        //?????????????????????json?????????data??????recycled??????
        JSONObject jsonObject1 = JSONObject.parseObject(recallResult1.getResponse().getContentAsString());

        Integer recycledStatus1 = jsonObject1.getIntValue("data");
        char[] deleteStatus1 = String.valueOf(recycledStatus1).toCharArray();
        Assert.assertEquals('1', deleteStatus1[0]);


        //???????????? mock DEPENDENCY????????????  2
        MvcResult recallResult2 = mockMvc.perform(MockMvcRequestBuilders
                .put("/repo")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .param("repo_uuid", repoUuid)
                .param("service_name", serviceName[1])
                .header("token", token)
                .session(session)
        )
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        //?????????????????????json?????????data??????recycled??????
        JSONObject jsonObject2 = JSONObject.parseObject(recallResult2.getResponse().getContentAsString());

        Integer recycledStatus2 = jsonObject2.getIntValue("data");
        char[] deleteStatus2 = String.valueOf(recycledStatus2).toCharArray();
        Assert.assertEquals('1', deleteStatus2[1]);


        //???????????? mock CLONE????????????  3
        MvcResult recallResult3 = mockMvc.perform(MockMvcRequestBuilders
                .put("/repo")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .param("repo_uuid", repoUuid)
                .param("service_name", serviceName[2])
                .header("token", token)
                .session(session)
        )
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        //?????????????????????json?????????data??????recycled??????
        JSONObject jsonObject3 = JSONObject.parseObject(recallResult3.getResponse().getContentAsString());

        Integer recycledStatus3 = jsonObject3.getIntValue("data");
        char[] deleteStatus3 = String.valueOf(recycledStatus3).toCharArray();
        Assert.assertEquals('1', deleteStatus3[2]);


        //???????????? mock MEASURE????????????  4
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

        //?????????????????????json?????????data??????recycled??????
        JSONObject jsonObject4 = JSONObject.parseObject(recallResult4.getResponse().getContentAsString());

        Integer recycledStatus4 = jsonObject4.getIntValue("data");
        char[] deleteStatus4 = String.valueOf(recycledStatus4).toCharArray();
        Assert.assertEquals('1', deleteStatus4[3]);


        //???????????? mock CODETRACKER????????????  5
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

        //?????????????????????json?????????data??????recycled??????
        JSONObject jsonObject5 = JSONObject.parseObject(recallResult5.getResponse().getContentAsString());

        Integer recycledStatus5 = jsonObject5.getIntValue("data");
        char[] deleteStatus5 = String.valueOf(recycledStatus5).toCharArray();
        Assert.assertEquals('1', deleteStatus5[4]);


        //???????????? mock ISSUE????????????  6
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

        //?????????????????????json?????????data??????recycled??????
        JSONObject jsonObject6 = JSONObject.parseObject(recallResult6.getResponse().getContentAsString());

        Integer recycledStatus6 = jsonObject6.getIntValue("data");
        char[] deleteStatus6 = String.valueOf(recycledStatus6).toCharArray();
        Assert.assertEquals('1', deleteStatus6[5]);


        //???????????? mock SCAN????????????  7
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

        //?????????????????????json?????????data??????recycled??????
        JSONObject jsonObject7 = JSONObject.parseObject(recallResult7.getResponse().getContentAsString());

        Integer recycledStatus7 = jsonObject7.getIntValue("data");
        char[] deleteStatus7 = String.valueOf(recycledStatus7).toCharArray();
        Assert.assertEquals('1', deleteStatus7[6]);


        //???????????? mock REPOSITORY????????????  8
        MvcResult recallResult8 = mockMvc.perform(MockMvcRequestBuilders
                .put("/repo")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .param("repo_uuid", repoUuid)
                .param("service_name", serviceName[7])
                .header("token", token)
                .session(session)
        )
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        //?????????????????????json?????????data??????recycled??????
        JSONObject jsonObject8 = JSONObject.parseObject(recallResult8.getResponse().getContentAsString());

        Integer recycledStatus8 = jsonObject8.getIntValue("data");
        char[] deleteStatus8 = String.valueOf(recycledStatus8).toCharArray();
        Assert.assertEquals('1', deleteStatus8[7]);

    }

    @Test
    public void test_04_deleteProjectRepo() throws Exception {

        //???????????????????????????failure
        if (subRepositoryDao.getRecycledStatus(repoUuid) != 111111111) {
            Assert.fail("recycled status is wrong!");
        }

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

        //?????????????????????json?????????status??????
        JSONObject jsonObject = JSONObject.parseObject(deleteProjectRepo.getResponse().getContentAsString());
        Assert.assertEquals(200, jsonObject.getIntValue("code"));
    }


    @Test
    @Rollback(value = false)
    public void test_05_DataRecover() throws Exception {

        Integer recycled = 100000000;

        //???????????????????????????
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

        //?????????????????????json?????????recycled???????????????????????????????????????
        JSONObject jsonObject = JSONObject.parseObject(setRecycled.getResponse().getContentAsString());
        Assert.assertEquals(0, jsonObject.getIntValue("data"));

    }


}