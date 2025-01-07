package com.xhf.leetcode.plugin.debug.execute.java.p;

import com.sun.jdi.*;
import com.xhf.leetcode.plugin.debug.execute.java.Context;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.ComputeError;
import com.xhf.leetcode.plugin.utils.LogUtils;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.jexl3.internal.Engine;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 执行expression
 *
 * @author feigebuge
 * @email 2508020102@qq.com
 */
public class JavaEvaluatorImpl implements Evaluator {

    /**
     * 环境对象
     */
    static class Env {
        private static Map<String, Value> localEnv;
        private static Map<String, Value> memberEnv;
        private static Map<String, Value> staticEnv;
        private static StackFrame frame;
        private static ThreadReference thread;
        private static Context ctx;
        private static VirtualMachine vm;
        private static JavaValueInspector inspector;
        private static ObjectReference _this;

        public static StackFrame getFrame() {
            return frame;
        }

        public static void setFrame(StackFrame frame) {
            Env.frame = frame;
        }

        public static ThreadReference getThread() {
            return thread;
        }

        public static void setThread(ThreadReference thread) {
            Env.thread = thread;
        }

        public static Context getCtx() {
            return ctx;
        }

        public static void setCtx(Context ctx) {
            Env.ctx = ctx;
        }

        public static VirtualMachine getVm() {
            return vm;
        }

        public static void setVm(VirtualMachine vm) {
            Env.vm = vm;
        }

        public static JavaValueInspector getInspector() {
            return inspector;
        }

        public static void setInspector(JavaValueInspector inspector) {
            Env.inspector = inspector;
        }

        public static void doPrepare() throws IncompatibleThreadStateException, AbsentInformationException {
            // 获取当前栈帧
            thread = ctx.getThread();
            frame = thread.frame(0);
            _this = frame.thisObject();
            // 获取所有变量
            localEnv = takeLocalVariable(frame);
            memberEnv = takeMemberValues(_this);
            staticEnv = takeStaticValues(ctx.getLocation().declaringType());
        }

        /**
         * 获取当前栈帧的所有变量
         * @param frame
         * @return
         * @throws AbsentInformationException
         */
        public static Map<String, Value> takeLocalVariable(StackFrame frame) throws AbsentInformationException {
            List<LocalVariable> localVariables = frame.visibleVariables();
            Map<LocalVariable, Value> values = frame.getValues(localVariables);
            return DebugUtils.convert(values);
        }

        public static Map<String, Value> getLocalEnv() {
            return localEnv;
        }

        /**
         * 通过Value获取成员变量, 并转化为环境数据
         * @param v
         * @return
         */
        public static Map<String, Value> takeMemberValues(Value v) {
            if (! (v instanceof ObjectReference)) {
                throw new ComputeError("方法使用错误! 只有ObjectReference才会拥有成员变量! type = " + v.type());
            }
            ObjectReference objRef = (ObjectReference) v;
            List<Field> fields = objRef.referenceType().fields().stream().filter(e -> !e.isStatic()).collect(Collectors.toList());
            Map<Field, Value> values = objRef.getValues(fields);
            return DebugUtils.convert2(values);
        }

        public static Map<String, Value> getMemberEnv() {
            return memberEnv;
        }

        /**
         * 从referenceType中获取静态变量
         * @param referenceType
         * @return
         */
        private @NotNull static Map<String, Value> takeStaticValues(ReferenceType referenceType ) {
            // 获取所有静态变量
            List<Field> fields = referenceType.fields();
            Map<String, Value> statics = new HashMap<>();
            for (Field field : fields) {
                // 处理静态变量
                if (field.isStatic()) {
                    Value value = referenceType.getValue(field);  // 获取静态字段的值
                    statics.put(field.name(), value);
                }
            }
            return statics;
        }

        public static Map<String, Value> getStaticValues() {
            return staticEnv;
        }
    }

    /**
     * 计算工具
     */
    public static class ComputeEngin {
        private static final Engine engine = new Engine();

        public static Object execute(String expression) {
            return engine.createExpression(expression).evaluate(new MapContext());
        }

        public static Object execute(String expression, JexlContext jexlContext) {
            return engine.createExpression(expression).evaluate(jexlContext);
        }
    }

    public JavaEvaluatorImpl() {
        Env.setInspector(JavaValueInspector.getInstance());
    }

    /**
     * 方法入口
     * @param expression
     * @param context
     * @return
     */
    @Override
    public String executeExpression(String expression, Context context) {
        Env.setVm(context.getVm());
        Env.setCtx(context);

        try {
            AbstractToken.doCheck(expression);
            Env.doPrepare();
            return Env.inspector.inspectValue(TokenFactory.getInstance().parseToToken(expression, context).getValue());
        } catch (Exception e) {
            LogUtils.error(e);
            throw new ComputeError(e.getMessage());
        }
    }


    /**
     * Token, 识别用户提供的表达式, 同时封装不同类型表达式的计算逻辑
     */
    public interface Token {
        String getToken();

        Value getValue();
    }

    public abstract static class AbstractToken implements Token {
        protected final Context ctx;
        protected final String token;

        protected final TokenFactory tf = TokenFactory.getInstance();

        public AbstractToken(String token, Context ctx) {
            this.token = token;
            this.ctx = ctx;
        }

        @Override
        public String getToken() {
            return token;
        }

