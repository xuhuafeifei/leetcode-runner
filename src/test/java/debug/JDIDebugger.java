package debug;

import com.sun.jdi.*;
import java.util.List;
import java.util.stream.Collectors;

//public class JDIDebugger {
//
//    private String handleObjectReference(ObjectReference objRef) throws Exception {
//        String res;
//
//        // ��ȡ����� ReferenceType
//        ReferenceType refType = objRef.referenceType();
//
//        // �������Ƿ�Ϊ String ����
//        if (objRef instanceof StringReference) {
//            res = "\"" + ((StringReference) objRef).value() + "\"";
//        }
//        // �������Ƿ�Ϊ Array ����
//        else if (objRef instanceof ArrayReference) {
//            res = "[" + ((ArrayReference) objRef).getValues().stream()
//                        .map(String::valueOf)
//                        .collect(Collectors.joining(", ")) + "]";
//        }
//        // �������Ƿ�ʵ���� List �ӿ�
//        else if (isListType(refType)) {
//            res = handleList(objRef, refType);
//        }
//        // �������͵Ĵ���
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
//    // �ж϶����Ƿ�ʵ���� List �ӿ�
//    private boolean isListType(ReferenceType refType) {
//        // ��ȡ��������ʵ�ֵ����нӿ�
//        List<InterfaceType> interfaces = refType.allInterfaces();
//        for (InterfaceType interfaceType : interfaces) {
//            if (interfaceType.name().equals("java.util.List")) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    // ���� List ����
//    private String handleList(ObjectReference objRef, ReferenceType refType) throws Exception {
//        StringBuilder sb = new StringBuilder();
//
//        // ��鲻ͬ List ʵ����
//        if (refType.name().equals("java.util.ArrayList") || refType.name().equals("java.util.Vector")) {
//            // ���� ArrayList �� Vector��ͨ���洢���ݵ��ֶ��� elementData
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
//            // ���� LinkedList����Ҫ��������ṹ
//            sb.append("LinkedList elements: ");
//            Field firstField = refType.fieldByName("first");
//            if (firstField != null) {
//                ObjectReference firstNode = (ObjectReference) objRef.getValue(firstField);
//                sb.append(traverseLinkedList(firstNode));
//            }
//        } else if (refType.name().equals("java.util.concurrent.CopyOnWriteArrayList")) {
//            // ���� CopyOnWriteArrayList��ͨ����Ҳʹ������
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
//    // ���� LinkedList �ķ���
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
//                break; // ���û���ֶΣ����˳�
//            }
//        }
//        return sb.toString();
//    }
//}
