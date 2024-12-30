package com.xhf.leetcode.plugin.debug.execute.java.p;

import com.sun.jdi.*;
import com.xhf.leetcode.plugin.debug.execute.java.Context;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 执行expression
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class doExp {
    private final JavaValueInspector inspector;
    private VirtualMachine vm;
    private Context ctx;
    private StackFrame frame;
    private ThreadReference thread;
    private Map<LocalVariable, Value> localEnv;

    public doExp() {
        this.inspector = JavaValueInspector.getInstance();
    }

    public String executeExpression(String expression, Context context) {
        vm = context.getVm();
        ctx = context;
        // todo: 解析
        // todo: 执行
        try {
            doPrepare();
//             return doExecute("solutionTest", "demo", List.of("demoContent"));
//            return doExecute("solutionTest", "test", List.of());
            return doExecute("solutionTest", "test", List.of("a","b","c","demo"));
        } catch (IncompatibleThreadStateException | AbsentInformationException | ClassNotLoadedException |
                 InvocationException | InvalidTypeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 数据准备
     */
    private void doPrepare() throws IncompatibleThreadStateException, AbsentInformationException {
        // 获取当前栈帧
        this.thread = ctx.getThread();
        this.frame = this.thread.frame(0);
        // 获取所有变量
        List<LocalVariable> localVariables = frame.visibleVariables();
        Map<LocalVariable, Value> values = frame.getValues(localVariables);
        this.localEnv = values;
    }

    /**
     * 目前只从本地变量中取值
     */
    private Value takeValueByVName(String vName) throws ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, InvalidTypeException {
        Optional<Map.Entry<LocalVariable, Value>> first = this.localEnv.entrySet().stream().filter(e -> e.getKey().name().equals(vName)).findFirst();
        return first.map(Map.Entry::getValue).orElse(null);
    }

    /**
     * 假执行
     */
    private String doExecute(String vName, String methodName, List<String> paramsName) throws IncompatibleThreadStateException, AbsentInformationException, ClassNotLoadedException, InvocationException, InvalidTypeException {
        // 找到vName对应的变量Value
        Value value = takeValueByVName(vName);
        if (value == null) {
            return vName + "不存在";
        }
        // 调用他的demo方法
        if (! checkMethodExist(vName, methodName, value)) {
            throw new RuntimeException("方法不存在");
        }
        ObjectReference objRef = (ObjectReference) value;
        Method method = objRef.referenceType().methodsByName(methodName).get(0);
        // 遍历method的Param和paramsName
        List<Type> types = method.argumentTypes();
        if (types.size() != paramsName.size()) {
            throw new RuntimeException(methodName + "方法入参个数和" + "变量数个数不匹配");
        }
        // 寻找参数
        ArrayList<Value> arguments = new ArrayList<>();
        for (int i = 0; i < types.size(); i++) {
            Type type = types.get(i);
            String paramName = paramsName.get(i);
            arguments.add(getValueByVName(paramName, type));
        }
        // 调用method
        for (ThreadReference thread : vm.allThreads()) {
            if (thread.isSuspended()) {
                thread.resume(); // 恢复挂起的线程
            }
        }
        vm.resume();

        Value resultV = objRef.invokeMethod(ctx.getThread(), method, arguments, ClassType.INVOKE_SINGLE_THREADED);
        return inspector.inspectValue(resultV);
    }

    /**
     * 判断v是否存在叫methodName的方法
     * @param vName 变量名
     * @param methodName 方法名
     * @param v 变量代表的Value
     */
    private boolean checkMethodExist(String vName, String methodName, Value v) {
        if (! (v instanceof ObjectReference) || ((ObjectReference) v).referenceType().methodsByName(methodName).isEmpty()) {
            throw new RuntimeException(vName + "不存在" + methodName + "方法");
        }
        return true;
    }

    private Value getValueByVName(@NotNull String paramName, Type type) throws ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, InvalidTypeException {
        return getValueByVName(
                paramName,
                DebugUtils.convert(localEnv),
                null,
                null,
                type
        );
    }

    /*--------------------------------------------------------下面的区域以后再来测试吧!----------------------------------------------------------------*/


    /**
     * 通过变量名找到Value
     *
     * @param vName
     * @param values
     * @return
     */
    private Value getValueByVName(@NotNull String vName, Map<LocalVariable, Value> values, Type type) throws ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, InvalidTypeException {
        return getValueByVName(
                vName,
                DebugUtils.convert(values),
                null,
                null,
                type
        );
    }

    /**
     * 将constant转换为对应的Value
     * @param constant
     * @param type 方法入参类型
     * @return
     */
    private Value getValueByConstant(@NotNull String constant, Type type) throws ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, InvalidTypeException {
        String typeName = type.name();
        VirtualMachine vm = ctx.getVm();
        // 检查类型
        boolean isPrimitiveType = this.inspector.isPrimitiveType(typeName);
        boolean isWrapperType = this.inspector.isWrapperType(typeName);
        if (!isPrimitiveType && !isWrapperType) {
            throw new RuntimeException("入参类型为: " + typeName + " 无法与常量 " + constant + " 匹配!");
        }

        // 如果type是基本类型, 直接创建对应的Value
        Value v = null;
        String primitiveTypeName = isPrimitiveType ? typeName : this.inspector.getPrimitiveTypeByWrapperTypeName(typeName);
        switch (primitiveTypeName) {
            case "int":
                v = vm.mirrorOf(Integer.parseInt(constant));
                break;
            case "double":
                v = vm.mirrorOf(Double.parseDouble(constant));
                break;
            case "char":
                v = vm.mirrorOf(constant.charAt(0));
                break;
            case "long":
                v = vm.mirrorOf(Long.parseLong(constant));
                break;
            case "float":
                v = vm.mirrorOf(Float.parseFloat(constant));
                break;
            case "boolean":
                v = vm.mirrorOf(Boolean.parseBoolean(constant));
                break;
            case "byte":
                v = vm.mirrorOf(Byte.parseByte(constant));
                break;
            case "short":
                v = vm.mirrorOf(Short.parseShort(constant));
                break;
        }
        if (v == null) {
            throw new RuntimeException("无法识别类型: " + typeName);
        }
        if (isPrimitiveType) {
            return v;
        }
        // 如果type是包装类型, 需要转化为基本类型对应的ObjectReference
        // return this.constantToWrapperObjReference(constant, typeName, ctx);
        return this.PrimitiveValueToWrapperObjReference(v);
    }

    /**
     * 通过变量名获取Value, 该方法只支持获取方法调用时使用的变量
     * <p>
     * 比如
     * a = 1;
     * demo.test(a)
     * <p>
     * 支持获取变量a, 但不支持获取demo
     *
     * <p>
     * 暂时只检查局部变量(tempEnv)
     *
     * @param vName
     * @param tempEnv
     * @param memberEnv
     * @param staticEnv
     * @param type
     * @return
     */
    private Value getValueByVName(@NotNull String vName,
                                  Map<String, Value> tempEnv,
                                  Map<String, Value> memberEnv,
                                  Map<String, Value> staticEnv,
                                  Type type
    ) throws ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, InvalidTypeException {
        Value v = null;
        for (Map.Entry<String, Value> next : tempEnv.entrySet()) {
            if (next.getKey().equals(vName)) {
                v = next.getValue();
                break;
            }
        }
        // todo: 抛出异常, 变量未定义
        if (v == null) {
            throw new RuntimeException("变量未定义 name = " + vName);
        }
        /*
            1. 检查是变量type和入参type是否一致
            2. 如果不一致, 检查是否存在自动装箱问题(包装类和基本类)
         */
        if (! checkConsist(v, type)) {
            if (checkWrapperAndPrimitive(v, type)) {
                return handleWrapperAndPrimitive(v, type);
            } else {
                // 报错
                throw new RuntimeException("变量类型错误, 变量类型和入参类型不匹配!\n 变量" + vName + "类型为" + v.type().name() + ", 入参类型为" + type.name());
            }
        }
        return v;
    }

    /**
     * 自动转换类型, 将v转换为type所属类型
     * 请注意, 该方法只允许对应的类型进行转换, 比如int和Integer, long和Long, float和Float, double和Double
     *
     * @param v
     * @param type
     * @return
     */
    private Value handleWrapperAndPrimitive(Value v, Type type) throws ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, InvalidTypeException {
        // 包装类型->基本类型
        if (this.inspector.isWrapperType(v)) {
            return WrapperObjReferenceToPrimitive(v);
        }
        // 基本类型->包装类型
        else if (this.inspector.isPrimitiveType(v)){
            return PrimitiveValueToWrapperObjReference(v);
        }
        throw new IllegalArgumentException("不支持自动转换类型, 请检查入参类型是否正确! v = " + v.type().name() + " type = " + type.name());
    }

    /**
     * 判断是否存在装箱问题, 即变量和入参是对应的 基本类型和包装类型/包装类型和基本类型
     * 比如v是int, type是Integer
     *
     * @param v
     * @param type
     * @return
     */
    private boolean checkWrapperAndPrimitive(Value v, Type type) {
        // 判断v和type是否是对应的类型
        /*
          要么v是基本类型, type是包装类型
          要么v是包装类型, type是基本类型
         */
        return type.name().equals(this.inspector.getWrapperTypeByPrimitiveTypeName(v.type().name())) ||
                type.name().equals(this.inspector.getPrimitiveTypeByWrapperTypeName(v.type().name()));
    }

    /**
     * 检查变量类型和入参类型是否一致
     * @param v
     * @param type
     * @return
     */
    private boolean checkConsist(Value v, Type type) {
        return v.type().name().equals(type.name());
    }

    /**
     * 包装类转换为基本类型
     * @param value
     * @return
     */
    private Value WrapperObjReferenceToPrimitive(Value value) {
        if (value instanceof ObjectReference) {
            ObjectReference objRef = (ObjectReference) value;
            if (inspector.isWrapperType(objRef)) {
                return inspector.getWrapperValue(objRef);
            } else {
                // todo: 抛出异常, 方法使用错误! 该方法只接受包装类型
                throw new RuntimeException("该方法只接受包装类型, 但传入的值为" + value.type().name());
            }
        }
        throw new RuntimeException("该方法只接受包装类型, 但传入的值为" + value.type().name());
    }

    /**
     * 通过调用包装类的valueOf方法, 将constant转换为对应wrapperType的包装类型对象
     * <p>
     * constant = "3.0f",
     * wrapperType = "java.lang.Float"
     * 通过反射得到Float.valueOf方法, 通过调用该方法得到ObjectReference Value
     * Value表示的是Float的引用
     *
     * @param constant
     * @param wrapperTypeName
     * @return
     * @throws ClassNotLoadedException
     * @throws IncompatibleThreadStateException
     * @throws InvocationException
     * @throws InvalidTypeException
     */
    @Deprecated // 通过valueOf创建对象可能存在问题, 暂时弃用
    private Value constantToWrapperObjReference(String constant, String wrapperTypeName) throws ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, InvalidTypeException {
        // 获取wrapperType对应的ClassType
        VirtualMachine vm = ctx.getVm();
        ClassType wrapperClass = (ClassType) vm.classesByName(wrapperTypeName).get(0);
        // 获取valueOf方法
        Method valueOf = null;
        for (Method method : wrapperClass.methods()) {
            if (method.name().equals("valueOf")
                    && method.argumentTypeNames().get(0).equals("java.lang.String")
            ) {
                valueOf = method;
                break;
            }
        }
        // 判断valueOf
        if (valueOf == null) {
            throw new RuntimeException("valueOf方法不存在于" + wrapperClass);
        }
        // 调用valueOf
        // ctx.getThread().suspend();
        Value value = wrapperClass.invokeMethod(
                ctx.getThread(),
                valueOf,
                Collections.singletonList(vm.mirrorOf(DebugUtils.removeQuotes(constant))),
                ClassType.INVOKE_SINGLE_THREADED
        );
        System.out.println(value);
        return value;
    }

    /**
     * 基本类型的Value转换为包装类型Ref
     * 比如IntegerValue -> ObjectReferenceImpl
     * @param value
     * @return
     * @throws ClassNotLoadedException
     * @throws IncompatibleThreadStateException
     * @throws InvocationException
     * @throws InvalidTypeException
     */
    private Value PrimitiveValueToWrapperObjReference(Value value) throws ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, InvalidTypeException {
        if (value instanceof PrimitiveValue) {
            PrimitiveValue primitiveValue = (PrimitiveValue) value;
            // 获取primitiveType
            String pType = primitiveValue.type().name();
            // 获取包装类
            ClassType wrapperClass = (ClassType) ctx.getVm().classesByName(
                    this.inspector.getWrapperTypeByPrimitiveTypeName(pType) /*-----获取基本类型对应的包装类型------*/
            ).get(0);
            Method constructor = wrapperClass.methodsByName("<init>").get(0);
            // 创建objRef
            return wrapperClass.newInstance(ctx.getThread(), constructor, Collections.singletonList(primitiveValue), 0);
        }
        throw new RuntimeException("该方法只接受基本类型, 但传入的值为" + value.type().name());
    }
}
