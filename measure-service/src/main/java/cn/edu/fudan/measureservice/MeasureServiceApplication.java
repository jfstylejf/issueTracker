package cn.edu.fudan.measureservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


/**
 * @author fancying
 */
@EnableSwagger2
@SpringBootApplication
// @EnableCaching(proxyTargetClass = true,mode = AdviceMode.ASPECTJ)
@EnableCaching(proxyTargetClass = true)
@EnableScheduling
@EnableAsync
@MapperScan("cn.edu.fudan.measureservice.mapper")
public class MeasureServiceApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(MeasureServiceApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        //measureDeveloperService.clearCache();
    }


}
