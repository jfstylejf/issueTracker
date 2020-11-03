package cn.edu.fudan.projectmanager;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author fancying
 */
@EnableSwagger2
@SpringBootApplication
@MapperScan("cn.edu.fudan.projectmanager.mapper")
public class ProjectManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectManagerApplication.class, args);
    }
}
