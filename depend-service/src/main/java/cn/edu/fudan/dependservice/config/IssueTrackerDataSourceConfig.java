package cn.edu.fudan.dependservice.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * @author Song Rui
 */
@Configuration
@MapperScan(basePackages = "cn.edu.fudan.dependservice.mapper", sqlSessionTemplateRef = "issueTrackerSqlSessionTemplate")
public class IssueTrackerDataSourceConfig {

    @Value("${spring.datasource.issue-tracker.url}")
    private String url;

    @Value("${spring.datasource.issue-tracker.driver-class-name}")
    private String driverClassName;
    @Value("${spring.datasource.issue-tracker.username}")
    private String username;
    @Value("${spring.datasource.issue-tracker.password}")
    private String password;

    @Bean(name = "issueTrackerDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.issue-tracker")
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean(name = "issueTrackerSqlSessionFactory")
    public SqlSessionFactory sessionFactory(@Qualifier("issueTrackerDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath:cn/edu/fudan/dependservice/mapper/*.xml"));
        return factoryBean.getObject();
    }

    @Bean(name = "issueTrackerTransactionManager")
    @Primary
    public DataSourceTransactionManager codeTrackerTransactionManager(@Qualifier("issueTrackerDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "issueTrackerSqlSessionTemplate")
    public SqlSessionTemplate sessionTemplate(
            @Qualifier("issueTrackerSqlSessionFactory") SqlSessionFactory sessionFactory) {
        return new SqlSessionTemplate(sessionFactory);
    }


}
