package cn.edu.fudan.dependservice.start;

import cn.edu.fudan.dependservice.component.BatchProcessor;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ApplicationStarter  implements ApplicationRunner {
    @Autowired
    BatchProcessor batchProcessor;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println(" in applicationStarter ");
//        batchProcessor.init();
//        new Thread(new ScanThread()).start();
    }
}
