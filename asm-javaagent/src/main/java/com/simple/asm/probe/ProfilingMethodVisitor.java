package com.simple.asm.probe;

import com.simple.asm.base.MethodTag;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-30 15:32
 **/
public class ProfilingMethodVisitor extends AdviceAdapter {
    private List<String> parameterTypeList = new ArrayList<>();

    /**
     * 参数个数
     */
    private int parameterTypeCount = 0;

    /**
     * 启动时间标记
     */
    private int startTimeIdentifier;

    /**
     * 入参内容标记
     */
    private int parameterIdentifier;

    /**
     * 方法全局唯一标记
     */
    private int methodId = -1;

    /**
     * 当前局部变量值
     */
    private int currentLocal = 0;

    /**
     * true；静态方法，false；非静态方法
     */
    private final boolean isStaticMethod;

    /**
     * 类名
     */
    private final String className;

    protected ProfilingMethodVisitor(int access, String methodName, String desc, MethodVisitor mv, String className, String fullClassName, String simpleClassName) {
        super(ASM5, mv, access, methodName, desc);
        this.className = className;
        // 判断是否为静态方法，非静态方法中局部变量第一个值是this，静态方法是第一个入参参数
        isStaticMethod = 0 != (access & ACC_STATIC);
        // (String var1,Object var2,String var3,int var4,long var5,int[] var6,Object[][] var7,Req var8)==
        // "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/String;IJ[I[[Ljava/lang/Object;Lorg/itstack/test/Req;)V"
        /**
         * 这里匹配出来就是下面的情况：
         * Ljava/lang/String;
         * Ljava/lang/Object;
         * Ljava/lang/String;
         * I
         * J
         * [I
         * [[Ljava/lang/Object;
         * Lcn/bugstack/test/Req;
         */
        Matcher matcher = Pattern.compile("(L.*?;|\\[{0,2}L.*?;|[ZCBSIFJD]|\\[{0,2}[ZCBSIFJD]{1})").matcher(desc.substring(0, desc.lastIndexOf(')') + 1));
        while (matcher.find()) {
            // 添加到参数列表之中
            parameterTypeList.add(matcher.group(1));
        }
        parameterTypeCount = parameterTypeList.size();
        methodId = ProfilingAspect.generateMethodId(new MethodTag(fullClassName, simpleClassName, methodName, desc, parameterTypeList, desc.substring(desc.lastIndexOf(')') + 1)));
    }

    private Label from = new Label(),
            to = new Label(),
            target = new Label();

    /**
     * 这里就是实际增强的方法
     */
    @Override
    protected void onMethodEnter() {
        // 1.方法执行时启动纳秒
        probeStartTime();
        // 2.方法入参信息
        probeMethodParameter();
        // 标志：try块开始位置
        visitLabel(from);
        visitTryCatchBlock(from, to, target, "java/lang/Exception");
    }

