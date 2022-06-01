package com.simple.test;

import java.util.Random;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-30 17:57
 **/
public class AgentTest {

    public String queryUserInfo(String uid, String token) throws InterruptedException {
        Thread.sleep(new Random().nextInt(500));
        Thread.sleep(1000);
        return "*** 我是要代理的方法的返回值 ***";
    }

    public static void main(String[] args) throws InterruptedException {
        String res = new AgentTest().queryUserInfo("100001", "LikdlNL13423");
        System.out.println(res);
    }
}
