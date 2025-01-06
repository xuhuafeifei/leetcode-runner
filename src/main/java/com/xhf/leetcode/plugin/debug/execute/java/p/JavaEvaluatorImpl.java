package com.xhf.leetcode.plugin.debug.execute.java.p;

import com.sun.jdi.*;
import com.xhf.leetcode.plugin.debug.execute.java.Context;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.DebugError;
import com.xhf.leetcode.plugin.utils.LogUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        private static Map<LocalVariable, Value> localEnv;
        private static StackFrame frame;
        private static ThreadReference thread;
        private static Context ctx;
        private static VirtualMachine vm;
        private static JavaValueInspector inspector;

        /**
         * 获取当前环境存在的所有变量
         * 目前只返回LocalEnv
         * @return 当前环境存在的所有变量
         */
        public static Map<String, Value> getEnvValues() {
            return DebugUtils.convert(localEnv);
        }

        public static Map<LocalVariable, Value> getLocalEnv() {
            return localEnv;
        }

        public static void setLocalEnv(Map<LocalVariable, Value> localEnv) {
            Env.localEnv = localEnv;
        }

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
            // 获取所有变量
            List<LocalVariable> localVariables = frame.visibleVariables();
            localEnv = frame.getValues(localVariables);
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
            throw new DebugError(e.getMessage());
        }
    }


    /**
     * Token, 识别用户提供的表达式, 同时封装不同类型表达式的计算逻辑
     */
    public interface Token {
        String getToken();

        Value getValue();
    }

    public abstract static class  AbstractToken implements Token {
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
         * @throws DebugError 如果括号不匹配，则抛出异常
         */
        public static void doCheck(String expression) {
            // 检查括号是否匹配
            if (!areParenthesesAndBracketsBalanced(expression)) {
                throw new DebugError("括号没有成对出现!!");
            }
            // 检查是否存在+=等赋值语句
            if (expression.contains("+=") || expression.contains("-=") || expression.contains("*=") || expression.contains("/=") || expression.contains("%=") || expression.contains("&=") || expression.contains("|=") || expression.contains("^=") || expression.contains("<<=") || expression.contains(">>=") || expression.contains(">>>=")) {
                throw new DebugError("不允许使用 += -= *= /= %= &= |= ^= <<= >>= >>>= 等赋值语句!!");
            }
            // 判断是否存在++, --等自变化操作
            if (expression.contains("++") || expression.contains("--")) {
                throw new DebugError("不允许使用 ++ -- 等自变化操作!!");
            }
            // 判断是否存在链式调用
            if (Pattern.compile("\\b[a-zA-Z_$][a-zA-Z0-9_$]*(\\[[^\\]]*\\])*\\.\\b[a-zA-Z_$][a-zA-Z0-9_$]*\\(.*\\)(\\.\\b[a-zA-Z_$][a-zA-Z0-9_$]*\\(.*\\))+").matcher(expression).find()) {
                throw new DebugError("不允许使用链式调用!!");
            }
        }

        /**
         * 检查表达式中的括号和方括号是否匹配
         *
         * @param expression 表达式
         * @return 如果括号和方括号都匹配，返回true，否则返回false
         */
        private static boolean areParenthesesAndBracketsBalanced(String expression) {
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
         * @param vName
         * @return
         */
        protected Value takeValueByVName(String vName) {
            Value v = null;
            Map<String, Value> convert = Env.getEnvValues();
            for (Map.Entry<String, Value> next : convert.entrySet()) {
                if (next.getKey().equals(vName)) {
                    v = next.getValue();
                    break;
                }
            }
            if (v == null) {
                throw new DebugError("变量未定义 name = " + vName);
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
            throw new DebugError("不支持自动转换类型, 请检查入参类型是否正确! v = " + v.type().name() + " type = " + type.name());
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
                        Env.inspector.getWrapperTypeByPrimitiveTypeName(pType) /*-----获取基本类型对应的包装类型------*/
                ).get(0);
                Method constructor = wrapperClass.methodsByName("<init>").get(0);
                // 创建objRef
                return wrapperClass.newInstance(ctx.getThread(), constructor, Collections.singletonList(primitiveValue), 0);
            }
            throw new RuntimeException("该方法只接受基本类型, 但传入的值为" + value.type().name());
        }
    }

    /**
     * 计算类型的Token
     */
    public static class EvalToken extends AbstractToken {

        public EvalToken(String token, Context ctx) {
            super(token, ctx);
        }

        @Override
        public Value getValue() {
            // 去除最外层括号
            return null;
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
        private int dot;

        @Override
        public Value getValue() {
            // 找到最外层调用方法的变量名
            Value callV;
            try {
                callV = getCallVariable();
            } catch (Exception e) {
                LogUtils.error(e);
                throw new DebugError(e.toString());
            }
            // 定位方法名字
            Method method = getMethod(callV);
            // 触发invokeMethod
            return doInvoke(callV, method);
        }

        private Value doInvoke(Value callV, Method method) {
            Context ctx = Env.getCtx();
            // invokeMethod开始执行
            ctx.invokeMethodStart();
            Value resultV = null;
            try {
                resultV = ((ObjectReference) callV).invokeMethod(ctx.getThread(), method, params, 0);
            } catch (Exception e) {
                LogUtils.error(e);
                throw new DebugError("invokeMethod调用错误! " + e);
            }
            // invokeMethod执行结束
            ctx.invokeMethodDone();
            return resultV;
        }

        private Method getMethod(Value callV) {
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
                throw new DebugError(callVName + "不存在" + methodName + "方法");
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
                throw new DebugError(callVName + "不存在适配表达式的" + methodName + "方法");
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
                        LogUtils.error(e);
                        throw new DebugError(e.toString());
                    }
                } catch (Exception e) {
                    LogUtils.error(e);
                    throw new DebugError(e.toString());
                }
            } else {
                // todo: 以后支持一下重载类型
                throw new DebugError("暂不支持方法重载");
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
                    throw new DebugError("变量类型错误, 变量类型和入参类型不匹配!\n 变量" + vName + "类型为" + v.type().name() + ", 入参类型为" + type.name());
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
                            LogUtils.error(ex);
                            throw new DebugError(ex.toString());
                        }
                    }
                }

            } catch (Exception ex) {
                LogUtils.error(ex);
                throw new DebugError(ex.toString());
            }
        }

        private String getClassName(Type type) {
            if (type instanceof ArrayType) {
                return ((ArrayType) type).componentTypeName() + "[]";
            } else if (type instanceof ReferenceType) {
                return type.name();
            }
            throw new DebugError("未识别的type类型: " + type.name());
        }

        private List<Value> getParams(String params) {
            this.paramNames = params.split(",");
            List<Value> values = new ArrayList<>();
            for (int i = 0; i < paramNames.length; i++) {
                try {
                    values.add(tf.parseToToken(paramNames[i], ctx).getValue());
                } catch (Exception e) {
                    throw new DebugError(callVName + "的第" + i + "个参数解析错误! cause is " + e.getMessage());
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
                throw new DebugError("代码编写有误! InvokeToken无法定位调用方法的变量名! token = " + token);
            }
            return dot;
        }

        private Value getCallVariable() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
            this.callVName = getCallVName();
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
     * Variable类型的Token
     */
    public static class VariableToken extends AbstractToken {

        public VariableToken(String token, Context ctx) {
            super(token, ctx);
        }

        @Override
        public Value getValue() {
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
                throw new DebugError("变量 " + vName + " 不是数组类型");
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
                throw new DebugError("数组没有维度!");
            }
            List<Integer> dims = new ArrayList<>();
            for (String s : dimsStr) {
                try {
                    // 获取索引数值
                    Token t = tf.parseToToken(s, ctx);
                    Value vv = t.getValue();
                    String typeName = vv.type().name();
                    if (!Objects.equals(typeName, "int")) {
                        throw new DebugError("数组索引必须为整型!!");
                    }
                    int value = ((IntegerValue) vv).value();
                    dims.add(value);
                } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                         IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
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
                    throw new DebugError("数组越界!! 数组第" + i + "维度长度=" + array.length() + ", 索引=" + dim);
                }
                value = array.getValue(dim);
                // 如果没有迭代到最后一位, 则判断是否是数组类型
                if (i != dims.size() - 1) {
                    if (!(value instanceof ArrayReference)) {
                        throw new DebugError("变量" + vName + "第 " + i + " 维度不是数组类型, 是" + value.type().name() + "类型!!");
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
            final Rule rule;
            final Matcher matcher;
            final int start;
            final int end;
            final int length;

            public Hit(Rule rule, Matcher matcher, int start, int end, int length) {
                this.rule = rule;
                this.matcher = matcher;
                this.start = start;
                this.end = end;
                this.length = length;
            }
        }

        Rule[] rules = new Rule[]{
                // 匹配计算类型的Token
                new Rule(Pattern.compile("[\\+\\-\\*\\/\\%&\\|\\^\\~\\<\\>\\!]"), EvalToken.class),
                // 匹配invoke类型的Token
                new Rule(Pattern.compile("\\b[a-zA-Z_$][a-zA-Z0-9_$]*(\\[[^\\]]*\\])*\\.\\b[a-zA-Z_$][a-zA-Z0-9_$]*\\(.*\\)"), InvokeToken.class),
                // 匹配纯粹的变量
                new Rule(Pattern.compile("\\b[a-zA-Z_$][a-zA-Z0-9_$]*\\b(?!\\S)"), VariableToken.class),
                // 匹配数组
                new Rule(Pattern.compile("\\b[a-zA-Z_$][a-zA-Z0-9_$]*(\\[[^\\]]+\\])+$"), ArrayToken.class),
                // 匹配常量(数值、字符、字符串)
                new Rule(Pattern.compile("-?\\d+(\\.\\d+)?|\"([^\"\\\\]|\\\\.)*\"|\'([^\'\\\\]|\\\\.)*\'"), ConstantToken.class)
        };

        /**
         * 解析字符串得到对应的Token
         * <p>
         * 该方法会贪婪的匹配所有正则规则, 得到Hits数组. 从Hits中选取匹配得到最长的规则作为targetRule, 返回的对应的Token
         * <p>
         * 在匹配过程中, 如果匹配得到含有EvalToken.class的Rule, 则会进行二次检查. 只有判断出运算符号
         * 并没有囊括在其他类型表达式中时, 则认为该Token为EvalToken.class. 否则表达将会从剩余Hit中
         * 选择最长匹配的规则作为targetRule, 返回对应Token
         *
         * @param s 表达式
         * @return token
         * @throws NoSuchMethodException 方法找不到
         * @throws InvocationTargetException invoke 异常
         * @throws InstantiationException 实例化异常
         * @throws IllegalAccessException 非法访问
         */
        public Token parseToToken(String s, Context ctx) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
            // 处理s
            s = s.trim();
            // 判断是否存在外层括号
            if (s.startsWith("(") && s.endsWith(")")) {
                s = s.substring(1, s.length() - 1);
            }
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
                    hits.add(new Hit(rule, matcher, start, end, matcher.end() - matcher.start()));
                }
            }

            if (hits.isEmpty()) {
                throw new DebugError("无法识别的表达式: " + s);
            }

            // double check
            // 包含EvalToken
            Rule targetRule = null;
            if (hits.stream().anyMatch(hit -> hit.rule.clazz == EvalToken.class)) {
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
                if (diff[EvalHit.start] == 0 && diff[EvalHit.end] == 0) {
                    targetRule = EvalHit.rule;
                } else {
                    Matcher m = EvalHit.matcher;
                    while (m.find()) {
                        if (m.start() == EvalHit.start && m.end() == EvalHit.end) {
                            targetRule = EvalHit.rule;
                            break;
                        }
                    }
                }
                // 从剩余元素中选择最大值
                if (targetRule == null) {
                    // 过滤EvalToken
                    targetRule = hits.stream().filter(e -> !e.rule.clazz.equals(EvalToken.class)).max(Comparator.comparingInt(hit -> hit.length)).get().rule;
                }
            } else {
                // 获取匹配长度最大的那一个
                targetRule = hits.stream().max(Comparator.comparingInt(hit -> hit.length)).get().rule;
            }

            return targetRule.clazz.getDeclaredConstructor(String.class, Context.class).newInstance(s, ctx);
        }

        /**
         *
         * @param token 表达式
         * @return 返回Token
         */
        public Token parseToToken(String token) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
            return parseToToken(token, null);
        }
    }

}
