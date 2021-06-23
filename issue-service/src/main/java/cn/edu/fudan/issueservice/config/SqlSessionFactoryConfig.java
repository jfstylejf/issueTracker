package cn.edu.fudan.issueservice.config;

import cn.edu.fudan.issueservice.interceptor.MapF2FInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.plugin.Interceptor;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * @author beethoven
 * @date 2021-06-23 16:08:10
 */
@Configuration
@Slf4j
public class SqlSessionFactoryConfig {

    private DataSource dataSource;

    private MapF2FInterceptor interceptor;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Autowired
    public void setInterceptor(MapF2FInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Bean
    public SqlSessionFactoryBean createSqlSessionFactory() {

        SqlSessionFactoryBean sqlSessionFactoryBean = null;
        try {
            sqlSessionFactoryBean = new SqlSessionFactoryBean();

            sqlSessionFactoryBean.setDataSource(dataSource);

            PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

            sqlSessionFactoryBean.setMapperLocations(resourcePatternResolver.getResources("classpath*:cn/edu/fudan/issueservice/mapper/*.xml"));

            sqlSessionFactoryBean.setTypeAliasesPackage("cn/edu/fudan/issueservice/domain");

            sqlSessionFactoryBean.setPlugins(new Interceptor[]{interceptor});
        } catch (Exception e) {
            log.error("创建SqlSession连接工厂错误：{}", e.getMessage());
        }
        return sqlSessionFactoryBean;
    }

}
