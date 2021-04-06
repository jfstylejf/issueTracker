package cn.edu.fudan.scanservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author WZY
 * @version 1.0
 **/
@Configuration
public class RestTemplateConfig {

    // 启动的时候要注意，由于我们在controller中注入了RestTemplate，所以启动的时候需要实例化该类的一个实例

    private RestTemplateBuilder builder;

    @Autowired
    public void setBuilder(RestTemplateBuilder builder) {
        this.builder = builder;
    }

    // 使用RestTemplateBuilder来实例化RestTemplate对象，spring默认已经注入了RestTemplateBuilder实例
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        // 建立连接超时设置
        requestFactory.setConnectTimeout(5000);
        // 获取数据超时设置 最多等待7秒
        requestFactory.setReadTimeout(7000);
        return new RestTemplate(requestFactory);
    }

}
