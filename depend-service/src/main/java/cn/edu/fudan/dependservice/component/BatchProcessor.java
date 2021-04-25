package cn.edu.fudan.dependservice.component;

import cn.edu.fudan.dependservice.domain.ScanRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Service
public class BatchProcessor {

    private static final int batchNum = 10;
    Queue<ScanRepo> scanQueue;
    @Autowired
    public void setScanQueue(){
        scanQueue=new LinkedList<>();
    }

    public List<ScanRepo> getScanList() {
        List<ScanRepo> res = new ArrayList<>();
        synchronized (scanQueue){
            while (res.size()< batchNum &&!scanQueue.isEmpty()) {
                res.add(scanQueue.poll());
            }
        }
        return res;
    }
//    public void init(){
//        scanQueue=new LinkedList<>();
//    }

    public void addRepo(ScanRepo scanRepo){
        synchronized (scanQueue){
            scanQueue.offer(scanRepo);
        }

    }
    public boolean continueScan(){
        return !scanQueue.isEmpty();
    }


}