        /**
         * 检验表达式是否符合规范
         *
         * @param expression 表达式
         * @throws ComputeError 如果括号不匹配，则抛出异常
         */
        public static void doCheck(String expression) {
            // 检查括号是否匹配
            if (!areParenthesesAndBracketsBalanced(expression)) {
                throw new ComputeError("括号没有成对出现!!");
            }
            // 检查是否存在+=等赋值语句
            if (expression.contains("+=") || expression.contains("-=") || expression.contains("*=") || expression.contains("/=") || expression.contains("%=") || expression.contains("&=") || expression.contains("|=") || expression.contains("^=") || expression.contains("<<=") || expression.contains(">>=") || expression.contains(">>>=")) {
                throw new ComputeError("不允许使用 += -= *= /= %= &= |= ^= <<= >>= >>>= 等赋值语句!!");
            }
            if (expression.contains("==")) {
                throw new ComputeError("不允许使用 == 运算符!!");
            }
            // 排除<=, >=的干扰
            String tmp = expression.replaceAll("<=","").replaceAll(">=","");
            if (tmp.contains("=")) {
                throw new ComputeError("不允许使用 = 运算符!!");
            }
            // 判断是否存在++, --等自变化操作
            if (expression.contains("++") || expression.contains("--")) {
                throw new ComputeError("不允许使用 ++ -- 等自变化操作!!");
            }
            // 判断是否存在链式调用
            if (Pattern.compile("\\b[a-zA-Z_$][a-zA-Z0-9_$]*(\\[[^\\]]*\\])*\\.\\b[a-zA-Z_$][a-zA-Z0-9_$]*\\(.*\\)(\\.\\b[a-zA-Z_$][a-zA-Z0-9_$]*\\(.*\\))+").matcher(expression).find()) {
                throw new ComputeError("不允许使用链式调用!!");
            }
        }

        /**
         * 检查表达式中的括号和方括号是否匹配
         *
         * @param expression 表达式
         * @return 如果括号和方括号都匹配，返回true，否则返回false
         */
        protected static boolean areParenthesesAndBracketsBalanced(String expression) {
            Stack<Character> stack = new Stack<>();

            for (char ch : expression.toCharArray()) {
                if (ch == '(' || ch == '[') {
                    stack.push(ch);  // 遇到开括号或方括号时压栈
                } else if (ch == ')') {
                    if (stack.isEmpty() || stack.peek() != '(') {
                        return false;  // 如果栈为空，或者栈顶不是 '('，表示括号不匹配
                    }
                    stack.pop();  // 遇到闭括号时弹栈
                } else if (ch == ']') {
                    if (stack.isEmpty() || stack.peek() != '[') {
                        return false;  // 如果栈为空，或者栈顶不是 '[ '，表示方括号不匹配
                    }
                    stack.pop();  // 遇到闭方括号时弹栈
                }
            }

            return stack.isEmpty();  // 如果栈为空，表示所有的括号和方括号都匹配
        }

        /**
         * 通过变量名获取变量值
         * 从LocalEnv获取数据
         * 如果都没有, 报错
         *
         * @param vName
         * @return
         */
        protected Value takeValueByVName(String vName) {
            Value v = takeValueByVName(vName, Env.getLocalEnv());
//            if (v != null) {
//                return v;
//            }
//            v = takeValueByVName(vName, Env.getMemberEnv());
//            if (v != null) {
//                return v;
//            }
//            v = takeValueByVName(vName, Env.getStaticValues());
            if (v == null) {
                throw new ComputeError("变量未定义 name = " + vName);
            }
            return v;
        }

        protected Value takeValueByVName(String vName, Map<String, Value> dataSource) {
            Value v = null;
            for (Map.Entry<String, Value> next : dataSource.entrySet()) {
                if (next.getKey().equals(vName)) {
                    v = next.getValue();
                    break;
                }
            }
            return v;
        }

        /**
         * 判断v是否存在叫methodName的方法
         * @param vName 变量名
         * @param methodName 方法名
         * @param v 变量代表的Value
         */
        protected boolean checkMethodExist(String vName, String methodName, Value v) {
            if (! (v instanceof ObjectReference) || ((ObjectReference) v).referenceType().methodsByName(methodName).isEmpty()) {
                throw new RuntimeException(vName + "不存在" + methodName + "方法");
            }
            return true;
        }

        /**
         * 检查变量类型和入参类型是否一致
         * @param v
         * @param type
         * @return
         */
        protected boolean checkConsist(Value v, Type type) {
            return v.type().name().equals(type.name());
        }

        /**
         * 判断是否存在装箱问题, 即变量和入参是对应的 基本类型和包装类型/包装类型和基本类型
         * 比如v是int, type是Integer
         *
         * @param v 参数Value
         * @param type 方法入参类型
         * @return 是否存在基本类型/包装类型的问题, 并且对应
         */
        protected boolean checkWrapperAndPrimitive(Value v, Type type) {
            // 判断v和type是否是对应的类型
            /*
              要么v是基本类型, type是包装类型
              要么v是包装类型, type是基本类型
             */
            return type.name().equals(Env.inspector.getWrapperTypeByPrimitiveTypeName(v.type().name())) ||
                    type.name().equals(Env.inspector.getPrimitiveTypeByWrapperTypeName(v.type().name()));
        }

        /**
         * 自动转换类型, 将v转换为type所属类型
         * 请注意, 该方法只允许对应的类型进行转换, 比如int和Integer, long和Long, float和Float, double和Double
         *
         * @param v 参数Value
         * @param type 方法入参类型
         * @return 类型转换
         */
        protected Value handleWrapperAndPrimitive(Value v, Type type) throws ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, InvalidTypeException {
            // 包装类型->基本类型
            if (Env.inspector.isWrapperType(v)) {
                return WrapperObjReferenceToPrimitive(v);
            }
            // 基本类型->包装类型
            else if (Env.inspector.isPrimitiveType(v)){
                return PrimitiveValueToWrapperObjReference(v);
            }
            throw new ComputeError("不支持自动转换类型, 请检查入参类型是否正确! v = " + v.type().name() + " type = " + type.name());
        }

