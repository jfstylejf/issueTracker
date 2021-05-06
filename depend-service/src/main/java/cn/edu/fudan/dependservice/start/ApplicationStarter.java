package cn.edu.fudan.dependservice.start;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author shaoxi
 * @version 0.1.0
 * @create 2021-04-22 15:22
 * @description
 **/
@Component
public class ApplicationStarter implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println(" in shaoxi's application Starter ");
    }
}
