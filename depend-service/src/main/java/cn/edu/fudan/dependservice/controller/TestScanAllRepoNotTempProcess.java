package cn.edu.fudan.dependservice.controller;

import cn.edu.fudan.dependservice.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class TestScanAllRepoNotTempProcess {
    TestService testService;



    @Autowired
    public void setTestService(TestService testService) {
        this.testService=testService;
    }


    @RequestMapping(value = {"/scanallrepo"}, method = RequestMethod.GET)
    //String repoPath
    //get repouuuid that should scan
    public String put2Database() {
//        toolScan.scanOneCommit(null);
        long scanStartTime =System.currentTimeMillis();
        testService.getAllRepoUuidThatNeedScan();
        long costTime =System.currentTimeMillis()-scanStartTime;
        return "scan all repo ok\n"+costTime/1000 +"seconds";
        //
    }
}
