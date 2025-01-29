package com.xhf.leetcode.plugin.debug.execute.java.p;

import com.sun.jdi.*;
import com.xhf.leetcode.plugin.debug.execute.java.Context;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.exception.ComputeError;
import com.xhf.leetcode.plugin.search.utils.CharacterHelper;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.MapUtils;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.jexl3.internal.Engine;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.xhf.leetcode.plugin.debug.execute.java.p.JavaEvaluatorImpl.TokenFactory.AbstractRule.arrayPattern;
import static com.xhf.leetcode.plugin.debug.execute.java.p.JavaEvaluatorImpl.TokenFactory.AbstractRule.pureCallPattern;

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
        /**
         * TargetVM当前执行栈帧中的局部变量
         */
        private static Map<String, Value> localEnv;
        /**
         * TargetVM当前执行栈帧对应的thisObject中存在的成员变量
         */
        private static Map<String, Value> memberEnv;
        /**
         * TargetVM当前执行栈帧对应的thisObject中存在的静态变量
         */
        private static Map<String, Value> staticEnv;
        /**
         * TargetVM执行的线程引用
         */
        private static ThreadReference thread;
        private static Context ctx;
        private static VirtualMachine vm;
        private static JavaValueInspector inspector;
        /**
         * thisObject
         */
        private static ObjectReference _this;

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

        public static void setVm(VirtualMachine vm) {
            Env.vm = vm;
        }

        public static void setInspector(JavaValueInspector inspector) {
            Env.inspector = inspector;
        }

        public static void doPrepare() throws IncompatibleThreadStateException, AbsentInformationException {
            // 获取当前栈帧
            thread = ctx.getThread();
            StackFrame frame = thread.frame(0);
            _this = frame.thisObject();
        }

        /**
         * 获取当前栈帧的所有变量
         * @param frame frame
         * @return 返回变量池
         * @throws AbsentInformationException 行号或者变量信息不可访问
         */
        public static Map<String, Value> takeLocalVariable(StackFrame frame) throws AbsentInformationException {
            List<LocalVariable> localVariables = frame.visibleVariables();
            Map<LocalVariable, Value> values = frame.getValues(localVariables);
            return DebugUtils.convert(values);
        }

        public static Map<String, Value> getLocalEnv() throws IncompatibleThreadStateException, AbsentInformationException {
            return takeLocalVariable(ctx.getThread().frame(0));
        }

        /**
         * 通过Value获取他的成员变量, 并转化为环境数据. Value必须是ObjectReference
         * @param v Value
         * @return 变量池
         */
        public static Map<String, Value> takeMemberValues(Value v) {
            if (v == null) {
                return MapUtils.emptyMap();
            }
            if (! (v instanceof ObjectReference)) {
                throw new ComputeError("方法使用错误! 只有ObjectReference才会拥有成员变量! type = " + v.type());
            }
            ObjectReference objRef = (ObjectReference) v;
            List<Field> fields = objRef.referenceType().fields().stream().filter(e -> !e.isStatic()).collect(Collectors.toList());
            Map<Field, Value> values = objRef.getValues(fields);
            return DebugUtils.convert2(values);
        }

        public static Map<String, Value> getMemberEnv() {
            return takeMemberValues(_this);
        }

        /**
         * 从referenceType中获取静态变量
         * @param referenceType 引用类型
         * @return 变量池
         */
        private @NotNull static Map<String, Value> takeStaticValues(ReferenceType referenceType ) {
            if (referenceType == null) {
                return MapUtils.emptyMap();
            }
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

        /**
         * 从v中获取静态变量, v必须是ObjectReference
         * @param v Value
         * @return map
         */
        private @NotNull static Map<String, Value> takeStaticValues(Value v) {
            if (v == null) {
                return MapUtils.emptyMap();
            }
            if (v instanceof ObjectReference) {
                ReferenceType referenceType = ((ObjectReference) v).referenceType();
                return takeStaticValues(referenceType);
            }
            return new HashMap<>();
        }

        private static Map<String, Value> getStaticValues() {
            return takeStaticValues(ctx.getLocation().declaringType());
        }
    }

    /**
     * 计算工具
     */
    public static class ComputeEngine {
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
     * @param expression 计算表达式
     * @param context context
     * @return 计算结果
     */
    @Override
    public String executeExpression(String expression, Context context) {
        Env.setVm(context.getVm());
        Env.setCtx(context);

        try {
            AbstractToken.doCheck(expression);
            Env.doPrepare();
            return Env.inspector.inspectValue(TokenFactory.getInstance().parseToToken(expression, context).getValue());
        } catch (ComputeError e) {
            // 把他变成字符串
            String err = DebugUtils.getStackTraceAsString(e);
            LogUtils.warn(err);
            throw new ComputeError(e.getMessage());
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
         * 当前Token是否是链式调用的最后一个Token
         * @param token token
         * @return boolean
         */
        boolean isLastChain(String token);

        /**
         * 链式调用法则, 通过token前一个调用链封装的到的PValue作为当前链式调用的调用方
         * @param pV 链式调用前一个链式计算结果
         * @return 当前第一个链式执行结果
         */
        Value getValue(PValue pV);
    }

    /**
     * 封装链式调用的前一个调用者的信息
     */
    public static class PValue {
        /**
         * 前一个调用者的Value
         */
        final Value value;
        /**
         * 前一个调用者的name
         */
        final String preName;

        public PValue(Value value, String preName) {
            this.value = value;
            this.preName = preName;
        }
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
         * @param vName 变量名
         * @return 变量对应的Value
         */
        protected Value takeValueByVName(String vName) {
            TValue v = null;
            try {
                v = takeValueByVName(vName, Env.getLocalEnv());
            } catch (IncompatibleThreadStateException | AbsentInformationException e) {
                throw new ComputeError(e.getMessage());
            }
//            if (v != null) {
//                return v;
//            }
//            v = takeValueByVName(vName, Env.getMemberEnv());
//            if (v != null) {
//                return v;
//            }
//            v = takeValueByVName(vName, Env.getStaticValues());
            if (! v.contain) {
                throw new ComputeError("变量" + vName + "未定义!");
            }
            return v.value;
        }

        /**
         * take value, 封装takeValue方法返回的数据结果
         */
        public static class TValue {
            public Value value;
            public boolean contain;

            public TValue(Value v, boolean b) {
                this.value = v;
                contain = b;
            }

            public static TValue notFound() {
                return new TValue(null, false);
            }

            public static TValue found(Value v) {
                return new TValue(v, true);
            }
        }

        /**
         * 从数据源中获取名为vName的变量
         * @param vName vName
         * @param dataSource 数据源
         * @return value
         */
        protected TValue takeValueByVName(String vName, Map<String, Value> dataSource) {
            Value v;
            for (Map.Entry<String, Value> next : dataSource.entrySet()) {
                if (next.getKey().equals(vName)) {
                    v = next.getValue();
                    return TValue.found(v);
                }
            }
            return TValue.notFound();
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
         * @param v 变量Value
         * @param type 方法入参类型
         * @return 是否匹配
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
         * @return v和type类型不同且是对应的包装类型/基本类型, 或者基本类型/包装类型
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
            throw new ComputeError("方法编写错误! 不支持自动转换类型, 请检查入参类型是否正确! v = " + v.type().name() + " type = " + type.name());
        }

        /**
         * 包装类转换为基本类型
         * @param value value
         * @return 转换后的Value
         */
        protected Value WrapperObjReferenceToPrimitive(Value value) {
            if (value instanceof ObjectReference) {
                ObjectReference objRef = (ObjectReference) value;
                if (Env.inspector.isWrapperType(objRef)) {
                    return Env.inspector.getWrapperValue(objRef);
                } else {
                    // todo: 抛出异常, 方法使用错误! 该方法只接受包装类型
                    throw new ComputeError("该方法只接受包装类型, 但传入的值为" + value.type().name());
                }
            }
            throw new ComputeError("该方法只接受包装类型, 但传入的值为" + value.type().name());
        }

        /**
         * 基本类型的Value转换为包装类型Ref
         * 比如IntegerValue -> ObjectReferenceImpl
         * @param value Value
         * @return Value
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
            return name.equals("int") || name.equals("long") || name.equals("float") || name.equals("double") ||
                    name.equals("java.lang.Integer") || name.equals("java.lang.Long") ||
                    name.equals("java.lang.Float") || name.equals("java.lang.Double");
        }
    }

    /**
     * 计算类型的Token. 支持String类型的计算
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
                    Value v = t.getValue();
                    if (t instanceof  OperatorToken) {
                        // 运算符需要去除引号
                        sb.append(DebugUtils.removeQuotes(Env.inspector.inspectValue(v)));
                    } else {
                        if (!super.isNumberValue(v) && !(v instanceof StringReference)) {
                            throw new ComputeError(t.getToken() + "的结果值既不是数值类型, 也不是String类型, 无法参与计算! type = " + v.type().name());
                        }
                        sb.append(Env.inspector.inspectValue(v));
                    }

                    // skip已经匹配过的内容
                    i += t.getToken().length() - 1; // -1是为了应对下一轮循环前的i++
                }
            }


            Object execute = ComputeEngine.execute(sb.toString());
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
                case "java.lang.String":
                    return ctx.getVm().mirrorOf((String) execute);
                default:
                    throw new ComputeError("无法识别的计算结果类型! 计算结果 = " + execute + " 结果类型 = " + className);
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
        public InvokeToken(String token, Context ctx) {
            super(token, ctx);
        }

        public static class InvokeInfo {
            /**
             * 方法调用的变量名
             */
            public String callVName;
            /**
             * 调用方法需要使用的参数变量
             */
            public List<Value> params;
            /**
             * 方法名字
             */
            public String methodName;
            /**
             * 调用方法使用参数的名字
             */
            public @Nullable String[] paramNames;
            /**
             * 被解析的表达式
             */
            public String exp;

            @Override
            public String toString() {
                return "InvokeInfo{" +
                        "callVName='" + callVName + '\'' +
                        ", params=" + params +
                        ", methodName='" + methodName + '\'' +
                        ", paramNames=" + Arrays.toString(paramNames) +
                        ", exp='" + exp + '\'' +
                        '}';
            }
        }

        @Override
        public Value getValue() {
            // 找到最外层调用方法的变量名
            Value callV;
            InvokeInfo info;
            try {
                info = parseForInvokeInfo();
                callV = getCallVariable(info);
            } catch (Exception e) {
                throw new ComputeError(e.toString());
            }
            // 定位方法名字
            Method method = getMethod(callV, info);
            // 触发invokeMethod
            return doInvoke(callV, method, info);
        }

        public InvokeInfo parseForInvokeInfo() {
            return parseForInvokeInfo(token);
        }

        /**
         * 解析token, 并返回调用信息
         * token只能是方法调用或者纯函数调用类型, 且不能是链式调用
         * <p>
         * eg:
         * ----- 正确的 -----
         * a.invoke()
         * invoke()
         * ----- 错误的 -----
         * a.invoke().b
         * invoke().b
         *
         * @param token token
         * @return info
         */
        public InvokeInfo parseForInvokeInfo(String token) {
            if (! new TokenFactory.InvokeRule().match(token) && ! new TokenFactory.PureCallRule().match(token)) {
                throw new ComputeError("parseForInvokeInfo方法只能识别方法调用, 或者纯函数调用类型, 方法使用错误! token = " + token);
            }
            InvokeInfo info = new InvokeInfo();
            // 获取dot
            int dot = getDot(token);
            // 解析调用者的名字(适配纯函数调用: PureCallToken)
            if (dot != -1) {
                info.callVName = token.substring(0, dot);
            } else {
                info.callVName = "this";
            }
            // 解析方法名 + 方法参数
            {
                // 去除调用的变量名
                String suffix = token.substring(dot + 1);
                // 定位第一个(
                int start = suffix.indexOf("(");
                int last = suffix.lastIndexOf(")");
                // 方法名字
                info.methodName = suffix.substring(0, start);
                String params = suffix.substring(start + 1, last);
                info.paramNames = StringUtils.isBlank(params) ? null : params.split(",");
                info.params = info.paramNames == null ? Collections.emptyList() :getParams(info.paramNames);
            }
            info.exp = token;
            return info;
        }

        /**
         * 解析token, 并返回调用信息
         * token只能是方法调用或者纯函数调用类型, 且不能是链式调用
         * <p>
         * 如果是纯函数调用, info.callVName 赋值为前一个链式调用的信息
         *
         * @param token token
         * @param pValue pValue
         * @return InvokeInfo
         */
        public InvokeInfo parseForInvokeInfo(String token, @NotNull PValue pValue) {
            if (! new TokenFactory.InvokeRule().match(token) && ! new TokenFactory.PureCallRule().match(token)) {
                throw new ComputeError("parseForInvokeInfo方法只能识别方法调用, 或者纯函数调用类型, 方法使用错误! token = " + token);
            }
            InvokeInfo info = new InvokeInfo();
            // 获取dot
            int dot = getDot(token);
            // 解析调用者的名字(适配纯函数调用: PureCallToken)
            if (dot != -1) {
                info.callVName = token.substring(0, dot);
            } else {
                info.callVName = pValue.preName;
            }
            // 解析方法名 + 方法参数
            {
                // 去除调用的变量名
                String suffix = token.substring(dot + 1);
                // 定位第一个(
                int start = suffix.indexOf("(");
                int last = suffix.lastIndexOf(")");
                // 方法名字
                info.methodName = suffix.substring(0, start);
                String params = suffix.substring(start + 1, last);
                info.paramNames = StringUtils.isBlank(params) ? null : params.split(",");
                info.params = info.paramNames == null ? Collections.emptyList() :getParams(info.paramNames);
            }
            info.exp = token;
            return info;
        }


        /**
         * invokeMethod
         * @param callV 调用Value
         * @param method 被调用的方法
         * @param info InvokeInfo
         * @return 方法返回结果
         */
        protected Value doInvoke(Value callV, Method method, InvokeInfo info) {
            Context ctx = Env.getCtx();
            // invokeMethod开始执行
            ctx.invokeMethodStart();
            Value resultV;
            try {
                resultV = ((ObjectReference) callV).invokeMethod(ctx.getThread(), method, info.params, 0);
            } catch (Exception e) {
                throw new ComputeError("invokeMethod调用错误! " + e);
            }
            // invokeMethod执行结束
            ctx.invokeMethodDone();
            return resultV;
        }

        /**
         * 从callV中获取method
         * @param callV 调用者
         * @param info InvokeInfo
         * @return Method
         */
        public Method getMethod(Value callV, InvokeInfo info) {
            String callVName = info.callVName;
            // 方法名字
            String methodName = info.methodName;
            // 寻找methodName
            Method method;
            if (! checkMethodExist(callVName, methodName, callV)) {
                throw new ComputeError(callVName + "不存在" + methodName + "方法");
            }
            // 判断重载
            method = overloadCheck(callV, info);

            return method;
        }

        /**
         * 处理重载, 目前不支持...
         * @param callV 调用者
         * @param info InvokeInfo
         * @return Method
         */
        private Method overloadCheck(Value callV, InvokeInfo info) {
            ObjectReference objRef = (ObjectReference) callV;
            Method targetMethod;
            List<Method> methods = objRef.referenceType().methods();
            // 候选方法
            List<Method> candidates = new ArrayList<>();
            // 获取Params
            List<Value> params = info.params;
            // 方法名
            String methodName = info.methodName;
            // 调用名
            String callVName = info.callVName;

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
                    matchValueAndType(targetMethod, info);
                } catch (ClassNotLoadedException e) {
                    // 手动类加载, 并重新匹配
                    loadClass(targetMethod);
                    try {
                        matchValueAndType(targetMethod, info);
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
        private void matchValueAndType(Method targetMethod, InvokeInfo info) throws ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, InvalidTypeException {
            List<Value> params = info.params;
            String[] paramNames = info.paramNames;

            List<Type> types = targetMethod.argumentTypes();
            if (!types.isEmpty() && (paramNames == null || paramNames.length != types.size()) ) {
                throw new ComputeError("方法解析错误! 解析得到的信息 = " + info + " 但实际方法所需参数 = " + types.stream().map(Type::name).collect(Collectors.joining(",")));
            }

            for (int i = 0; i < types.size(); i++) {
                Type type = types.get(i);
                // 尝试匹配并进行纠正
                Value matchedV = tryMatchValue(params.get(i), type, paramNames[i]);
                params.set(i, matchedV);
            }
        }

        /**
         * 尝试匹配value类型和方法的入参类型. 如果Value类型和type类型不一致, 会尝试纠正
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
         * @param method 方法
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

        private List<Value> getParams(String[] paramNames) {
            List<Value> values = new ArrayList<>();
            for (int i = 0; i < paramNames.length; i++) {
                try {
                    values.add(tf.parseToToken(paramNames[i], ctx).getValue());
                } catch (Exception e) {
                    throw new ComputeError(token + "的第" + i + "个参数" + paramNames[i] + "解析错误! cause is " + e.getMessage());
                }
            }
            return values;
        }

        /**
         * 获取函数调用的调用符号位置
         * @param token token 如: a.invoke(9)
         * @return dot位置
         */
        public int getDot(String token) {
            // 变量名\[ {匹配任意内容但不包括[]} \] {出现0次或者多次}  . 方法名()
            Pattern pattern = Pattern.compile("\\b[a-zA-Z_$][a-zA-Z0-9_$]*(\\[[^\\[\\]]*\\])*(\\.)[a-zA-Z_$][a-zA-Z0-9_$]*\\(.*\\)");
            Matcher matcher = pattern.matcher(token);
            int dot = -1;
            // 查找 "." 的位置
            if (matcher.find()){
                dot = matcher.start(2);
            }
            return dot;
        }

        public Value getCallVariable(InvokeInfo info) {
            String callVName = info.callVName;
            if ("this".equals(callVName)) {
                return Env._this;
            }
            return tf.parseToToken(callVName, ctx).getValue();
        }
    }

    /**
     * 纯函数调用Token, 匹配规则可见{@link TokenFactory.PureCallRule}
     * 不匹配调用方法的实例, 实例默认为self.
     * eg:
     * -------- 正确的 ----------
     * dfs(1,2,3)
     * -------- 错误的 ----------
     * a.dfs(1,2,3)
     * self.dfs(1,2,3)
     * <p>
     *
     * 对于dfs(1,2,3), 在系统执行时, 会默认调用者为this
     */
    public static class PureCallToken extends InvokeToken {

        public PureCallToken(String token, Context ctx) {
            super(token, ctx);
        }

        @Override
        public Value getValue() {
            return getValue(token);
        }

        protected Value getValue(String token) {
            // 因为是纯粹的方法调用, 所以callVName = this
            Value callV = Env._this;
            // 定位方法名字
            InvokeInfo info = super.parseForInvokeInfo(token);
            Method method = getMethod(callV, info);
            // 触发invokeMethod
            return doInvoke(callV, method, info);
        }
    }

    /**
     * 纯函数调用TokenChain, 匹配规则可见{@link TokenFactory.PureCallRuleChain}
     * 不匹配调用方法的实例, 且必须是链式调用
     * eg:
     * -------- 正确的 ----------
     * dfs(1,2,3).a
     * dfs(1,2,3).arr
     * dfs(1,2,3).invoke()
     * <p>
     * -------- 错误的 ----------
     * dfs(1,2,3)
     * a.dfs(1,2,3)
     * self.dfs(1,2,3)
     * self.dfs(1,2,3).invoke()
     * <p>
     *
     * 对于dfs(1,2,3), 在系统执行时, 会默认调用者为this
     */
    public static class PureCallTokenChain extends PureCallToken implements TokenChain {

        public PureCallTokenChain(String token, Context ctx) {
            super(token, ctx);
        }


        /**
         * 从token中截取pureCall: 如dfs(1).invoke(1) -> dfs(1)
         * @param token token
         * @return pureCall
         */
        private String getPureCall(String token) {
            TokenFactory.MyMatcher matcher = pureCallPattern.matcher(token);
            if (! matcher.find()) {
                throw new ComputeError("PureCallTokenChain无法识别token: " + token);
            }
            int end = matcher.end();
            return token.substring(0, end);
        }

        /**
         * 该方法无需考虑this.token是链式调用的结尾. 因为getValue()调用的时机决定了一定不会遇到最后的链式调用
         * getValue()总是在处理token的最开始调用, 因此不可能存在遇到链式调用的结尾
         * @return Value
         */
        @Override
        public Value getValue() {
            String pureCall = getPureCall(token);
            Value value = super.getValue(pureCall);
            // 链式调用
            String chain = token.substring(pureCall.length() + 1);
            TokenChain tokenChain = tf.parseToTokenChain(chain, ctx);
            return tokenChain.getValue(new PValue(value, pureCall));
        }

        @Override
        public boolean isLastChain(String token) {
            TokenFactory.MyMatcher matcher = pureCallPattern.matcher(token);
            if (! matcher.find()) {
                throw new ComputeError("PureCallTokenChain无法识别token: " + token);
            }
            int end = matcher.end();
            return end == token.length();
        }

        @Override
        public Value getValue(@NotNull PValue pValue) {
            if (pValue.value == null) {
                throw new ComputeError("链式调用的前一个调用" + pValue.preName + "为null, 链式表达式错误!");
            }
            String pureCall = getPureCall(token);
            InvokeInfo info = super.parseForInvokeInfo(pureCall, pValue);
            Value pV = pValue.value;

            if (! (pV instanceof ObjectReference) ) {
                throw new ComputeError("链式调用错误! 只有对象才拥有方法! pV = " + pV.type().name());
            }
            if (! isLastChain(token)) {
                Value value = super.doInvoke(pV, super.getMethod(pV, info), info);
                // 链式调用
                String chain = token.substring(pureCall.length() + 1);
                TokenChain tokenChain = tf.parseToTokenChain(chain, ctx);
                return tokenChain.getValue(new PValue(value, pureCall));
            } else {
                return super.doInvoke(pV, super.getMethod(pV, info), info);
            }
        }
    }

    /**
     * Variable类型的Token, 匹配规则详见{@link TokenFactory.VariableRule}
     */
    public static class VariableToken extends AbstractToken {

        public VariableToken(String token, Context ctx) {
            super(token, ctx);
        }

        @Override
        public Value getValue() {
            return getValue(token);
        }

        /**
         * 从当Env中找寻等于token的变量
         * @param token token
         * @return Value
         */
        public Value getValue(String token) {
            // 检查变量名. 如果包含.  比如this.head, 迭代获取
            if (token.contains(".")) {
                String[] split = token.split("\\.");
                // 迭代获取
                TValue v = null;
                String preName = split[0];
                for (int i = 1; i < split.length; i++) {
                    String vName = split[i];
                    if (Objects.equals(preName, "this")) {
                        v = takeValueByVName(vName, Env.getMemberEnv());
                    }else {
                        // md, 不支持通过.的方式获取持静态变量, 要不然太tm麻烦了
                        // 甚至我连静态变量都不想支持, 谁特么写leetcode吃饱了撑着用静态变量
                        v = takeValueByVName(vName, Env.takeMemberValues(v.value));
                    }
                }
                return v.value;
            }
            // 从变量env中获取
            if ("this".equals(token)) {
                return Env._this;
            }
            return super.takeValueByVName(token);
        }
    }

    /**
     * VariableTokenChain将顶替InvokeTokenChain, InvokeToken的功能
     * 匹配规则详见{@link TokenFactory.VariableRuleChain}
     */
    public static class VariableTokenChain extends VariableToken implements TokenChain {

        public VariableTokenChain(String token, Context ctx) {
            super(token, ctx);
        }

        @Override
        public boolean isLastChain(String token) {
            return CharacterHelper.isVName(token);
        }

        @Override
        public Value getValue(PValue pValue) {
            if (pValue.value == null) {
                throw new ComputeError("链式调用的前一个调用" + pValue.preName + "为null, 链式表达式错误!");
            }
            String vName = token.substring(0, CharacterHelper.startVNameLen(token));

            // 从pV中获取名为vName的变量值
            Value value = getVariableValue(vName, pValue);

            if (isLastChain(this.token)) {
                return value;
            } else {
                String chain = token.substring(vName.length() + 1);
                TokenChain tokenChain = tf.parseToTokenChain(chain, ctx);
                return tokenChain.getValue(new PValue(value, vName));
            }
        }

        /**
         * 从pValue中获取变量值
         * @param vName vName
         * @param pValue pValue
         * @return Value
         */
        private Value getVariableValue(String vName, PValue pValue) {
            Value pV = pValue.value;
            Map<String, Value> memberSource = Env.takeMemberValues(pV);
            TValue value = takeValueByVName(vName, memberSource);
            if (! value.contain) {
                value = takeValueByVName(vName, Env.takeStaticValues(pV));
                if (! value.contain) {
                    throw new ComputeError(pValue.preName + "不存在变量" + vName + "!");
                }
            }
            return value.value;
        }

        @Override
        public Value getValue() {
            int len = CharacterHelper.startVNameLen(token);
            String vName = token.substring(0, len);
            Value value = super.getValue(vName);
            // 从vName后开始为链式调用
            String chain = token.substring(vName.length() + 1);
            TokenChain tokenChain = tf.parseToTokenChain(chain, ctx);
            return tokenChain.getValue(new PValue(value, vName));
        }
    }

    /**
     * Array类型的Token, 规则详见{@link TokenFactory.ArrayRule}
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
            return getArrayValue(this.token);
        }

        public ArrayReference getArrayValue(String arrayId) {
            // 识别变量, 并获取array变量
            int idx = arrayId.indexOf("[");
            String vName = arrayId.substring(0, idx);
            Value v = takeValueByVName(vName);
            if (! (v instanceof ArrayReference) ) {
                throw new ComputeError("变量 " + vName + " 不是数组类型");
            }
            return (ArrayReference) v;
        }

        // 该方法用于测试, 请勿随意调用
        public List<String> getDimsStr() {
            return getDimsStr(this.token);
        }

        /**
         * 从token中解析维度信息
         * @param token token 形如: arr[1][2][3], arr[1], arr[a.invoke()][1+2], [1][2] 属于完全的数组类型, 不会包含除数组外的任何内容. 此外, 为了配合隐式调用, 支持只有数组括号内容的解析
         * @return 维度信息: 每一个维度的表达式: 如{1,2,3}; {1}, {a.invoke(), 1+2}, {1,2}
         */
        public List<String> getDimsStr(String token) {
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
         * 通过this.token获取维度信息
         * @return 维度信息: 每一个维度的整形数据
         */
        public List<Integer> getDims() {
            return getDims(this.token);
        }

        /**
         * 从arrayId中解析维度
         * @param arrayId 形如: arr[1][2][3], arr[1], arr[a.invoke()][1+2] 属于完全的数组类型, 不会包含除数组外的任何内容
         * @return 维度信息: 每一个维度的int索引数据
         */
        public List<Integer> getDims(String arrayId) {
            List<String> dimsStr = getDimsStr(arrayId);
            if (dimsStr.isEmpty()) {
                throw new ComputeError("数组没有维度!");
            }
            List<Integer> dims = new ArrayList<>();
            for (String s : dimsStr) {
                // 获取索引数值
                Token t = tf.parseToToken(s, ctx);
                Value vv = t.getValue();
                int value = getDimValue(vv);
                dims.add(value);
            }
            return dims;
        }

        /**
         * 将Value转换为索引数值
         * @param vv Value
         * @return int
         */
        private int getDimValue(Value vv) {
            String typeName = vv.type().name();
            int value;
            switch (typeName) {
                case "int":
                    value = ((IntegerValue) vv).value();
                    break;
                case "java.lang.Integer":
                    value = ((IntegerValue) WrapperObjReferenceToPrimitive(vv)).value();
                    break;
                case "long":
                    // 忽略精度损失. 谁吃饱了撑着拿个long型的作为索引, feigebuge能支持这种离谱的计算都算是仁慈了
                    value = (int) ((LongValue) vv).value();
                    break;
                case "java.lang.Long":
                    value = (int) ((LongValue) WrapperObjReferenceToPrimitive(vv)).value();
                    break;
                case "short":
                    value = ((ShortValue) vv).value();
                    break;
                case "java.lang.Short":
                    value = ((ShortValue) WrapperObjReferenceToPrimitive(vv)).value();
                    break;
                default:
                    throw new ComputeError("数组索引必须为整型!!");
            }
            return value;
        }

        /**
         * 迭代arrayReference
         * @param array arrayReference
         * @param dims 维度
         * @return Value
         */
        public Value itrArray(ArrayReference array, List<Integer> dims) {
            return itrArray(array, dims, getArrayIdentify(this.token));
        }

        public Value itrArray(ArrayReference array, List<Integer> dims, String arrayId) {
            Value value = null;
            // 迭代array
            for (int i = 0; i < dims.size(); i++) {
                int dim = dims.get(i);
                if (dim >= array.length()) {
                    throw new ComputeError("数组 " + arrayId + " 访问越界越界!! 数组第" + (i + 1) + "维度的元素个数=" + array.length() + ", 索引=" + dim);
                }
                if (dim <= -1) {
                    throw new ComputeError("数组索引必须为正数!! index = " + value);
                }
                value = array.getValue(dim);
                // 如果没有迭代到最后一位, 则判断是否是数组类型
                if (i != dims.size() - 1) {
                    if (!(value instanceof ArrayReference)) {
                        throw new ComputeError("变量" + arrayId + "第 " + i + " 维度不是数组类型, 是" + value.type().name() + "类型!!");
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

        protected Value getValue(String token) {
            // 获取array变量
            ArrayReference array = getArrayValue(token);
            // 获取索引
            List<Integer> dims = getDims(token);
            // 迭代所有的维度, 获取最终的value
            return itrArray(array, dims, token);
        }

        /**
         * 从token解析数组的标识
         * arr[1].invoke -> arr[1]
         * @param token token
         * @return 数组标识
         */
        private String getArrayIdentify(String token) {
            TokenFactory.MyMatcher matcher = arrayPattern.matcher(token);
            if (! matcher.find()) {
                throw new ComputeError("无法匹配数组, ArrayChainRule规则错误! " + token);
            }
            int end = matcher.end();
            return token.substring(0, end);
        }
    }

    /**
     * Rule: {@link TokenFactory.ArrayRuleChain}
     * 计算Array链式调用的结果
     */
    public static class ArrayTokenChain extends ArrayToken implements TokenChain {

        public ArrayTokenChain(String token, Context ctx) {
            super(token, ctx);
        }

        @Override
        public boolean isLastChain(String token) {
            boolean flag = isImplicitCall(token);
            String array = flag ? token.substring(0, CharacterHelper.matchArrayBracketSafe(token, 0)) : super.getArrayIdentify(token); // 判断是否为隐式调用
            int end = array.length();
            return end == token.length();
        }

        /**
         * 无需判断是否是链式调用结尾的情况. 该结论可以从以下两个角度解释
         * 1: 变量角度:
         *      如果是链式调用结尾, 必然存在Value pV, 也就是当前链式的调用方. 含有pV的方法是getValue(Value), 不是本方法, 因此不用考虑链式结尾
         * 2: 链式结尾产生条件:
         *      想要产生链式结尾, 必须是在链式表达式计算过程中不断迭代, 直到末尾. 然而链式迭代计算过程中, 只会调用getValue(Value), 因此getValue()无需考虑链式结尾
         * @return Value
         */
        public Value getValue() {
            // 获取arrayId
            String arrayId = super.getArrayIdentify(token);
            /*
            切忌不要直接调用super.getValue(), 否则存在极大的安全隐患.

            因为super.getValue(), 也就是ArrayToken.getValue()内部处理时, 没有考虑兼容ArrayTokenChain的处理逻辑
            因此此处需要先截取链式token, 然后传递arrayId给ArrayToken进行处理
             */
            Value value = super.getValue(arrayId);
            // 从array后开始为链式调用
            String chain = token.substring(arrayId.length() + 1);
            TokenChain tokenChain = tf.parseToTokenChain(chain, ctx);
            return tokenChain.getValue(new PValue(value, arrayId));
        }

        @Override
        public Value getValue(PValue pValue) {
            if (pValue.value == null) {
                throw new ComputeError("链式调用的前一个调用" + pValue.preName + "为null, 链式表达式错误!");
            }
            boolean flag = isImplicitCall(token);
            String arrayId = flag ? token.substring(0, CharacterHelper.matchArrayBracketSafe(token, 0)) : super.getArrayIdentify(token); // 判断是否为隐式调用
            Value pV = pValue.value;

            Value value = getArrayValue(pV, arrayId); // 获取数组值

            // 判断是否是最后一个链式调用
            if (isLastChain(token)) {
                return value;
            } else {
                // 链式调用
                String chain = token.substring(arrayId.length() + 1);
                TokenChain tokenChain = tf.parseToTokenChain(chain, ctx);
                return tokenChain.getValue(new PValue(value, arrayId));
            }
        }

        /**
         * 从array名字中解析对应的Value
         * @param pV 当前链式调用的调用方
         * @param arrayId 数组名, 如 arr[1][2], arr[invoke][c.invoke()]
         * @return Value
         */
        private Value getArrayValue(Value pV, String arrayId) {
            boolean flag = isImplicitCall(token);
            // 如果是隐式调用, 那么pV必须是ArrayReference
            if (flag) {
                if (! (pV instanceof ArrayReference) ) {
                    throw new ComputeError("pV不是Array类型的变量! 表达式计算结果错误! type = " + pV.type().name());
                }
            } else {
                // 非隐式调用, 从pV中取值
                String arrayName = arrayId.substring(0, arrayId.indexOf("["));
                TValue value = takeValueByVName(arrayName, Env.takeMemberValues(pV));
                if (! value.contain) {
                    value = takeValueByVName(arrayName, Env.takeStaticValues(pV));
                }
                if (! value.contain) {
                    throw new ComputeError("变量" + arrayId + "未定义!");
                }
                pV = value.value;
            }
            List<Integer> dims = super.getDims(arrayId);
            return super.itrArray((ArrayReference) pV, dims, arrayId);
        }

        private boolean isImplicitCall(String token) {
            return token.startsWith("[");
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

        /**
         * 表示规则的兼容
         */
        @Retention(RetentionPolicy.RUNTIME)
        public @interface AdaptTo {
            Class<? extends Rule>[] value();
        }

        public interface Rule {
            /**
             * 判断s是否完全匹配Rule的规则
             * @param s s
             * @return 是否匹配
             */
            boolean match(String s);
            /**
             * 从头开始匹配, 截取匹配规则的字符串. 该方法区别于{@link Rule#match(String)}, 只要从头开始能够部分匹配, 则返回匹配的字符串
             * @param s s
             * @return 匹配得到的字符串
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

        /**
         * 正则在某些特殊情况下无法满足复杂的匹配需求, 但正则已经广泛应用, 因此采用独立编写符合Java Pattern的仿照类
         * 采用不同的逻辑执行匹配功能
         */
        public interface MyPattern {
            MyMatcher matcher(String s);
        }

        /**
         * {@link MyPattern}
         */
        public interface MyMatcher {

            boolean find();

            int start();

            int end();
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
            @Deprecated
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
            // @Deprecated 废弃理由和pureCallPattern一致
            // public static final Pattern invokePattern = Pattern.compile("^\\b[a-zA-Z_$][a-zA-Z0-9_$]*\\.\\b[a-zA-Z_$][a-zA-Z0-9_$]*\\(.*?\\)");
            public static final MyPattern invokePattern = s -> new MyMatcher() {

                private String token = s;
                private int end = -1;
                @Override
                public boolean find() {
                    int len = CharacterHelper.startVNameLen(token);
                    if (len == 0 || len == token.length()) return false;
                    if (token.charAt(len) != '.') {
                        return false;
                    }
                    // 从.后一位匹配
                    int len2 = CharacterHelper.startVNameLen(s.substring(len + 1));
                    if (len2 == 0) return false;
                    int start = len + 1 + len2;
                    if (start == token.length()) return false;
                    if (token.charAt(start) != '(') return false;
                    // 匹配右括号
                    end = CharacterHelper.matchBracket(token, start);
                    // 截取字符串
                    token = token.substring(end);
                    return true;
                }

                @Override
                public int start() {
                    if (end == -1) {
                        throw new IllegalStateException("No match available");
                    }
                    return 0;
                }

                @Override
                public int end() {
                    if (end == -1) {
                        throw new IllegalStateException("No match available");
                    }
                    return end;
                }
            };

            /*
             匹配纯方法调用[只能匹配一次的方法调用]
             eg:
             demoTest -> null 不存在函数调用
             self.dfs(1,2) -> 匹配得到: null [不是纯粹的函数调用, 因为有self作为调用者]
             dfs(1,2) -> 匹配得到: dfs(1,2)
             dfs(1,2).test() -> dfs(1,2) [只会匹配一次纯函数调用]
             */
            // @Deprecated // 该正则存在缺陷, 如invoke(1, (1 + 2) * 3). 他无法忽略函数括号内部的里层括号, 因为正则是非贪婪的. 如果将正则改为贪婪, 则他可能会匹配多组函数调用. 因此正则无法满足匹配需求, 因此废弃
            // public static final Pattern pureCallPattern = Pattern.compile("^[a-zA-Z_$][a-zA-Z0-9_$]*\\(.*?\\)");
            public static final MyPattern pureCallPattern = s -> new MyMatcher() {
                String token = s;
                int end = -1;
                @Override
                public boolean find() {
                    int len = CharacterHelper.startVNameLen(token);
                    if (len == 0 || len == token.length()) return false;
                    if (token.charAt(len) != '(') {
                        return false;
                    }
                    // 匹配右括号
                    end = CharacterHelper.matchBracket(token, len);
                    // 切割token
                    token = token.substring(end);
                    return true;
                }

                @Override
                public int start() {
                    if (end == -1) {
                        throw new IllegalStateException("No match available");
                    }
                    return 0;
                }

                @Override
                public int end() {
                    if (end == -1) {
                        throw new IllegalStateException("No match available");
                    }
                    return end;
                }
            };

            // 匹配纯粹的变量
            public static final Pattern variablePattern = Pattern.compile("\\b[a-zA-Z_$][a-zA-Z0-9_$]*\\b");

            // 匹配数组, 如arr[0], arr[b.test][c.invoke + c[0][1]]
            // 不支持隐式调用类型识别, 如[0][1]
            // @Deprecated
            // public static final Pattern arrayPattern = Pattern.compile("^[a-zA-Z_$][a-zA-Z0-9_$]*(\\[[^\\[\\]]+\\])+");
            public static final MyPattern arrayPattern = new MyPattern() {
                @Override
                public MyMatcher matcher(String s) {
                    return new MyMatcher() {
                        private String token = s;
                        private int end = -1;
                        @Override
                        public boolean find() {
                            int len = CharacterHelper.startVNameLen(token);
                            if (len == 0) {
                                return false;
                            }
                            if (len == token.length()) {
                                return false;
                            }
                            end = CharacterHelper.matchArrayBracketSafe(token, len);
                            if (end == -1) {
                                return false;
                            }
                            token = token.substring(end);
                            return true;
                        }

                        @Override
                        public int start() {
                            if (end == -1) {
                                throw new IllegalStateException("No match available");
                            }
                            return 0;
                        }

                        @Override
                        public int end() {
                            if (end == -1) {
                                throw new IllegalStateException("No match available");
                            }
                            return end;
                        }
                    };
                }
            };

            // 匹配常量(数值、字符、字符串): 该正则非常宽松: 1 + 2也能匹配
            public static final Pattern constantPattern = Pattern.compile("\\d+(\\.\\d+)?|\"([^\"\\\\]|\\\\.)*\"|\'([^\'\\\\]|\\\\.)*\'");
        }

        public static abstract class AbstractRuleChain extends AbstractRule implements RuleChain {
            public AbstractRuleChain(Class<? extends TokenChain> clazz) {
                super(clazz);
            }

            /**
             * 从start处开始匹配链式, 如果能匹配到, 返回匹配到的字符串, 同时包含start以前的字符串
             * 否则返回null
             * @param start start
             * @param s s
             * @return String
             */
            protected String parseChainByStart(int start, String s) {
                if (start >= s.length()) {
                    return null;
                }
                // 检查是否存在链式调用
                if (s.charAt(start) != '.') {
                    return null;
                }
                int end = CharacterHelper.matchChain(s, start);
                return s.substring(0, end);
            }
        }

        /**
         * 匹配EvalToken的规则
         */
        public static class EvalRule extends AbstractRule {
            public EvalRule () {
                super(EvalToken.class);
            }

            /**
             * 匹配计算表达式
             * 如果能被识别为eval rule. 那么必然存在运算符, 且运算符不属于任何别的区间, 比如arr[1 + 2]
             * 他就不符合eval rule. 因为运算内容存在于数组内. 而arr + 2属于eval rule. 因为运算符+号不属于任何其他规则的一部分
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
             * @param s s
             * @return boolean
             */
            public boolean match(String s) {
                if (operatorPattern.matcher(s).find()) {
                    // s是纯粹的计算符号, 不是计算表达式
                    return false;
                }
                return CharacterHelper.isEvalExpression(s);
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
                    Matcher m = operatorPattern.matcher(tmp);
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
                MyMatcher matcher = invokePattern.matcher(s);
                if (!matcher.find()) return false;
                return matcher.end() >= s.length();
            }

            /**
             * 从s的start位置开始匹配, 满足方法调用的内容将会被识别并返回
             * eg:
             * a.invoke(1+2, 2) + 2 -> 返回: a.invoke(1+2, 2)
             * a.invoke(1+2, 2).test.c -> 返回: a.invoke(1+2, 2)
             * a.invoke(1+2, 2).b.test() -> 返回: a.invoke(1+2, 2)
             * @param s s
             * @return s
             */
            @Override
            public String matchFromStart(String s) {
                MyMatcher matcher = invokePattern.matcher(s);
                boolean b = matcher.find();
                if (! b) return null;
                return s.substring(0, matcher.end());
            }

            @Override
            public String getName() {
                return "InvokeRule";
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
                MyMatcher matcher = pureCallPattern.matcher(s);
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
             * @param s s
             * @return s
             */
            @Override
            public String matchFromStart(String s) {
                MyMatcher matcher = pureCallPattern.matcher(s);
                boolean b = matcher.find();
                if (! b) return null;
                int len = matcher.end();
                if (len == s.length()) {
                    return s;
                }
                // 判断是否属于函数, 数组, 链式调用
                if (s.charAt(len) == '(' || s.charAt(len) == '[' || s.charAt(len) == '{' || s.charAt(len) == '.') {
                    return null;
                }
                return s.substring(0, matcher.end());
            }

            @Override
            public String getName() {
                return "PureCallRule";
            }
        }

        /**
         * 纯函数调用链. 要求token开始必须是纯函数调用, 且后续存在链式调用
         * eg:
         * dfs(1,2).a √
         * dfs(1,2).demo() √
         * <p>
         * dfs(1,2) x
         * a.dfs(1,2) x
         */
        public static class PureCallRuleChain extends AbstractRuleChain {

            public PureCallRuleChain() {
                super(PureCallTokenChain.class);
            }

            @Override
            public boolean match(String s) {
                String match = matchFromStart(s);
                if (match == null) return false;
                return match.length() == s.length();
            }

            @Override
            public String matchFromStart(String s) {
                MyMatcher matcher = pureCallPattern.matcher(s);
                // 纯函数的链式调用可能存在隐链式调用, 比如dfs(1)[0][0], 因此需要特殊判断
                // 不能调用super.parseChainByEnd
                if (! matcher.find()) return null;
                int end = matcher.end();
                if (end >= s.length()) {
                    return null;
                }
                // 检查是否存在链式调用
                if (s.charAt(end) != '.' && s.charAt(end) != '[') {
                    return null;
                }
                String dummyS = "." + s;
                end = CharacterHelper.matchChain(dummyS, 0);
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
                if (len == 0) {
                    return null;
                }
                // 判断是否属于函数, 数组, 链式调用
                if (s.charAt(len) == '(' || s.charAt(len) == '[' || s.charAt(len) == '{' || s.charAt(len) == '.') {
                    return null;
                }
                return s.substring(0, len);
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
        public static class VariableRuleChain extends AbstractRuleChain {

            public VariableRuleChain() {
                super(VariableTokenChain.class);
            }

            /**
             * 需要满足Variable链式调用, 且不满足EvalRule
             * @param s s
             * @return boolean
             */
            @Override
            public boolean match(String s) {
                String match = matchFromStart(s);
                if (match == null) {
                    return false;
                }
                return match.length() == s.length();
            }

            @Override
            public String matchFromStart(String s) {
                int len = CharacterHelper.startVNameLen(s);
                return super.parseChainByStart(len, s);
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
                MyMatcher matcher = arrayPattern.matcher(s);
                boolean b = matcher.find();
                if (! b) return false;
                int end = matcher.end();
                return end >= s.length();
            }

            @Override
            public String matchFromStart(String s) {
                MyMatcher matcher = arrayPattern.matcher(s);
                if (! matcher.find()) return null;
                int end = matcher.end();
                if (end >= s.length()) return s;
                // 属于ArrayRuleChain链式调用, 不属于ArrayRule
                if (s.charAt(end) == '.') {
                    return null;
                }
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
        public static class ArrayRuleChain extends AbstractRuleChain {

            public ArrayRuleChain() {
                super(ArrayTokenChain.class);
            }

            @Override
            public boolean match(String s) {
                String match = matchFromStart(s);
                if (match == null) {
                    return false;
                }
                return match.length() == s.length();
            }

            /**
             * 从开始位置匹配数组链式调用, 同时支持隐链式调用
             * 所谓隐链式调用, 是指在链式调用迭代过程中产生的不含调用符号的表达式, 如dfs()[0][1], 虽然不存在调用符号.
             * 但他却是方法调用 + 数组调用. 本质上属于两种调用, 因此被称为隐式调用
             * @param s s
             * @return s
             */
            @Override
            public String matchFromStart(String s) {
                MyMatcher matcher = arrayPattern.matcher(s);
                if (! matcher.find()) {
                    // 链式调用可能会存在如下情况
                    /*
                        dfs()[0][1]
                        整体会被PureRuleChain识别, 在迭代计算时, 会产生[0][1]这样的隐链式调用表达式
                        为了支持链式调用, 因此需要在ArrayRuleChain中支持相应规则
                     */
                    if (s.charAt(0) == '[') {
                        int end = CharacterHelper.matchArrayBracketSafe(s, 0);
                        // 纯隐链式调用, 如[1][2]
                        if (end == s.length()) {
                            return s;
                        } else if (s.charAt(end) == '.') {
                            // 隐链式存在后续的链式调用, 如 [1][2].invoke()
                            return super.parseChainByStart(end, s);
                        } else {
                            return s.substring(0, end);
                        }
                    } else {
                        // 不是隐链式调用
                        return null;
                    }
                } else {
                    return super.parseChainByStart(matcher.end(), s);
                }
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
                // match 为null, 表示s不是常量
                if (match == null) {
                    return false;
                }
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
                    if (j == arr.length) {
                        throw new ComputeError("表达式错误, 字符串类型表达式必须包含一个匹配的\" " + s);
                    }
                    // j 指向", 因此需要j + 1
                    return s.substring(0, j + 1);
                } else if (c == '\'') {
                    // 去除''内部的所有信息,包括匹配的另一个'
                    int j = i + 1;
                    while (j < arr.length && arr[j] != '\'') {
                        ++j;
                    }
                    if (j == arr.length) {
                        throw new ComputeError("表达式错误, 字符类型表达式必须包含一个匹配的' " + s);
                    }
                    // j指向', 因此需要j + 1
                    return s.substring(0, j + 1);
                } else if (CharacterHelper.isArabicNumber(c)) {
                    // 如果是数字
                    int j = i + 1;
                    // 是否检测到过小数点
                    boolean dot = false;
                    while (j < arr.length) {
                        if (! CharacterHelper.isArabicNumber(arr[j])) {
                            // 检测小数
                            if (arr[j] != '.') {
                                break;
                            } else if (!dot && arr[j] == '.') {
                                dot = true;
                            } else if (dot && arr[j] == '.') {
                                throw new ComputeError("表达式错误, 数字类型表达式不允许出现两个小数点 " + s);
                            }
                        }
                        j += 1;
                    }
                    // j指向非数字, 因此无需j - 1
                    return j == arr.length ? s: s.substring(0, j);
                }
                return null;
            }

            @Override
            public String getName() {
                return "ConstantRule";
            }
        }

        Rule[] rules = new Rule[]{
                new EvalRule(),
                new OperatorRule(),
//                new InvokeRule(),
//                new InvokeRuleChain(),
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
                throw new ComputeError(s + " 被多条规则匹配! 请检查规则编写是否正确! + " + matches.stream().map(Rule::getName).collect(Collectors.joining(",")));
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
         * 处理兼容规则
         *
         * @param matches List<Rule>
         * @return 处理后的Rule
         */
        @Deprecated // 通过修改需要匹配的规则, 废除兼容导致的多个matches结果
        private List<Rule> handleRuleAdapter(List<Rule> matches) {
            List<Rule> newMatches = new ArrayList<>();
            Set<Class<? extends Rule>> filtered = new HashSet<>();
            for (Rule match : matches) {
                AdaptTo annotation = match.getClass().getAnnotation(AdaptTo.class);
                if (! filtered.contains(match.getClass())) {
                    newMatches.add(match);
                }
                if (annotation != null) {
                    Collections.addAll(filtered, annotation.value());
                }
            }
            return newMatches;
        }

        /**
         * 处理兼容规则
         *
         * @param hits hits
         * @return hits
         */
        @Deprecated // 通过修改需要匹配的规则, 废除兼容导致的多个matches结果
        private List<Hit2> handleHitAdapter(List<Hit2> hits) {
            List<Hit2> newHits = new ArrayList<>();
            Set<Class<? extends Rule>> filtered = new HashSet<>();
            for (Hit2 hit : hits) {
                AdaptTo annotation = hit.rule.getClass().getAnnotation(AdaptTo.class);
                if (! filtered.contains(hit.rule.getClass())) {
                    newHits.add(hit);
                }
                if (annotation != null) {
                    Collections.addAll(filtered, annotation.value());
                }
            }
            return newHits;
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
        public Token parseToToken(String token) {
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
         * a.demo() + 1: 从开始位置匹配, 将会被VariableRuleChain识别, 得到a.demo()
         * arr[0].b + 2: 从头开始匹配, 将会被ArrayRuleChain识别, 得到arr[0].b
         * 1 + 2 + 3: 从头开始匹配, 将会被ConstantRule识别, 得到1
         * + 2 + 3: 从头开始匹配, 将会被OperatorRule识别, 得到+
         *
         * @param token token
         * @param ctx ctx
         * @return Token
         */
        public Token parseToTokenFromStart(String token, Context ctx) {
            token = handleInput(token);
            List<Hit2> hits = new ArrayList<>(5);

            // 处理s
            String s = handleInput(token);

            // 根据解析规则解析得到不同的Token
            for (Rule rule : rules) {
                String match = rule.matchFromStart(token);
                if (match != null) {
                    hits.add(new Hit2(rule, match));
                }
            }

            // 判断
            if (hits.isEmpty()) {
                throw new ComputeError("无法识别的内容! " + s);
            }
            // double check
            Hit2 targetHit;
            if (hits.size() == 1) {
                targetHit = hits.get(0);
            } else if (hits.size() == 2) {
                if (hits.stream().anyMatch(e -> e.rule instanceof EvalRule) ) {
                    // 过滤EvalRule, 选择剩下的那一个Rule
                    targetHit = hits.stream().filter(e -> ! (e.rule instanceof EvalRule)).findFirst().orElse(null);
                    // 如果targetHit == 0, 表示EvalRule被重复匹配两次, 程序编写异常
                    if (targetHit == null) {
                        throw new ComputeError(s + " 被EvalRule重复匹配两次! 请检查规则编写是否正确! + " + hits.stream().map(e -> e.rule.getName()).collect(Collectors.joining(",")));
                    }
                } else  {
                    throw new ComputeError(s + " 被两条规则匹配! 请检查规则编写是否正确! + " + hits.stream().map(e -> e.rule.getName()).collect(Collectors.joining(",")));
                }
            } else {
                throw new ComputeError(s + " 被多条规则匹配! 请检查规则编写是否正确! + " + hits.stream().map(e -> e.rule.getName()).collect(Collectors.joining(",")));
            }

            Class<? extends Token> tokenClass = targetHit.rule.tokenClass();
            String className = tokenClass.getName();
            try {
                return tokenClass.getDeclaredConstructor(String.class, Context.class).newInstance(targetHit.token, ctx);
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
         */
        public Token parseToTokenFromStart(String token) {
            return parseToTokenFromStart(token, null);
        }

        public Map<String, Rule> ruleMap = new HashMap<>(3);

        {
            ruleMap.put("ArrayRule", new ArrayRuleChain());
            ruleMap.put("PureCallRule", new PureCallRuleChain());
            ruleMap.put("VariableRule", new VariableRuleChain());
        }

        /**
         * 解析TokenChain
         * @param s s
         * @param ctx ctx
         * @return TokenChain
         */
        public TokenChain parseToTokenChain(String s, Context ctx) {
            // 处理s
            s = handleInput(s);

            List<Rule> matches = new ArrayList<>(3);

            // 根据解析规则解析得到不同的Token
            for (Rule rule : rules) {
                if (rule.match(s)) {
                    matches.add(rule);
                }
            }

            // 判断
            if (matches.isEmpty()) {
                throw new ComputeError("无法识别的内容! " + s);
            }
            if (matches.size() > 1) {
                throw new ComputeError(s + " 被多条规则匹配! 请检查规则编写是否正确! + " + matches.stream().map(Rule::getName).collect(Collectors.joining(",")));
            }
            // 如果包含EvalRule, 报错
            if (matches.stream().anyMatch(e -> e instanceof EvalRule)) {
                throw new ComputeError(s + " 被EvalRule匹配! 无法得到对应的链式! parseToTokenChain方法使用错误! + " + matches.stream().map(Rule::getName).collect(Collectors.joining(",")));
            }
            Rule targetRule;
            // 规则转换
            if (! (matches.get(0) instanceof RuleChain) ) {
                String key = matches.get(0).getName();
                if (!ruleMap.containsKey(key)) {
                    throw new ComputeError(s + " 被" + key + "匹配! 无法得到对应的链式! parseToTokenChain方法使用错误! + " + matches.stream().map(Rule::getName).collect(Collectors.joining(",")));
                }
                targetRule = ruleMap.get(key);
            }else {
                targetRule = matches.get(0);
            }

            Class<? extends Token> tokenClass = targetRule.tokenClass();
            String className = tokenClass.getName();
            try {
                return (TokenChain) tokenClass.getDeclaredConstructor(String.class, Context.class).newInstance(s, ctx);
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
    }

}