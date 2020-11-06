package cn.edu.fudan.cloneservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


/**
 * @author fancying
 */
@SpringBootApplication
@EnableAsync
@EnableSwagger2
@MapperScan("cn.edu.fudan.cloneservice.mapper")
public class CloneServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloneServiceApplication.class, args);
    }
}
