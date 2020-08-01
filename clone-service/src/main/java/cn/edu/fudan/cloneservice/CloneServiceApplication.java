package cn.edu.fudan.cloneservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
@MapperScan(basePackages = {"cn.edu.fudan.cloneservice.mapper" , "cn.edu.fudan.cloneservice.scan.mapper"})
public class CloneServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloneServiceApplication.class, args);
    }
}
