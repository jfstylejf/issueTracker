package cn.edu.fudan.dependservice.config;

import cn.edu.fudan.dependservice.interceptor.MyInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shaoxi
 * @version 0.1.0
 * @create 2021-04-28 19:40
 **/
@Configuration

public class MvcConfig implements WebMvcConfigurer {
    //将拦截器配置为bean
    @Bean
    public MyInterceptor authTokenInterceptor() {
        return new MyInterceptor();
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> urlPatterns = new ArrayList<>();
        //添加拦截的URL
        urlPatterns.add("/codewisdom/depend/relation");
        registry.addInterceptor(authTokenInterceptor()).addPathPatterns(urlPatterns);
    }
}
