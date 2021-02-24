package cn.edu.fudan.dependservice.controller;

import cn.edu.fudan.dependservice.impl.RunnerImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PutDependController {

    @RequestMapping(value = {"/putDepend"}, method = RequestMethod.GET)
    public String put2Database() {
        // if ok return true else return false;
        RunnerImpl runner=new RunnerImpl();
        runner.runTool("tempRepoPath","tempCommitID");
        return "true";
        //
    }

}
