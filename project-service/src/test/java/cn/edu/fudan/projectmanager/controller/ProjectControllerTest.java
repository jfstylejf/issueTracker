package cn.edu.fudan.projectmanager.controller;

import cn.edu.fudan.projectmanager.service.AccountRepositoryService;
import cn.edu.fudan.projectmanager.service.ProjectControlService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ContextConfiguration(classes = {ProjectController.class})
@ExtendWith(SpringExtension.class)
public class ProjectControllerTest {
    @MockBean
    private AccountRepositoryService accountRepositoryService;

    @MockBean
    private ProjectControlService projectControlService;

    @Autowired
    private ProjectController projectController;

    @Test
    public void testUpdateProject() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.put("/project")
                .param("newProjectName", "foo")
                .param("oldProjectName", "foo");
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(this.projectController)
                .build()
                .perform(requestBuilder);
        actualPerformResult.andExpect(MockMvcResultMatchers.status().is(400));
    }
}

