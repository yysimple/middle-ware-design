package com.simple.test;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-30 15:54xk
 **/
public class AsmTest {

    private List<String> parameterTypeList = new ArrayList<String>() {{
        add("xxx");
    }};

    public static void main(String[] args) throws InterruptedException {
        AsmTest apiTest = new AsmTest();
        String res01 = apiTest.queryUserInfo(111, 17, "对象类");
        System.out.println("测试结果：" + res01 + "\r\n");
    }

    public String queryUserInfo(int uId, int age, String req) {
        return "你好，pjq | 精神小伙！";
    }

    /**
     * 测试案例01
     * 入参[✖]
     * 出参[✖]
     */
    public void method01() {
        System.out.println("hi ysq");
    }

    /**
     * 测试案例02
     * 入参[✔]、基本类型[✔]、单一参数[✔]
     * 出参[✖]
     */
    public void method02(int i) {
        System.out.println("hi zyy 小朋友ID：" + i);
    }

    /**
     * 测试案例03
     * 入参[✔]、对象类型[✔]、单一参数[✔]
     * 出参[✖]
     */
    public void method03(String str) {
        System.out.println("hi cmh 小朋友ID：" + str);
    }

    /**
     * 测试案例04
     * 入参[✔]、对象类型[✔]、基本类型[✔]、多项参数[✔]
     * 出参[✖]
     */
    public void method04(String name, int id) {
        System.out.println("hi gjb 小朋友：" + name + " ID：" + id);
    }

    /**
     * 测试案例05
     * 入参[✔]、基本类型覆盖8个[✔]、多项参数[✔]
     * boolean、char、byte、short、int、float、long、double
     */
    public void method06(boolean a, char b, byte c, short s, int i, long l, int ss, long ld, double xx, int mm, int nn, double mmm) {
        System.out.println("hi whl 基本类型全覆盖测试");
    }

    /**
     * 测试案例07
     * 入参[✖]
     * 出参[✔]、基本类型[✔]
     */
    public int method07() {
        return 1;
    }

    /**
     * 测试案例08
     * 入参[✖]
     * 出参[✔]、对象类型[✔]
     */
    public String method08() {
        return "hi wuxx";
    }

    /**
     * 测试案例09
     * 入参[✖]
     * 出参[✔]、对象类型[✔]、使用函数[✔]
     */
    public String method09() {
        return String.valueOf(123);
    }

    /**
     * 测试案例10
     * 入参[✔]、对象类型[✔]
     * 出参[✔]、基本类型[✔]
     */
    public int method10(String str) {
        return Integer.parseInt(str);
    }

    /**
     * method11
     * 入参[✔]、基本类型[✔]
     * 出参[✔]、对象类型[✔]
     */
    public String method11(int i) {
        return String.valueOf(i);
    }

    /**
     * method12
     * 入参[✔]、基本类型[✔]、对象类型[✔]
     * 出参[✔]、对象类型[✔]
     */
    public String method12(String str, int i) {
        return String.valueOf(i) + str;
    }
}
