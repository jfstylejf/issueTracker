package cn.edu.fudan.cloneservice.thread;

import cn.edu.fudan.cloneservice.component.RestInterfaceManager;
import cn.edu.fudan.cloneservice.dao.CloneRepoDao;
import cn.edu.fudan.cloneservice.domain.clone.CloneRepo;
import cn.edu.fudan.cloneservice.task.ScanTask;
import cn.edu.fudan.cloneservice.service.CloneMeasureService;
import cn.edu.fudan.cloneservice.util.JGitUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zyh
 * @date 2020/5/5
 * 多生产者多消费者模式
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MultiThreadingExtractor {

    private static final Logger logger = LoggerFactory.getLogger(MultiThreadingExtractor.class);

    private RestInterfaceManager restInterfaceManager;

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }


    private ScanTask scanTask;

    @Autowired
    public void setScanTask(ScanTask scanTask) {
        this.scanTask = scanTask;
    }


    private CloneMeasureService cloneMeasureService;

    @Autowired
    public void setCloneMeasureService(CloneMeasureService cloneMeasureService) {
        this.cloneMeasureService = cloneMeasureService;
    }

    /**
     * clone扫描阶段线程池
     * 之后对线程池的选择做优化
     */
    private ThreadPoolExecutor scanThreadPool;

    /**
     * clone度量阶段线程池
     */
    private ThreadPoolExecutor measureThreadPool;

    /**
     * 扫描阻塞队列
     */
    private BlockingQueue<String> scanQueue;

    /**
     * 度量阻塞队列
     */
    private BlockingQueue<String> measureQueue;

    private AtomicInteger scanCount;

    private CloneRepoDao cloneRepoDao;

    @Autowired
    public void setCloneRepoDao(CloneRepoDao cloneRepoDao) {
        this.cloneRepoDao = cloneRepoDao;
    }

    /**
     * 需要无参构造函数，可以创建非单例bean
     */
    public MultiThreadingExtractor(){
        this(5);
    }

    public MultiThreadingExtractor(int corePoolSize){

        scanQueue = new LinkedTransferQueue<>();

        measureQueue = new LinkedTransferQueue<>();

        CloneThreadFactory cloneScanThreadFactory = new CloneThreadFactory("cloneScan");

        CloneThreadFactory cloneMeasureThreadFactory = new CloneThreadFactory("cloneMeasure");

        scanThreadPool = new ThreadPoolExecutor(corePoolSize, corePoolSize, 10L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(2000), cloneScanThreadFactory);

        measureThreadPool = new ThreadPoolExecutor(corePoolSize, corePoolSize, 10L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(2000), cloneMeasureThreadFactory);

    }

    public void extract(String repoId, String startCommitId){

        scanCount = new AtomicInteger(0);

        logger.info("start clone cn.edu.fudan.common.scan........");

        long start = System.currentTimeMillis();

        String uuid = UUID.randomUUID().toString();

        extractCommitIds(repoId, startCommitId, uuid);
        //可以建立线程池来管理
        Thread scan = new Thread(()->
                scanSynchronously(repoId)
                ,"cloneScan");

        Thread measure = new Thread(()->
                measureSynchronously(repoId, uuid)
                ,"cloneMeasure");

        scan.start();
        measure.start();

        try {
            scan.join();
            measure.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();
        long cost = (end - start)/(1000*60);
        CloneRepo cloneRepo = new CloneRepo();
        cloneRepo.setUuid(uuid);
        cloneRepo.setEndScanTime(new Date());
        cloneRepo.setStatus("complete");
        cloneRepo.setScanTime((int)((end - start)/1000));
        cloneRepoDao.updateScan(cloneRepo);

        logger.info("repo:{} -> took {} minutes to complete the clone cn.edu.fudan.common.scan and measure cn.edu.fudan.common.scan", repoId, cost);

    }

    private void extractCommitIds(String repoId, String startCommitId, String cloneRepoUuid) {

        String repoPath = null;
        List<String> commitList = new ArrayList<>();
        try {
            repoPath = restInterfaceManager.getRepoPath1(repoId);
            JGitUtil jGitHelper = new JGitUtil(repoPath);
            commitList = jGitHelper.getCommitListByBranchAndBeginCommit(startCommitId);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (repoPath != null) {
                restInterfaceManager.freeRepoPath(repoId, repoPath);
            }
        }
        //先执行粒度为method，仅需执行一次最近的commit
        String latestCommitId = commitList.get(commitList.size() - 1);
        scanTask.runSynchronously(repoId,latestCommitId, "method", null);
        CloneRepo cloneRepo = initCloneRepo(repoId);
        cloneRepo.setUuid(cloneRepoUuid);
        cloneRepo.setStartScanTime(new Date());
        cloneRepo.setStartCommit(startCommitId);
        cloneRepo.setEndCommit(latestCommitId);
        cloneRepo.setTotalCommitCount(commitList.size());
        cloneRepoDao.insertCloneRepo(cloneRepo);

        for (String commitId : commitList) {
            scanQueue.offer(commitId);
        }

    }

    private CloneRepo initCloneRepo(String repoId){
        CloneRepo cloneRepo = new CloneRepo();
        cloneRepo.setRepoId(repoId);
        cloneRepo.setStatus("scanning");
        cloneRepo.setScanCount(cloneRepoDao.getScanCount(repoId)+1);
        return cloneRepo;
    }

    /**
     * commit列表的消费者，同时也是measure列表的生产者
     */
    private void scanSynchronously(String repoId){

        logger.info("start clone cn.edu.fudan.common.scan");
        while (true){

            try {
                //一分钟内取不出就退出
                String commitId = scanQueue.poll(1, TimeUnit.MINUTES);
                if(commitId == null){
                    break;
                }
                scanThreadPool.submit(()-> {
                    logger.info("{}-> start cn.edu.fudan.common.scan", Thread.currentThread().getName());
                    scanTask.runSynchronously(repoId,commitId, "snippet", null);
                    measureQueue.offer(commitId);
                });

            } catch (InterruptedException e) {
                logger.info("clone Multithreaded cn.edu.fudan.common.scan filed");
                e.printStackTrace();
                break;
            }
        }
    }

    /**
     * measure列表的消费者
     */
    private void measureSynchronously(String repoId, String uuid){

        logger.info("start clone measure");
        while (true){

            try {
                //10分钟内取不出就退出
                String commitId = measureQueue.poll(10, TimeUnit.MINUTES);
                if(commitId == null){
                    break;
                }
                measureThreadPool.submit(()->{
                    logger.info("{}-> start measure", Thread.currentThread().getName());
                    cloneMeasureService.insertCloneMeasure(repoId, commitId, null);
                    CloneRepo cloneRepo = new CloneRepo();
                    cloneRepo.setUuid(uuid);
                    cloneRepo.setScannedCommitCount(scanCount.incrementAndGet());
                    cloneRepoDao.updateScan(cloneRepo);
                });

            } catch (InterruptedException e) {
                logger.info("clone Multithreaded measure filed");
                e.printStackTrace();
                break;
            }

        }
    }

}
