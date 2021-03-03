package cn.edu.fudan.dependservice.controller;

import cn.edu.fudan.dependservice.impl.RunnerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PutDependController {

    private ApplicationContext applicationContext;

    @RequestMapping(value = {"/putDepend"}, method = RequestMethod.GET)
    //String repoPath
    public String put2Database(@RequestParam(name = "repoPath") String repoPath) {
        System.out.println();
        // if ok return true else return false;
        StringBuilder res=new StringBuilder();
        res.append("now dir:"+System.getProperty("user.dir"));
        res.append("\n");

        res.append("repoPath:"+repoPath);
        res.append("\n");
        repoPath="/home/fdse/codeWisdom/service/dependence-analysis/scenario-engine";
        RunnerImpl runner=new RunnerImpl(applicationContext);

//        runner.setShHome("D:\\allIdea\\IssueTracker-test\\depend-service\\src\\main\\java\\cn\\edu\\fudan\\dependservice\\");
        //D:\allIdea\IssueTracker-test\depend-service\src\main\java\cn\edu\fudan\dependservice
        runner.runTool(repoPath,"tempCommitID");
        return res.toString();
        //
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
