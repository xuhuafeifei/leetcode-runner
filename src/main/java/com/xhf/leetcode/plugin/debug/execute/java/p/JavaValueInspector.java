package com.xhf.leetcode.plugin.debug.execute.java.p;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.Field;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LongValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.xhf.leetcode.plugin.debug.analysis.converter.convert.TreeNode;
import com.xhf.leetcode.plugin.debug.utils.DebugUtils;
import com.xhf.leetcode.plugin.utils.LogUtils;
import com.xhf.leetcode.plugin.utils.MapUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * 强转value, 并给出value的实际内容, md, 真恶心啊我靠
 * 底层这些破玩意儿真是一点办法没有, 全得写死, 而且为了简化代码,
 * 使用了不少递归处理, 最怕递归太深了, 出现StackOverflow, 爆栈什么的, 那种事情不要啊~
 */
public class JavaValueInspector {

    // 单例
    private static final JavaValueInspector INSTANCE = new JavaValueInspector();
    // 包装类集合
    public final Map<String, String> WRAPPER_TO_PRIMITIVES = MapUtils.getMapFromList(
        Arrays.asList(
            "java.lang.Integer", "java.lang.Double", "java.lang.Character", "java.lang.Long",
            "java.lang.Float", "java.lang.Boolean", "java.lang.Byte", "java.lang.Short"
        ),
        Arrays.asList(
            "int", "double", "char", "long",
            "float", "boolean", "byte", "short"
        )
    );
    public final Map<String, String> PRIMITIVES_TO_WRAPPER = MapUtils.getMapFromList(
        Arrays.asList(
            "int", "double", "char", "long",
            "float", "boolean", "byte", "short"
        ),
        Arrays.asList(
            "java.lang.Integer", "java.lang.Double", "java.lang.Character", "java.lang.Long",
            "java.lang.Float", "java.lang.Boolean", "java.lang.Byte", "java.lang.Short"
        )
    );
    // 在打印复杂对象时, 有些字段不需要打印
    private final Set<String> skipWords = Set.of("hash", "next", "entrySet", "modCount", "threshold", "loadFactor",
        "keySet", "values", "comparator", "serialVersionUID", "DEFAULT_INITIAL_CAPACITY");

    private JavaValueInspector() {

    }

    public static JavaValueInspector getInstance() {
        return INSTANCE;
    }

    public String inspectValue(Value value) {
        return inspectValue(value, 0);
    }

