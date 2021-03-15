package cn.edu.fudan.dependservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * description: 启动类
 *
 * @author fancying
 * create: 2021-02-12 14:08
 **/
@EnableSwagger2
@SpringBootApplication
@MapperScan("cn.edu.fudan.dependservice.mapper")
public class DependServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DependServiceApplication.class, args);
    }

}
