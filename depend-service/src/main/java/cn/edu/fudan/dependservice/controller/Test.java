package cn.edu.fudan.dependservice.controller;

/**
 * @author shaoxi
 * @version 0.1.0
 * @create 2021-04-26 11:00
 **/

import cn.edu.fudan.dependservice.component.BatchProcessor;
import cn.edu.fudan.dependservice.domain.ResponseBean;
import cn.edu.fudan.dependservice.domain.ScanRepo;
import cn.edu.fudan.dependservice.domain.ScanStatus;
import cn.edu.fudan.dependservice.mapper.ScanMapper;
import cn.edu.fudan.dependservice.service.impl.ToolScanImplPara;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class Test {
    @Autowired
    ToolScanImplPara toolScanImplPara;
    @Autowired
    BatchProcessor batchProcessor;
    @Autowired
    ScanMapper scanMapper;
    //test  scanMapper.insert(scanRepo);

    @RequestMapping(value = {"depend/test"},method = RequestMethod.GET)
    public ResponseBean<List<ScanRepo>> test(){
        String msg ="test ";
        List<ScanRepo> list =new ArrayList<>();
        ScanRepo scanRepo =new ScanRepo();
        ScanStatus scanStatus=new ScanStatus();
        scanStatus.setScanTime("100");
        scanRepo.setRepoUuid("test");
        scanRepo.setScanStatus(scanStatus);
        System.out.println("batchNum"+batchProcessor.getBatchNum());
        System.out.println("batchWaitTime"+toolScanImplPara.getBatchWaitTime());
        return new ResponseBean<>(200, msg, list);
    }

}
