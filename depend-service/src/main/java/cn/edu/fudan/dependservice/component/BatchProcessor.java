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
    public void setScanQueue() {
        scanQueue = new LinkedList<>();
    }

    @Autowired
    public void setInScanning() {
        inScanning = new ArrayList<>();
    }

    public List<ScanRepo> getScanList() {
        List<ScanRepo> res = new ArrayList<>();
        synchronized (scanQueue) {
            while (res.size() < batchNum && !scanQueue.isEmpty()) {
                res.add(scanQueue.poll());
            }
        }
        inScanning = res;
        return res;
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
