package cn.edu.fudan.dependservice.controller;

import cn.edu.fudan.dependservice.domain.ResponseBean;
import cn.edu.fudan.dependservice.domain.ScanBody;
import cn.edu.fudan.dependservice.domain.ScanRepo;
import cn.edu.fudan.dependservice.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class Scan {
    boolean scannning;
    TestService testService;

    @Autowired
    public void setTestService(TestService testService) {
        this.testService=testService;
    }


    //todo san latest
    @RequestMapping(value = {"depend/scan"},method = RequestMethod.POST)
    public ResponseBean<List<ScanRepo>> scan(){
        if(scannning){
            return new ResponseBean<>(200,"in scannning",null);
        }
        scannning=true;

        long scanStartTime =System.currentTimeMillis();
        List<ScanRepo> data =testService.scanAllRepo();
        long costTime =(System.currentTimeMillis()-scanStartTime)/1000;
        String msg= "scan cost "+ costTime+" seconds";
        scannning=false;
        return new ResponseBean<>(200, msg, data);
    }

    @RequestMapping(value = {"depend/scanTest"},method = RequestMethod.POST)
    public ResponseBean<List<ScanRepo>> scanT(){
        long scanStartTime =System.currentTimeMillis();
        List<ScanRepo> data =testService.scanAllRepoNew();
        long costTime =(System.currentTimeMillis()-scanStartTime)/1000;
        String msg= "scan cost "+ costTime+" seconds";
        return new ResponseBean<>(200, msg, data);
    }

    //todo san one date
    @RequestMapping(value = {"depend/oneTimePoint"},method = RequestMethod.POST)
    public ResponseBean<List<ScanRepo>> oneTimePoint(@RequestBody ScanBody scanBody){
        try {
            if(scannning){
                return new ResponseBean<>(200,"in scannning",null);
            }
            scannning=true;
            String date= scanBody.getDatetime();
            long scanStartTime =System.currentTimeMillis();
            List<ScanRepo> data =testService.scanAllRepoNearToOneDate(date);
            long costTime =(System.currentTimeMillis()-scanStartTime)/1000;
            String msg= "scan cost "+ costTime+" seconds";
            scannning=false;
            return new ResponseBean<>(200, msg, data);

        }catch (Exception e){
            return new ResponseBean<>(500,e.getMessage(),null);
        }

    }

}
