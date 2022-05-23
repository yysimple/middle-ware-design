package com.simple.pagination.autoconfigure;

import com.simple.pagination.dialect.Dialect;
import com.simple.pagination.dialect.SqlDialect;
import com.simple.pagination.interceptor.PaginationInterceptor;
import com.simple.pagination.interceptor.PaginationSettings;
import com.simple.pagination.util.SqlUtils;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author chengxing.wu@xiaobao100.com
 * @date 2021/6/22 13:54
 */
@Configuration("simplePaginationConfiguration")
@ConditionalOnBean(SqlSessionFactory.class)
@ConditionalOnProperty(value = "simple.pagination.enabled", matchIfMissing = true)
@EnableConfigurationProperties({PaginationProperties.class, DataSourceProperties.class})
@AutoConfigureAfter(MybatisAutoConfiguration.class)
public class PaginationConfiguration {
    @Bean("simplePaginationInterceptor")
    @ConditionalOnMissingBean(PaginationInterceptor.class)
    public PaginationInterceptor paginationInterceptor(PaginationProperties properties, DataSourceProperties dataSourceProperties, @Autowired(required = false) Dialect dialect) {
        // 获取数据源
        if (properties.getSqlDialect() == null && dialect == null) {
            SqlDialect sqlDialect = SqlUtils.parseDataSourceUrl(dataSourceProperties.getUrl());
            if (sqlDialect == null) {
                throw new IllegalArgumentException("SqlDialect is required.");
            }
            dialect = sqlDialect.newDialect();
        }
        PaginationSettings settings = PaginationSettings.builder()
                .dialect(dialect)
                .sqlDialect(properties.getSqlDialect())
                .countExpr(properties.getCountExpr())
                .pageField(properties.getPageField())
                .sizeField(properties.getSizeField())
                .offsetField(properties.getOffsetField())
                .rowsField(properties.getRowsField())
                .countExprField(properties.getCountExprField())
                .countMethodNameField(properties.getCountMethodNameField())
                .enableCountField(properties.getEnableCountField())
                .skipQueryIfCountEqualsZero(properties.isSkipQueryIfCountEqualsZero())
                .useGeneratedIfCountMethodIsMissing(properties.isUseGeneratedIfCountMethodIsMissing())
                .defaultPage(properties.getDefaultPage())
                .defaultSize(properties.getDefaultSize())
                .maxPage(properties.getMaxPage())
                .maxSize(properties.getMaxSize())
                .build();
        return new PaginationInterceptor(settings);
    }

    @ConditionalOnBean(SqlSessionFactory.class)
    public static class PaginationLoadConfiguration implements InitializingBean {
        @Autowired(required = false)
        private List<SqlSessionFactory> sqlSessionFactories;
        @Autowired(required = false)
        private PaginationInterceptor interceptor;

        @Override
        public void afterPropertiesSet() {
            if (sqlSessionFactories != null && interceptor != null) {
                for (SqlSessionFactory sqlSessionFactory : sqlSessionFactories) {
                    List<Interceptor> interceptors = sqlSessionFactory.getConfiguration().getInterceptors();
                    if (isAbsent(interceptors)) {
                        sqlSessionFactory.getConfiguration().addInterceptor(interceptor);
                    }
                }
            }
        }

        private boolean isAbsent(List<Interceptor> interceptors) {
            for (Interceptor interceptor : interceptors) {
                if (interceptor instanceof PaginationInterceptor) {
                    return false;
                }
            }
            return true;
        }

    }

}
