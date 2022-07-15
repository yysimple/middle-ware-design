package com.simple.schedule.service;

import com.alibaba.fastjson.JSON;
import com.simple.schedule.common.Constants;
import com.simple.schedule.domain.ScheduleInstruct;
import com.simple.schedule.task.CronTaskRegister;
import com.simple.schedule.task.SchedulingRunnable;
import com.simple.schedule.util.StrUtil;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 项目: whitelist-spring-boot-starter
 * <p>
 * 功能描述: zookpper的连接
 *
 * @author: WuChengXing
 * @create: 2022-07-12 15:37
 **/
public class ZkCuratorServer {

    private static Logger logger = LoggerFactory.getLogger(ZkCuratorServer.class);

    public static CuratorFramework getClient(String connectString) {
        if (null != Constants.Global.client) {
            return Constants.Global.client;
        }
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        //添加重连监听
        client.getConnectionStateListenable().addListener((curatorFramework, connectionState) -> {
            switch (connectionState) {
                //Sent for the first successful connection to the server
                case CONNECTED:
                    logger.info("simple schedule init server connected {}", connectString);
                    break;
                //A suspended, lost, or read-only connection has been re-established
                case RECONNECTED:

                    break;
                default:
                    break;
            }
        });
        client.start();
        Constants.Global.client = client;
        return client;
    }

    /**
     * 所有子节点监听
     *
     * @param applicationContext
     * @param client
     * @param path
     * @throws Exception
     */
    public static void addTreeCacheListener(final ApplicationContext applicationContext, final CuratorFramework client, String path) throws Exception {
        TreeCache treeCache = new TreeCache(client, path);
        treeCache.start();
        // 根据某个目录为根目录，然后拿到对应的缓存树，并进行监听
        treeCache.getListenable().addListener((curatorFramework, event) -> {
            if (null == event.getData()) {
                return;
            }
            byte[] eventData = event.getData().getData();
            if (null == eventData || eventData.length < 1) {
                return;
            }
            String json = new String(eventData, Constants.Global.CHARSET_NAME);
            // 判断是否是正常的对象数据
            if ("".equals(json) || json.indexOf(Constants.Global.LEFT_PARENTHESIS) != 0 || json.lastIndexOf(Constants.Global.RIGHT_PARENTHESIS) + 1 != json.length()) {
                return;
            }
            // 解析出指令
            ScheduleInstruct instruct = JSON.parseObject(json, ScheduleInstruct.class);
            // 根据不同的事件类型做判断
            switch (event.getType()) {
                case NODE_ADDED:
                case NODE_UPDATED:
                    // 根据初始化时候的ip和应用名，做自己应用的执行逻辑，防止操作其他应用数据
                    if (Constants.Global.ip.equals(instruct.getIp()) && Constants.Global.schedulerServerId.equals(instruct.getSchedulerServerId())) {
                        // 获取cron任务执行对象
                        CronTaskRegister cronTaskRegistrar = applicationContext.getBean("com-simple-schedule-cronTaskRegister", CronTaskRegister.class);
                        boolean isExist = applicationContext.containsBean(instruct.getBeanName());
                        if (!isExist) {
                            return;
                        }
                        Object scheduleBean = applicationContext.getBean(instruct.getBeanName());
                        // 拼接后的路径：/com/simple/schedule/server/a-service-001/ip/127.0.0.1/clazz/AService/method/aSchedule/status
                        String pathRootServerIpClazzMethodStatus = StrUtil.joinStr(Constants.Global.path_root, Constants.Global.LINE, "server",
                                Constants.Global.LINE, instruct.getSchedulerServerId(),
                                Constants.Global.LINE, "ip",
                                Constants.Global.LINE, instruct.getIp(),
                                Constants.Global.LINE, "clazz",
                                Constants.Global.LINE, instruct.getBeanName(),
                                Constants.Global.LINE, "method",
                                Constants.Global.LINE, instruct.getMethodName(), "/status");
                        // 拿到状态
                        Integer status = instruct.getStatus();
                        switch (status) {
                            case 0:
                                // 从项目组里面移除该定时任务
                                cronTaskRegistrar.removeCronTask(instruct.getBeanName() + "_" + instruct.getMethodName());
                                setData(client, pathRootServerIpClazzMethodStatus, "0");
                                logger.info("simple schedule task stop {} {}", instruct.getBeanName(), instruct.getMethodName());
                                break;
                            case 1:
                                cronTaskRegistrar.addCronTask(new SchedulingRunnable(scheduleBean, instruct.getBeanName(), instruct.getMethodName()), instruct.getCron());
                                setData(client, pathRootServerIpClazzMethodStatus, "1");
                                logger.info("simple schedule task start {} {}", instruct.getBeanName(), instruct.getMethodName());
                                break;
                            case 2:
                                cronTaskRegistrar.removeCronTask(instruct.getBeanName() + "_" + instruct.getMethodName());
                                cronTaskRegistrar.addCronTask(new SchedulingRunnable(scheduleBean, instruct.getBeanName(), instruct.getMethodName()), instruct.getCron());
                                setData(client, pathRootServerIpClazzMethodStatus, "1");
                                logger.info("simple schedule task refresh {} {}", instruct.getBeanName(), instruct.getMethodName());
                                break;
                            default:
                                break;
                        }
                    }
                    break;
                default:
                    break;
            }
        });
    }

