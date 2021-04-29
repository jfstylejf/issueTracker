package cn.edu.fudan.dependservice.controller;

/**
 * @author shaoxi
 * @version 0.1.0
 * @create 2021-04-26 11:00
 **/

import cn.edu.fudan.dependservice.domain.ResponseBean;
import cn.edu.fudan.dependservice.domain.ScanRepo;
import cn.edu.fudan.dependservice.domain.ScanStatus;
import cn.edu.fudan.dependservice.mapper.ScanMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class Test {
    @Autowired
    ScanMapper scanMapper;
    //test  scanMapper.insert(scanRepo);
    @RequestMapping(value = {"depend/test"},method = RequestMethod.GET)
    public ResponseBean<List<ScanRepo>> oneTimePoint(){
        String msg ="test ";
        List<ScanRepo> list =new ArrayList<>();
        ScanRepo scanRepo =new ScanRepo();
        ScanStatus scanStatus=new ScanStatus();
        scanStatus.setScanTime("100");
        scanRepo.setRepoUuid("test");
        scanRepo.setScanStatus(scanStatus);
         int res=scanMapper.insert(scanRepo);
         log.info("res = ->{}",res);
            return new ResponseBean<>(200, msg, list);
    }

}
