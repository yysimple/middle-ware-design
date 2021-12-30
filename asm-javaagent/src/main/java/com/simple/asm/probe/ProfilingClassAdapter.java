package com.simple.asm.probe;

import com.simple.asm.config.ProfilingFilter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * 功能描述: 这里也是硬编码
 * |********
 * | access & ACC_PRIVATE 这里一般值为其本身，要么就为 0
 * |*******
 *
 * @author: WuChengXing
 * @create: 2021-12-30 15:32
 **/
public class ProfilingClassAdapter extends ClassVisitor {
    /**
     * 对应目录下的文件名
     */
    private final String className;

    /**
     * 全限定名：com.simple.xx.User
     */
    private final String fullClazzName;

    /**
     * 类名：User
     */
    private final String simpleClassName;

    private boolean isInterface;

    public ProfilingClassAdapter(final ClassVisitor cv, String className) {
        super(ASM5, cv);
        this.className = className;
        this.fullClazzName = className.replace('/', '.');
        this.simpleClassName = className.substring(className.lastIndexOf('/') + 1);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        // 判断是否是接口 0x0200（字节码中16进制表达数） & 512  001000000000 & 001000000000 = 512，所以这里不等于 0 既是接口
        this.isInterface = (access & ACC_INTERFACE) != 0;
    }

    /**
     * 需要进行监视的方法
     *
     * @param access
     * @param name
     * @param descriptor 方法的描述信息
     * @param signature
     * @param exceptions
     * @return
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

        // 不对接口和私有方法注入
        if (isInterface || (access & ACC_PRIVATE) != 0) {
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        // 不对抽象方法、native方法、桥接方法、合成方法进行注入
        if ((access & ACC_ABSTRACT) != 0
                || (access & ACC_NATIVE) != 0
                || (access & ACC_BRIDGE) != 0
                || (access & ACC_SYNTHETIC) != 0) {
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        if ("<init>".equals(name) || "<clinit>".equals(name)) {
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        // 过滤Object类默认方法
        if (ProfilingFilter.isNotNeedInjectMethod(name)) {
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);
        if (null == mv) {
            return null;
        }

        return new ProfilingMethodVisitor(access, name, descriptor, mv, className, fullClazzName, simpleClassName);
    }

}
