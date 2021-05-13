package cn.edu.fudan.dependservice.service.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
@Data
public class EndScanRunner implements Runnable {


    String dependenceHome;
    String shName;
    String command;

    public void  setCommand(String dependenceHome,String shName){

    }


    @Override
    public void run() {
        runSh();
    }

    public boolean runSh() {
        try {
            Runtime rt = Runtime.getRuntime();

            String command = "sh " + dependenceHome + shName;
            log.info("end sh -> {}", command);
            Process process = rt.exec(command);
            boolean timeout = process.waitFor(300L, TimeUnit.SECONDS);
            if (!timeout) {
                process.destroy();
                log.error("invoke tool timeout ! (300s)");
                return false;
            }

            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


}