        /**
         * 包装类转换为基本类型
         * @param value
         * @return
         */
        protected Value WrapperObjReferenceToPrimitive(Value value) {
            if (value instanceof ObjectReference) {
                ObjectReference objRef = (ObjectReference) value;
                if (Env.inspector.isWrapperType(objRef)) {
                    return Env.inspector.getWrapperValue(objRef);
                } else {
                    // todo: 抛出异常, 方法使用错误! 该方法只接受包装类型
                    throw new RuntimeException("该方法只接受包装类型, 但传入的值为" + value.type().name());
                }
            }
            throw new RuntimeException("该方法只接受包装类型, 但传入的值为" + value.type().name());
        }

        /**
         * 基本类型的Value转换为包装类型Ref
         * 比如IntegerValue -> ObjectReferenceImpl
         * @param value
         * @return
         * @throws ClassNotLoadedException 类未加载
         * @throws IncompatibleThreadStateException 线程非法
         * @throws InvocationException 方法invoke时发生异常
         * @throws InvalidTypeException 方法对应的入参类型不匹配
         */
        protected Value PrimitiveValueToWrapperObjReference(Value value) throws ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, InvalidTypeException {
            if (value instanceof PrimitiveValue) {
                PrimitiveValue primitiveValue = (PrimitiveValue) value;
                // 获取primitiveType
                String pType = primitiveValue.type().name();
                // 获取包装类
                ClassType wrapperClass = (ClassType) ctx.getVm().classesByName(
                        Env.inspector.getWrapperTypeByPrimitiveTypeName(pType) /*-----获取基本类型对应的包装类型------*/
                ).get(0);
                Method constructor = wrapperClass.methodsByName("<init>").get(0);
                // 创建objRef
                ctx.invokeMethodStart();
                Value v = wrapperClass.newInstance(ctx.getThread(), constructor, Collections.singletonList(primitiveValue), 0);
                ctx.invokeMethodDone();
                return v;
            }
            throw new RuntimeException("该方法只接受基本类型, 但传入的值为" + value.type().name());
        }

        protected boolean isNumberValue(Value v) {
            String name = v.type().name();
            if (name.equals("int") || name.equals("long") || name.equals("float") || name.equals("double") ||
                    name.equals("java.lang.Integer") || name.equals("java.lang.Long") ||
                    name.equals("java.lang.Float") || name.equals("java.lang.Double")
            ) {
                return true;
            }
            return false;
        }
    }

    /**
     * 计算类型的Token
     */
    public static class EvalToken extends AbstractToken {
        public EvalToken(String token, Context ctx) {
            super(token, ctx);
        }

        private final Set<Character> skip = new HashSet<Character>() {{
            add(' ');
            add('\t');
            add('\r');
            add('\n');
            add('(');
            add(')');
        }};

        @Override
        public Value getValue() {
            StringBuilder sb = new StringBuilder();
            int len = token.length();
            for (int i = 0; i < len; i++) {
                if (skip.contains(token.charAt(i))) {
                    sb.append(token.charAt(i));
                } else {
                    Token t = tf.parseToToken(token, ctx, i);
                    sb.append(DebugUtils.removeQuotes(Env.inspector.inspectValue(t.getValue())));
                        /*
                        // 最新的Engine支持String类型计算
                        Value value = t.getValue();
                        if (! isNumberValue(value)) {
                            throw new ComputeError("表达式计算出现错误, 在第" + i + "个字符识别得到的token " + t.getToken() + " 计算结果不是数值类型! 而是" + value.type().name());
                        }
                         */
                    // skip已经匹配过的内容
                    i += t.getToken().length() - 1; // -1是为了应对下一轮循环前的i++
                }
            }
            Object execute = ComputeEngin.execute(sb.toString());
            /*
            Arrays.asList(
                    "java.lang.Integer", "java.lang.Double", "java.lang.Character", "java.lang.Long",
                    "java.lang.Float", "java.lang.Boolean", "java.lang.Byte", "java.lang.Short"
            ),
             */
            String className = execute.getClass().getName();
            Value v;
            switch (className) {
                case "java.lang.Integer":
                    v = ctx.getVm().mirrorOf((Integer) execute);
                    break;
                case "java.lang.Double":
                    v = ctx.getVm().mirrorOf((Double) execute);
                    break;
                case "java.lang.Character":
                    v = ctx.getVm().mirrorOf((Character) execute);
                    break;
                case "java.lang.Long":
                    v = ctx.getVm().mirrorOf((Long) execute);
                    break;
                case "java.lang.Float":
                    v = ctx.getVm().mirrorOf((Float) execute);
                    break;
                case "java.lang.Boolean":
                    v = ctx.getVm().mirrorOf((Boolean) execute);
                    break;
                case "java.lang.Byte":
                    v = ctx.getVm().mirrorOf((Byte) execute);
                    break;
                case "java.lang.Short":
                    v = ctx.getVm().mirrorOf((Short) execute);
                    break;
                default:
                    throw new ComputeError("无法识别的计算结果类型! execute = " + execute + " type = " + className);
            }
            try {
                return super.PrimitiveValueToWrapperObjReference(v);
            } catch (ClassNotLoadedException | IncompatibleThreadStateException | InvocationException |
                     InvalidTypeException e) {
                throw new ComputeError("计算模块, 处理基本类型转包装类型出现错误! " + e.getCause());
            }
        }
    }

    /**
     * invoke类型的Token
     */
    public static class InvokeToken extends AbstractToken {
        /**
         * 方法调用的变量名
         */

        private String callVName;
        /**
         * 调用方法需要使用的参数变量
         */
        private List<Value> params;
        /**
         * 方法名字
         */
        private String methodName;
        /**
         * 调用方法使用参数的名字
         */
        private String[] paramNames;

        public InvokeToken(String token, Context ctx) {
            super(token, ctx);
        }

        /**
         * 调用方法的.的下标
         */
        protected int dot;

