package com.simple.mybatis.mybatis;

import java.util.Map;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-28 13:41
 **/
public class XNode {

    /**
     * 命名空间
     */
    private String namespace;
    private String id;
    private String parameterType;
    private String resultType;
    private String sql;
    private Map<Integer, String> parameter;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Map<Integer, String> getParameter() {
        return parameter;
    }

    public void setParameter(Map<Integer, String> parameter) {
        this.parameter = parameter;
    }
}
