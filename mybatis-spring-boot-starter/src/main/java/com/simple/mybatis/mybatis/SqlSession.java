package com.simple.mybatis.mybatis;

import java.util.List;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-28 13:39
 **/
public interface SqlSession {

    /**
     * 无参查询
     *
     * @param statement
     * @param <T>
     * @return
     */
    <T> T selectOne(String statement);

    /**
     * 有参查询
     *
     * @param statement
     * @param parameter
     * @param <T>
     * @return
     */
    <T> T selectOne(String statement, Object parameter);

    /**
     * 无参查询集合
     *
     * @param statement
     * @param <T>
     * @return
     */
    <T> List<T> selectList(String statement);

    /**
     * 有参查询所有
     *
     * @param statement
     * @param parameter
     * @param <T>
     * @return
     */
    <T> List<T> selectList(String statement, Object parameter);

    /**
     * 关闭连接
     */
    void close();
}