        @Override
        public Value getValue() {
            // 找到最外层调用方法的变量名
            Value callV;
            try {
                callV = getCallVariable();
            } catch (Exception e) {
                throw new ComputeError(e.toString());
            }
            // 定位方法名字
            Method method = getMethod(callV);
            // 触发invokeMethod
            return doInvoke(callV, method);
        }

        protected Value doInvoke(Value callV, Method method) {
            Context ctx = Env.getCtx();
            // invokeMethod开始执行
            ctx.invokeMethodStart();
            Value resultV = null;
            try {
                resultV = ((ObjectReference) callV).invokeMethod(ctx.getThread(), method, params, 0);
            } catch (Exception e) {
                throw new ComputeError("invokeMethod调用错误! " + e);
            }
            // invokeMethod执行结束
            ctx.invokeMethodDone();
            return resultV;
        }

        protected Method getMethod(Value callV) {
            // 去除调用的变量名
            String suffix = token.substring(dot + 1);
            // 定位第一个(
            int start = suffix.indexOf("(");
            int last = suffix.lastIndexOf(")");
            // 方法名字
            this.methodName = suffix.substring(0, start);
            // 获取Params
            this.params = getParams(suffix.substring(start + 1, last));
            // 寻找methodName
            Method method = null;
            if (! checkMethodExist(callVName, methodName, callV)) {
                throw new ComputeError(callVName + "不存在" + methodName + "方法");
            }
            // 判断重载
            method = overloadCheck(callV, methodName);

            return method;
        }

        private Method overloadCheck(Value callV, String methodName) {
            ObjectReference objRef = (ObjectReference) callV;
            Method targetMethod = null;
            List<Method> methods = objRef.referenceType().methods();
            // 候选方法
            List<Method> candidates = new ArrayList<>();

            for (Method method : methods) {
                if (method.name().equals(methodName)
                        && method.argumentTypeNames().size() == params.size()
                ) {
                    candidates.add(method);
                }
            }
            if (candidates.isEmpty()) {
                throw new ComputeError(callVName + "不存在适配表达式的" + methodName + "方法");
            }
            // 如果只有一个
            if (candidates.size() == 1) {
                targetMethod = candidates.get(0);
                try {
                    // 获取方法的参数类型
                    matchValueAndType(targetMethod);
                } catch (ClassNotLoadedException e) {
                    // 手动类加载, 并重新匹配
                    loadClass(targetMethod);
                    try {
                        matchValueAndType(targetMethod);
                    } catch (Exception ex) {
                        throw new ComputeError(e.toString());
                    }
                } catch (Exception e) {
                    throw new ComputeError(e.toString());
                }
            } else {
                // todo: 以后支持一下重载类型
                throw new ComputeError("暂不支持方法重载");
            }
            return targetMethod;
        }

        /**
         * 匹配参数与方法的参数类型
         *
         * @param targetMethod 目标方法
         * @throws ClassNotLoadedException class未加载
         * @throws IncompatibleThreadStateException thread状态非法
         * @throws InvocationException invoke异常
         * @throws InvalidTypeException 类型错误
         */
        private void matchValueAndType(Method targetMethod) throws ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, InvalidTypeException {
            List<Type> types = targetMethod.argumentTypes();
            for (int i = 0; i < types.size(); i++) {
                Type type = types.get(i);
                // 尝试匹配并进行纠正
                Value matchedV = tryMatchValue(params.get(i), type, paramNames[i]);
                params.set(i, matchedV);
            }
        }

        /**
         * 尝试匹配value类型和方法的入参类型
         * <p>
         * 如果不匹配, 并且value和type是对应的 基本类型/包装类型, 则自动进行转换
         * <p>
         * 否则报错
         *
         * @param v 参数Value
         * @param type 方法的参数类型
         */
        private Value tryMatchValue(Value v, Type type, String vName) throws ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, InvalidTypeException {
            /*
                1. 检查是变量type和入参type是否一致
                2. 如果不一致, 检查是否存在自动装箱问题(包装类和基本类)
             */
            if (! checkConsist(v, type)) {
                if (checkWrapperAndPrimitive(v, type)) {
                    return handleWrapperAndPrimitive(v, type);
                } else {
                    // 报错
                    throw new ComputeError("变量类型错误, 变量类型和入参类型不匹配!\n 变量" + vName + "类型为" + v.type().name() + ", 入参类型为" + type.name());
                }
            }
            return v;
        }

        /**
         * 触发targetVM 进行类加载操作
         *
         * @param method
         */
        private void loadClass(Method method) {
            // 处理未加载类的情况，手动加载类
            try {
                // 动态加载每个参数类型
                List<Type> types = method.argumentTypes();
                for (Type type : types) {
                    // 获取类的名称
                    String className = getClassName(type);
                    if (className != null) {
                        try {
                            // 尝试加载类
                            ReferenceType referenceType = Env.vm.classesByName(className).get(0); // 加载该类
                            LogUtils.simpleDebug("class " + referenceType.name() + " 被加载!");
                        } catch (Exception ex) {
                            throw new ComputeError(ex.toString());
                        }
                    }
                }

            } catch (Exception ex) {
                throw new ComputeError(ex.toString());
            }
        }

        private String getClassName(Type type) {
            if (type instanceof ArrayType) {
                return ((ArrayType) type).componentTypeName() + "[]";
            } else if (type instanceof ReferenceType) {
                return type.name();
            }
            throw new ComputeError("未识别的type类型: " + type.name());
        }

        private List<Value> getParams(String params) {
            this.paramNames = params.split(",");
            List<Value> values = new ArrayList<>();
            for (int i = 0; i < paramNames.length; i++) {
                try {
                    values.add(tf.parseToToken(paramNames[i], ctx).getValue());
                } catch (Exception e) {
                    throw new ComputeError(callVName + "的第" + i + "个参数解析错误! cause is " + e.getMessage());
                }
            }
            return values;
        }