    private String inspectValue(Value value, int depth) {
        if (value == null) {
            return null;
        }
        // 处理基本类型
        if (value instanceof PrimitiveValue) {
            return handlePrimitiveValue((PrimitiveValue) value);
        }
        // 处理引用类型
        else if (value instanceof ObjectReference) {
            return handleObjectReference((ObjectReference) value, depth);
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

    private String handleObjectReference(ObjectReference objRef, int depth) {
        String res;
        if (objRef instanceof StringReference) {
            res = "\"" + ((StringReference) objRef).value() + "\"";
        } else if (objRef instanceof ArrayReference) {
            if (((ArrayReference) objRef).length() == 0) {
                return "[]";
            }
            Value value = ((ArrayReference) objRef).getValue(0);
            if (value instanceof StringReference) {
                res = "[" + ((ArrayReference) objRef).getValues().stream().map(String::valueOf)
                    .collect(Collectors.joining(", ")) + "]";
            } else if (value instanceof PrimitiveValue) {
                res = "[" + ((ArrayReference) objRef).getValues().stream()
                    .map(e -> handlePrimitiveValue((PrimitiveValue) e)).collect(Collectors.joining(", ")) + "]";
            } else if (isWrapperType(value)) {
                res = "[" + ((ArrayReference) objRef).getValues().stream().map(e -> handleWrapper((ObjectReference) e))
                    .collect(Collectors.joining(", ")) + "]";
            } else {
                res = "[" + ((ArrayReference) objRef).getValues().stream()
                    .map(e -> handleObjectReference((ObjectReference) e, depth + 1)).collect(Collectors.joining(", "))
                    + "]";
            }
        } else if (objRef instanceof ThreadReference) {
            res = ((ThreadReference) objRef).name();
        } else if (objRef instanceof ClassObjectReference) {
            res = String.valueOf(((ClassObjectReference) objRef).reflectedType());
        } else if (objRef instanceof ClassLoaderReference) {
            res = "ClassLoader...";
        } else if (isWrapperType(objRef)) {
            res = handleWrapper(objRef);
        } else if (isArrayList(objRef)) {
            res = handleArrayList(objRef, depth);
        } else if (isArraysArrayList(objRef)) {
            res = handleArraysArrayList(objRef, depth);
        } else if (isLinkedList(objRef)) {
            res = handleLinkedList(objRef, depth);
        } else if (isSingletonList(objRef)) {
            res = handleSingletonList(objRef, depth);
        } else if (isArrayDeque(objRef)) {
            res = handleArrayDeque(objRef, depth);
        } else if (objRef instanceof ThreadGroupReference) {
            res = "objRef 是 ThreadGroupReference, 不支持处理!";
        } else if (isTreeNode(objRef)) {
            res = handleTreeNode(objRef, depth);
        } else if (isListNode(objRef)) {
            res = handleListNode(objRef, depth);
        } else if (isHashMap(objRef)) {
            res = handleHashMap(objRef, depth);
        } else if (isPriorityQueue(objRef)) {
            res = handlePriorityQueue(objRef, depth);
        } else if (isHashSet(objRef)) {
            res = handleHashSet(objRef, depth);
        } else {
            res = handleComplexObject(objRef, depth);
        }
        return res;
    }

    /**
     * 移除末尾的空格和逗号
     */
    @NotNull
    private String removeBlankAndComma(StringBuilder sb) {
        while (sb.length() > 0 && sb.charAt(sb.length() - 1) == ' ') {
            sb.deleteCharAt(sb.length() - 1);
        }
        int len = sb.length();
        if (len - 1 > 0 && sb.charAt(len - 1) == ',') {
            sb.deleteCharAt(len - 1);
        }
        sb.append("}");
        return sb.toString();
    }

    private boolean isHashSet(ObjectReference objRef) {
        if (objRef == null) {
            return false;
        }
        return objRef.type().name().equals("java.util.HashSet");
    }

    /**
     * 借鉴了{@link java.util.HashMap.HashIterator}的写法
     */
    private String handleMapBasedCollection(ObjectReference objRef, int depth, boolean includeValue) {
        if (objRef == null) {
            return "null";
        }
        try {
            if (!includeValue) {
                // 对于HashSet，需要先获取内部的HashMap
                objRef = (ObjectReference) objRef.getValue(objRef.referenceType().fieldByName("map"));
            }
            ReferenceType referenceType = objRef.referenceType();
            Value table = objRef.getValue(referenceType.fieldByName("table"));
            int size = ((IntegerValue) objRef.getValue(referenceType.fieldByName("size"))).intValue();
            if (table == null || size == 0) {
                return "{}";
            }
            ArrayReference t = (ArrayReference) table;
            int index = 0;
            StringBuilder sb = new StringBuilder(getTabsByDepth(depth));
            sb.append("{");

            for (; index < t.length(); ++index) {
                Value node = t.getValue(index);
                if (node != null) {
                    // 获取Node节点
                    ObjectReference objNode = (ObjectReference) node;
                    ReferenceType objNodeRef = objNode.referenceType();
                    Value key = objNode.getValue(objNodeRef.fieldByName("key"));
                    Value next = objNode.getValue(objNodeRef.fieldByName("next"));
                    if (includeValue) {
                        Value value = objNode.getValue(objNodeRef.fieldByName("value"));
                        sb.append(this.inspectValue(key)).append(":  ").append(this.inspectValue(value)).append(",   ");
                    } else {
                        sb.append(this.inspectValue(key)).append(",   ");
                    }

                    // 迭代node直到next为null
                    while (next != null) {
                        objNode = (ObjectReference) next;
                        objNodeRef = objNode.referenceType();
                        key = objNode.getValue(objNodeRef.fieldByName("key"));
                        next = objNode.getValue(objNodeRef.fieldByName("next"));
                        if (includeValue) {
                            Value value = objNode.getValue(objNodeRef.fieldByName("value"));
                            sb.append(this.inspectValue(key)).append(":  ").append(this.inspectValue(value))
                                .append(",   ");
                        } else {
                            sb.append(this.inspectValue(key)).append(",   ");
                        }
                    }
                }
            }
            return removeBlankAndComma(sb);
        } catch (Exception e) {
            LogUtils.warn(DebugUtils.getStackTraceAsString(e));
            return handleComplexObject(objRef, depth);
        }
    }

    private String handleHashSet(ObjectReference objRef, int depth) {
        return handleMapBasedCollection(objRef, depth, false);
    }

    private String handleHashMap(ObjectReference objRef, int depth) {
        return handleMapBasedCollection(objRef, depth, true);
    }

    private boolean isHashMap(ObjectReference objRef) {
        if (objRef == null) {
            return false;
        }
        return objRef.type().name().equals("java.util.HashMap");
    }

    private String handlePriorityQueue(ObjectReference objRef, int depth) {
        if (objRef == null) {
            return "null";
        }
        ReferenceType referenceType = objRef.referenceType();
        Value value = objRef.getValue(referenceType.fieldByName("queue"));
        if (value == null) {
            return null;
        }
        return this.inspectValue(value, depth);
    }

    private boolean isPriorityQueue(ObjectReference objRef) {
        if (objRef == null) {
            return false;
        }
        return objRef.type().name().equals("java.util.PriorityQueue");
    }

    private String handleListNode(ObjectReference objRef, int depth) {
        if (!isMyListNode(objRef)) {
            return handleComplexObject(objRef, depth);
        }
        ReferenceType referenceType = objRef.referenceType();
        Value val = objRef.getValue(referenceType.fieldByName("val"));
        Value next = objRef.getValue(referenceType.fieldByName("next"));

        StringBuilder sb = new StringBuilder("[");
        while (next != null) {
            val = objRef.getValue(referenceType.fieldByName("val"));
            next = objRef.getValue(referenceType.fieldByName("next"));
            objRef = (ObjectReference) next;
            sb.append(getIntNodeVal(val)).append(",");
        }
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("]");
        return sb.toString();
    }

    private boolean isListNode(Value value) {
        // 判断是否是 java.util.ArrayDeque
        return value instanceof ObjectReference && value.type().name().contains("ListNode");
    }

    /**
     * 判断是否是我提供的ListNode
     */
    private boolean isMyListNode(ObjectReference objRef) {
        // 检查并获取变量
        ReferenceType referenceType = objRef.referenceType();
        Value val = objRef.getValue(referenceType.fieldByName("val"));
        Value next = objRef.getValue(referenceType.fieldByName("next"));
        if (val == null || next == null) {
            return false;
        }
        if (!(val instanceof IntegerValue)) {
            return false;
        }
        // 判断类型
        return next.type().name().equals(referenceType.name());
    }

    /**
     * 处理复杂对象的打印
     */
    private String handleComplexObject(ObjectReference objRef, int depth) {
        StringBuilder sb = new StringBuilder();
        if (objRef == null || objRef.referenceType() == null) {
            return "null";
        }
        List<String> results = new ArrayList<>();
        for (Field field : objRef.referenceType().allFields()) {
            if (!field.isStatic()) {
                Value value = objRef.getValue(field);
                // 过滤某些变量
                if (isSkipWord(field.name())) {
                    continue;
                }
                results.add(field.name() + " : " + this.inspectValue(value, depth + 1));  // 深度+1
            }
        }
        if (!results.isEmpty()) {
            sb.append("\n").append(getTabsByDepth(depth));  // 外层缩进
            sb.append("{\n");
            for (String s : results) {
                sb.append(getTabsByDepth(depth + 1)).append(s);  // 字段缩进
                sb.append("\n");
            }
            sb.append(getTabsByDepth(depth)).append("}");  // 结束的 右大括号 也要缩进
        } else {
            sb.append(getTabsByDepth(depth));
            sb.append("{}");
        }
        return sb.toString();
    }

    private String handleTreeNode(ObjectReference objRef, int depth) {
        // 检查是否和我项目提供的TreeNode一致, 否则不进行打印
        //        if (! isMyTreeNode(objRef)) {
        //            // 采用默认的复杂对象打印器
        //            return handleComplexObject(objRef, depth);
        //        }
        // 创建头节点
        TreeNode cp = dfs(objRef);
        return new TreeNodePrinter(cp).visitAndReturn().toString();
    }

    /**
     * 返回头节点
     */
    private TreeNode dfs(ObjectReference objRef) {
        if (objRef == null) {
            return null;
        }
        ReferenceType referenceType = objRef.referenceType();
        Value val = objRef.getValue(referenceType.fieldByName("val"));
        Value left = objRef.getValue(referenceType.fieldByName("left"));
        Value right = objRef.getValue(referenceType.fieldByName("right"));

        TreeNode head = new TreeNode(getIntNodeVal(val));

        head.left = dfs((ObjectReference) left);
        head.right = dfs((ObjectReference) right);

        return head;
    }

    private int getIntNodeVal(Value val) {
        return ((IntegerValue) val).intValue();
    }

    /**
     * 判断是否是我提供的TreeNode
     */
    @Deprecated // 因为debug内核采用的是我提供的TreeNode. 所以不用判断
    private boolean isMyTreeNode(ObjectReference objRef) {
        // 检查并获取变量
        ReferenceType referenceType = objRef.referenceType();
        Value val = objRef.getValue(referenceType.fieldByName("val"));
        Value left = objRef.getValue(referenceType.fieldByName("left"));
        Value right = objRef.getValue(referenceType.fieldByName("right"));
        if (val == null || left == null || right == null) {
            return false;
        }
        if (!(val instanceof IntegerValue)) {
            return false;
        }
        // 判断类型
        if (!left.type().name().equals(referenceType.name())) {
            return false;
        }
        return right.type().name().equals(referenceType.name());
    }

    private boolean isTreeNode(Value value) {
        // 判断是否是 java.util.ArrayDeque
        return value instanceof ObjectReference && value.type().name().contains("TreeNode");
    }

    private boolean isSkipWord(String s) {
        return skipWords.contains(s);
    }

    public String getTabsByDepth(int depth) {
        return "    ".repeat(Math.max(0, depth));
    }

    private boolean isArrayDeque(Value value) {
        // 判断是否是 java.util.ArrayDeque
        return value instanceof ObjectReference && "java.util.ArrayDeque".equals(value.type().name());
    }

    private String handleArrayDeque(ObjectReference objRef, int depth) {
        ReferenceType referenceType = objRef.referenceType();
        Value elements = objRef.getValue(referenceType.fieldByName("elements"));
        Value head = objRef.getValue(referenceType.fieldByName("head"));
        Value tail = objRef.getValue(referenceType.fieldByName("tail"));
        if (elements != null && head != null && tail != null) {
            StringBuilder sb = new StringBuilder("[");
            // 迭代循环
            ArrayReference arrayReference = (ArrayReference) elements;
            int length = arrayReference.length();
            for (int i = ((IntegerValue) head).value(); i != ((IntegerValue) tail).value(); i = (i + 1) % length) {
                sb.append(this.inspectValue(arrayReference.getValue(i), depth + 1)).append(",");
            }
            // 如果最后一个是, 删除
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.append(']').toString();
        }
        return null;
    }

    /**
     * 获取 Collections.singletonList 的值
     */
    private String handleSingletonList(ObjectReference objRef, int depth) {
        Value value = objRef.getValue(objRef.referenceType().fieldByName("element"));
        if (value != null) {
            return "[" + this.inspectValue(value, depth + 1) + "]";
        }
        return null;
    }

    /**
     * Collections.singletonList, 就很操蛋
     */
    private boolean isSingletonList(Value value) {
        return value instanceof ObjectReference && "java.util.Collections$SingletonList".equals(value.type().name());
    }

    private boolean isLinkedList(Value value) {
        return value instanceof ObjectReference && "java.util.LinkedList".equals(value.type().name());
    }

    private boolean isLinkedListNode(Value value) {
        return value instanceof ObjectReference && "java.util.LinkedList$Node".equals(value.type().name());
    }

    public String handleLinkedList(ObjectReference objRef, int depth) {
        Field first = objRef.referenceType().fieldByName("first");
        if (first != null) {
            Value value = objRef.getValue(first);
            StringBuilder sb = new StringBuilder("[");
            // 循环迭代
            while (isLinkedListNode(value)) {
                ObjectReference node = (ObjectReference) value;
                // 获取node.item
                Field item = node.referenceType().fieldByName("item");
                if (item != null) {
                    Value itemValue = node.getValue(item);
                    sb.append(inspectValue(itemValue, depth + 1)).append(",");
                }
                // 获取node.next
                Field next = node.referenceType().fieldByName("next");
                // 更新node(value)
                if (next != null) {
                    value = node.getValue(next);
                }
            }
            // 如果最后一位是逗号 移除最后一个逗号
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',') {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.append("]").toString();
        }
        return "";
    }

    /**
     * 判断是否是Arrays.ArrayList
     */
    private boolean isArraysArrayList(Value value) {
        return value instanceof ObjectReference && "java.util.Arrays$ArrayList".equals(value.type().name());
    }

    /**
     * 获取Arrays.ArrayList
     *
     * @return 以String的形式表示Arrays.ArrayList
     */
    private String handleArraysArrayList(ObjectReference objRef, int depth) {
        Field a = objRef.referenceType().fieldByName("a");
        if (a != null) {
            Value value = objRef.getValue(a);
            if (value instanceof ArrayReference) {
                StringBuilder sb = new StringBuilder("[");
                ArrayReference arrayReference = (ArrayReference) value;
                for (int i = 0; i < arrayReference.getValues().size(); i++) {
                    sb.append(this.inspectValue(arrayReference.getValue(i), depth + 1));
                    if (i != arrayReference.getValues().size() - 1) {
                        sb.append(", ");
                    }
                }

                return sb.append("]").toString();
            }
        }
        return "";
    }

    /**
     * 获取ArrayList
     *
     * @return 以String的形式表示ArrayList
     */
    private String handleArrayList(ObjectReference objRef, int depth) {
        Field elementData = objRef.referenceType().fieldByName("elementData");
        Field size = objRef.referenceType().fieldByName("size");

        if (elementData != null && size != null) {
            Value elementDataValue = objRef.getValue(elementData);
            Value sizeValue = objRef.getValue(size);
            if (elementDataValue instanceof ArrayReference && sizeValue instanceof IntegerValue) {
                ArrayReference arrayReference = (ArrayReference) elementDataValue;
                int sizeInt = ((IntegerValue) sizeValue).value();
                StringBuilder sb = new StringBuilder("[");

                // 递归判断ArrayList的每个元素
                for (int i = 0; i < sizeInt; i++) {
                    Value value = arrayReference.getValue(i);
                    sb.append(this.inspectValue(value, depth + 1));
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
        return value instanceof ObjectReference && "java.util.ArrayList".equals(value.type().name());
    }

    /**
     * 通过包装类型返回对应的基本类型
     *
     * @param wrapperTypeName 包装类型
     * @return 对应的基本类型
     */
    public String getPrimitiveTypeByWrapperTypeName(String wrapperTypeName) {
        return WRAPPER_TO_PRIMITIVES.get(wrapperTypeName);
    }

    /**
     * 通过primitiveType获取对应的包装类型
     *
     * @param primitiveTypeName 基本类型
     * @return 对应的包装类型
     */
    public String getWrapperTypeByPrimitiveTypeName(String primitiveTypeName) {
        return PRIMITIVES_TO_WRAPPER.get(primitiveTypeName);
    }


    /**
     * 判断是不是primitiveType
     */
    public boolean isPrimitiveType(Value value) {
        return value instanceof PrimitiveValue;
    }


    public boolean isPrimitiveType(String typeName) {
        return PRIMITIVES_TO_WRAPPER.containsKey(typeName);
    }


    public boolean isWrapperType(String typeName) {
        return WRAPPER_TO_PRIMITIVES.containsKey(typeName);
    }

    // 判断对象是否为包装类型
    public boolean isWrapperType(Value value) {
        if (value == null) {
            return false;
        }
        if (value instanceof ObjectReference) {
            // 获取对象的 ReferenceType
            ObjectReference objRef = (ObjectReference) value;
            ReferenceType refType = objRef.referenceType();
            String className = refType.name();
            // 判断该类是否是包装类型
            return WRAPPER_TO_PRIMITIVES.containsKey(className);
        }
        return false;  // 如果是基本类型值，则认为它不是包装类型
    }

    private String handleWrapper(ObjectReference objRef) {
        if (objRef == null) {
            return "null";
        }
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

    /**
     * 获取包装类型类的 'value' 字段. 该字段是PrimitiveValue
     */
    public Value getWrapperValue(ObjectReference objRef) {
        if (objRef == null) {
            return null;
        }
        // 获取包装类型类的 'value' 字段
        Field valueField = objRef.referenceType().fieldByName("value");
        if (valueField != null) {
            // 获取字段值
            return objRef.getValue(valueField);
        }
        return null;
    }

    public List<String> getWrapperTypeList() {
        return new ArrayList<>(WRAPPER_TO_PRIMITIVES.keySet());
    }
}