    /**
     * 方法执行时启动纳秒
     */
    private void probeStartTime() {
        // long l = System.nanoTime();
        // 这里应该是ASM做好了字节码解析，字节码存在方式一般是二进制文件，转成 ASCII字符形式，就是对应下面 name 和 desc的信息，可以看看
        // 我写的文章：https://www.wolai.com/fkvaPasLsWQ1ghzv9h46ho
        // 所以这里我们按需传入我们要执行的方法描述就行
        mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(System.class), "nanoTime", "()J", false);
        // 这里应该是设置返回值信息，然后会存入到局部变量表
        currentLocal = newLocal(Type.LONG_TYPE);
        startTimeIdentifier = currentLocal;
        // 这里就是将返回值 currentLocal 存入到局部变量表
        mv.visitVarInsn(LSTORE, currentLocal);
    }

    /**
     * 参数解析
     */
    private void probeMethodParameter() {
        int parameterCount = parameterTypeList.size();
        if (parameterCount <= 0) {
            return;
        }
        // 初始化数组 BIPUSH 可以理解为都是对 int的操作，byte short 也是以这种方式处理
        // 在JVM指令设计之处，因为只支持255个操作码，又要为了节省资源，所以对这两种操作提供
        // 为数不多的几个可以使用一个操作码就行了，无需操作数的指令（操作数都隐含在指令中）
        if (parameterCount >= 4 && parameterCount < 128) {
            // valuebyte值带符号扩展成int值入栈。
            // 其实这里是有问题的，参数大于127以上BIPUSH是无效的，要用SIPUSH； 参数个大于32767的时候BIPUSH是无效的，使用LDC
            mv.visitVarInsn(BIPUSH, parameterCount);
        } else if (parameterCount >= 128 && parameterCount < 32768) {
            mv.visitVarInsn(SIPUSH, parameterCount);
        } else if (parameterCount >= 32768) {
            mv.visitVarInsn(LDC, parameterCount);
        } else {
            switch (parameterCount) {
                case 1:
                    // 1(int)值入栈
                    mv.visitInsn(ICONST_1);
                    break;
                case 2:
                    // 2(int)值入栈
                    mv.visitInsn(ICONST_2);
                    break;
                case 3:
                    // 3(int)值入栈
                    mv.visitInsn(ICONST_3);
                    break;
                default:
                    // 0(int)值入栈
                    mv.visitInsn(ICONST_0);
            }
        }
        // 初始化slot数组容量，也即局部变量表的容量
        mv.visitTypeInsn(ANEWARRAY, Type.getDescriptor(Object.class));

        // 局部变量
        int localCount = isStaticMethod ? -1 : 0;
        // 给数组赋值
        for (int i = 0; i < parameterCount; i++) {
            mv.visitInsn(DUP);
            if (i > 5) {
                mv.visitVarInsn(BIPUSH, i);
            } else {
                switch (i) {
                    case 0:
                        mv.visitInsn(ICONST_0);
                        break;
                    case 1:
                        mv.visitInsn(ICONST_1);
                        break;
                    case 2:
                        mv.visitInsn(ICONST_2);
                        break;
                    case 3:
                        mv.visitInsn(ICONST_3);
                        break;
                    case 4:
                        mv.visitInsn(ICONST_4);
                        break;
                    case 5:
                        mv.visitInsn(ICONST_5);
                        break;
                    default:
                }
            }

            String type = parameterTypeList.get(i);
            if ("Z".equals(type)) {
                // 获取对应的参数
                mv.visitVarInsn(ILOAD, ++localCount);
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Boolean.class), "valueOf", "(Z)Ljava/lang/Boolean;", false);
            } else if ("C".equals(type)) {
                // 获取对应的参数
                mv.visitVarInsn(ILOAD, ++localCount);
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Character.class), "valueOf", "(C)Ljava/lang/Character;", false);
            } else if ("B".equals(type)) {
                // 获取对应的参数
                mv.visitVarInsn(ILOAD, ++localCount);
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Byte.class), "valueOf", "(B)Ljava/lang/Byte;", false);
            } else if ("S".equals(type)) {
                // 获取对应的参数
                mv.visitVarInsn(ILOAD, ++localCount);
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Short.class), "valueOf", "(S)Ljava/lang/Short;", false);
            } else if ("I".equals(type)) {
                // 获取对应的参数
                mv.visitVarInsn(ILOAD, ++localCount);
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Integer.class), "valueOf", "(I)Ljava/lang/Integer;", false);
            } else if ("F".equals(type)) {
                // 获取对应的参数
                mv.visitVarInsn(FLOAD, ++localCount);
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Float.class), "valueOf", "(F)Ljava/lang/Float;", false);
            } else if ("J".equals(type)) {
                // 获取对应的参数
                mv.visitVarInsn(LLOAD, ++localCount);
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Long.class), "valueOf", "(J)Ljava/lang/Long;", false);
                localCount++;
            } else if ("D".equals(type)) {
                // 获取对应的参数
                mv.visitVarInsn(DLOAD, ++localCount);
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Double.class), "valueOf", "(D)Ljava/lang/Double;", false);
                localCount++;
            } else {
                // 获取对应的参数
                mv.visitVarInsn(ALOAD, ++localCount);
            }
            mv.visitInsn(AASTORE);
        }

        parameterIdentifier = newLocal(Type.LONG_TYPE);
        mv.visitVarInsn(ASTORE, parameterIdentifier);

    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
        // 可以打印方法中所有入参的名称，这也可以用于后续自定义插针
        int methodParameterIndex = isStaticMethod ? index : index - 1;
        if (0 <= methodParameterIndex && methodParameterIndex < parameterTypeList.size()) {
            ProfilingAspect.setMethodParameterGroup(methodId, name);
        }
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        //标志：try块结束
        mv.visitLabel(to);
        //标志：catch块开始位置
        mv.visitLabel(target);

        // 设置visitFrame：mv.visitFrame(Opcodes.F_FULL, 4,
        // new Object[]{"java/lang/String", Opcodes.INTEGER, Opcodes.LONG, "[Ljava/lang/Object;"}, 1, new Object[]{"java/lang/Exception"});
        int nLocal = (isStaticMethod ? 0 : 1) + parameterTypeCount + (parameterTypeCount == 0 ? 1 : 2);
        Object[] localObjs = new Object[nLocal];
        int objIdx = 0;
        if (!isStaticMethod) {
            localObjs[objIdx++] = className;
        }
        for (String parameter : parameterTypeList) {
            if ("Z".equals(parameter)) {
                localObjs[objIdx++] = Opcodes.INTEGER;
            } else if ("C".equals(parameter)) {
                localObjs[objIdx++] = Opcodes.INTEGER;
            } else if ("B".equals(parameter)) {
                localObjs[objIdx++] = Opcodes.INTEGER;
            } else if ("S".equals(parameter)) {
                localObjs[objIdx++] = Opcodes.INTEGER;
            } else if ("I".equals(parameter)) {
                localObjs[objIdx++] = Opcodes.INTEGER;
            } else if ("F".equals(parameter)) {
                localObjs[objIdx++] = Opcodes.FLOAD;
            } else if ("J".equals(parameter)) {
                localObjs[objIdx++] = Opcodes.LONG;
            } else if ("D".equals(parameter)) {
                localObjs[objIdx++] = Opcodes.DOUBLE;
            } else {
                localObjs[objIdx++] = parameter;
            }
        }
        localObjs[objIdx++] = Opcodes.LONG;
        if (parameterTypeCount > 0) {
            localObjs[objIdx] = "[Ljava/lang/Object;";
        }
        mv.visitFrame(Opcodes.F_FULL, nLocal, localObjs, 1, new Object[]{"java/lang/Exception"});

        // 异常信息保存到局部变量
        int local = newLocal(Type.LONG_TYPE);
        mv.visitVarInsn(ASTORE, local);

        // 输出参数
        mv.visitVarInsn(LLOAD, startTimeIdentifier);
        mv.visitLdcInsn(methodId);
        if (parameterTypeList.isEmpty()) {
            mv.visitInsn(ACONST_NULL);
        } else {
            mv.visitVarInsn(ALOAD, parameterIdentifier);
        }
        mv.visitVarInsn(ALOAD, local);
        // 注入监控方法
        mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(ProfilingAspect.class), "point", "(JI[Ljava/lang/Object;Ljava/lang/Throwable;)V", false);

        // 抛出异常
        mv.visitVarInsn(ALOAD, local);
        mv.visitInsn(ATHROW);

        super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    protected void onMethodExit(int opcode) {
        if ((IRETURN <= opcode && opcode <= RETURN) || opcode == ATHROW) {
            probeMethodReturn(opcode);
            mv.visitVarInsn(LLOAD, startTimeIdentifier);
            mv.visitLdcInsn(methodId);
            // 判断入参
            if (parameterTypeList.isEmpty()) {
                mv.visitInsn(ACONST_NULL);
            } else {
                mv.visitVarInsn(ALOAD, parameterIdentifier);
            }
            // 判断出参
            if (RETURN == opcode) {
                mv.visitInsn(ACONST_NULL);
            } else if (IRETURN == opcode) {
                mv.visitVarInsn(ILOAD, currentLocal);
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Integer.class), "valueOf", "(I)Ljava/lang/Integer;", false);
            } else {
                mv.visitVarInsn(ALOAD, currentLocal);
            }
            // 注入监控方法
            mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(ProfilingAspect.class), "point", "(JI[Ljava/lang/Object;Ljava/lang/Object;)V", false);
        }
    }

    /**
     * 方法出参
     */
    private void probeMethodReturn(int opcode) {
        currentLocal = this.nextLocal;
        switch (opcode) {
            case RETURN:
                break;
            case ARETURN:
                // 将栈顶引用类型值保存到局部变量indexbyte中。
                mv.visitVarInsn(ASTORE, currentLocal);
                // 从局部变量indexbyte中装载引用类型值入栈。
                mv.visitVarInsn(ALOAD, currentLocal);
                break;
            case IRETURN:
                visitVarInsn(ISTORE, currentLocal);
                visitVarInsn(ILOAD, currentLocal);
                break;
            case LRETURN:
                visitVarInsn(LSTORE, currentLocal);
                visitVarInsn(LLOAD, currentLocal);
                break;
            case DRETURN:
                visitVarInsn(DSTORE, currentLocal);
                visitVarInsn(DLOAD, currentLocal);
                break;
            default:
        }
    }
}
