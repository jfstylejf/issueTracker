package cn.edu.fudan.dependservice.controller;

import cn.edu.fudan.common.scan.CommonScanService;
import cn.edu.fudan.common.scan.ToolScan;
import cn.edu.fudan.dependservice.service.TestService;
import cn.edu.fudan.dependservice.service.impl.ScanServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.ws.Service;

@RestController
public class Test {
    TestService testService;



    @Autowired
    public void setTestService(TestService testService) {
        this.testService=testService;
    }


    @RequestMapping(value = {"/test"}, method = RequestMethod.GET)
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
