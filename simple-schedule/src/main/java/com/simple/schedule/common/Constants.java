package com.simple.schedule.common;

import com.simple.schedule.domain.ExecuteInstance;
import com.simple.schedule.task.ScheduledTask;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 项目: whitelist-spring-boot-starter
 * <p>
 * 功能描述: 常量
 *
 * @author: WuChengXing
 * @create: 2022-07-10 13:37
 **/
public class Constants {

    /**
     * 任务组；beanName->ExecuteInstance
     */
    public static final Map<String, List<ExecuteInstance>> EXECUTE_INSTANCE = new ConcurrentHashMap<>();
    public static final Map<String, ScheduledTask> SCHEDULED_TASKS = new ConcurrentHashMap<>(16);

    public static class Global {
        public static ApplicationContext applicationContext;
        public static final String LINE = "/";
        public static String CHARSET_NAME = "utf-8";
        public static int schedulePoolSize = 8;     //定时任务执行线程池核心线程数
        public static String ip;                    //本机IP
        public static String zkAddress;             //zookeeper服务地址；x.x.x.x:2181
        public static String schedulerServerId;     //任务服务ID；  工程名称En
        public static String schedulerServerName;   //任务服务名称；工程名称Ch
        public static CuratorFramework client;      //zk配置；client
        public static String path_root = "/com/simple/schedule";   //zk配置；根目录
        public static String path_root_exec = path_root + "/exec";
        public static String path_root_server;
        public static String path_root_server_ip;
        public static String path_root_server_ip_clazz;              //[结构标记]类名称
        public static String path_root_server_ip_clazz_method;       //[结构标记]临时节点
        public static String path_root_server_ip_clazz_method_status;//[结构标记]永久节点
    }

    public static class InstructStatus{
        public final static Integer STOP = 0;     //停止
        public final static Integer START = 1;    //启动
        public final static Integer REFRESH = 2;  //刷新
    }
}
