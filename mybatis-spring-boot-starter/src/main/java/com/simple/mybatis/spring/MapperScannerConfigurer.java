package com.simple.mybatis.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.util.ClassUtils;

import java.beans.Introspector;
import java.io.IOException;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-28 17:28
 **/
public class MapperScannerConfigurer implements BeanDefinitionRegistryPostProcessor {
    private String basePackage;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        try {
            // classpath*:com/simple/**/*.class
            String packageSearchPath = "classpath*:" + basePackage.replace('.', '/') + "/**/*.class";

            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);

            for (Resource resource : resources) {
                MetadataReader metadataReader = new SimpleMetadataReader(resource, ClassUtils.getDefaultClassLoader());

                ScannedGenericBeanDefinition beanDefinition = new ScannedGenericBeanDefinition(metadataReader);
                String beanName = Introspector.decapitalize(ClassUtils.getShortName(beanDefinition.getBeanClassName()));

                beanDefinition.setResource(resource);
                beanDefinition.setSource(resource);
                beanDefinition.setScope("singleton");
                // 这里只需要去注册一个bean的class名称进构造器就行了，之后的sqlSessionFactory会放到自动装配那里处理
                beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(beanDefinition.getBeanClassName());
                // 这里也是一个很关键的操作，拿到所有的 mapper层的接口，因为这里的类之后都要走代理，所以标记其是代理类，之后处理逻辑就会往这里走
                beanDefinition.setBeanClass(MapperFactoryBean.class);

                BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);
                registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // left intentionally blank
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

}
