package cn.edu.fudan.measureservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * description: 线程池配置
 * TODO 线程池的配置参数应该根据实际的机器和场景来配置 目前先采用默认方案
 * @author fancying
 * create: 2020-06-23 17:08
 **/
@Configuration
@EnableAsync
public class TaskPoolConfig {

    @Bean("taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //设置核心线程数
        executor.setCorePoolSize(3);
        //设置最大线程数
        executor.setMaxPoolSize(5);
        //设置队列容量
        executor.setQueueCapacity(30);
        //设置线程活跃时间
        executor.setKeepAliveSeconds(300);
        //设置线程默认名称
        executor.setThreadNamePrefix("cn.edu.fudan.common.scan-");
        // fixme 设置拒绝策略，pool已满，直接丢弃之后任务，待商榷
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        //待任务都运行完再关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;

    }
}

