package cn.edu.fudan.dependservice.component;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author shaoxi
 * @version 0.1.0
 * @create 2021-04-22 15:01
 **/
//@Service
public class ScanThread implements Runnable {
    @Autowired
    ScanProcessor scanProcessor;

    private boolean serviceRunning =true;
    @SneakyThrows
    @Override
    public void run() {
//        System.out.println(" in scanThread");
//        while (serviceRunning){
//            if(!scanProcessor.batchProcessor.isEmpty()){
//                // not empty
//            }
//            Thread.sleep(60*1000);
//
////            if()
//
//
//
//        }

    }
}
