package cn.edu.fudan.scanservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author WZY
 * @version 1.0
 **/
@Configuration
public class ThreadPoolConfig {

    @Value("${dto.pool.size}")
    private int corePoolSize;
    @Value("${max.pool.size}")
    private int maxPoolSize;
    @Value("${queue.capacity}")
    private int queueCapacity;
    @Value("${keep.alive.seconds}")
    private int keepAliveSeconds;
    @Value("${thread.name.prefix}")
    private String threadNamePrefix;


    @Bean("forRequest")
    public Executor threadPoolForRequest(){
        ThreadPoolTaskExecutor executor=new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(corePoolSize);
        // 设置最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        // 设置队列容量
        executor.setQueueCapacity(queueCapacity);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(keepAliveSeconds);
        // 设置默认线程名称前缀
        executor.setThreadNamePrefix("scan-");
        // 设置拒绝策略,这里选择的是由用户线程去执行被拒绝的task
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }
}
