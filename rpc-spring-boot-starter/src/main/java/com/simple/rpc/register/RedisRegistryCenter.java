package com.simple.rpc.register;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 功能描述: 使用redis作为注册中心，保存一些基本信息
 *
 * @author: WuChengXing
 * @create: 2021-12-29 11:20
 **/
public class RedisRegistryCenter {
    /**
     * 非切片额客户端连接
     */
    private static Jedis jedis;

    /**
     * 初始化redis
     *
     * @param host
     * @param port
     */
    public static void init(String host, int port, String password) {
        // 池基本配置
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(5);
        config.setTestOnBorrow(false);
        JedisPool jedisPool = new JedisPool(config, host, port, 10 * 1000, password);
        jedis = jedisPool.getResource();
    }

    /**
     * 注册生产者
     *
     * @param nozzle 接口
     * @param alias  别名
     * @param info   信息
     * @return 注册结果
     */
    public static Long registryProvider(String nozzle, String alias, String info) {
        return jedis.sadd(nozzle + "_" + alias, info);
    }

    /**
     * 获取生产者
     * 模拟权重，随机获取
     *
     * @param nozzle 接口名称
     */
    public static String obtainProvider(String nozzle, String alias) {
        return jedis.srandmember(nozzle + "_" + alias);
    }

    public static Jedis jedis() {
        return jedis;
    }

}
