package cn.edu.fudan.issueservice.core;

import cn.edu.fudan.issueservice.config.ScanThreadExecutorConfig;
import cn.edu.fudan.issueservice.dao.IssueRepoDao;
import cn.edu.fudan.issueservice.domain.dto.ScanCommitInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
public class ScanManagementAsync {

    private ToolInvoker toolInvoker;

    private BlockingQueue<ScanCommitInfoDTO> scanCommitInfoDTOBlockingQueue;

    private StringRedisTemplate stringRedisTemplate;

    private IssueRepoDao issueRepoDao;



    @Async("scanProducerExecutor")
    public void addProjectToScanQueue(ScanCommitInfoDTO scanCommitInfoDTO) {
        try{
            //每个添加程序秩序执行一次 ，且添加速度快，所以是否采用多线程还需考虑


            scanCommitInfoDTOBlockingQueue.put (scanCommitInfoDTO);


        }catch (Exception e){
            //todo 分多个exception处理，可能会存在队列满了或是其他情况 返回对应的信息

        }


    }

    @Async("scanConsumerExecutor")
    public void getProjectFromScanQueue() {
        String repoId = null;
        String toolName = null;
        String branch = null;
        try{
            while(true){
                //从queue中获取要扫描的信息
                ScanCommitInfoDTO scanCommitInfoDTO = scanCommitInfoDTOBlockingQueue.take ();

                //执行扫描任务
                repoId = scanCommitInfoDTO.getRepoId ();
                branch = scanCommitInfoDTO.getBranch ();
                String beginCommit = scanCommitInfoDTO.getCommitId ();
                toolName = scanCommitInfoDTO.getToolName ();

                //todo 先采用简单的判断，后续更新为redis 分布式锁
                while(stringRedisTemplate.opsForValue ().get (repoId + "-" + toolName) != null){
                    TimeUnit.MINUTES.sleep (3);
                }

                // 获取线程名 ，以repo id 为key，线程名为value存入redis中
                String threadName = Thread.currentThread ().getName ();
                stringRedisTemplate.opsForValue ().set (repoId + "-" + toolName, threadName);
                ScanThreadExecutorConfig.setConsumerThreadSwitch (repoId , true, toolName);//后续扫描过程中，需要这个开关  开始扫描
                toolInvoker.invoke (repoId, branch, beginCommit, toolName);

                //单个task扫描结束后的收尾处理
                // 从redis中删除相应的记录
                if(repoId != null){
                    stringRedisTemplate.opsForValue ().getOperations().delete (repoId + "-" + toolName);
                }
                //删除开关
                ScanThreadExecutorConfig.delConsumerThreadSwitch (repoId, toolName);
                Thread.currentThread ().isInterrupted ();

                //删除 扫描commit 列表
                ScanThreadExecutorConfig.delNeedToScanCommitList (repoId, toolName);

            }
        }catch(InterruptedException e){
            e.printStackTrace ();
            if(repoId != null){
                stringRedisTemplate.opsForValue ().getOperations().delete (repoId + "-" + toolName);
            }
            ScanThreadExecutorConfig.delConsumerThreadSwitch (repoId, toolName);
            Thread.currentThread ().isInterrupted ();

            ScanThreadExecutorConfig.delNeedToScanCommitList (repoId, toolName);
            //todo 做什么处理后面待考虑完善
        }finally{
            /**
             * 存在当进行清除当前repo信息的时候 ，又来一个commit 更新消息的可能性，概率很小
             */
            if(repoId != null && toolName != null && ScanThreadExecutorConfig.getRepoUpdateStatus (repoId, toolName)){
                ScanCommitInfoDTO scanCommitInfoDTO = ScanCommitInfoDTO.builder()
                        .repoId(repoId).branch(branch).toolName(toolName).build();
                addProjectToScanQueue(scanCommitInfoDTO);
            }

        }
    }


    @Autowired
    public void setToolInvoker(ToolInvoker toolInvoker) {
        this.toolInvoker = toolInvoker;
    }

    @Autowired
    public void setScanCommitInfoDTOBlockingQueue(BlockingQueue<ScanCommitInfoDTO> scanCommitInfoDTOBlockingQueue) {
        this.scanCommitInfoDTOBlockingQueue = scanCommitInfoDTOBlockingQueue;
    }


    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Autowired
    public void setIssueRepoDao(IssueRepoDao issueRepoDao) {
        this.issueRepoDao = issueRepoDao;
    }
}
