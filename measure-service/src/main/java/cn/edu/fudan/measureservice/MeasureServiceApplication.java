package cn.edu.fudan.measureservice;

import cn.edu.fudan.measureservice.mapper.RepoMeasureMapper;
import cn.edu.fudan.measureservice.service.MeasureDeveloperService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


/**
 * @author fancying
 */
@EnableSwagger2
@SpringBootApplication
// @EnableCaching(proxyTargetClass = true,mode = AdviceMode.ASPECTJ)
@EnableCaching(proxyTargetClass = true)
@EnableScheduling
@MapperScan("cn.edu.fudan.measureservice.mapper")
public class MeasureServiceApplication implements ApplicationRunner {

    @Value("${token}")
    private String token;

//    RedisTemplate

    private MeasureDeveloperService measureDeveloperService;
    private RepoMeasureMapper repoMeasureMapper;

    public static void main(String[] args) {
        SpringApplication.run(MeasureServiceApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
//        measureDeveloperService.clearCache();
    }



    @Autowired
    public void setMeasureDeveloperService(MeasureDeveloperService measureDeveloperService) {
        this.measureDeveloperService = measureDeveloperService;
    }

    @Autowired
    public void setRepoMeasureMapper(RepoMeasureMapper repoMeasureMapper) {
        this.repoMeasureMapper = repoMeasureMapper;
    }
}
