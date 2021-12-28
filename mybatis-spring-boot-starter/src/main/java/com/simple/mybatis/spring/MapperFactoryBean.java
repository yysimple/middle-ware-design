package com.simple.mybatis.spring;

import com.simple.mybatis.mybatis.SqlSession;
import com.simple.mybatis.mybatis.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import javax.annotation.Resource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collection;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-28 17:22
 **/
public class MapperFactoryBean<T> implements FactoryBean<T> {

    private Logger logger = LoggerFactory.getLogger(MapperFactoryBean.class);

    private Class<T> mapperInterface;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    public MapperFactoryBean(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    @Override
    public T getObject() throws Exception {

        InvocationHandler handler = (proxy, method, args) -> {
            logger.info("你被代理了，执行SQL操作！{}", method.getName());
            try {
                SqlSession session = sqlSessionFactory.openSession();
                try {
                    Class<?> returnType = method.getReturnType();
                    if (Collection.class.isAssignableFrom(returnType)) {
                        return session.selectList(mapperInterface.getName() + "." + method.getName(), args[0]);
                    }
                    return session.selectOne(mapperInterface.getName() + "." + method.getName(), args[0]);
                } finally {
                    session.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return method.getReturnType().newInstance();
        };
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{mapperInterface}, handler);


    }

    @Override
    public Class<?> getObjectType() {
        return mapperInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
