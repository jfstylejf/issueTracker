package cn.edu.fudan.projectmanager.config;


import cn.edu.fudan.projectmanager.interceptor.AuthTokenInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author WZY
 * @version 1.0
 **/
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    //将拦截器配置为bean
    @Bean
    public AuthTokenInterceptor authTokenInterceptor() {
        return new AuthTokenInterceptor();
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> urlPatterns = new ArrayList<>();
        //添加拦截的URL
        urlPatterns.add("/project/**");
        registry.addInterceptor(authTokenInterceptor()).addPathPatterns(urlPatterns);
    }

}
