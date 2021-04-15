package cn.edu.fudan.dependservice.service.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
public class ShThread implements Runnable {

    String reslutFile;
    String repoPath;

    String dependenceHome;
    String shName;
    final static long timeout = 1800;


    @Override
    public void run() {
        runSh();
    }
    public boolean runSh() {
        try {
            Runtime rt = Runtime.getRuntime();

            String command = "sh " + dependenceHome + shName;
            log.info("command -> {}", command);
            Process process = rt.exec(command);
            boolean timeout = process.waitFor(ShThread.timeout, TimeUnit.SECONDS);
            if (!timeout) {
                process.destroy();
                log.error("invoke tool timeout ! (1800s)");
                return false;
            }
            log.info("end of sh1");
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


}
