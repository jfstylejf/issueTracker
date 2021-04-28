package cn.edu.fudan.dependservice.controller;

import cn.edu.fudan.dependservice.domain.ResponseBean;
import cn.edu.fudan.dependservice.domain.ScanBody;
import cn.edu.fudan.dependservice.domain.ScanRepo;
import cn.edu.fudan.dependservice.service.ScanService;
import cn.edu.fudan.dependservice.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class Scan {
    ScanService scanService;

    @Autowired
    public void setTestService(ScanService scanService) {
        this.scanService = scanService;
    }
    @RequestMapping(value = {"depend/scanAllByDate"},method = RequestMethod.POST)
    public ResponseBean<List<ScanRepo>> oneTimePoint(@RequestBody ScanBody scanBody){
        try {
            String date= scanBody.getDatetime();
            if(date==null){
                date= TimeUtil.getCurrentDateTime();
            }
            long scanStartTime =System.currentTimeMillis();
            List<ScanRepo> data = scanService.scanAllRepoNearToOneDate(date);
            long costTime =(System.currentTimeMillis()-scanStartTime)/1000;
            String msg= "scan cost "+ costTime+" seconds";
            return new ResponseBean<>(200, msg, data);
        }catch (Exception e){
            return new ResponseBean<>(500,e.getMessage(),null);
        }

    }


}
