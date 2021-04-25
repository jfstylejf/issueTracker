package cn.edu.fudan.dependservice.component;

import cn.edu.fudan.dependservice.domain.ScanRepo;
import cn.edu.fudan.dependservice.domain.ScanStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Slf4j
@Service
public class BatchProcessor {

    private static final int batchNum = 10;
    Queue<ScanRepo> scanQueue;
    List<ScanRepo> inScanning;
    @Autowired
    public void setScanQueue(){
        scanQueue=new LinkedList<>();
    }
    @Autowired
    public void setInScanning(){
        inScanning =new ArrayList<>();
    }

    public List<ScanRepo> getScanList() {
        List<ScanRepo> res = new ArrayList<>();
        synchronized (scanQueue){
            while (res.size()< batchNum &&!scanQueue.isEmpty()) {
                res.add(scanQueue.poll());
            }
        }
        inScanning=res;
        return res;
    }
    public boolean inScanning(ScanRepo scanRepo){
        boolean scanning = inScanning.contains(scanRepo)||scanQueue.contains(scanRepo);
        return scanning;
    }
    public ScanStatus getScanStatus(String repouuid){
        System.out.println();
        log.info("repoUUid:" + repouuid);
        ScanStatus res=null;
        for(ScanRepo s:scanQueue){
            System.out.println(s.getRepoUuid());
            if(s.getRepoUuid().equals(repouuid)){
                System.out.println("==");
                res=s.getScanStatus();
            }
        }
        System.out.println(" inScaning");
        log.info(" in scaning");
        for(ScanRepo s:inScanning){
            System.out.println(s.getRepoUuid());
            if(s.getRepoUuid().equals(repouuid)){
                System.out.println("==");
                // is
                res=new ScanStatus();
                res=s.getScanStatus();
            }
        }
        log.info(" com to end");
        if(res!=null){
            log.info(res.toString());
        }
        return res;
    }
    public ScanStatus getScanStatus(String repouuid,String commitId,String branch){
        for(ScanRepo s:scanQueue){
            if(s.getRepoUuid()==repouuid&&s.getBranch()==branch&&s.getScanCommit()==commitId){
                return s.getScanStatus();
            }
        }
        for(ScanRepo s:inScanning){
            if(s.getRepoUuid()==repouuid&&s.getBranch()==branch&&s.getScanCommit()==commitId){
                return s.getScanStatus();
            }
        }
        return null;


    }

    public void addRepo(ScanRepo scanRepo){
        synchronized (scanQueue){
            scanQueue.offer(scanRepo);
        }

    }
    public boolean continueScan(){
        return !scanQueue.isEmpty();
    }


}
