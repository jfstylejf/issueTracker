package cn.edu.fudan.cloneservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * @author fancying
 */
@SpringBootApplication
@EnableAsync
@EnableCaching
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@MapperScan("cn.edu.fudan.cloneservice.mapper")
public class CloneServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloneServiceApplication.class, args);
    }
}
