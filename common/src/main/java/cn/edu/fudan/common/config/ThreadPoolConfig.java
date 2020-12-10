package cn.edu.fudan.common.config;

import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * description: 线程池配置
 *
 * @author fancying
 * create: 2020-12-08 14:31
 **/
public class ThreadPoolConfig{


    private int corePoolSize = 3;
    private int maxPoolSize = 5;
    private int queueCapacity = 30;
    private int keepAliveSeconds = 300;
    private String threadNamePrefix = "codeWisdom";
    private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.DiscardPolicy();
    private boolean allowCoreThreadTimeOut = false;

    public TaskExecutor customizationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //设置核心线程数
        executor.setCorePoolSize(corePoolSize);
        //设置最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        //设置队列容量
        executor.setQueueCapacity(queueCapacity);
        //设置线程活跃时间
        executor.setKeepAliveSeconds(keepAliveSeconds);
        //设置线程默认名称
        executor.setThreadNamePrefix(threadNamePrefix);
        //设置拒绝策略，pool已满，直接丢弃之后任务，待商榷
        executor.setRejectedExecutionHandler(rejectedExecutionHandler);
        //待任务都运行完再关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }


}