        public int getDot() {
            // 变量名\[ {匹配任意内容但不包括[]} \] {出现0次或者多次}  . 方法名()
            Pattern pattern = Pattern.compile("\\b[a-zA-Z_$][a-zA-Z0-9_$]*(\\[[^\\[\\]]*\\])*(\\.)[a-zA-Z_$][a-zA-Z0-9_$]*\\(.*\\)");
            Matcher matcher = pattern.matcher(token);
            dot = -1;
            // 查找 "." 的位置
            if (matcher.find()){
                dot = matcher.start(2);
            }
            if (dot == -1) {
                throw new ComputeError("代码编写有误! InvokeToken无法定位调用方法的变量名! token = " + token);
            }
            return dot;
        }

        private Value getCallVariable() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
            this.callVName = getCallVName();
            if ("this".equals(callVName)) {
                return Env._this;
            }
            return tf.parseToToken(callVName, ctx).getValue();
        }

        /**
         * 获取调用的变量名
         * @return 变量名
         */
        public String getCallVName() {
            // 获取dot坐标
            getDot();
            return token.substring(0, dot);
        }
    }


    /**
     * 纯函数调用, 不匹配调用方法的实例, 实例默认为self. 比如dfs(1,2,3). 但a.dfs(1,2,3)或者self.dfs(1,2,3)不会被当前class承认
     * 但在内部执行时, 会自动将调用函数的实例设置为this
     */
    public static class PureCallToken extends InvokeToken {

        public PureCallToken(String token, Context ctx) {
            super(token, ctx);
        }

        @Override
        public Value getValue() {
            // 因为是纯粹的方法调用, 所以callVName = this
            Value callV = Env._this;
            // 定位方法名字
            dot = -1;
            Method method = getMethod(callV);
            // 触发invokeMethod
            return doInvoke(callV, method);
        }
    }

    /**
     * Variable类型的Token
     */
    public static class VariableToken extends AbstractToken {

        public VariableToken(String token, Context ctx) {
            super(token, ctx);
        }

        @Override
        public Value getValue() {
            // 检查变量名. 如果包含.  比如this.head, 迭代获取
            if (token.contains(".")) {
                String[] split = token.split("\\.");
                // 迭代获取
                Value v = null;
                String preName = split[0];
                for (int i = 1; i < split.length; i++) {
                    String vName = split[i];
                    if (Objects.equals(preName, "this")) {
                        v = takeValueByVName(vName, Env.getMemberEnv());
                    }else {
                        // md, 不支持通过.的方式获取持静态变量, 要不然太tm麻烦了
                        // 甚至我连静态变量都不想支持, 谁特么写leetcode吃饱了撑着用静态变量
                        v = takeValueByVName(vName, Env.takeMemberValues(v));
                    }
                }
                return v;
            }
            // 从变量env中获取
            if ("this".equals(token)) {
                return Env._this;
            }
            return super.takeValueByVName(token);
        }
    }

    /**
     * Array类型的Token
     */
    public static class ArrayToken extends AbstractToken {

        public ArrayToken(String token, Context ctx) {
            super(token, ctx);
        }

        /**
         * 通过token查询array变量
         * @return 返回array变量的引用
         */
        private ArrayReference getArrayValue() {
            // 识别变量, 并获取array变量
            int idx = token.indexOf("[");
            String vName = token.substring(0, idx);
            Value v = takeValueByVName(vName);
            if (! (v instanceof ArrayReference) ) {
                throw new ComputeError("变量 " + vName + " 不是数组类型");
            }
            return (ArrayReference) v;
        }

        public List<String> getDimsStr() {
            // 计算array的维度, 并计算索引
            int idx = token.indexOf("[");
            Pattern compile = Pattern.compile("(\\[[^\\]]*\\])");
            String dimPart = token.substring(idx);
            Matcher matcher = compile.matcher(dimPart);
            List<String> dims = new ArrayList<>();
            while (matcher.find()) {
                //  匹配出每一个[]
                String range = matcher.group().replace("[", "").replace("]", "");
                dims.add(range);
            }
            return dims;
        }


        /**
         * 通过token获取维度信息
         * @return 维度信息: 每一个维度的整形数据
         */
        private List<Integer> getDims() {
            List<String> dimsStr = getDimsStr();
            if (dimsStr.isEmpty()) {
                throw new ComputeError("数组没有维度!");
            }
            List<Integer> dims = new ArrayList<>();
            for (String s : dimsStr) {
                // 获取索引数值
                Token t = tf.parseToToken(s, ctx);
                Value vv = t.getValue();
                String typeName = vv.type().name();
                if (!Objects.equals(typeName, "int")) {
                    throw new ComputeError("数组索引必须为整型!!");
                }
                int value = ((IntegerValue) vv).value();
                dims.add(value);
            }
            return dims;
        }

        /**
         * 迭代arrayReference
         * @param array arrayReference
         * @param dims 维度
         * @return Value
         */
        public Value itrArray(ArrayReference array, List<Integer> dims) {
            int idx = token.indexOf("[");
            String vName = token.substring(0, idx);
            Value value = null;
            // 迭代array
            for (int i = 0; i < dims.size(); i++) {
                int dim = dims.get(i);
                if (dim >= array.length()) {
                    throw new ComputeError("数组越界!! 数组第" + i + "维度长度=" + array.length() + ", 索引=" + dim);
                }
                value = array.getValue(dim);
                // 如果没有迭代到最后一位, 则判断是否是数组类型
                if (i != dims.size() - 1) {
                    if (!(value instanceof ArrayReference)) {
                        throw new ComputeError("变量" + vName + "第 " + i + " 维度不是数组类型, 是" + value.type().name() + "类型!!");
                    }
                    // 跟新array
                    array = (ArrayReference) value;
                }
            }
            return value;
        }

        @Override
        public Value getValue() {
            // 获取array变量
            ArrayReference array = getArrayValue();
            // 获取索引
            List<Integer> dims = getDims();
            // 迭代所有的维度, 获取最终的value
            return itrArray(array, dims);
        }

    }

    public static class ConstantToken extends AbstractToken {

        // 正则表达式，用于判断不同的常量类型
        private static final String INT_REGEX = "-?\\d+";
        private static final String FLOAT_REGEX = "-?\\d*\\.\\d+";
        private static final String BOOLEAN_REGEX = "true|false";
        private static final String STRING_REGEX = "^\".*\"$";
        private static final String CHAR_REGEX = "^'.'$"; // 单个字符，使用单引号括起来

        public ConstantToken(String token, Context ctx) {
            super(token, ctx);
        }

        @Override
        public Value getValue() {
            // 判断是否是字符串类型
            if (token.matches(STRING_REGEX)) {
                return ctx.getVm().mirrorOf(token.substring(1, token.length() - 1)); // 去掉引号
            }

            // 判断是否是布尔值
            if (token.matches(BOOLEAN_REGEX)) {
                return ctx.getVm().mirrorOf(Boolean.parseBoolean(token));
            }

            // 判断是否是整数类型
            if (token.matches(INT_REGEX)) {
                return ctx.getVm().mirrorOf(Integer.parseInt(token));
            }

            // 判断是否是浮点数类型
            if (token.matches(FLOAT_REGEX)) {
                return ctx.getVm().mirrorOf(Double.parseDouble(token));
            }

            // 判断是否是字符类型
            if (token.matches(CHAR_REGEX)) {
                char charValue = token.charAt(1); // 提取单引号内的字符
                return ctx.getVm().mirrorOf(charValue);
            }

            // 如果无法识别的类型，抛出异常
            throw new IllegalArgumentException("无法识别的常量类型: " + token);
        }
    }

    public static class OperatorToken extends AbstractToken {

        public OperatorToken(String token, Context ctx) {
            super(token, ctx);
        }

        @Override
        public Value getValue() {
            return Env.vm.mirrorOf(DebugUtils.removeQuotes(token));
        }
    }



    /**
     * Token工厂
     */
    public static class TokenFactory {
        public TokenFactory() {
        }
        private static final TokenFactory instance = new TokenFactory();
        public static TokenFactory getInstance() {
            return instance;
        }

        public static class Rule {
            final Pattern pattern;
            final Class<? extends Token> clazz;

            public Rule(Pattern pattern, Class<? extends Token> clazz) {
                this.pattern = pattern;
                this.clazz = clazz;
            }
        }

        public static class Hit {
            /**
             * 命中规则
             */
            final Rule rule;
            /**
             * 命中得到的匹配器
             */
            final Matcher matcher;
            /**
             * 第一个命中token的start位置
             */
            final int start;
            /**
             * 第一个命中的token的结束位置
             */
            final int end;
            /**
             * 规则命中的字符串长度
             */
            final int length;
            /**
             * 命中的token
             */
            final String token;

            public Hit(Rule rule, Matcher matcher, int start, int end, int length, String token) {
                this.rule = rule;
                this.matcher = matcher;
                this.start = start;
                this.end = end;
                this.length = length;
                this.token = token;
            }
        }

        Rule[] rules = new Rule[]{
                // 匹配计算类型的Token(<<,>>,+,-,*,/,&,|,!,^,<,>,!). TIP: 该正则包含了匹配运算符的正则, 因此在处理EvalToken和OperatorToken时需要额外判断
                // 详细逻辑可参考parseToToken(String, Context)的double check部分
                new Rule(Pattern.compile("<<|>>|[\\+\\-\\*\\/\\%&\\|\\^\\~\\<\\>\\!]"), EvalToken.class),
                // 匹配只含有计算符号类型的Token
                new Rule(Pattern.compile("^(<<|>>|[\\+\\-\\*\\/\\%&\\|\\^\\~\\<\\>\\!])$"), OperatorToken.class),
                // 匹配invoke类型的Token
                new Rule(Pattern.compile("\\b[a-zA-Z_$][a-zA-Z0-9_$]*(\\[[^\\]]*\\])*\\.\\b[a-zA-Z_$][a-zA-Z0-9_$]*\\(.*\\)"), InvokeToken.class),
                // 匹配纯粹的方法调用类型的Token, 比如匹配dfs(1,2), 但不匹配self.dfs(1,2)
                new Rule(Pattern.compile("\\b[a-zA-Z_$][a-zA-Z0-9_$]*\\([^()]*\\)"), PureCallToken.class),
                // 匹配纯粹的变量
                new Rule(Pattern.compile("\\b[a-zA-Z_$][a-zA-Z0-9_$]*\\b"), VariableToken.class),
                // 匹配数组
                new Rule(Pattern.compile("\\b[a-zA-Z_$][a-zA-Z0-9_$]*(\\[[^\\[\\]]+\\])+"), ArrayToken.class),
                // 匹配常量(数值、字符、字符串)
                new Rule(Pattern.compile("\\d+(\\.\\d+)?|\"([^\"\\\\]|\\\\.)*\"|\'([^\'\\\\]|\\\\.)*\'"), ConstantToken.class)
        };

        /**
         * 解析字符串得到对应的Token
         * <p>
         * 该方法会贪婪的匹配所有正则规则, 得到Hits数组. 从Hits中选取匹配得到最长的规则作为targetRule, 返回的对应的Token
         * <p>
         * 在匹配过程中, 如果匹配得到含有EvalToken.class的Rule, 则会进行二次检查. 只有判断出运算符号
         * 并没有囊括在其他类型表达式中时, 则认为该Token为EvalToken.class. 否则表达将会从剩余Hit中
         * 选择最长匹配的规则作为targetRule, 返回对应Token
         * <p>
         * 如果匹配出含有OperatorToken.class的Rule, 则在二次检查时直接锁定该规则
         *
         * @param s 表达式
         * @return token
         * @throws NoSuchMethodException 方法找不到
         * @throws InvocationTargetException invoke 异常
         * @throws InstantiationException 实例化异常
         * @throws IllegalAccessException 非法访问
         */
        public Token parseToToken(String s, Context ctx) {
            // 处理s
            s = handleInput(s);

            List<Hit> hits = new ArrayList<>(5);

            // 根据解析规则解析得到不同的Token
            for (Rule rule : rules) {
                // 如果匹配
                Pattern pattern = rule.pattern;
                Matcher matcher = pattern.matcher(s);

                if (matcher.find()) {
                    /*
                      贪婪的匹配最长内容. 以匹配出的最长内容作为匹配结果
                      此处无需考虑存在多个find结果. 因为一旦存在多个结果, 那必然需要通过运算符作为连接
                      最终返回的Token必然是EvalToken. 这会在double check部分检测

                      如果只有一个find结果, 直接获取最大即可
                     */
                    int start = matcher.start(), end = matcher.end();
                    hits.add(new Hit(rule, matcher, start, end, matcher.end() - matcher.start(), s));
                }
            }

            if (hits.isEmpty()) {
                throw new ComputeError("无法识别的表达式: " + s);
            }

            Hit targetHit = null;
            // double check
            /*
             * 需要对OperatorToken和EvalToken做出额外说明
             * OperatorToken:只能包含运算符, 如 '+', '-', ...
             * EvalToken: 不能只包含运算符, 如'1+2', '1-2', ...
             * 但因为正则编写以及后续判断逻辑的原因, 导致如果OperatorToken匹配成功, 那么EvalToken也会匹配成功
             * 但EvalToken匹配成功, OperatorToken不一定会被匹配.
             * 也就是说, EvalToken的正则包含OperatorToken的正则. 因此在处理OperatorToken和EvalToken时需要
             * 额外判断
             */
            // 包含OperatorToken
            if (hits.stream().anyMatch(hit -> hit.rule.clazz == OperatorToken.class)) {
                // 如果有运算符, 那么直接返回. 因为他的正则非常严格, 只允许字符串包含运算符. 因此当他满足时, 直接返回他
                targetHit = hits.stream().filter(hit -> hit.rule.clazz == OperatorToken.class).findFirst().get();
            }
            // 包含EvalToken
            else if (hits.stream().anyMatch(hit -> hit.rule.clazz == EvalToken.class)) {
                // 获取EvalToken
                Hit EvalHit = hits.stream().filter(hit -> hit.rule.clazz == EvalToken.class).findFirst().get();
                // 差分统计区间
                int[] diff = new int[s.length() + 1];
                for (Hit hit : hits) {
                    if (hit.equals(EvalHit)) {
                        continue;
                    }
                    Matcher m = hit.matcher;
                    diff[hit.start] += 1;
                    diff[hit.end] -= 1;
                    while (m.find()) {
                        diff[m.start()] += 1;
                        diff[m.end()] -= 1;
                    }
                }
                // 复原差分
                for (int i = 1; i < diff.length; i++) {
                    diff[i] += diff[i - 1];
                }
                // 判断EvalToken是否在差分区间内
                if (diff[EvalHit.start] == 0 && diff[EvalHit.end - 1] == 0) {
                    targetHit = EvalHit;
                } else {
                    Matcher m = EvalHit.matcher;
                    while (m.find()) {
                        if (m.start() == EvalHit.start && m.end() == EvalHit.end) {
                            targetHit = EvalHit;
                            break;
                        }
                    }
                }
                // 从剩余元素中选择最大值
                if (targetHit == null) {
                    // 过滤EvalToken
                    targetHit = hits.stream().filter(e -> !e.rule.clazz.equals(EvalToken.class)).max(Comparator.comparingInt(hit -> hit.length)).get();
                }
            } else {
                // 获取匹配长度最大的那一个
                targetHit = hits.stream().max(Comparator.comparingInt(hit -> hit.length)).get();
            }

            Rule targetRule = targetHit.rule;
            String token = targetHit.token;

            String className = targetRule.clazz.getName();
            try {
                return targetRule.clazz.getDeclaredConstructor(String.class, Context.class).newInstance(token, ctx);
            } catch (InstantiationException e) {
                throw new ComputeError(className + "实例化错误, 该对象是抽象类, 无法实例化! " + e.getMessage());
            } catch (IllegalAccessException e) {
                throw new ComputeError(className + "构造函数访问错误, String.class, Context.class的构造函数无权访问! " + e.getMessage());
            } catch (InvocationTargetException e) {
                throw new ComputeError(className + "构造函数执行异常, String.class, Context.class的运行过程中发生报错! " + e.getMessage());
            } catch (NoSuchMethodException e) {
                throw new ComputeError(className + "没有符合String.class, Context.class的构造函数! " + e.getMessage());
            }
        }

        /**
         * 处理字符串s. 移除两端的空白字符和匹配的括号
         * @param s s
         * @return 处理后的字符串
         */
        public String handleInput(String s) {
            s = s.trim();
            // 匹配括号, 如果存在最外层匹配的括号, 移除
            while (s.startsWith("(") && s.endsWith(")") && bracketMatch(s)) {
                s = s.substring(1, s.length() - 1);
                s = s.trim();
            }
            return s;
        }

        /**
         * 检查字符串最外层括号是否匹配
         * 比如() + () : false. 因为字符最开始的(和结尾的)无法对应
         * ( () + () ) : true
         * @param s s
         * @return 是否匹配成功
         */
        public boolean bracketMatch(String s) {
            s = s.trim();
            if (s.charAt(0) != '(' || s.charAt(s.length() - 1) != ')') {
                return false;
            }
            Stack<Character> stack = new Stack<>();
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '(') {
                    stack.push(c);
                } else if (c == ')') {
                    if (stack.isEmpty()) {
                        // 表达式异常(一般来说不会出现这个报错, 因为在计算开始前, 会进行语法检查)
                        throw new ComputeError("表达式异常, 括号不匹配!");
                    }
                    stack.pop();
                    // 最开始的(被其他的)匹配走了, 因此匹配失败
                    if (stack.isEmpty() && i != s.length() - 1) {
                        return false;
                    } else if (stack.isEmpty() && i == s.length() - 1) {
                        return true;
                    }
                }
            }
            // 一般不会走到这一步
            return true;
        }

        /**
         *
         * @param token 表达式
         * @return 返回Token
         */
        public Token parseToToken(String token) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
            return parseToToken(token, null);
        }

        /**
         * 解析字符串得到对应的Token. 该方法限定匹配的开始范围. start表明所有规则匹配的字符串, 初始位置必须是start
         * 如果匹配到OperatorToken, 则在double check时直接返回
         * 如果匹配到EvalToken, 则在double check时直接返回. 否则选择匹配长度最大的规则
         *
         * @param token token
         * @param ctx ctx
         * @param start 开始位置
         * @return Token
         * @throws NoSuchMethodException 方法找不到
         * @throws InvocationTargetException invoke 异常
         * @throws InstantiationException 实例化异常
         * @throws IllegalAccessException 非法访问
         */
        public Token parseToToken(String token, Context ctx, int start) {
            token = handleInput(token);
            List<Hit> hits = new ArrayList<>(5);

            for (Rule rule : rules) {
                Matcher matcher = rule.pattern.matcher(token);
                // 如果遇到OperationToken, 单独处理
                // OperationToken只能识别单纯的运算符号, 因此他只能识别简单字符串, 无需迭代
                if (rule.clazz.equals(OperatorToken.class)) {
                    boolean flag = matcher.find();
                    if (!flag) {
                        // 因为OperatorToken匹配只包含运算符的token, 因此这里需要进行字符串切割, 获取纯粹的运算符. 尝试匹配
                        // 切割字符串. 并且必须从最大长度开始切割, 因为存在<<, <这样的运算符
                        for (int len = 2; len > 0; len--) {
                            String s = (start + len) > token.length() ? token.substring(start) : token.substring(start, start + len);
                            // 匹配s
                            Matcher m = rule.pattern.matcher(s);
                            if (m.find()) {
                                hits.add(new Hit(rule, m, start, start + len - 1, len, s));
                                break;
                            }
                        }
                    } else {
                        // 直接添加. 无需判断start是否匹配
                        // 因为OperationToken对应的正则只能匹配纯运算符, 其余规则均会被忽略
                        hits.add(new Hit(rule, matcher, start, token.length(), token.length() - start, token));
                    }
                }
                /*
                  同一个匹配器只可能在同一个start位置匹配得到唯一的结果
                 */
                while (matcher.find()) {
                    int begin = matcher.start();
                    int end = matcher.end();
                    // 后续也不用匹配了, 跳过
                    if (begin > start) {
                        break;
                    }
                    if (begin == start) {
                        hits.add(new Hit(rule, matcher, begin, end, end - begin, matcher.group()));
                        break;
                    }
                }
            }

            if (hits.isEmpty()) {
                throw new ComputeError("无法识别的表达式: " + token + " start = " + start);
            }
            // double check
            Hit targetHit = null;
            if (hits.stream().anyMatch(hit -> hit.rule.clazz == OperatorToken.class)) {
                targetHit =  hits.stream().filter(hit -> hit.rule.clazz == OperatorToken.class).findFirst().get();
            }
            else if (hits.stream().anyMatch(hit -> hit.rule.clazz == EvalToken.class)) {
                targetHit =  hits.stream().filter(hit -> hit.rule.clazz == EvalToken.class).findFirst().get();
            } else {
                // 获取最大长度的规则
                targetHit = hits.stream().max(Comparator.comparingInt(hit -> hit.length)).get();
            }

            Rule targetRule = targetHit.rule;

            String className = targetRule.clazz.getName();
            try {
                return targetRule.clazz.getDeclaredConstructor(String.class, Context.class).newInstance(targetHit.token, ctx);
            } catch (InstantiationException e) {
                throw new ComputeError(className + "实例化错误, 该对象是抽象类, 无法实例化! " + e.getMessage());
            } catch (IllegalAccessException e) {
                throw new ComputeError(className + "构造函数访问错误, String.class, Context.class的构造函数无权访问! " + e.getMessage());
            } catch (InvocationTargetException e) {
                throw new ComputeError(className + "构造函数执行异常, String.class, Context.class的运行过程中发生报错! " + e.getMessage());
            } catch (NoSuchMethodException e) {
                throw new ComputeError(className + "没有符合String.class, Context.class的构造函数! " + e.getMessage());
            }
        }

        /**
         * 解析字符串得到对应的Token. 该方法限定匹配的开始范围. start表明所有规则匹配的字符串, 初始位置必须是start
         * 如果匹配到OperatorToken, 则在double check时直接返回
         * 如果匹配到EvalToken, 则在double check时直接返回. 否则选择匹配长度最大的规则
         *
         * @param token token
         * @param start 开始位置
         * @return Token
         * @throws NoSuchMethodException 方法找不到
         * @throws InvocationTargetException invoke 异常
         * @throws InstantiationException 实例化异常
         * @throws IllegalAccessException 非法访问
         */
        public Token parseToToken(String token, int start) {
            return parseToToken(token, null, start);
        }
    }

}
