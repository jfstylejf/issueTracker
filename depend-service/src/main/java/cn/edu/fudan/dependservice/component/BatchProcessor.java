package cn.edu.fudan.dependservice.component;

import cn.edu.fudan.dependservice.domain.ScanRepo;
import cn.edu.fudan.dependservice.domain.ScanStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Slf4j
@Service
public class BatchProcessor {

    @Value("${batchSize}")
    private int batchNum;
    Queue<ScanRepo> scanQueue;
    List<ScanRepo> inScanning;
    public int getBatchNum(){
        return this.batchNum;
    }

    @Autowired
    public void setScanQueue() {
        scanQueue = new LinkedList<>();
    }

    @Autowired
    public void setInScanning() {
        inScanning = new ArrayList<>();
    }

    public List<ScanRepo> getScanList() {
        log.info("batchNum: "+batchNum);
        log.info("in queue:"+ scanQueue.size());
        synchronized (scanQueue) {
            while (inScanning.size() < batchNum && !scanQueue.isEmpty()) {
                inScanning.add(scanQueue.poll());
            }
        }
        return inScanning;
    }

    public ScanStatus getScanStatus(String repouuid) {
        ScanStatus res = null;
        for (ScanRepo s : scanQueue) {
            if (s.getRepoUuid().equals(repouuid)) {
                res = s.getScanStatus();
                break;
            }
        }
        if (res == null) {
            for (ScanRepo s : inScanning) {
                if (s.getRepoUuid().equals(repouuid)) {
                    res = s.getScanStatus();
                    break;
                }
            }

        }

        return res;
    }


    public void addRepo(ScanRepo scanRepo) {
        synchronized (scanQueue) {
            scanQueue.offer(scanRepo);
        }
    }

    public boolean continueScan() {
        return !scanQueue.isEmpty();
    }


}
