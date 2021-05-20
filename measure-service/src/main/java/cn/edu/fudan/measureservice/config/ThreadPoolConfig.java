package cn.edu.fudan.measureservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

/**
 * @Description 线程池配置
 * @author wjzho
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolConfig.class);

    private static final int corePoolSize = 5;
    private static final int maxPoolSize = 7;
    private static final int queueCapacity = 5;
    private static final int keepAliveSeconds = 300;
    private static final String threadNamePrefix = "async-task-measureService-";
    private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.DiscardPolicy();

    @Bean("taskExecutor")
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
        //执行初始化
        executor.initialize();
        return executor;
    }
}
