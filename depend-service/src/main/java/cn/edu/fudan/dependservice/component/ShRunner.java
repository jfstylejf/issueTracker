package cn.edu.fudan.dependservice.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author shaoxi
 * @version 0.1.0
 * @create 2021-05-13 15:39
 **/
@Component
@Slf4j
public class ShRunner {

    String command;
    public void initCommand(String dependenceHome,String shName){
        log.info("shName: "+shName);
        this.command="sh "+ dependenceHome+shName;
    }
    final static long timeout = 1800;
    @Async("taskExecutor")
    public void runSh(){
        log.info("threadName: "+Thread.currentThread().getName());

        try {
            Runtime rt = Runtime.getRuntime();
            log.info("command -> {}", command);
            Process process = rt.exec(command);
            boolean timeout = process.waitFor(this.timeout, TimeUnit.SECONDS);
            if (!timeout) {
                process.destroy();
                log.error("invoke sh timeout ! (1800s)");
                log.error("comman is -> {}",command);
            }
            log.info("end of ->{}",command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
