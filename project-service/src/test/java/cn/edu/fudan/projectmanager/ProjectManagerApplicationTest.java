package cn.edu.fudan.projectmanager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;


/**
 * description:
 *
 * @author Richy
 * create: 2021-04-06 18:04
 **/
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
//@TestPropertySource(value = {"classpath:application-test-richy.properties"})
@SpringBootTest(classes = ProjectManagerApplication.class)
//@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@ActiveProfiles("test-richy")
//@SpringBootTest
public class ProjectManagerApplicationTest {

    @Autowired
    DataSource dataSource;

    @Test
    public void contextLoads() throws Exception{
        System.out.println("数据库的连接为："+dataSource.getConnection());
    }

    @Before
    public void start(){
        loadData();
    }


    @After
    public void end(){
        clearData();
    }

    /**
     * 装载表结果和测试数据
     **/
    private void loadData() {

    }

    /**
     * 清楚测试数据 恢复现场
     **/
    private void clearData() {

    }
}