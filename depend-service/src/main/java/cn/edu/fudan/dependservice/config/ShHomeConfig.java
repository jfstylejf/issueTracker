package cn.edu.fudan.dependservice.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author
 * @version 1.0
 **/
@Component
@Data
public class ShHomeConfig {

//    @Value("${dependenceHome}")
//    private String shHome;

    @Value("${dependenceHome}")
    private String dependenceHome;

//    public RestTemplate restTemplate(){
//        return new RestTemplate();
//    }
}
