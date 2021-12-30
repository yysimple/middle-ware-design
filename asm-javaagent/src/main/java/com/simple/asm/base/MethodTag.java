package com.simple.asm.base;

import java.util.List;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-30 15:06
 **/
public class MethodTag {
    /**
     * 全限定名
     */
    private String fullClassName;

    /**
     * 类名
     */
    private String simpleClassName;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 描述
     */
    private String desc;

    /**
     * 参数列表
     */
    private List<String> parameterTypeList;

    /**
     * 返回值列表
     */
    private String returnParameterType;

    public MethodTag() {
    }

    public MethodTag(String fullClassName, String simpleClassName, String methodName, String desc, List<String> parameterTypeList, String returnParameterType) {
        this.fullClassName = fullClassName;
        this.simpleClassName = simpleClassName;
        this.methodName = methodName;
        this.desc = desc;
        this.parameterTypeList = parameterTypeList;
        this.returnParameterType = returnParameterType;
    }

    public String getFullClassName() {
        return fullClassName;
    }

    public void setFullClassName(String fullClassName) {
        this.fullClassName = fullClassName;
    }

    public String getSimpleClassName() {
        return simpleClassName;
    }

    public void setSimpleClassName(String simpleClassName) {
        this.simpleClassName = simpleClassName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<String> getParameterTypeList() {
        return parameterTypeList;
    }

    public void setParameterTypeList(List<String> parameterTypeList) {
        this.parameterTypeList = parameterTypeList;
    }

    public String getReturnParameterType() {
        return returnParameterType;
    }

    public void setReturnParameterType(String returnParameterType) {
        this.returnParameterType = returnParameterType;
    }
}
