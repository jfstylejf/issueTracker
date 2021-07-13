package cn.edu.fudan.projectmanager.controller;

import cn.edu.fudan.projectmanager.service.AccountRepositoryService;
import cn.edu.fudan.projectmanager.service.ProjectControlService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ContextConfiguration(classes = {ProjectQueryController.class})
@ExtendWith(SpringExtension.class)
public class ProjectQueryControllerTest {
    @MockBean
    private AccountRepositoryService accountRepositoryService;

    @MockBean
    private ProjectControlService projectControlService;

    @Autowired
    private ProjectQueryController projectQueryController;

    @Test
    public void testGetProjectAndRepoRelation() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/project/all");
        MockMvcBuilders.standaloneSetup(this.projectQueryController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers.content().string("{\"code\":200,\"msg\":\"get info success\",\"data\":{}}"));
    }

    @Test
    public void testGetProjectAndRepoRelation2() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/project/all", "Uri Vars");
        MockMvcBuilders.standaloneSetup(this.projectQueryController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers.content().string("{\"code\":200,\"msg\":\"get info success\",\"data\":{}}"));
    }

    @Test
    public void testGetProjectAndRepoRelation3() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/project/all")
                .param("project_names", "foo");
        MockMvcBuilders.standaloneSetup(this.projectQueryController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers.content().string("{\"code\":200,\"msg\":\"get info success\",\"data\":{}}"));
    }

    @Test
    public void testGetProjectAndRepoRelation4() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/project/all")
                .param("project_names", "");
        MockMvcBuilders.standaloneSetup(this.projectQueryController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers.content().string("{\"code\":200,\"msg\":\"get info success\",\"data\":{}}"));
    }
}

