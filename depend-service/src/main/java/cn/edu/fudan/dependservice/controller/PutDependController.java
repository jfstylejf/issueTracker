package cn.edu.fudan.dependservice.controller;
import cn.edu.fudan.common.scan.CommonScanService;
import cn.edu.fudan.common.scan.ToolScan;
import cn.edu.fudan.dependservice.service.impl.ScanServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PutDependController {

//    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    ToolScan toolScan;
    

    @Autowired
    ScanServiceImpl scanService;


    @RequestMapping(value = {"/putDepend"}, method = RequestMethod.GET)
    //String repoPath
    public String put2Database(@RequestParam(name = "repoPath") String repoPath) {
//        toolScan.scanOneCommit(null);
        testRunService();


        return "run ok";
        //
    }
    @RequestMapping(value = {"/scanAll"}, method = RequestMethod.GET)
    //String repoPath
    public String scanAll() {

        return " this is to scan all repo ";

        //
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void testRunService(){
        //repouuid
        //1994e17f-a891-4190-8e67-af85f673af39
        //master
        String testCommmit ="1994e17f-a891-4190-8e67-af85f673af39";
        //add value to dependencyHome
//        toolScan.prepareForScan();
//        commonScanService.sc
//        toolScan.prepareForO
        //2fa1a67e-3862-11eb-8dca-4dbb5f7a5f33
        //52bb4f90-225d-11eb-8610-491d2d684483
        // 2fa1a67e-3862-11eb-8dca-4dbb5f7a5f33 wrong
        // one commit id :9b2b7e7f63482e5e29cafbceac7146b56c8faf7f
        scanService.scan("2fa1a67e-3862-11eb-8dca-4dbb5f7a5f33","master",null);
//        scanService.scan("2fa1a67e-3862-11eb-8dca-4dbb5f7a5f33","master","9b2b7e7f63482e5e29cafbceac7146b56c8faf7f");



    }
}
