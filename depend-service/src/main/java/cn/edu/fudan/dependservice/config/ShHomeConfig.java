package cn.edu.fudan.dependservice.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author shaoxi
 * @version 1.0
 **/
@Component
@Data
public class ShHomeConfig {

    @Value("${dependenceHome}")
    private String dependenceHome;

    @Value("${resultFileDir}")
    private String resultFileDir;

    @Value("${shName}")
    private String shName;

    @Value("${repoDir}")
    private String repoDir;

}
