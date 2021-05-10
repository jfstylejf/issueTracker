package cn.edu.fudan.dependservice.controller;

import cn.edu.fudan.dependservice.domain.ScanBody;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

/**
 * @author shaoxi
 * @version 0.1.0
 * @create 2021-04-29 10:33
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class RequestForScanControllerTest {
    @Autowired
    WebApplicationContext wac;

    @Autowired
    RequestForScanController requestForScanController;

    MockMvc mockMvc;
    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void scanByScan() throws Exception {
        ObjectMapper om =new ObjectMapper();
        ScanBody scanBody=new ScanBody();
        scanBody.setRepoUuid("ff");
        scanBody.setBranch("main");
        scanBody.setBeginCommit("testCommit");
        String s=om.writeValueAsString(scanBody);
        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.post("/dependency/dependency").contentType(MediaType.APPLICATION_JSON).content(s))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        System.out.println(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void getScanStatus() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.get("/dependency/dependency/scan-status")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("repo_uuid", "60930a84-4f50-11eb-b7c3-394c0d058805")
        ).andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        System.out.println(mvcResult.getResponse().getContentAsString());

    }
}