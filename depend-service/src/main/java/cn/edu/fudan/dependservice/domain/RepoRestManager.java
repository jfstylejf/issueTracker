package cn.edu.fudan.dependservice.domain;

import cn.edu.fudan.common.component.BaseRepoRestManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RepoRestManager extends BaseRepoRestManager {

//    @Autowired
    public RepoRestManager(RestTemplate restTemplate, @Value("${code.service.path}") String codeServicePath) {
        super(restTemplate,codeServicePath);
    }
}
