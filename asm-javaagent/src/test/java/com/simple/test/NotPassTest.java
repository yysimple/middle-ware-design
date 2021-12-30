package com.simple.test;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-30 15:55
 **/
public class NotPassTest {

    public static void main(String[] args) {
        System.out.println("");
    }

    public String echoHi(String xx) {
        for (int i = 0; i < 100; i++) {
            new StringBuffer(i);
        }
        System.out.println("Hi trisomy 三体监控!");
        return "111";
    }

    @Test
    public void test_desc() {
        String desc = "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/String;IJ[I[[Ljava/lang/Object;Lcn/bugstack/test/Req;)Ljava/lang/String;";

        Matcher m = Pattern.compile("(L.*?;|\\[{0,2}L.*?;|[ZCBSIFJD]|\\[{0,2}[ZCBSIFJD]{1})").matcher(desc.substring(0, desc.lastIndexOf(')') + 1));

        while (m.find()) {
            String block = m.group(1);
            System.out.println(block);
        }

    }
}
