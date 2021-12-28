package com.simple.mybatis.autoconfigure;

import com.simple.mybatis.mybatis.SqlSessionFactory;
import com.simple.mybatis.mybatis.SqlSessionFactoryBuilder;
import com.simple.mybatis.spring.MapperFactoryBean;
import com.simple.mybatis.spring.MapperScannerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-28 17:21
 **/
@Configuration
@ConditionalOnClass({SqlSessionFactory.class})
@EnableConfigurationProperties({MybatisProperties.class, DataSourceProperties.class})
public class MybatisAutoConfiguration implements InitializingBean {

    Logger logger = LoggerFactory.getLogger(MybatisAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean()
    @ConditionalOnClass({Connection.class})
    public SqlSessionFactory sqlSessionFactory(Connection connection, MybatisProperties mybatisProperties) throws Exception {
        return new SqlSessionFactoryBuilder().build(connection, setProperties(mybatisProperties));
    }


    private Properties setProperties(MybatisProperties mybatisProperties) {
        Properties properties = new Properties();
        properties.setProperty("mapperLocations", dealProperty(mybatisProperties.getMapperLocations()));
        properties.setProperty("baseMapperPackage", dealProperty(mybatisProperties.getBaseMapperPackage()));
        Boolean isAllowedMapper = mybatisProperties.getIsAllowedMapper();
        isAllowedMapper = Objects.isNull(isAllowedMapper) || isAllowedMapper;
        properties.setProperty("isAllowedMapper", dealProperty(String.valueOf(isAllowedMapper)));
        properties.setProperty("typeAliasesPackage", dealProperty(mybatisProperties.getTypeAliasesPackage()));
        return properties;
    }

    private String dealProperty(String property) {
        return StringUtils.isEmpty(property) ? "" : property;
    }

    @Bean
    @ConditionalOnMissingBean
    public Connection connection(DataSourceProperties dataSourceProperties) {
        try {
            Class.forName(dataSourceProperties.getDriverClassName());
            return DriverManager.getConnection(dataSourceProperties.getUrl(), dataSourceProperties.getUsername(), dataSourceProperties.getPassword());
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return null;
    }

    public static class AutoConfiguredMapperScannerRegistrar implements EnvironmentAware, ImportBeanDefinitionRegistrar {

        private String basePackage;

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
            builder.addPropertyValue("basePackage", basePackage);
            registry.registerBeanDefinition(MapperScannerConfigurer.class.getName(), builder.getBeanDefinition());
        }

        @Override
        public void setEnvironment(Environment environment) {
            this.basePackage = environment.getProperty("mybatis.base-mapper-package");
        }
    }

    @Configuration
    @Import(AutoConfiguredMapperScannerRegistrar.class)
    @ConditionalOnMissingBean({MapperFactoryBean.class, MapperScannerConfigurer.class})
    public static class MapperScannerRegistrarNotFoundConfiguration implements InitializingBean {

        @Override
        public void afterPropertiesSet() {
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }


}
