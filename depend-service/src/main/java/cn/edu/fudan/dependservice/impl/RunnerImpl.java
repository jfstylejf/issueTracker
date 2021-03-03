package cn.edu.fudan.dependservice.impl;

import cn.edu.fudan.dependservice.Runner;
import cn.edu.fudan.dependservice.config.ShHomeConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.TimeUnit;

@Slf4j
@Data
public class RunnerImpl implements Runner {
    private String shHome;
    private String shName;
    private String repoPath="/home/fdse/codeWisdom/service/dependence-analysis/scenario-engine";
    private String dependenceHome;
    public RunnerImpl(ApplicationContext applicationContext){
        this.dependenceHome = applicationContext.getBean(ShHomeConfig.class).getDependenceHome();
        this.shHome=dependenceHome;
        this.setShName("tdepend.sh");
    }

//    public String getRepoPath() {
//        return repoPath;
//    }
//
//    public void setRepoPath(String repoPath) {
//        this.repoPath = repoPath;
//    }
//
//    public String getShHome() {
//        return shHome;
//    }
//
//    public void setShHome(String shHome) {
//        this.shHome = shHome;
//    }
//
//    public String getShName() {
//        return shName;
//    }
//
//    public void setShName(String shName) {
//        this.shName = shName;
//    }

    @Override
    public void runTool(String repoPath, String commitID) {
        invoke(repoPath,commitID);


    }
    public boolean invoke(String repoPath, String commitID) {
        try {
            Runtime rt = Runtime.getRuntime();
//            String command ="sh "+shHome+shName+" "+repoPath+" "+commitID;
            String command ="sh "+shHome+shName+" "+repoPath;
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
}
