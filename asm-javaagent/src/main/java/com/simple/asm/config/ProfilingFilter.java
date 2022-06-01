package com.simple.asm.config;

import java.util.HashSet;
import java.util.Set;

/**
 * 功能描述: 过滤类，将不需要增强的类和方法过滤
 *
 * @author: WuChengXing
 * @create: 2021-12-30 15:06
 **/
public class ProfilingFilter {
    private static Set<String> exceptPackagePrefix = new HashSet<>();

    private static Set<String> exceptMethods = new HashSet<>();

    static {

        // 默认不注入的包
        exceptPackagePrefix.add("java/");
        exceptPackagePrefix.add("org/objectweb/asm");
        exceptPackagePrefix.add("javax/");
        exceptPackagePrefix.add("sun/");
        exceptPackagePrefix.add("com/sun/");
        exceptPackagePrefix.add("com/intellij/");
        exceptPackagePrefix.add("org/jetbrains/");
        exceptPackagePrefix.add("org/slf4j");
        exceptPackagePrefix.add("com/alibaba");
        // 不注入监控工程，在我们写方法去增强的时候，就不能放在这个目录对应的包下面了
        exceptPackagePrefix.add("com/simple/asm");

        // 默认不注入的方法
        exceptMethods.add("main");
        exceptMethods.add("premain");
        // java.lang.Object
        exceptMethods.add("getClass");
        // java.lang.Object
        exceptMethods.add("hashCode");
        exceptMethods.add("equals");
        exceptMethods.add("clone");
        exceptMethods.add("toString");
        exceptMethods.add("notify");
        exceptMethods.add("notifyAll");
        exceptMethods.add("wait");
        exceptMethods.add("finalize");
        // spring的初始化方法
        exceptMethods.add("afterPropertiesSet");

    }

    /**
     * 不需要增强的类
     *
     * @param className
     * @return
     */
    public static boolean isNotNeedInject(String className) {
        if (null == className) {
            return false;
        }

        for (String prefix : exceptPackagePrefix) {
            if (className.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 不需要增强的方法
     *
     * @param methodName
     * @return
     */
    public static boolean isNotNeedInjectMethod(String methodName) {
        if (null == methodName) {
            return false;
        }

        return exceptMethods.contains(methodName);
    }
}
