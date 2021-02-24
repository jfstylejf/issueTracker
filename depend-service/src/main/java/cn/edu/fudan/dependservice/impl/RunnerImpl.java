package cn.edu.fudan.dependservice.impl;

import cn.edu.fudan.dependservice.Runner;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class RunnerImpl implements Runner {
    private String shHome;
    private String shName;

    public String getShHome() {
        return shHome;
    }

    public void setShHome(String shHome) {
        this.shHome = shHome;
    }

    public String getShName() {
        return shName;
    }

    public void setShName(String shName) {
        this.shName = shName;
    }

    @Override
    public void runTool(String repoPath, String commitID) {
        invoke(repoPath,commitID);


    }
    public boolean invoke(String repoPath, String commitID) {
        try {
            Runtime rt = Runtime.getRuntime();
            String command =shHome+shName+" "+repoPath+" "+commitID;
            log.info("command -> {}",command);
            Process process = rt.exec(command);
            //最多等待sonar脚本执行200秒,超时则认为该commit解析失败
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
        return false;
    }


    public static void main(String[] args) {
        RunnerImpl runner =new RunnerImpl();
        // can not test in win
        runner.setShHome("D:\\allIdea\\IssueTracker-test\\depend-service\\src\\main\\java\\cn\\edu\\fudan\\dependservice\\");
        //D:\allIdea\IssueTracker-test\depend-service\src\main\java\cn\edu\fudan\dependservice
        runner.setShName("depend.sh");
        runner.runTool("tempRepo","tempCommitId");


    }
}