    /**
     * 创建节点
     *
     * @param client
     * @param path
     * @throws Exception
     */
    public static void createNode(CuratorFramework client, String path) throws Exception {
        List<String> pathChild = new ArrayList<>();
        pathChild.add(path);
        while (path.lastIndexOf(Constants.Global.LINE) > 0) {
            path = path.substring(0, path.lastIndexOf(Constants.Global.LINE));
            pathChild.add(path);
        }
        for (int i = pathChild.size() - 1; i >= 0; i--) {
            Stat stat = client.checkExists().forPath(pathChild.get(i));
            if (null == stat) {
                client.create().creatingParentsIfNeeded().forPath(pathChild.get(i));
            }
        }
    }

    /**
     * 创建节点
     *
     * @param client
     * @param path
     * @throws Exception
     */
    public static void createNodeSimple(CuratorFramework client, String path) throws Exception {
        if (null == client.checkExists().forPath(path)) {
            client.create().creatingParentsIfNeeded().forPath(path);
        }
    }

    /**
     * 删除节点
     *
     * @param client
     * @param path
     * @throws Exception
     */
    public static void deleteNodeSimple(CuratorFramework client, String path) throws Exception {
        if (null != client.checkExists().forPath(path)) {
            client.delete().deletingChildrenIfNeeded().forPath(path);
        }
    }

    /**
     * 设置数据
     *
     * @param client
     * @param path
     * @param data
     * @throws Exception
     */
    public static void setData(CuratorFramework client, String path, String data) throws Exception {
        if (null == client.checkExists().forPath(path)) {
            return;
        }
        client.setData().forPath(path, data.getBytes(Constants.Global.CHARSET_NAME));
    }

    /**
     * 获取数据
     *
     * @param client
     * @param path
     * @return
     * @throws Exception
     */
    public static byte[] getData(CuratorFramework client, String path) throws Exception {
        return client.getData().forPath(path);
    }

    /**
     * 删除数据
     *
     * @param client
     * @param path
     * @throws Exception
     */
    public static void deleteDataRetainNode(CuratorFramework client, String path) throws Exception {
        if (null != client.checkExists().forPath(path)) {
            client.delete().forPath(path);
        }
    }

    /**
     * 添加临时节点数据
     *
     * @param client
     * @param path
     * @param data
     * @throws Exception
     */
    public static void appendPersistentData(CuratorFramework client, String path, String data) throws Exception {
        PersistentEphemeralNode node = new PersistentEphemeralNode(client, PersistentEphemeralNode.Mode.EPHEMERAL, path, data.getBytes(Constants.Global.CHARSET_NAME));
        node.start();
        node.waitForInitialCreate(3, TimeUnit.SECONDS);
    }

    /**
     * 删除某个目录下所有的节点
     *
     * @param client
     * @param path
     * @throws Exception
     */
    public static void deletingChildrenIfNeeded(CuratorFramework client, String path) throws Exception {
        if (null == client.checkExists().forPath(path)) {
            return;
        }
        // 递归删除节点
        client.delete().deletingChildrenIfNeeded().forPath(path);
    }
}
