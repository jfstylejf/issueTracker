package cn.edu.fudan.issueservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


/**
 * @author Beethoven
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@MapperScan("cn.edu.fudan.issueservice.mapper")
@EnableTransactionManagement
@EnableAsync(proxyTargetClass=true)
@EnableScheduling
@EnableCaching
@EnableSwagger2
public class IssueServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IssueServiceApplication.class, args);
    }
}
