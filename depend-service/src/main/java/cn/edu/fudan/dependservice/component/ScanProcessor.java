package cn.edu.fudan.dependservice.component;

import cn.edu.fudan.dependservice.domain.ScanRepo;
import cn.edu.fudan.dependservice.domain.ScanStatus;
import cn.edu.fudan.dependservice.service.ProcessPrepare;
import cn.edu.fudan.dependservice.service.ScanProcess;
import cn.edu.fudan.dependservice.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author shaoxi
 * @version 0.1.0
 * @create 2021-04-22 14:31
 **/
@Slf4j
@Service
public class ScanProcessor extends Thread {

    @Autowired
    BatchProcessor batchProcessor;



    @Autowired
    ProcessPrepare processPrepare;

    @Autowired
    ScanProcess scanProcess;

    ApplicationContext applicationContext;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    // 1 is batch
    // 2 is one by one but scan one batch
    // if not static it is should be
//    private static Boolean scanning=false;
    // must be a Object but not Bollo
    private static class Lock{

    }
    private static Object lock =new Lock();

//    @Async
    // todo do not need Async
    public void scan(List<ScanRepo> scanRepos){
        // if is a list
        for(ScanRepo s:scanRepos){
            batchProcessor.addRepo(s);
        }
        scanOneBatch();
    }
    // todo getScanstatus
    /**
     * @return: scanstatus true false
     * @Description get scan status
     */
    public ScanStatus getScanStatus(String repouuid){
        return batchProcessor.getScanStatus(repouuid);

    }

     public void scanOneBatch(){
        synchronized(lock){
            //todo not all project is java
            while (batchProcessor.continueScan()){
                // todo prepare
                List<ScanRepo> scanRepos =batchProcessor.getScanList();
                log.info("in one scanOneBatch, size ="+scanRepos.size());
                for(ScanRepo scanRepo:scanRepos){
                    if(scanRepo.toString()==null) scanRepo.setToScanDate(TimeUtil.getCurrentDateTime());
                    processPrepare.prepareFile(scanRepo.getToScanDate(),scanRepo);
                }
                scanProcess.beginScan(scanRepos,null);
                log.info("end of a batch");
                batchProcessor.inScanning.clear();
            }
            log.info("end of processor");

        }


    }








}
