package cn.edu.fudan.dependservice.utill;

import cn.edu.fudan.dependservice.domain.ScanRepo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Service
public class BatchProcessor {

    private static final int batchNum = 10;
    Queue<ScanRepo> scanQueue;

    public List<ScanRepo> getScanList() {
        List<ScanRepo> res = new ArrayList<>();
        int size = 0;
        while (size < batchNum) {
            res.add(scanQueue.poll());
        }
        return res;

    }
    public void init(List<ScanRepo> scanRepoList){
        scanQueue=new LinkedList<>();
        for(ScanRepo scanRepo:scanRepoList){
            scanQueue.offer(scanRepo);
        }
    }


}
