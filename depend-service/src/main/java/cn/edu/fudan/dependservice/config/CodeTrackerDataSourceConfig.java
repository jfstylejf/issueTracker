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
@MapperScan(basePackages = "cn.edu.fudan.dependservice.codetrackermapper", sqlSessionTemplateRef = "codeTrackerSqlSessionTemplate")
public class CodeTrackerDataSourceConfig {

    @Value("${spring.datasource.code-tracker.url}")
    private String url;

    @Value("${spring.datasource.code-tracker.driver-class-name}")
    private String driverClassName;
    @Value("${spring.datasource.code-tracker.username}")
    private String username;
    @Value("${spring.datasource.code-tracker.password}")
    private String password;

    @Bean(name = "codeTrackerDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.code-tracker")
    @Primary
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean(name = "codeTrackerSqlSessionFactory")
    @Primary
    public SqlSessionFactory sessionFactory(@Qualifier("codeTrackerDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath:cn/edu/fudan/dependservice/codetrackermapper/*.xml"));
        return factoryBean.getObject();
    }

    @Bean(name = "codeTrackerTransactionManager")
    @Primary
    public DataSourceTransactionManager codeTrackerTransactionManager(@Qualifier("codeTrackerDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "codeTrackerSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate sessionTemplate(@Qualifier("codeTrackerSqlSessionFactory") SqlSessionFactory sessionFactory) {
        return new SqlSessionTemplate(sessionFactory);
    }


}
