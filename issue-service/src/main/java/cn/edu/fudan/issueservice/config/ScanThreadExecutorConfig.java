package cn.edu.fudan.issueservice.config;

import cn.edu.fudan.issueservice.domain.dto.ScanCommitInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Beethoven
 */
@Slf4j
@Configuration
@EnableAsync
public class ScanThreadExecutorConfig {

    /**
     * Set the ThreadPoolExecutor's core pool size.
     */
    private int corePoolSize = 5;
    /**
     * Set the ThreadPoolExecutor's maximum pool size.
     */
    private int maxPoolSize = 10;
    /**
     * Set the capacity for the ThreadPoolExecutor's BlockingQueue.
     */
    private int queueCapacity = 9999;

    private ThreadPoolTaskExecutor producerExecutor;
    private static ThreadPoolTaskExecutor consumerExecutor;


    /**
     * key 为 repo id 加上 tool name , value 为 扫描开关
     */
    private static volatile Map<String, Boolean> threadSwitch = new HashMap<>();

    /**
     * key 为 repo id 加上 tool name， value 为该repo 需要扫描的commit 列表
     */
    private static volatile Map<String, ConcurrentLinkedDeque<String>> needToScanCommitLists = new HashMap<>();

    /**
     * key 为 repo id 加上 tool name， value 表示该repo 是否已经更新过
     */
    private static volatile Map<String, Boolean> repoUpdatedStatus = new HashMap<>();

    @Bean(name = "ScanCommitInfoQueue")
    public BlockingQueue<ScanCommitInfoDTO> scanCommitInfoDTOBlockingQueue() {
        return new LinkedBlockingDeque<>();
    }

    @Bean
    public Executor scanProducerExecutor() {
        log.info("start scanProducerExecutor");
        producerExecutor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        producerExecutor.setCorePoolSize(corePoolSize);
        //配置最大线程数
        producerExecutor.setMaxPoolSize(maxPoolSize);
        //配置队列大小
        producerExecutor.setQueueCapacity(queueCapacity);
        //配置线程池中的线程的名称前缀
        producerExecutor.setThreadNamePrefix("async-issue-scan-");

        // rejection-policy：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
        producerExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

        producerExecutor.setKeepAliveSeconds(60);
        //执行初始化
        producerExecutor.initialize();
        return producerExecutor;
    }

    @Bean
    public Executor scanConsumerExecutor() {
        log.info("start scanConsumerExecutor");
        consumerExecutor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        consumerExecutor.setCorePoolSize(corePoolSize);
        //配置最大线程数
        consumerExecutor.setMaxPoolSize(maxPoolSize);
        //配置队列大小
        consumerExecutor.setQueueCapacity(queueCapacity);
        //配置线程池中的线程的名称前缀
        consumerExecutor.setThreadNamePrefix("async-issue-scan-");

        // rejection-policy：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
        consumerExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

        consumerExecutor.setKeepAliveSeconds(60);
        //执行初始化
        consumerExecutor.initialize();
        return consumerExecutor;
    }


    public static int getConsumerThreadPoolAliveThreadCounts() {
        return consumerExecutor.getActiveCount();
    }

    public static void setConsumerThreadSwitch(String repoId, Boolean status, String toolName) {
        String key = repoId + "-" + toolName;
        threadSwitch.put(key, status);
    }

    public static boolean getConsumerThreadSwitch(String repoId, String toolName) {
        String key = repoId + "-" + toolName;
        return threadSwitch.get(key);
    }

    public static void delConsumerThreadSwitch(String repoId, String toolName) {
        String key = repoId + "-" + toolName;
        threadSwitch.remove(key);
    }

    /**
     * 返回true 代表正常插入数据成功 ，返回 false 代表，非正常插入
     *
     * @param repoId
     * @param commits
     * @return
     */
    public static boolean setNeedToScanCommitLists(String repoId, ConcurrentLinkedDeque<String> commits, String toolName) {
        boolean result = false;
        String key = repoId + "-" + toolName;

        //采用double check的方式插入 commit列表，避免重复插入
        if (needToScanCommitLists.get(key) == null) {
            synchronized (needToScanCommitLists) {
                if (needToScanCommitLists.get(key) == null) {
                    needToScanCommitLists.put(key, commits);
                    result = true;
                } else {
                    //todo 此处有风险，应该将commit id 复制出去，而不是直接赋值
                    commits = needToScanCommitLists.get(key);
                }
            }
        }
        return result;
    }

    public static void updateScannedRepoStatus(String repoId, String toolName) {
        synchronized (repoUpdatedStatus) {
            String key = repoId + "-" + toolName;
            repoUpdatedStatus.put(key, true);
        }
    }

    public static void delNeedToScanCommitList(String repoId, String toolName) {
        //目前因为主线程是唯一的所以这一步只做简单的重复判断。
        String key = repoId + "-" + toolName;
        if (needToScanCommitLists.get(key) != null) {
            needToScanCommitLists.remove(key);
        }
    }

    public static void delRepoUpdateStatus(String repoId, String toolName) {
        //目前因为主线程是唯一的所以这一步只做简单的重复判断。
        String key = repoId + "-" + toolName;
        if (repoUpdatedStatus.get(key) != null) {
            repoUpdatedStatus.remove(key);
        }
    }

    public static boolean getRepoUpdateStatus(String repoId, String toolName) {
        //目前因为主线程是唯一的所以这一步只做简单的重复判断。
        String key = repoId + "-" + toolName;
        if (repoUpdatedStatus.get(key) != null) {
            return true;
        }
        return false;
    }

}


