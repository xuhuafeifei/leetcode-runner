package debug;

import com.sun.jdi.*;
import java.util.List;
import java.util.stream.Collectors;

//public class JDIDebugger {
//
//    private String handleObjectReference(ObjectReference objRef) throws Exception {
//        String res;
//
//        // 获取对象的 ReferenceType
//        ReferenceType refType = objRef.referenceType();
//
//        // 检查对象是否为 String 类型
//        if (objRef instanceof StringReference) {
//            res = "\"" + ((StringReference) objRef).value() + "\"";
//        }
//        // 检查对象是否为 Array 类型
//        else if (objRef instanceof ArrayReference) {
//            res = "[" + ((ArrayReference) objRef).getValues().stream()
//                        .map(String::valueOf)
//                        .collect(Collectors.joining(", ")) + "]";
//        }
//        // 检查对象是否实现了 List 接口
//        else if (isListType(refType)) {
//            res = handleList(objRef, refType);
//        }
//        // 其他类型的处理
//        else if (objRef instanceof ThreadReference) {
//            res = ((ThreadReference) objRef).name();
//        } else if (objRef instanceof ClassObjectReference) {
//            res = String.valueOf(((ClassObjectReference) objRef).reflectedType());
//        } else if (objRef instanceof ClassLoaderReference) {
//            res = "ClassLoader...";
//        } else {
//            res = null;
//        }
//
//        return res;
//    }
//
//    // 判断对象是否实现了 List 接口
//    private boolean isListType(ReferenceType refType) {
//        // 获取对象类型实现的所有接口
//        List<InterfaceType> interfaces = refType.allInterfaces();
//        for (InterfaceType interfaceType : interfaces) {
//            if (interfaceType.name().equals("java.util.List")) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    // 处理 List 类型
//    private String handleList(ObjectReference objRef, ReferenceType refType) throws Exception {
//        StringBuilder sb = new StringBuilder();
//
//        // 检查不同 List 实现类
//        if (refType.name().equals("java.util.ArrayList") || refType.name().equals("java.util.Vector")) {
//            // 对于 ArrayList 和 Vector，通常存储数据的字段是 elementData
//            Field elementDataField = refType.fieldByName("elementData");
//            if (elementDataField != null) {
//                ArrayReference elementDataArray = (ArrayReference) objRef.getValue(elementDataField);
//                if (elementDataArray != null) {
//                    sb.append("[");
//                    sb.append(elementDataArray.getValues().stream()
//                        .map(value -> value != null ? value.toString() : "null")
//                        .collect(Collectors.joining(", ")));
//                    sb.append("]");
//                }
//            }
//        } else if (refType.name().equals("java.util.LinkedList")) {
//            // 对于 LinkedList，需要处理链表结构
//            sb.append("LinkedList elements: ");
//            Field firstField = refType.fieldByName("first");
//            if (firstField != null) {
//                ObjectReference firstNode = (ObjectReference) objRef.getValue(firstField);
//                sb.append(traverseLinkedList(firstNode));
//            }
//        } else if (refType.name().equals("java.util.concurrent.CopyOnWriteArrayList")) {
//            // 对于 CopyOnWriteArrayList，通常它也使用数组
//            Field arrayField = refType.fieldByName("array");
//            if (arrayField != null) {
//                ArrayReference array = (ArrayReference) objRef.getValue(arrayField);
//                if (array != null) {
//                    sb.append("[");
//                    sb.append(array.getValues().stream()
//                        .map(value -> value != null ? value.toString() : "null")
//                        .collect(Collectors.joining(", ")));
//                    sb.append("]");
//                }
//            }
//        }
//
//        return sb.toString();
//    }
//
//    // 遍历 LinkedList 的方法
//    private String traverseLinkedList(ObjectReference node) {
//        StringBuilder sb = new StringBuilder();
//        while (node != null) {
//            Field valueField = node.referenceType().fieldByName("item");
//            Field nextField = node.referenceType().fieldByName("next");
//
//            if (valueField != null && nextField != null) {
//                Value itemValue = node.getValue(valueField);
//                sb.append(itemValue != null ? itemValue.toString() : "null");
//
//                node = (ObjectReference) node.getValue(nextField);
//                if (node != null) {
//                    sb.append(", ");
//                }
//            } else {
//                break; // 如果没有字段，就退出
//            }
//        }
//        return sb.toString();
//    }
//}
