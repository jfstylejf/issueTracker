package cn.edu.fudan.dependservice.controller;

import cn.edu.fudan.common.scan.ToolScan;
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

    @Autowired
    ToolScan toolScan;

    @RequestMapping(value = {"/putDepend"}, method = RequestMethod.GET)
    //String repoPath
    public String put2Database(@RequestParam(name = "repoPath") String repoPath) {
        toolScan.scanOneCommit(null);

        return "run ok";
        //
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
