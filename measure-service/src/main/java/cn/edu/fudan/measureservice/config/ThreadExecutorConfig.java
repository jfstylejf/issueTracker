package cn.edu.fudan.measureservice.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.*;

/**
 * @Description 线程池配置
 * @author wjzho
 */
@Configuration
@EnableAsync
public class ThreadExecutorConfig {

    private static final Logger logger = LoggerFactory.getLogger(ThreadExecutorConfig.class);

    /**
     * 核心线程数
     */
    private int corePoolSize = Runtime.getRuntime().availableProcessors();
    /**
     * 最大线程数
     */
    private int maxPoolSize = Integer.MAX_VALUE;
    /**
     * 线程销毁时间
     */
    private Long keepAliveTime = 60L;
    /**
     * 任务队列
     */
    private SynchronousQueue synchronousQueue = new SynchronousQueue<>();
    /**
     * 线程名称
     */
    private ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("scan-thread-%d").build();

    @Bean
    public ExecutorService myThreadPool() {
        logger.info("线程池创建===>开始");
        ExecutorService threadPool = new ThreadPoolExecutor(corePoolSize,maxPoolSize,keepAliveTime, TimeUnit.SECONDS,synchronousQueue,namedThreadFactory);
        logger.info("线程池创建===>结束");
        return threadPool;
    }
}
