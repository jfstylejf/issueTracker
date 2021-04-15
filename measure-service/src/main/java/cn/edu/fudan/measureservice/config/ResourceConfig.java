package cn.edu.fudan.measureservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName: ResourceConfig
 * @Description:
 * @Author wjzho
 * @Date 2021/4/1
 */
@Configuration
public class ResourceConfig {

    @Value("${jsResultFileHome}")
    private String jsResultFileHome;



}
