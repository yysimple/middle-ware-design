package com.simple.schedule.service;

import com.alibaba.fastjson.JSON;
import com.simple.schedule.common.Constants;
import com.simple.schedule.domain.ExecuteInstance;
import com.simple.schedule.task.ScheduledTask;
import com.simple.schedule.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 项目: whitelist-spring-boot-starter
 * <p>
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2022-07-12 15:54
 **/
public class HeartbeatService {

    private Logger logger = LoggerFactory.getLogger(HeartbeatService.class);

    private ScheduledExecutorService ses;

    private static class SingletonHolder {
        private static final HeartbeatService INSTANCE = new HeartbeatService();
    }

    private HeartbeatService() {
    }

    public static HeartbeatService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void startFlushScheduleStatus() {
        ses = Executors.newScheduledThreadPool(1);
        //300秒后，每60秒心跳一次
        ses.scheduleAtFixedRate(() -> {
            try {
                logger.info("simple schedule heart beat On-Site Inspection task");
                Map<String, ScheduledTask> scheduledTasks = Constants.SCHEDULED_TASKS;
                Map<String, List<ExecuteInstance>> execOrderMap = Constants.EXECUTE_INSTANCE;
                Set<String> beanNameSet = execOrderMap.keySet();
                for (String beanName : beanNameSet) {
                    List<ExecuteInstance> execOrderList = execOrderMap.get(beanName);
                    for (ExecuteInstance execOrder : execOrderList) {
                        String taskId = execOrder.getBeanName() + "_" + execOrder.getMethodName();
                        ScheduledTask scheduledTask = scheduledTasks.get(taskId);
                        if (null == scheduledTask) {
                            continue;
                        }
                        boolean cancelled = scheduledTask.isCancelled();
                        // 路径拼装
                        String path_root_server_ip_clazz = StrUtil.joinStr(Constants.Global.path_root_server_ip, Constants.Global.LINE, "clazz", Constants.Global.LINE, execOrder.getBeanName());
                        String path_root_server_ip_clazz_method = StrUtil.joinStr(path_root_server_ip_clazz, Constants.Global.LINE, "method", Constants.Global.LINE, execOrder.getMethodName(), Constants.Global.LINE, "value");
                        // 获取现有值
                        ExecuteInstance oldExecOrder;
                        byte[] bytes = Constants.Global.client.getData().forPath(path_root_server_ip_clazz_method);
                        if (null != bytes) {
                            String oldJson = new String(bytes, Constants.Global.CHARSET_NAME);
                            oldExecOrder = JSON.parseObject(oldJson, ExecuteInstance.class);
                        } else {
                            oldExecOrder = new ExecuteInstance();
                            oldExecOrder.setBeanName(execOrder.getBeanName());
                            oldExecOrder.setMethodName(execOrder.getMethodName());
                            oldExecOrder.setDesc(execOrder.getDesc());
                            oldExecOrder.setCron(execOrder.getCron());
                            oldExecOrder.setAutoStartup(execOrder.getAutoStartup());
                        }
                        oldExecOrder.setAutoStartup(!cancelled);
                        //临时节点[数据]
                        if (null == Constants.Global.client.checkExists().forPath(path_root_server_ip_clazz_method)) {
                            continue;
                        }
                        String newJson = JSON.toJSONString(oldExecOrder);
                        Constants.Global.client.setData().forPath(path_root_server_ip_clazz_method, newJson.getBytes(Constants.Global.CHARSET_NAME));
                        //永久节点[数据]
                        String path_root_ip_server_clazz_method_status = StrUtil.joinStr(path_root_server_ip_clazz, Constants.Global.LINE, "method", Constants.Global.LINE, execOrder.getMethodName(), "/status");
                        if (null == Constants.Global.client.checkExists().forPath(path_root_ip_server_clazz_method_status)) {
                            continue;
                        }
                        Constants.Global.client.setData().forPath(path_root_ip_server_clazz_method_status, (execOrder.getAutoStartup() ? "1" : "0").getBytes(Constants.Global.CHARSET_NAME));
                    }
                }
            } catch (Exception ignore) {
            }

        }, 300, 60, TimeUnit.SECONDS);
    }
}
