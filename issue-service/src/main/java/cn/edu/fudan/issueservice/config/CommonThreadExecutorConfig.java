package cn.edu.fudan.issueservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@EnableAsync
public class CommonThreadExecutorConfig {


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


    private ThreadPoolTaskExecutor commonExecutor;

    @Bean
    public Executor commonExecutor() {

        commonExecutor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        commonExecutor.setCorePoolSize(corePoolSize);
        //配置最大线程数
        commonExecutor.setMaxPoolSize(maxPoolSize);
        //配置队列大小
        commonExecutor.setQueueCapacity(queueCapacity);
        //配置线程池中的线程的名称前缀
        commonExecutor.setThreadNamePrefix("issue-task-");

        // rejection-policy：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
        commonExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

        commonExecutor.setKeepAliveSeconds(60);
        //执行初始化
        commonExecutor.initialize();
        return commonExecutor;
    }
}
