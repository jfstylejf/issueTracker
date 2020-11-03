package cn.edu.fudan.scanservice;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTimeZone;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * ServletComponentScan 扫描Filter
 * EnableTransactionManagement 开启事务支持
 * EnableAsync 开启异步调用的支持
 *
 * @author fancying
 */
@SpringBootApplication
@ServletComponentScan
@MapperScan("cn.edu.fudan.scanservice.mapper")
@EnableTransactionManagement
@EnableAsync
@Slf4j
public class ScanServiceApplication implements ApplicationRunner {

    @Value("${offsetHours}")
    private int offsetHours;

    public static void main(String[] args) {
        SpringApplication.run(ScanServiceApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args){
        log.info("initial date time zone : {}", offsetHours);
        DateTimeZone.forOffsetHours(offsetHours);
    }

}
