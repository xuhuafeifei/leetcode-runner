package com.xhf.leetcode.plugin.debug.execute.java.p;

import com.sun.jdi.*;
import com.xhf.leetcode.plugin.debug.execute.java.Context;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.ComputeError;
import com.xhf.leetcode.plugin.search.utils.CharacterHelper;
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

    /**
     * 链式调用的Token
     */
    public interface TokenChain extends Token {
        /**
         * 链式法则, 通过前面的Token获取的Value作为当前链式调用的调用方
         * @param pV
         * @return
         */
        Value getValue(Value pV);
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
            // 判断是否存在链式方法调用
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
            char[] tokenChars = token.toCharArray(); // 将字符串转换为字符数组
            int len = tokenChars.length;
            for (int i = 0; i < len; i++) {
                if (skip.contains(tokenChars[i])) {
                    sb.append(tokenChars[i]);
                } else {
                    Token t = tf.parseToTokenFromStart(new String(tokenChars, i, len - i), ctx); // 使用字符数组创建子字符串
                    sb.append(DebugUtils.removeQuotes(Env.inspector.inspectValue(t.getValue())));

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

    public static class InvokeTokenChain extends InvokeToken implements TokenChain {

        public InvokeTokenChain(String token, Context ctx) {
            super(token, ctx);
        }

        @Override
        public Value getValue(Value pV) {
            return null;
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

    public static class PureCallTokenChain extends PureCallToken implements TokenChain {

        public PureCallTokenChain(String token, Context ctx) {
            super(token, ctx);
        }

        @Override
        public Value getValue() {
            return super.getValue();
        }

        @Override
        public Value getValue(Value pV) {
            return null;
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

    public static class VariableTokenChain extends VariableToken implements TokenChain {

        public VariableTokenChain(String token, Context ctx) {
            super(token, ctx);
        }

        @Override
        public Value getValue(Value pV) {
            return null;
        }

        @Override
        public Value getValue() {
            return null;
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

    public static class ArrayTokenChain extends ArrayToken implements TokenChain {

        public ArrayTokenChain(String token, Context ctx) {
            super(token, ctx);
        }

        @Override
        public Value getValue(Value pV) {
            return null;
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

        public interface Rule {
            /**
             * 判断s是否完全匹配Rule的规则
             * @param s
             * @return
             */
            boolean match(String s);
            /**
             * 从头开始匹配, 截取匹配规则的字符串. 该方法区别于{@link Rule#match(String)}, 只要从头开始能够部分匹配, 则返回匹配的字符串
             * @param s
             * @return
             */
            String matchFromStart(String s);
            String getName();
            Class<? extends Token> tokenClass();
        }

        /**
         * 链式法则, 匹配链式Token
         */
        public interface RuleChain extends Rule {
        }

        public static abstract class AbstractRule implements Rule {
            final Class<? extends Token> clazz;

            public AbstractRule(Class<? extends Token> clazz) {
                this.clazz = clazz;
            }

            @Override
            public Class<? extends Token> tokenClass() {
                return this.clazz;
            }

            // 匹配计算类型的Token(<<,>>,+,-,*,/,&,|,!,^,<,>,!). TIP: 该正则包含了匹配运算符的正则, 因此在处理EvalToken和OperatorToken时需要额外判断
            // 详细逻辑可参考parseToToken(String, Context)的double check部分
            public static final Pattern evalPattern = Pattern.compile("<<|>>|[\\+\\-\\*\\/\\%&\\|\\^\\~\\<\\>\\!]");

            // 匹配只含有计算符号类型的Token
            public static final Pattern operatorPattern = Pattern.compile("^(<<|>>|[\\+\\-\\*\\/\\%&\\|\\^\\~\\<\\>\\!])$");

            /*
                匹配invoke类型的Token [正则只会匹配一组方法调用]
                eg:
                a.test -> null [不是方法调用]
                a.test() -> 匹配得到: a.test()
                a.test().b -> a.test()
                a.test().b.test() -> a.test()
             */
            public static final Pattern invokePattern = Pattern.compile("^\\b[a-zA-Z_$][a-zA-Z0-9_$]*\\.\\b[a-zA-Z_$][a-zA-Z0-9_$]*\\(.*?\\)");

            /*
             匹配纯方法调用[只能匹配一次的方法调用]
             eg:
             demoTest -> null 不存在函数调用
             self.dfs(1,2) -> 匹配得到: null [不是纯粹的函数调用, 因为有self作为调用者]
             dfs(1,2) -> 匹配得到: dfs(1,2)
             dfs(1,2).test() -> dfs(1,2)
             */
            public static final Pattern pureCallPattern = Pattern.compile("\\b[a-zA-Z_$][a-zA-Z0-9_$]*\\([^()]*\\)");

            // 匹配纯粹的变量
            public static final Pattern variablePattern = Pattern.compile("\\b[a-zA-Z_$][a-zA-Z0-9_$]*\\b");

            // 匹配数组
            public static final Pattern arrayPattern = Pattern.compile("\\b[a-zA-Z_$][a-zA-Z0-9_$]*(\\[[^\\[\\]]+\\])+");

            // 匹配常量(数值、字符、字符串): 该正则非常宽松: 1 + 2也能匹配
            public static final Pattern constantPattern = Pattern.compile("\\d+(\\.\\d+)?|\"([^\"\\\\]|\\\\.)*\"|\'([^\'\\\\]|\\\\.)*\'");
        }


        /**
         * 匹配EvalToken的规则
         */
        public static class EvalRule extends AbstractRule {
            public EvalRule () {
                super(EvalToken.class);
            }

            /**
             * 匹配原理: 如果能被识别为eval rule. 那么必然存在运算符, 且运算符不属于任何别的区间, 比如arr[1 + 2]
             * 他就不符合eval rule. 因为运算内容存在于数组内.
             * <p>
             * eg:
             * arr[1+2] x
             * dfs(1+2, b) x
             * a.invoke(1+2, 2) x
             * <p>
             * arr[1+2] + (1+2) √
             * arr[1+2] * b √
             * <p>
             * TIP: 字符串s已经去除无效信息, 诸如外层空格, 和最外层匹配的括号, 详见{@link TokenFactory#bracketMatch(String)}
             *
             * @param s
             * @return
             */
            public boolean match(String s) {
                char[] arr = s.toCharArray();
                int len = arr.length;
                for (int i = 0; i < len; ++i) {
                    char c = arr[i];
                    if (c == '(' || c== '[' || c == '{') {
                        // stack匹配. 无需考虑合法性, 因为存在语法检查
                        i = CharacterHelper.matchBracket(arr, i);
                    } else if (CharacterHelper.isArabicNumber(c)) {
                        int j = i;
                        char cc = c;
                        while (j < len && CharacterHelper.isArabicNumber(cc)) {
                            j += 1;
                            cc = arr[cc];
                        }
                        i = j - 1;
                    } else if (c == '_' || CharacterHelper.isEnglishLetter(c) || c == '$') {
                        int j = i;
                        char cc = c;
                        while (j < len &&
                                (cc == '_'
                                || CharacterHelper.isArabicNumber(cc)
                                || CharacterHelper.isEnglishLetter(cc)
                                || c == '$'
                                )
                        ) {
                            j += 1;
                            cc = arr[cc];
                        }
                        i = j - 1;
                    } else if (operatorPattern.matcher(String.valueOf(c)).find()) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public String matchFromStart(String s) {
                if (match(s)) {
                    return s;
                }
                return null;
            }

            @Override
            public String getName() {
                return "EvalRule";
            }
        }

        /**
         * 匹配OperatorToken的规则
         */
        public static class OperatorRule extends AbstractRule {
            public OperatorRule() {
                super(OperatorToken.class);
            }

            public boolean match(String s) {
               return operatorPattern.matcher(String.valueOf(s)).find();
            }

            @Override
            public String matchFromStart(String s) {
                // 因为OperatorToken匹配只包含运算符的token, 因此这里需要进行字符串切割, 获取纯粹的运算符. 尝试匹配
                // 切割字符串. 并且必须从最大长度开始切割, 因为存在<<, <这样的运算符
                int start = 0;
                for (int len = 2; len > 0; len--) {
                    String tmp = (start + len) > s.length() ? s.substring(start) : s.substring(start, start + len);
                    // 匹配s
                    Matcher m = operatorPattern.matcher(s);
                    if (m.find()) {
                        return tmp;
                    }
                }
                return null;
            }

            @Override
            public String getName() {
                return "OperatorRule";
            }
        }

        /**
         * 匹配InvokeToken的规则
         */
        public static class InvokeRule extends AbstractRule {
            public InvokeRule() {
                super(InvokeToken.class);
            }

            public boolean match(String s) {
                Matcher matcher = invokePattern.matcher(s);
                if (!matcher.find()) return false;
                return matcher.end() >= s.length();
            }

            /**
             * 从s的start位置开始匹配, 满足方法调用的内容将会被识别并返回
             * eg:
             * a.invoke(1+2, 2) + 2 -> 返回: a.invoke(1+2, 2)
             * a.invoke(1+2, 2).test.c -> 返回: a.invoke(1+2, 2)
             * a.invoke(1+2, 2).b.test() -> 返回: a.invoke(1+2, 2)
             * @param s
             * @return
             */
            @Override
            public String matchFromStart(String s) {
                Matcher matcher = invokePattern.matcher(s);
                boolean b = matcher.find();
                if (! b) return null;
                return s.substring(0, matcher.end());
            }

            @Override
            public String getName() {
                return "InvokeRule";
            }
        }

        /**
         * 匹配链式方法调用: 要求字符串开头属于实例方法调用, 后续存在别的调用
         * eg:
         * a.invoke().a √
         * a.invoke().demo() √
         * a.invoke().arr[1] √
         * a.invoke().arr[1].test() √
         *
         * a.invoke() x
         * b.c.test() x
         * b.c x
         */
        public static class InvokeRuleChain extends AbstractRule implements RuleChain {
            public InvokeRuleChain() {
                super(InvokeTokenChain.class);
            }

            public boolean match(String s) {
                Matcher matcher = invokePattern.matcher(s);
                if (! matcher.find()) return false;
                int end = matcher.end();
                if (end >= s.length()) return false;
                return s.charAt(end) == '.';
            }

            /**
             * 从s的start位置开始匹配, 满足方法的链式调用的内容将会被识别并返回
             * a.invoke() -> null 【不存在链式】
             * a.invoke().a + 1 -> a.invoke().a
             * a.invoke().a() + 1 -> a.invoke().a()
             * a.invoke().a().b().c -> a.invoke().a().b().c
             *
             * @param s
             * @return
             */
            @Override
            public String matchFromStart(String s) {
                if (! match(s)) return null;
                String dummyS = "." + s;
                int end = CharacterHelper.matchChain(dummyS, 0);
                return dummyS.substring(1, end);
            }

            @Override
            public String getName() {
                return "InvokeRuleChain";
            }
        }


        /*
         * 匹配PureCallToken的规则
         * eg:
         * dfs(1,2) √
         *
         * self.dfs(1,2) x
         * dfs(1,2).a x
         */
        public static class PureCallRule extends AbstractRule {
            public PureCallRule() {
                super(PureCallToken.class);
            }

            public boolean match(String s) {
                Matcher matcher = pureCallPattern.matcher(s);
                if (! matcher.find()) return false;
                return matcher.end() >= s.length();
            }

            /**
             * 从start位置开始匹配, 满足纯函数调用的内容将会被识别并返回
             * eg:
             * dfs(1,2).a + 1 -> dfs(1,2)
             * self.dfs(1,2) + 1 -> null 【不存在纯函数调用】
             * dfs(1,2).dfs(1) -> dfs(1,2)
             *
             * @param s
             * @return
             */
            @Override
            public String matchFromStart(String s) {
                Matcher matcher = pureCallPattern.matcher(s);
                boolean b = matcher.find();
                if (! b) return null;
                return s.substring(0, matcher.end());
            }

            @Override
            public String getName() {
                return "PureCallRule";
            }
        }

        /**
         * 纯函数调用链
         * eg:
         * dfs(1,2).a √
         * dfs(1,2).demo() √
         * <p>
         * dfs(1,2) x
         */
        public static class PureCallRuleChain extends AbstractRule implements RuleChain {

            public PureCallRuleChain() {
                super(PureCallTokenChain.class);
            }

            @Override
            public boolean match(String s) {
                Matcher matcher = pureCallPattern.matcher(s);
                if (! matcher.find()) return false;
                int end = matcher.end();
                if (end >= s.length()) return false;
                return s.charAt(end) == '.';
            }

            @Override
            public String matchFromStart(String s) {
                if (! match(s)) return null;
                String dummyS = "." + s;
                int end = CharacterHelper.matchChain(dummyS, 0);
                return dummyS.substring(1, end);
            }

            @Override
            public String getName() {
                return "PureCallRuleChain";
            }
        }

        /**
         * 匹配VariableToken的规则
         */
        public static class VariableRule extends AbstractRule {
            public VariableRule() {
                super(VariableToken.class);
            }

            @Override
            public boolean match(String s) {
                return CharacterHelper.isVName(s);
            }

            @Override
            public String matchFromStart(String s) {
                int len = CharacterHelper.startVNameLen(s);
                return len == 0 ? null : s.substring(0, len);
            }

            @Override
            public String getName() {
                return "VariableRule";
            }
        }

        /**
         * 匹配符合变量链式法则的内容: 要求字符串以合法的变量开始, 并且含有调用符号'.'
         * eg:
         * ---- 正确的 ------
         * a.test
         * a.test.b
         * a.test.arr[0]
         * a.test.dfs()
         * a.test()
         * a.arr[0]
         * ---- 错误的 ------
         * arr[0].test
         * dfs().test
         */
        public static class VariableRuleChain extends AbstractRule implements RuleChain {

            public VariableRuleChain() {
                super(VariableTokenChain.class);
            }

            @Override
            public boolean match(String s) {
                int len = CharacterHelper.startVNameLen(s);
                /*
                len = 0, 表示s开头不存在变量名字
                s.length() == len, 表示s只存在一个变量, 不存在链式调用
                 */
                if (len == 0 || s.length() == len) {
                    return false;
                }
                if (s.charAt(len) != '.') {
                    return false;
                }
                int cnt = CharacterHelper.getChainCnt(s, len);
                return cnt > 1;
            }

            @Override
            public String matchFromStart(String s) {
                if (! match(s)) return null;
                String dummyS = "." + s;
                int end = CharacterHelper.matchChain(dummyS, 0);
                return dummyS.substring(1, end);
            }

            @Override
            public String getName() {
                return "VariableRuleChain";
            }
        }

        /**
         * 匹配ArrayRule的规则. 只匹配数组, 不包含其他任何内容
         * eg:
         * --- 正确的 ---
         * arr[0]
         * arr[1][0]
         * arr[a.test][0]
         * <p>
         * --- 错误的 ---
         * arr[0].a
         * arr[0].test(0)
         */
        public static class ArrayRule extends AbstractRule {
            public ArrayRule() {
                super(ArrayToken.class);
            }

            public boolean match(String s) {
                // arrayPattern可以匹配arr[0][0] + 1, 因此需要做出额外判断
                Matcher matcher = arrayPattern.matcher(s);
                boolean b = matcher.find();
                if (! b) return false;
                int end = matcher.end();
                return end >= s.length();
            }

            @Override
            public String matchFromStart(String s) {
                Matcher matcher = arrayPattern.matcher(s);
                if (! matcher.find()) return null;
                int end = matcher.end();
                return s.substring(0, end);
            }

            @Override
            public String getName() {
                return "ArrayRule";
            }
        }

        /**
         * 匹配数组, 且数组后续内容是链式调用
         * eg:
         * --- 正确的 ---
         * arr[0].abab
         * arr[1][0].b[0][0]
         * arr[a.test][0].invoke()
         * <p>
         * --- 错误的 ---
         * arr[0]
         * arr[0][1]
         */
        public static class ArrayRuleChain extends AbstractRule implements RuleChain {

            public ArrayRuleChain() {
                super(ArrayTokenChain.class);
            }

            @Override
            public boolean match(String s) {
                Matcher matcher = arrayPattern.matcher(s);
                if (! matcher.find()) return false;
                int end = matcher.end();
                if (end >= s.length()) return false;
                return s.charAt(end) == '.';
            }

            @Override
            public String matchFromStart(String s) {
                if (! match(s)) return null;
                String dummyS = "." + s;
                int end = CharacterHelper.matchChain(dummyS, 0);
                return dummyS.substring(1, end);
            }

            @Override
            public String getName() {
                return "ArrayRuleChain";
            }
        }

        /**
         * 匹配常量的规则
         * 字符串必须只存在常量才会被匹配
         */
        public static class ConstantRule extends AbstractRule {
            public ConstantRule() {
                super(ConstantToken.class);
            }

            @Override
            public boolean match(String s) {
                String match = matchFromStart(s);
                // 如果返回的match的长度等于s, 表示s是完全由常量组成
                return match.length() == s.length();
            }

            @Override
            public String matchFromStart(String s) {
                char[] arr = s.toCharArray();
                int i = 0;
                char c = arr[i];
                if (c == '"') {
                    // 去除""内部的所有信息,包括匹配的另一个"
                    int j = i + 1;
                    while (j < arr.length && arr[j] != '"') {
                        ++j;
                    }
                    return j == arr.length ? s: s.substring(0, j + 1);
                } else if (c == '\'') {
                    // 去除''内部的所有信息,包括匹配的另一个'
                    int j = i + 1;
                    while (j < arr.length && arr[j] != '\'') {
                        ++j;
                    }
                    return j == arr.length ? s: s.substring(0, j + 1);
                } else if (CharacterHelper.isArabicNumber(c)) {
                    // 如果是数字
                    int j = i + 1;
                    while (j < arr.length && CharacterHelper.isArabicNumber(c)) {
                        ++j;
                    }
                    return j == arr.length ? s: s.substring(0, j + 1);
                }
                return null;
            }

            @Override
            public String getName() {
                return "ConstantRule";
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
                new EvalRule(),
                new OperatorRule(),
                new InvokeRule(),
                new InvokeRuleChain(),
                new PureCallRule(),
                new PureCallRuleChain(),
                new VariableRule(),
                new VariableRuleChain(),
                new ArrayRule(),
                new ArrayRuleChain(),
                new ConstantRule()
        };

        /**
         * 解析字符串得到对应的Token
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

            List<Rule> matches = new ArrayList<>(5);

            // 根据解析规则解析得到不同的Token
            for (Rule rule : rules) {
                boolean match = rule.match(s);
                if (match) {
                    matches.add(rule);
                }
            }

            // 判断
            if (matches.isEmpty()) {
                throw new ComputeError("无法识别的内容! " + s);
            }
            if (matches.size() > 1) {
                throw new ComputeError(s + " 被多条规则匹配! 请检查规则编写是否正确! + " + matches.stream().map(Rule::getName).collect(Collectors.joining()));
            }

            Rule targetRule = matches.get(0);

            Class<? extends Token> tokenClass = targetRule.tokenClass();
            String className = tokenClass.getName();
            try {
                return tokenClass.getDeclaredConstructor(String.class, Context.class).newInstance(s, ctx);
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

        public static class Hit2 {
            public final Rule rule;
            public final String token;

            public Hit2(Rule rule, String token) {
                this.rule = rule;
                this.token = token;
            }
        }

        /**
         * 从字符的起始位置解析, 并得到对应的Token. 该方法区别于{@link #parseToToken(String, Context)}, 只需要局部满足规则
         * 则视为匹配成功
         * eg:
         * a.demo() + 1: 从开始位置匹配, 将会被InvokeRule识别, 得到a.demo()
         * arr[0].b + 2: 从头开始匹配, 将会被ArrayRuleChain识别, 得到arr[0].b
         * 1 + 2 + 3: 从头开始匹配, 将会被ConstantRule识别, 得到1
         * + 2 + 3: 从头开始匹配, 将会被OperatorRule识别, 得到+
         *
         * @param token token
         * @param ctx ctx
         * @return Token
         * @throws NoSuchMethodException 方法找不到
         * @throws InvocationTargetException invoke 异常
         * @throws InstantiationException 实例化异常
         * @throws IllegalAccessException 非法访问
         */
        public Token parseToTokenFromStart(String token, Context ctx) {
            token = handleInput(token);
            List<Hit2> hits = new ArrayList<>(5);

            // 处理s
            String s = handleInput(token);

            // 根据解析规则解析得到不同的Token
            for (Rule rule : rules) {
                String match = rule.matchFromStart(token);
                hits.add(new Hit2(rule, match));
            }

            // 判断
            if (hits.isEmpty()) {
                throw new ComputeError("无法识别的内容! " + s);
            }
            // double check
            Hit2 targetHit = null;
            if (hits.size() > 1) {
                throw new ComputeError(s + " 被多条规则匹配! 请检查规则编写是否正确! + " + hits.stream().map(e -> e.rule.getName()).collect(Collectors.joining()));
            }
            targetHit = hits.get(0);

            Class<? extends Token> tokenClass = targetHit.rule.tokenClass();
            String className = tokenClass.getName();
            try {
                return tokenClass.getDeclaredConstructor(String.class, Context.class).newInstance(s, ctx);
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
         * @return Token
         * @throws NoSuchMethodException 方法找不到
         * @throws InvocationTargetException invoke 异常
         * @throws InstantiationException 实例化异常
         * @throws IllegalAccessException 非法访问
         */
        public Token parseToTokenFromStart(String token) {
            return parseToTokenFromStart(token, null);
        }
    }

}
