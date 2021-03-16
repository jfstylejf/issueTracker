package cn.edu.fudan.cloneservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


/**
 * @author fancying
 */
@SpringBootApplication
@EnableAsync
@MapperScan("cn.edu.fudan.cloneservice.mapper")
public class CloneServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloneServiceApplication.class, args);
    }
}
