package com.simple.schedule.config;

import com.alibaba.fastjson.JSON;
import com.simple.schedule.annotation.SimpleSchedule;
import com.simple.schedule.common.Constants;
import com.simple.schedule.domain.ExecuteInstance;
import com.simple.schedule.service.HeartbeatService;
import com.simple.schedule.service.ZkCuratorServer;
import com.simple.schedule.task.CronTaskRegister;
import com.simple.schedule.task.SchedulingRunnable;
import com.simple.schedule.util.StrUtil;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 项目: whitelist-spring-boot-starter
 * <p>
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2022-07-12 16:08
 **/
public class SchedulingConfiguration implements ApplicationContextAware, BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {

    private Logger logger = LoggerFactory.getLogger(SchedulingConfiguration.class);

    private final Set<Class<?>> alreadyDealClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Constants.Global.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        // 过滤已经被处理了bean
        if (this.alreadyDealClasses.contains(targetClass)) {
            return bean;
        }
        // 拿到所有的method
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
        for (Method method : methods) {
            // 拿到对应的注解信息
            SimpleSchedule dcsScheduled = AnnotationUtils.findAnnotation(method, SimpleSchedule.class);
            // 在扫描的过程中，一个方法会得到两次，因为有一个CGLIB给代理的。所以需要使用 method.getDeclaredAnnotations() 判断一下
            if (null == dcsScheduled || 0 == method.getDeclaredAnnotations().length) {
                continue;
            }
            List<ExecuteInstance> execOrderList = Constants.EXECUTE_INSTANCE.computeIfAbsent(beanName, k -> new ArrayList<>());
            // 构建执行实例
            ExecuteInstance execOrder = new ExecuteInstance();
            execOrder.setBean(bean);
            execOrder.setBeanName(beanName);
            execOrder.setMethodName(method.getName());
            execOrder.setDesc(dcsScheduled.desc());
            execOrder.setCron(dcsScheduled.cron());
            execOrder.setAutoStartup(dcsScheduled.autoStartup());
            // 这里会将所有要执行的任务实例存起来
            execOrderList.add(execOrder);
            this.alreadyDealClasses.add(targetClass);
        }
        return bean;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        try {
            ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
            //1. 初始化配置
            initConfig(applicationContext);
            //2. 初始化服务
            initServer(applicationContext);
            //3. 启动任务
            initTask(applicationContext);
            //4. 挂载节点
            initNode();
            //5. 心跳监听
            HeartbeatService.getInstance().startFlushScheduleStatus();
            logger.info("simple schedule init config、server、task、node、heart done!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 1. 初始化配置
     *
     * @param applicationContext
     */
    private void initConfig(ApplicationContext applicationContext) {
        try {
            ScheduleServerProperties properties = applicationContext.getBean("com-simple-schedule-simpleScheduleAutoConfig", SimpleScheduleAutoConfig.class).getProperties();
            Constants.Global.zkAddress = properties.getZkAddress();
            Constants.Global.schedulerServerId = properties.getSchedulerServerId();
            Constants.Global.schedulerServerName = properties.getSchedulerServerName();
            InetAddress id = InetAddress.getLocalHost();
            Constants.Global.ip = id.getHostAddress();
        } catch (Exception e) {
            logger.error("simple schedule init config error！", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 2. 初始化服务
     *
     * @param applicationContext
     */
    private void initServer(ApplicationContext applicationContext) {
        try {
            // 获取zk连接
            CuratorFramework client = ZkCuratorServer.getClient(Constants.Global.zkAddress);
            // 节点组装，假设schedulerServerId = a-service-001；那么最后数据：/com/simple/schedule/server/a-service-001
            Constants.Global.path_root_server = StrUtil.joinStr(Constants.Global.path_root, Constants.Global.LINE, "server", Constants.Global.LINE, Constants.Global.schedulerServerId);
            // 这里加上ip /com/simple/schedule/server/a-service-001/ip/127.0.0.1
            Constants.Global.path_root_server_ip = StrUtil.joinStr(Constants.Global.path_root_server, Constants.Global.LINE, "ip", Constants.Global.LINE, Constants.Global.ip);
            // 创建节点&递归删除本服务IP下的旧内容
            ZkCuratorServer.deletingChildrenIfNeeded(client, Constants.Global.path_root_server_ip);
            // 创建新的节点
            ZkCuratorServer.createNode(client, Constants.Global.path_root_server_ip);
            // 在对应的/com/simple/schedule/server/a-service-001节点下面插入服务名称
            ZkCuratorServer.setData(client, Constants.Global.path_root_server, Constants.Global.schedulerServerName);
            // 添加节点&监听 /com/simple/schedule/exec
            ZkCuratorServer.createNodeSimple(client, Constants.Global.path_root_exec);
            // 监听执行节点
            ZkCuratorServer.addTreeCacheListener(applicationContext, client, Constants.Global.path_root_exec);
        } catch (Exception e) {
            logger.error("simple schedule init server error！", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 3. 启动任务
     *
     * @param applicationContext
     */
    private void initTask(ApplicationContext applicationContext) {
        CronTaskRegister cronTaskRegistrar = applicationContext.getBean("com-simple-schedule-cronTaskRegister", CronTaskRegister.class);
        Set<String> beanNames = Constants.EXECUTE_INSTANCE.keySet();
        for (String beanName : beanNames) {
            List<ExecuteInstance> execOrderList = Constants.EXECUTE_INSTANCE.get(beanName);
            for (ExecuteInstance execOrder : execOrderList) {
                // 判断注解上面的任务状态是否是已启动
                if (!execOrder.getAutoStartup()) {
                    continue;
                }
                SchedulingRunnable task = new SchedulingRunnable(execOrder.getBean(), execOrder.getBeanName(), execOrder.getMethodName());
                // 将定时任务注册到任务池里面
                cronTaskRegistrar.addCronTask(task, execOrder.getCron());
            }
        }
    }

    /**
     * 4. 挂载节点
     *
     * @throws Exception
     */
    private void initNode() throws Exception {
        Set<String> beanNames = Constants.EXECUTE_INSTANCE.keySet();
        for (String beanName : beanNames) {
            List<ExecuteInstance> execOrderList = Constants.EXECUTE_INSTANCE.get(beanName);
            for (ExecuteInstance execOrder : execOrderList) {
                String path_root_server_ip_clazz = StrUtil.joinStr(Constants.Global.path_root_server_ip, Constants.Global.LINE, "clazz", Constants.Global.LINE, execOrder.getBeanName());
                String path_root_server_ip_clazz_method = StrUtil.joinStr(path_root_server_ip_clazz, Constants.Global.LINE, "method", Constants.Global.LINE, execOrder.getMethodName());
                String path_root_server_ip_clazz_method_status = StrUtil.joinStr(path_root_server_ip_clazz, Constants.Global.LINE, "method", Constants.Global.LINE, execOrder.getMethodName(), "/status");
                //添加节点
                ZkCuratorServer.createNodeSimple(Constants.Global.client, path_root_server_ip_clazz);
                ZkCuratorServer.createNodeSimple(Constants.Global.client, path_root_server_ip_clazz_method);
                ZkCuratorServer.createNodeSimple(Constants.Global.client, path_root_server_ip_clazz_method_status);
                //添加节点数据[临时]
                ZkCuratorServer.appendPersistentData(Constants.Global.client, path_root_server_ip_clazz_method + "/value", JSON.toJSONString(execOrder));
                //添加节点数据[永久]
                ZkCuratorServer.setData(Constants.Global.client, path_root_server_ip_clazz_method_status, execOrder.getAutoStartup() ? "1" : "0");
            }
        }
    }
}
