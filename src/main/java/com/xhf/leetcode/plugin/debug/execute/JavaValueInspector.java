package com.xhf.leetcode.plugin.debug.execute;

import com.sun.jdi.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 强转value, 并给出value的实际内容, md, 真恶心啊我靠
 */
public class JavaValueInspector {
    // 单例
    private static final JavaValueInspector INSTANCE = new JavaValueInspector();
    private JavaValueInspector() {

    }
    public static JavaValueInspector getInstance() {
        return INSTANCE;
    }

    public String inspectValue(Value value) {
        if (value == null) {
            return null;
        }

        // 获取 value 的类型
        Type type = value.type();

        // 处理基本类型
        if (value instanceof PrimitiveValue) {
            return handlePrimitiveValue((PrimitiveValue) value);
        }
        // 处理引用类型
        else if (value instanceof ObjectReference) {
            return handleObjectReference((ObjectReference) value);
        }
        // 其他未知类型
        else {
            return null;
        }
    }

    private String handlePrimitiveValue(PrimitiveValue value) {
        String res;
        if (value instanceof BooleanValue) {
            res = String.valueOf(((BooleanValue) value).value());
        } else if (value instanceof ByteValue) {
            res = String.valueOf(((ByteValue) value).value());
        } else if (value instanceof CharValue) {
            res = "\"" + ((CharValue) value).value() + "\"";
        } else if (value instanceof DoubleValue) {
            res = String.valueOf(((DoubleValue) value).value());
        } else if (value instanceof FloatValue) {
            res = String.valueOf(((FloatValue) value).value());
        } else if (value instanceof IntegerValue) {
            res = String.valueOf(((IntegerValue) value).value());
        } else if (value instanceof LongValue) {
            res = String.valueOf(((LongValue) value).value());
        } else if (value instanceof ShortValue) {
            res = String.valueOf(((ShortValue) value).value());
        } else {
            res = null;
        }
        return res;
    }

    private String handleObjectReference(ObjectReference objRef) {
        String res;
        if (objRef instanceof StringReference) {
            res = "\"" + ((StringReference) objRef).value() + "\"";
        } else if (objRef instanceof ArrayReference) {
            res = "[" + ((ArrayReference) objRef).getValues().stream().map(String::valueOf).collect(Collectors.joining(", ")) + "]";
        } else if (objRef instanceof ThreadReference) {
            res = ((ThreadReference) objRef).name();
        } else if (objRef instanceof ClassObjectReference) {
            res = String.valueOf(((ClassObjectReference) objRef).reflectedType());
        } else if (objRef instanceof ClassLoaderReference) {
            res = "ClassLoader...";
        } else {
            res = null;
        }
        return res;
    }
}
