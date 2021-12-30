package com.simple.dbrouter;

import com.simple.dbrouter.annotation.DBRouter;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;

/**
 * 功能描述: 切面
 *
 * @author: WuChengXing
 * @create: 2021-12-30 10:11
 **/
@Aspect
public class DBRouterJoinPoint {

    private Logger logger = LoggerFactory.getLogger(DBRouterJoinPoint.class);

    @Autowired
    private DBRouterConfig dbRouterConfig;

    @Pointcut("@annotation(com.simple.dbrouter.annotation.DBRouter)")
    public void aopPoint() {
    }

    @Around("aopPoint() && @annotation(dbRouter)")
    public Object doRouter(ProceedingJoinPoint jp, DBRouter dbRouter) throws Throwable {
        // 拿到对应的key，可以是用户id之类的
        String dbKey = dbRouter.key();
        // 如果key为空，则抛出异常
        if (StringUtils.isBlank(dbKey)) {
            throw new RuntimeException("annotation DBRouter key is null！");
        }
        // 计算路由
        String dbKeyAttr = getAttrValue(dbKey, jp.getArgs());
        // 这里的size需要是2的整数倍
        int size = dbRouterConfig.getDbCount() * dbRouterConfig.getTbCount();
        // 扰动函数参考HashMap（p = tab[i = (n - 1) & hash])）此处操作也是相当于
        // (dbKeyAttr.hashCode() ^ (dbKeyAttr.hashCode() >>> 16)) % size，最后定位到指定索引位置
        int idx = (size - 1) & (dbKeyAttr.hashCode() ^ (dbKeyAttr.hashCode() >>> 16));
        // 库表索引为了方便计算，假设 dbCount = 2 / tbCount = 4 / idx = 7
        // 那么这里数据库索引就是 1 ~ 2
        int dbIdx = idx / dbRouterConfig.getTbCount() + 1;
        // 这里表索引就是 7 -（0 ~ 4） = （3 ~ 7） % 4 + 1 = （1 ~ 4）
        int tbIdx = (idx - dbRouterConfig.getTbCount() * (dbIdx - 1)) % dbRouterConfig.getTbCount() + 1;
        // 设置到 ThreadLocal,这里就是将 1，2 这样的 设置成 01 02 ，所以在配置文件中，我们需要注意我们的设置要以 01 这样的结尾或者开头
        DBContextHolder.setDBKey(String.format("%02d", dbIdx));
        DBContextHolder.setTBKey(String.format("%02d", tbIdx));
        logger.info("数据库路由 method：{} dbIdx：{} tbIdx：{}", getMethod(jp).getName(), dbIdx, tbIdx);
        // 返回结果
        try {
            // 这里代理去调用方法的时候，mybatis会选择一次数据源，这个时候会去DBContextHolder里面拿到对应的 数据源的 key
            // 找到数据库后，那就是选择表了，让查询的实体都去继承DBRouterBase，这里会去拿到tbIdx，我们在xml中就需要自己接一下
            // FROM user_${tbIdx}，我们也可以在解析xml的时候动态解析
            return jp.proceed();
        } finally {
            DBContextHolder.clearDBKey();
            DBContextHolder.clearTBKey();
        }
    }

    private Method getMethod(JoinPoint jp) throws NoSuchMethodException {
        Signature sig = jp.getSignature();
        MethodSignature methodSignature = (MethodSignature) sig;
        return jp.getTarget().getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
    }

    public String getAttrValue(String attr, Object[] args) {
        String filedValue = null;
        for (Object arg : args) {
            try {
                if (StringUtils.isNotBlank(filedValue)) {
                    break;
                }
                filedValue = BeanUtils.getProperty(arg, attr);
            } catch (Exception e) {
                logger.error("获取路由属性值失败 attr：{}", attr, e);
            }
        }
        return filedValue;
    }
}
