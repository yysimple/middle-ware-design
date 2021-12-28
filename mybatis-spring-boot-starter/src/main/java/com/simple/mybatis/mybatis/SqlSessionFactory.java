package com.simple.mybatis.mybatis;

/**
 * 功能描述: 工厂，用于获取到sql的会话，用于接下来的处理
 *
 * @author: WuChengXing
 * @create: 2021-12-28 13:52
 **/
public interface SqlSessionFactory {
    /**
     * 打开一个会话
     *
     * @return
     */
    SqlSession openSession();
}
