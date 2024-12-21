package com.xhf.leetcode.plugin.debug.execute;

import com.sun.jdi.*;

import java.util.*;
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

    // 包装类集合
    private static final Set<String> WRAPPER_CLASSES = new HashSet<>(Arrays.asList(
            "java.lang.Integer", "java.lang.Double", "java.lang.Character", "java.lang.Long",
            "java.lang.Float", "java.lang.Boolean", "java.lang.Byte", "java.lang.Short"
    ));

    // 判断对象是否为包装类型
    public boolean isWrapperType(Value value) {
        if (value instanceof ObjectReference) {
            // 获取对象的 ReferenceType
            ObjectReference objRef = (ObjectReference) value;
            ReferenceType refType = objRef.referenceType();
            String className = refType.name();
            // 判断该类是否是包装类型
            return WRAPPER_CLASSES.contains(className);
        }
        return false;  // 如果是基本类型值，则认为它不是包装类型
    }

    private String handleObjectReference(ObjectReference objRef) {
        String res;
        if (objRef instanceof StringReference) {
            res = "\"" + ((StringReference) objRef).value() + "\"";
        } else if (objRef instanceof ArrayReference) {
            if (((ArrayReference) objRef).length() == 0) {
                res = "[]";
            }
            // 如果数组元素类型不是基本类型或者StringReference，则不显示
            Value value = ((ArrayReference) objRef).getValue(0);
            if (value instanceof StringReference) {
                res = "[" + ((ArrayReference) objRef).getValues().stream().map(String::valueOf).collect(Collectors.joining(", ")) + "]";
            }else if (value instanceof PrimitiveValue) {
                res = "[" + ((ArrayReference) objRef).getValues().stream().map(e -> handlePrimitiveValue((PrimitiveValue) e)).collect(Collectors.joining(", ")) + "]";
            }else if (isWrapperType(value)) {
                res = "[" + ((ArrayReference) objRef).getValues().stream().map(e -> getWrapperValue((ObjectReference) e)).collect(Collectors.joining(", ")) + "]";
            }else {
                res = "[" + ((ArrayReference) objRef).getValues().stream().map(e -> handleObjectReference((ObjectReference) e)).collect(Collectors.joining(", ")) + "]";
            }
        } else if (objRef instanceof ThreadReference) {
            res = ((ThreadReference) objRef).name();
        } else if (objRef instanceof ClassObjectReference) {
            res = String.valueOf(((ClassObjectReference) objRef).reflectedType());
        } else if (objRef instanceof ClassLoaderReference) {
            res = "ClassLoader...";
        } else if (isWrapperType(objRef)) {
            res = getWrapperValue(objRef);
        } else if (isArrayList(objRef)) {
            res = getArrayList(objRef);
        } else {
            StringBuilder sb = new StringBuilder();
            if (objRef == null || objRef.referenceType() == null) {
                return "";
            }
            for (Field field : objRef.referenceType().allFields()) {
                // 不处理静态变量
                if (! field.isStatic()) {
                    Value value = objRef.getValue(field);
                    sb.append(field.name()).append(" = ").append(this.inspectValue(value)).append(" ");
                }
            }
            res = sb.toString();
        }
        return res;
    }

    private String getArrayList(ObjectReference objRef) {
        Field elementData = objRef.referenceType().fieldByName("elementData");
        Field size = objRef.referenceType().fieldByName("size");

        if (elementData != null && size != null) {
            Value elementDataValue = objRef.getValue(elementData);
            Value sizeValue = objRef.getValue(size);
            if (elementDataValue instanceof ArrayReference && sizeValue instanceof IntegerValue) {
                ArrayReference arrayReference = (ArrayReference) elementDataValue;
                int sizeInt = ((IntegerValue) sizeValue).value();
                StringBuilder sb = new StringBuilder("[");

                for (int i = 0; i < sizeInt; i++) {
                    Value value = arrayReference.getValue(i);
                    sb.append(this.inspectValue(value));
                    if (i != sizeInt - 1) {
                        sb.append(", ");
                    }
                }
                return sb.append("]").toString();
            }
        }
        return "";
    }

    private boolean isArrayList(Value value) {
        if (value instanceof ObjectReference) {
            // 获取对象的 ReferenceType
            ObjectReference objRef = (ObjectReference) value;
            ReferenceType refType = objRef.referenceType();
            String className = refType.name();
            return "java.util.ArrayList".equals(className);
        }
        return false;
    }

    private String getWrapperValue(ObjectReference objRef) {
        // 获取包装类型类的 'value' 字段
        Field valueField = objRef.referenceType().fieldByName("value");
        if (valueField != null) {
            // 获取字段值
            Value value = objRef.getValue(valueField);
            // 将值转换为字符串并返回
            return value.toString();
        }
        return "";
    }
}
