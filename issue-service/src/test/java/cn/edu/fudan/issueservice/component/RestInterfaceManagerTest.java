package cn.edu.fudan.issueservice.component;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

@RunWith(JUnit4.class)
public class RestInterfaceManagerTest {
    @InjectMocks //用来注解被测类IssueScanController的实例对象，对其注入mock
    private RestInterfaceManager restInterfaceManager = new RestInterfaceManager(new RestTemplate());
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
}